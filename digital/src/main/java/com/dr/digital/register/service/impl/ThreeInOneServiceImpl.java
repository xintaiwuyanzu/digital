package com.dr.digital.register.service.impl;

import com.dr.digital.configManager.entity.FlowPathDetail;
import com.dr.digital.configManager.entity.FlowPathDetailInfo;
import com.dr.digital.event.ArchiveDataStatusChangeEvent;
import com.dr.digital.manage.form.entity.FileStructure;
import com.dr.digital.manage.form.entity.FileStructureInfo;
import com.dr.digital.manage.form.service.ArchiveDataManager;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.manage.task.entity.ArchiveBatch;
import com.dr.digital.manage.task.entity.YuanWenBatchDetail;
import com.dr.digital.manage.task.service.ArchiveBatchService;
import com.dr.digital.ocr.entity.GeneralResultEntity;
import com.dr.digital.ocr.entity.TableResultEntity;
import com.dr.digital.ocr.service.OcrGeneralClient;
import com.dr.digital.ocr.service.OcrService;
import com.dr.digital.ocr.service.OcrTableClient;
import com.dr.digital.register.service.RegisterService;
import com.dr.digital.register.service.ThreeInOneService;
import com.dr.digital.uploadfiles.service.UploadFilesService;
import com.dr.digital.util.UUIDUtils;
import com.dr.digital.wssplit.entity.SplitRule;
import com.dr.digital.wssplit.entity.SplitRuleInfo;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.StatusEntity;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.form.core.service.FormDataService;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RetryableException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service

public class ThreeInOneServiceImpl implements ThreeInOneService {
    private Logger logger = LoggerFactory.getLogger(ThreeInOneServiceImpl.class);
    @Autowired
    ArchiveDataManager dataManager;
    @Autowired
    ArchiveBatchService archiveBatchService;
    @Autowired
    FormDataService formDataService;
    @Autowired
    RegisterService registerService;
    @Autowired
    UploadFilesService uploadFilesService;
    @Autowired
    CommonMapper commonMapper;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    OcrTableClient ocrTableClient;
    @Autowired
    OcrGeneralClient ocrGeneralClient;
    @Autowired
    OcrService ocrService;
    @Autowired
    ApplicationEventPublisher applicationEventPublisher;
    @Value("${filePath}")
    private String filePath;
    //?????????
    ExecutorService executorService = Executors.newFixedThreadPool(6);
    ExecutorService executor = Executors.newFixedThreadPool(6);
    List<String> fileList = new ArrayList<>();
    List<String> fileList3 = new ArrayList<>();
    CountDownLatch countDownLatch = new CountDownLatch(10);

    /**
     * ??????????????????????????????????????????????????????
     *
     * @param query
     * @param registerId
     * @param person
     */
    @Override
    public void dataCleaning(BaseQuery query, String registerId, Person person) {
        //??????????????????????????????????????????
        List<FormData> list = dataManager.findDataByQuery(query);
        if (list.size() > 0) {
            //???????????????jpg??????
            SplitJpg(query, person);
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //?????????ocr????????????,????????????????????????
            distinguishTxt(list, registerId, query);
        } else {
            Assert.isTrue(false, "????????????????????????????????????!");
        }
    }

/******************************** ??????jpg ***********************************/
    /**
     * tif ???pdf?????????jpg???jpg???????????????
     *
     * @param query
     * @param person
     */
    public void SplitJpg(BaseQuery query, Person person) {
        final File tifOldFile = new File(String.join(File.separator, filePath, "tifold"));
        if (!tifOldFile.getParentFile().exists()) {
            tifOldFile.getParentFile().mkdirs();
        }
        fileList = new ArrayList<>();
        List<String> directory = isDirectory(tifOldFile);
        if (directory.size() > 0) {
            countDownLatch = new CountDownLatch(directory.size());
            for (String str : directory) {
                executorService.execute(() -> fileSplit(str, query, person));
            }
        } else {
            countDownLatch = new CountDownLatch(1);
            countDownLatch.countDown();
        }
    }

    /**
     * jpg ????????????
     *
     * @param str
     * @param query
     * @param person
     */
    public void fileSplit(String str, BaseQuery query, Person person) {
        File file = new File(str);
        String suffix = file.getName().split("\\.")[1];
        //??????????????????
        ArchiveBatch batch = archiveBatchService.newBatchJpgByPath("IMP_YUANWEN", query, person);
        if ("tif".equals(suffix) || "TIF".equals(suffix) || "pdf".equals(suffix) || "PDF".equals(suffix)) {
            String fileName = file.getName().split("\\.")[0];
            List<FormData> formDataList = formDataService.selectFormData(query.getFormDefinitionId(), (sqlQuery, formRelationWrapper) -> {
                sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE), fileName)
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), "RECEIVE")
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '0')
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), '0')
                        .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
            });
            //?????????????????????????????????????????????????????????
            List<FormData> formDataCheck = formDataService.selectFormData(query.getFormDefinitionId(), (sqlQuery, formRelationWrapper) -> {
                sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE), fileName)
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), "RECEIVE")
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '1')
                        .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
            });
            //??????????????????,????????????????????????
            if (formDataCheck.size() > 0) {
                repeatData(fileName, batch, person, file);
            }
            if (formDataList.size() > 0) {
                if ("tif".equals(suffix) || "TIF".equals(suffix)) {
                    uploadFilesService.tifTojPGXc(file, batch, person);
                } else if ("pdf".equals(suffix) || "PDF".equals(suffix)) {
                    uploadFilesService.pdfToImageFile(file, batch, fileName, person);
                }
                for (FormData formData : formDataList) {
                    //??????????????????
                    formData.put(ArchiveEntity.COLUMN_SPLIT_STATE, "1");
                    formData.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "1");
                    formDataService.updateFormDataById(formData);
                }
            }
        } else if ("jpg".equals(suffix) || "JPG".equals(suffix)) {
            //?????????jpg ??????????????? ??????????????????
            String dirName = file.getParentFile().getName();
            List<FormData> formDataList = formDataService.selectFormData(query.getFormDefinitionId(), (sqlQuery, formRelationWrapper) -> {
                sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE), dirName)
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), "RECEIVE")
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '0')
                        .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
            });
            if (formDataList.size() > 0) {
                String fondID = dirName.substring(0, dirName.indexOf("-"));
                File file1 = new File(String.join(File.separator, filePath, "filePath", fondID, dirName));
                if (!file1.exists()) {
                    file1.mkdirs();
                }
                //????????????????????????????????????????????????????????????
                file.renameTo(new File(String.join(File.separator, filePath, "filePath", fondID, dirName, file.getName())));
                for (FormData formData : formDataList) {
                    //??????????????????
                    formData.put(ArchiveEntity.COLUMN_SPLIT_STATE, "1");
                    formData.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "1");
                    formDataService.updateFormDataById(formData);
                }
            }
        }
        countDownLatch.countDown();
    }

    /**
     * ?????????????????????
     *
     * @param fileName ?????????
     * @param batch
     * @param person
     * @param file
     */
    public void repeatData(String fileName, ArchiveBatch batch, Person person, File file) {
        YuanWenBatchDetail yuanWenBatchDetail = new YuanWenBatchDetail();
        yuanWenBatchDetail.setMuLuYeShu("");
        yuanWenBatchDetail.setArchival_code(fileName);
        yuanWenBatchDetail.setId(UUIDUtils.getUUID());
        yuanWenBatchDetail.setBatchId(batch.getId());
        yuanWenBatchDetail.setCreatePerson(person.getId());
        yuanWenBatchDetail.setCreateDate(System.currentTimeMillis());
        yuanWenBatchDetail.setdHMs("??????????????????");
        commonMapper.insert(yuanWenBatchDetail);
        batch.setStatus(StatusEntity.STATUS_ENABLE_STR);
        batch.setEndDate(System.currentTimeMillis());
        commonMapper.updateById(batch);
        if (file.isFile() && file.exists()) {
            file.delete();
        }
    }

    /************************************* ocr??????,???????????????  **************************************/
    public void distinguishTxt(List<FormData> list, String registerId, BaseQuery query) {
        for (FormData formData : list) {
            String archive_code = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);//??????
            List<FormData> formDataList = formDataService.selectFormData(query.getFormDefinitionId(), (sqlQuery, formRelationWrapper) -> {
                sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE), archive_code)
                        .in(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), "RECEIVE", "WSSPLIT")
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '1')
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), '1')
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_DISTINGUISH_STATE), '0')
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_DISASSEMBLY_STATE), '0')
                        .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
            });
            if (formDataList.size() > 0) {
                TwoToOne(formDataList, registerId);
            }
        }
    }

    private void TwoToOne(List<FormData> formDataList, String registerId) {
        for (FormData formDataTw : formDataList) {
            try {
                //????????????????????????
                imageToText(formDataTw, registerId);
                countDownLatch.await();
                //??????  ??????????????????????????????
                if (formDataTw.get(ArchiveEntity.COLUMN_CATEGORY_CODE).toString().contains("WS")) {
                    Thread.sleep(14000);
                    //????????????????????????????????????????????????
                    List<SplitRule> splitRules = commonMapper.selectByQuery(SqlQuery.from(SplitRule.class).equal(SplitRuleInfo.ISENABLE, "1").orderBy(SplitRuleInfo.IFORDER));
                    ocrService.wsSplintChaiJan(registerId, formDataTw, splitRules);
                    formDataTw.put(ArchiveEntity.COLUMN_DISASSEMBLY_STATE, '1');
                }
                //??????????????????
                formDataTw.put(ArchiveEntity.COLUMN_DISTINGUISH_STATE, '1');
                formDataService.updateFormDataById(formDataTw);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ??????
     */
    public void ocrToChaiJian(FormData formData, String registerId) {
        String formDefinitionId = registerService.selectById(registerId).getFormDefinitionId();
        //??????  ??????????????????????????????
        if (formData.get(ArchiveEntity.COLUMN_CATEGORY_CODE).toString().contains("WS")) {
            //????????????????????????????????????????????????
            List<SplitRule> splitRules = commonMapper.selectByQuery(SqlQuery.from(SplitRule.class).equal(SplitRuleInfo.ISENABLE, "1")
                    .orderBy(SplitRuleInfo.IFORDER));
            //???????????????????????????,????????????????????????
            long fileStructure = commonMapper.countByQuery(SqlQuery.from(FileStructure.class)
                    .equal(FileStructureInfo.REGISTERID, registerId)
                    .equal(FileStructureInfo.DEFAULT_STATE,0)
                    .equal(FileStructureInfo.AJ_ARCHIVAL_CODE, formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE).toString()));
           if (fileStructure==0){
               ocrService.wsSplintChaiJan(registerId, formData, splitRules);
           }
            formData.put(ArchiveEntity.COLUMN_DISASSEMBLY_STATE, '1');
            formDataService.updateFormDataById(formData);
            FlowPathDetail flowPathDetails = commonMapper.selectOneByQuery(SqlQuery.from(FlowPathDetail.class)
                    .equal(FlowPathDetailInfo.FORMDEFINITIONID, formDefinitionId));
            String link = registerService.manualLink(flowPathDetails.getFlowBatchName());
            BaseQuery query = new BaseQuery();
            query.setFondId(registerId);
            query.setFormDefinitionId(formDefinitionId);
            Person person = new Person();
            person.setUserName("???????????????");
            person.setUserCode("admin");
            if (!formData.get(ArchiveEntity.COLUMN_STATUS).equals(link)){
                registerService.updateDataDetail(person,formData,link,"",query,"");
            }
        }
    }

    /**
     * ocr ?????? txt??????
     *
     * @param formData
     * @return
     */
    private void imageToText(FormData formData, String registerId) {
        String fond_code = formData.get(ArchiveEntity.COLUMN_FOND_CODE);//?????????
        String archive_code = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);//??????
        String jpgFilePath = String.join(File.separator, filePath, "filePath", fond_code, archive_code);
        fileList3 = new ArrayList<>();
        File file = new File(jpgFilePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        List<String> directory = isDirectory2(file);
        if (directory.size() > 0) {
            countDownLatch = new CountDownLatch(directory.size());
            for (String s : directory) {
                executor.execute(() -> jpgToTxt(s, fond_code, archive_code, registerId));
            }
        } else {
            countDownLatch = new CountDownLatch(1);
            countDownLatch.countDown();
        }
    }

    public void jpgToTxt(String str, String fond_code, String archive_code, String registerId) {
        File imageFile = new File(str);
        String code = "";
        String message = "";
        long startTime = 0;
        long endTime = 0;
        if (imageFile.exists()) {
            try {
                String fileName = imageFile.getName().split("\\.")[0];
                File pathFile = new File(String.join(File.separator, filePath, "txt", fond_code, archive_code));
                if (!pathFile.exists()) {
                    pathFile.mkdirs();
                }
                File textFile = new File(String.join(File.separator, filePath, "txt", fond_code, archive_code), fileName + ".txt");
                String img = Base64Utils.encodeToString(FileUtils.readFileToByteArray(imageFile));
                Thread.sleep(3000);
                //?????????????????? ??????????????????????????? ????????? ??????????????????
                startTime = System.currentTimeMillis();
                TableResultEntity tableResultEntity = ocrService.shiBieOcrTable(img);
                endTime = System.currentTimeMillis();
                if ("0".equals(tableResultEntity.getCode())) {
                    if (!textFile.getParentFile().exists()) {
                        textFile.getParentFile().mkdirs();
                    }
                    try (OutputStream stream = new FileOutputStream(textFile)) {
                        objectMapper.writeValue(stream, tableResultEntity.getData());
                        code = tableResultEntity.getCode();
                        message = tableResultEntity.getMessage();
                    } catch (Exception e) {
                        logger.warn("??????txt????????????", e);
                    }
                } else {
                    startTime = System.currentTimeMillis();
                    GeneralResultEntity generalResultEntity = ocrService.shiBieOcrTxt(img);
                    endTime = System.currentTimeMillis();
                    if ("0".equals(generalResultEntity.getCode())) {
                        if (!textFile.getParentFile().exists()) {
                            textFile.getParentFile().mkdirs();
                        }
                        try (OutputStream stream = new FileOutputStream(textFile)) {
                            objectMapper.writeValue(stream, generalResultEntity.getData());
                            code = tableResultEntity.getCode();
                            message = tableResultEntity.getMessage();
                        } catch (Exception e) {
                            logger.warn("??????txt????????????", e);
                        }
                    } else {//??????????????????????????????????????????????????????????????????????????????
                        try {
                            //?????????????????????
                            String cuPath = filePath + File.separator + "ocrError" + File.separator + archive_code;
                            File ocrFile = new File(cuPath);
                            if (!ocrFile.exists()) {
                                ocrFile.mkdirs();
                            }
                            OutputStream os = new FileOutputStream(cuPath + File.separator + imageFile.getName());
                            InputStream is = new FileInputStream(imageFile);
                            FileCopyUtils.copy(is, os);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (RetryableException e) {
                e.printStackTrace();
            } finally {
                String fileName = imageFile.getName().split("\\.")[0];
                File textFile = new File(String.join(File.separator, filePath, "txt", fond_code, archive_code), fileName + ".txt");
                if (!textFile.exists()) {
                    try {
                        String img = Base64Utils.encodeToString(FileUtils.readFileToByteArray(imageFile));
                        Thread.sleep(3000);
                        //??????????????? ????????? ??????????????????
                        startTime = System.currentTimeMillis();
                        TableResultEntity tableResultEntity = ocrService.shiBieOcrTable(img);
                        endTime = System.currentTimeMillis();
                        if ("0".equals(tableResultEntity.getCode())) {
                            try (OutputStream stream = new FileOutputStream(textFile)) {
                                objectMapper.writeValue(stream, tableResultEntity.getData());
                            } catch (Exception e) {
                                logger.warn("??????txt????????????", e);
                            }
                        } else {
                            textFile.createNewFile();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                uploadFilesService.insetOcrRecord(archive_code, code, message, imageFile.getName(), imageFile.getPath(), "????????????", (endTime - startTime) + "", startTime, endTime, imageFile.length() / 1024 + "", registerId);
                countDownLatch.countDown();
            }
        }
    }

    public List<String> isDirectory(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                fileList.add(file.getPath());
            } else {
                File[] list = file.listFiles();
                if (list.length != 0) {
                    for (int i = 0; i < list.length; i++) {
                        isDirectory(list[i]);
                    }
                }
            }
        } else {
            System.out.println("??????????????????");
        }
        return fileList;
    }

    public List<String> isDirectory2(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                String fileName = file.getName().toUpperCase();
                // ???????????????
                if (fileName.indexOf(".JPG") != -1 || fileName.indexOf(".jpg") != -1) {
                    fileList3.add(file.getPath());
                }
            } else {
                File[] list = file.listFiles();
                if (list.length != 0) {
                    for (int i = 0; i < list.length; i++) {
                        isDirectory2(list[i]);
                    }
                }
            }
        } else {
            System.out.println("??????????????????");
        }
        return fileList3;
    }

}