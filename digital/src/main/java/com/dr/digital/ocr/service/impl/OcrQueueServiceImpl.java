package com.dr.digital.ocr.service.impl;

import com.dr.digital.configManager.bo.LinkFlowPath;
import com.dr.digital.configManager.entity.FlowPathDetail;
import com.dr.digital.configManager.entity.FlowPathDetailInfo;
import com.dr.digital.configManager.entity.TypeFile;
import com.dr.digital.configManager.entity.TypeFileInfo;
import com.dr.digital.manage.form.entity.FileStructure;
import com.dr.digital.manage.form.entity.FileStructureInfo;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.ocr.entity.*;
import com.dr.digital.ocr.service.OcrQueueService;
import com.dr.digital.ocr.service.OcrService;
import com.dr.digital.register.entity.Register;
import com.dr.digital.register.entity.RegisterInfo;
import com.dr.digital.register.service.RegisterService;
import com.dr.digital.register.service.ThreeInOneService;
import com.dr.digital.uploadfiles.entity.JpgQueue;
import com.dr.digital.uploadfiles.entity.JpgQueueInfo;
import com.dr.digital.uploadfiles.service.UploadFilesService;
import com.dr.digital.util.UUIDUtils;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.form.core.service.FormDataService;
import com.dr.framework.common.service.CommonService;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OcrQueueServiceImpl implements OcrQueueService, InitializingBean, DisposableBean {
    private Logger logger = LoggerFactory.getLogger(OcrQueueServiceImpl.class);
    @Autowired
    CommonService commonService;
    @Resource
    CommonMapper commonMapper;
    @Autowired
    FormDataService formDataService;
    @Autowired
    RegisterService registerService;
    @Autowired
    ThreeInOneService threeInOneService;
    @Autowired
    OcrService ocrService;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    UploadFilesService uploadFilesService;
    @Autowired
    ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    TaskInfoList taskInfoList;

    volatile Object queryLock = new Object();
    @Value("${filePath}")
    private String filePath;
    //???????????????????????????
    List<String> ocrCodes = Collections.synchronizedList(new ArrayList<>());
    //?????????
    ThreadPoolExecutor executorService;

    /**
     * ??????ocr??????
     */
    @Override
    @Async
    public void updateStatus() {
        //???????????????????????????
        List<OcrQueue> ocrQueues = commonMapper.selectByQuery(
                SqlQuery.from(OcrQueue.class)
                        .equal(OcrQueueInfo.STATUS, '0')
                        .notIn(OcrQueueInfo.ARCHIVECODE, ocrCodes)
                        .orderBy(OcrQueueInfo.CREATEDATE)
        );
        if (ocrQueues.size() > 0) {
            for (OcrQueue ocrQueue : ocrQueues) {
                ocrQueue.setStatus("4");//??????
                commonMapper.updateIgnoreNullById(ocrQueue);
            }
        }
    }

    /**
     * ????????????
     *
     * @param ocrQueueId
     * @param person
     */
    @Override
    public void selectOcrStart(String ocrQueueId, Person person) {
        for (String id : ocrQueueId.split(",")) {
            SqlQuery<OcrQueue> sqlQuery = SqlQuery.from(OcrQueue.class);
            sqlQuery.equal(OcrQueueInfo.ID, id);
            OcrQueue ocrQueueOne = commonMapper.selectOneByQuery(sqlQuery);
            SqlQuery<OcrQueue> sqlQueryFiles = SqlQuery.from(OcrQueue.class)
                    .equal(OcrQueueInfo.FONDCODE, ocrQueueOne.getFondCode())
                    .notEqual(OcrQueueInfo.STATUS, "2")
                    .equal(OcrQueueInfo.ARCHIVECODE, ocrQueueOne.getArchiveCode())
                    .equal(OcrQueueInfo.BATCHID, ocrQueueOne.getBatchID());
            commonMapper.updateByQuery(sqlQueryFiles.set(OcrQueueInfo.STATUS, '0'));
        }
        //??????????????????ocr????????????
        implementOcr();
    }

    /**
     * ??????????????????
     *
     * @param entity
     * @param person
     */
    @Override
    public void searchOcrStart(OcrQueue entity, Person person) {
        SqlQuery<OcrQueue> sqlQuery = SqlQuery.from(OcrQueue.class);
        if (!StringUtils.isEmpty(entity.getBatchName())) {
            sqlQuery.equal(OcrQueueInfo.BATCHNAME, entity.getBatchName());
        }
        if (!StringUtils.isEmpty(entity.getArchiveCode())) {
            sqlQuery.equal(OcrQueueInfo.ARCHIVECODE, entity.getArchiveCode());
        }
        if (!StringUtils.isEmpty(entity.getStatus())) {
            sqlQuery.equal(OcrQueueInfo.STATUS, entity.getStatus());
        } else {
            sqlQuery.notEqual(OcrQueueInfo.STATUS, "2");
        }
        sqlQuery.orderBy(OcrQueueInfo.CREATEDATE);
        List<OcrQueue> ocrQueues = commonMapper.selectByQuery(sqlQuery);
        if (ocrQueues.size() > 0) {
            for (OcrQueue ocrQueue : ocrQueues) {
                ocrQueue.setStatus("0");//??????
                commonMapper.updateIgnoreNullById(ocrQueue);
            }
        }
        //??????????????????ocr????????????
        implementOcr();
    }

    /**
     * ??????????????????
     */
    @Override
    @Async
    public void allOcrStart() {
        //???????????????????????????
        List<OcrQueue> ocrQueues = commonMapper.selectByQuery(
                SqlQuery.from(OcrQueue.class)
                        .equal(OcrQueueInfo.STATUS, '4')
                        .orderBy(OcrQueueInfo.CREATEDATE)
        );
        if (ocrQueues.size() > 0) {
            for (OcrQueue ocrQueue : ocrQueues) {
                ocrQueue.setStatus("0");//??????
                commonMapper.updateIgnoreNullById(ocrQueue);
            }
        }
        //??????????????????ocr????????????
        implementOcr();
    }


    /**
     * ??????ocr ??????
     */
    @Async
    @Override
    public void implementOcr() {
        //???????????????????????????
        synchronized (executorService) {
            while (executorService.getActiveCount() < THREAD_POOL_SIZE) {
                executorService.execute(this::selectAndOcr);
            }
        }
    }

    @Transactional
    protected void selectAndOcr() {
        //????????????
        correct();
        List<OcrQueue> ocrQueueFiles;
        OcrQueue ocrQueueOne;
        synchronized (queryLock) {
            //???????????????????????????
            List<OcrQueue> ocrQueues = commonMapper.selectLimitByQuery(
                    SqlQuery.from(OcrQueue.class)
                            .equal(OcrQueueInfo.STATUS, '0')
                            .notIn(OcrQueueInfo.ARCHIVECODE, ocrCodes)
                            .orderBy(OcrQueueInfo.PRIORITY)
                            .orderBy(OcrQueueInfo.CREATEDATE),
                    0, 1);
            if (ocrQueues.isEmpty()) {
                return;
            }
            //????????????????????????????????????????????????
            ocrQueueOne = ocrQueues.get(0);
            SqlQuery<OcrQueue> sqlQuery = SqlQuery.from(OcrQueue.class)
                    .equal(OcrQueueInfo.FONDCODE, ocrQueueOne.getFondCode())
                    .equal(OcrQueueInfo.STATUS, '0')
                    .equal(OcrQueueInfo.ARCHIVECODE, ocrQueueOne.getArchiveCode())
                    .equal(OcrQueueInfo.BATCHID, ocrQueueOne.getBatchID());
            ocrQueueFiles = commonMapper.selectByQuery(sqlQuery);
            commonMapper.updateByQuery(sqlQuery.set(OcrQueueInfo.STATUS, '1'));
            //??????????????????1?????????????????????list
            ocrCodes.add(ocrQueueOne.getArchiveCode());
        }
        boolean success = true;
        //?????????????????????????????????
        for (OcrQueue ocrQueue : ocrQueueFiles) {
            //????????????ocr??????
            success = success & doOcrAndSaveStatus(ocrQueue);
            //??????ocr???????????????????????????
        }
        if (success) {
            //?????????????????????????????????
            if (ocrQueueOne.getFormDateId() != null && ocrQueueOne.getFormDefinitionId() != null) {
                FormData formData = formDataService.selectOneFormData(ocrQueueOne.getFormDefinitionId(), ocrQueueOne.getFormDateId());
                formData.put(ArchiveEntity.COLUMN_DISTINGUISH_STATE, '1');
                formDataService.updateFormDataIgnoreNullById(formData);

                //??????ocr?????????????????????????????????????????????????????????ocr??????????????????????????????
                FlowPathDetail flowPathDetails = commonMapper.selectOneByQuery(SqlQuery.from(FlowPathDetail.class)
                        .equal(FlowPathDetailInfo.FORMDEFINITIONID, ocrQueueOne.getFormDefinitionId()));
                //???????????????????????????
                String link = registerService.manualLink(flowPathDetails.getFlowBatchName());
                //??????ocr??????
                String ocrYs = commonMapper.countByQuery(
                        SqlQuery.from(OcrQueue.class,false)
                                .column(OcrQueueInfo.ID)
                                .equal(OcrQueueInfo.STATUS, '2')
                                .equal(OcrQueueInfo.ARCHIVECODE, ocrQueueOne.getArchiveCode()))+"";

                JpgQueue jpgQueue = commonMapper.selectOneByQuery(
                        SqlQuery.from(JpgQueue.class, false)
                                .column(JpgQueueInfo.FILEYS)
                                .equal(JpgQueueInfo.STATUS, '2')
                                .equal(JpgQueueInfo.FORMDEFINITIONID, ocrQueueOne.getFormDefinitionId())
                                .equal(JpgQueueInfo.ARCHIVECODE, ocrQueueOne.getArchiveCode()));
//                ocr??????????????????????????????????????????ocr?????????????????????
                if (ocrYs.equals(jpgQueue.getFileYs())){
                    //?????????????????????????????????ocr?????????????????????????????????????????????????????????????????????
                    if (!flowPathDetails.getFlowBatchName().contains(LinkFlowPath.CHAIJIAN)){
                        BaseQuery query = new BaseQuery();
                        query.setFondId(ocrQueueOne.getBatchID());
                        query.setFormDefinitionId(ocrQueueOne.getFormDefinitionId());
                        Person person = new Person();
                        person.setUserName("???????????????");
                        person.setUserCode("admin");
                        //??????????????????????????????????????????????????????????????????
                        if (flowPathDetails.getFlowBatchName().contains(LinkFlowPath.WSSPLIT)){
                            // ocr????????????????????????????????? ?????????????????????????????????????????????????????????????????????????????????ocr?????????????????????????????????????????????????????????????????????????????????
                            insetFolder(formData, ocrQueueOne.getBatchID());
                        }

                        if (!formData.get(ArchiveEntity.COLUMN_STATUS).equals(link)){
                            registerService.updateDataDetail(person,formData,link,"",query,"");
                        }
                    }else {
                        threeInOneService.ocrToChaiJian(formData,ocrQueueOne.getBatchID());
                    }
                }



                formData.setFormDefinitionId(ocrQueueOne.getFormDefinitionId());

            }
        }
        synchronized (queryLock) {
            ocrCodes.remove(ocrQueueOne.getArchiveCode());
        }
        //??????????????????ocr????????????
        implementOcr();
    }

    /**
     * ????????????ocr??????
     *
     * @param ocrQueue
     * @return
     */
    private boolean doOcrAndSaveStatus(OcrQueue ocrQueue) {
        //????????????????????????ocr??????
        int count = 0;
        boolean success = false;
        while (count < 10) {
            try {
                success = doOcr(ocrQueue);
                if (success) {
                    break;
                }
            } catch (Throwable e) {
                if (count >= 1) {
                    try {
                        //???????????????????????????????????????ocr???????????????5??????
                        Thread.sleep(5 * 60 * 1000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
                count++;
            }
        }
        if (success) {
            //??????????????????
            ocrQueue.setStatus("2");//????????????


            //????????????
            ocrQueue.setUpdateDate(UUIDUtils.currentTimeMillis());
            commonMapper.updateIgnoreNullById(ocrQueue);
        } else {
            //????????????????????????????????????
            ocrQueue.setStatus("3");//????????????
            commonMapper.updateIgnoreNullById(ocrQueue);
        }
        return success;
    }

    /**
     * ??????ocr????????????ocr??????
     *
     * @param ocrQueue
     * @return ????????????true
     */
    private boolean doOcr(OcrQueue ocrQueue) {
        String registerId = ocrQueue.getBatchID();
        //??????????????????jpg?????????
        String fileDz = ocrQueue.getFilePath();
        String fondCode = ocrQueue.getFondCode();
        String archiveCode = ocrQueue.getArchiveCode();
        if (!StringUtils.isEmpty(fileDz)) {
            String code = "";
            String message = "";
            String fhAge = "????????????";
            long startTime = 0;
            long endTime = 0;
            //????????????
            File imageFile = new File(fileDz);
            //???????????????txt??????????????????
            File pathFile = new File(String.join(File.separator, filePath, "txt", fondCode, archiveCode));
            if (!pathFile.exists()) {
                pathFile.mkdirs();
            }
            File textFile = new File(String.join(File.separator, filePath, "txt", fondCode, archiveCode), ocrQueue.getFileName() + ".txt");
            logger.info("??????txt?????????{}", textFile.getAbsolutePath());
            try {
                String img = Base64Utils.encodeToString(FileUtils.readFileToByteArray(imageFile));
                //?????????????????? ??????????????????????????? ????????? ??????????????????
                startTime = System.currentTimeMillis();
                Thread.sleep(1000);
//                logger.info("img========???{}",img);
                TableResultEntity tableResultEntity = ocrService.shiBieOcrTable(img);
                endTime = System.currentTimeMillis();
                logger.info("tableResultEntity========???{}", tableResultEntity);
                if ("0".equals(tableResultEntity.getCode())) {
                    if (!textFile.getParentFile().exists()) {
                        textFile.getParentFile().mkdirs();
                    }
//                    logger.info("?????????txt?????????{}",textFile.getAbsolutePath());
                    try (OutputStream stream = new FileOutputStream(textFile)) {
                        logger.info("tableResultEntity.getData()============???{}", tableResultEntity.getData());
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
                            fhAge = "????????????";
                        } catch (Exception e) {
                            logger.warn("??????txt????????????", e);
                        }
                    } else {//??????????????????????????????????????????????????????????????????????????????????????????????????????
                        try {
                            String cuPath = String.join(File.separator, filePath, "ocrError", archiveCode);
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
            } finally {
                //??????????????????
                uploadFilesService.insetOcrRecord(archiveCode, code, message, imageFile.getName(), imageFile.getPath(), fhAge, (endTime - startTime) + "", startTime, endTime, imageFile.length() / 1024 + "", registerId);
                operationLog(ocrQueue, startTime, endTime, imageFile.getName());
            }
            return true;
        }
        return false;
    }

    /**
     * ????????? ocr??????
     *
     * @param formDefinitionId
     * @param registerId
     * @param person
     */
    public void ocrQueueLb(String formDefinitionId, String registerId, String batchName, String batchNo, Person person) {
        //1?????????????????????ocr??????????????????
        List<FormData> formDataList = formDataService.selectFormData(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
            sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), '1')
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '1')
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_DISTINGUISH_STATE), '0')
                    .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
        //2??????????????????????????????
        if (formDataList.size() > 0) {
            for (FormData formData : formDataList) {
                addOcrQueueLb(formDefinitionId, formData, registerId, batchName, batchNo, person);
            }
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param formData
     * @param registerId
     */
    public void addOcrQueueLb(String formDefinitionId, FormData formData, String registerId, String batchName, String batchNo, Person person) {
        String fond_code = formData.get(ArchiveEntity.COLUMN_FOND_CODE);//?????????
        String archive_code = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);//??????
        if (!StringUtils.isEmpty(fond_code) && !StringUtils.isEmpty(archive_code)) {
            //?????????????????????jpg????????????
            String jpgFilePath = String.join(File.separator, filePath, "filePath", fond_code, archive_code);
            fileList3 = new ArrayList<>();
            File file = new File(jpgFilePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            List<String> directory = isDirectory(file);
            if (directory.size() > 0) {
                for (String s : directory) {
                    addOcrQueue(s, formDefinitionId, formData, registerId, batchName, batchNo, person);
                }
            }
        }
        //?????????ocr????????????????????????????????????
        implementOcr();
    }

    public OcrQueue addOcrQueue(String filePath, String formDefinitionId, FormData formData, String registerId, String batchName, String batchNo, Person person) {
        File imageFile = new File(filePath);
        if (imageFile.exists()) {
            String fileName = imageFile.getName().split("\\.")[0];
            OcrQueue ocrQueue = new OcrQueue();
            ocrQueue.setSystem_code("INSPUR-DZZW-MACHINING");
            ocrQueue.setBatchID(registerId);
            ocrQueue.setBatchNo(batchNo);
            ocrQueue.setBatchName(batchName);
            ocrQueue.setFormDefinitionId(formDefinitionId);
            ocrQueue.setFormDateId(formData.getId());
            ocrQueue.setFondCode(formData.get(ArchiveEntity.COLUMN_FOND_CODE));
            ocrQueue.setArchiveCode(formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE));
            ocrQueue.setFileName(fileName);
            ocrQueue.setFilePath(filePath);
            ocrQueue.setFileSize((imageFile.length() / 1024) + "");
            ocrQueue.setBatch_number("0");
            ocrQueue.setStatus("0"); //?????????
            ocrQueue.setCreatePerson(person.getId());
            ocrQueue.setPersonName(person.getUserName());
            ocrQueue.setCreateDate(System.currentTimeMillis());
            ocrQueue.setId(UUID.randomUUID().toString());
            ocrQueue.setState("0");
            commonMapper.insert(ocrQueue);
            return ocrQueue;
        }
        return null;
    }

    public OcrQueue addOcrQueue(File imgFile, JpgQueue jpgQueue) {
        if (imgFile.exists()) {
            String fileName = imgFile.getName().split("\\.")[0];
            OcrQueue ocrQueue = new OcrQueue();
            ocrQueue.setFileRGB(jpgQueue.getFileRGB());
            ocrQueue.setFilePower(jpgQueue.getFilePower());
            ocrQueue.setSystem_code("INSPUR-DZZW-MACHINING");
            ocrQueue.setBatchID(jpgQueue.getBatchID());
            ocrQueue.setBatchNo(jpgQueue.getBatchNo());
            ocrQueue.setBatchName(jpgQueue.getBatchName());
            ocrQueue.setFormDefinitionId(jpgQueue.getFormDefinitionId());
            ocrQueue.setFormDateId(jpgQueue.getFormDateId());
            ocrQueue.setFondCode(jpgQueue.getFondCode());
            ocrQueue.setArchiveCode(jpgQueue.getArchiveCode());
            ocrQueue.setFileName(fileName);
            ocrQueue.setFilePath(imgFile.getAbsolutePath());
            ocrQueue.setFileSize((imgFile.length() / 1024) + "");
            ocrQueue.setBatch_number("0");
            ocrQueue.setStatus("0"); //?????????
            ocrQueue.setCreatePerson(jpgQueue.getCreatePerson());
            ocrQueue.setPersonName(jpgQueue.getPersonName());
            ocrQueue.setCreateDate(System.currentTimeMillis());
            ocrQueue.setId(UUID.randomUUID().toString());
            commonMapper.insert(ocrQueue);
            //??????ocr??????
            implementOcr();
            return ocrQueue;
        }
        return null;
    }

    /**
     * ??????????????????????????????
     * TODO
     *
     * @param file
     * @return
     */
    List<String> fileList3 = Collections.synchronizedList(new ArrayList<>());

    public List<String> isDirectory(File file) {
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
                        isDirectory(list[i]);
                    }
                }
            }
        } else {
            System.out.println("??????????????????");
        }
        return fileList3;
    }

    //???????????????
    @Override
    public void afterPropertiesSet() {
        ThreadFactory threadFactory = new CustomizableThreadFactory("ocr????????????");
        executorService = new ThreadPoolExecutor(1, THREAD_POOL_SIZE, 0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(THREAD_POOL_SIZE), threadFactory);
        implementOcr();
    }

    //???????????????
    @Override
    public void destroy() {
        executorService.shutdown();
    }

    /**
     * ????????????
     */
    public void correct() {
        //???????????????????????????
        List<OcrQueue> ocrQueues = commonMapper.selectByQuery(
                SqlQuery.from(OcrQueue.class)
                        .equal(OcrQueueInfo.STATUS, '0')
                        .orderBy(OcrQueueInfo.CREATEDATE)
        );
        if (ocrQueues.size() > 0) {
            List<String> reference = ocrQueues.stream().map(OcrQueue::getArchiveCode).collect(Collectors.toList());
            List<OcrQueue> ocrQueuesCorrect = commonMapper.selectByQuery(
                    SqlQuery.from(OcrQueue.class)
                            .notEqual(OcrQueueInfo.STATUS, "2")
                            .in(OcrQueueInfo.ARCHIVECODE, reference)
                            .orderBy(OcrQueueInfo.CREATEDATE)
            );
            if (ocrQueuesCorrect.size() > 0) {
                for (OcrQueue ocrQueue : ocrQueuesCorrect) {
                    //??????
                    ocrQueue.setStatus("0");
                    commonMapper.updateIgnoreNullById(ocrQueue);
                }
            }
        }
    }

    @Async
    @Override
    public void updatePriorityData(String fid, String type, String formDataid, String uuid) {
        TaskInfoList.TaskInfo task = new TaskInfoList.TaskInfo();
        task.setUuid(uuid);
        int process = 0;
        if (fid != null && type != null && formDataid != null) {
            process++;
            task.setProcess(process);
            task.setStatus(TaskInfoList.TaskStatus.RUNNING);
            taskInfoList.update(task);
            SqlQuery<Register> registerSqlQuery = SqlQuery.from(Register.class).equal(RegisterInfo.ID, formDataid).set(RegisterInfo.PRIORITY, type);
            commonMapper.updateByQuery(registerSqlQuery);
            SqlQuery<OcrQueue> ocrQueueSqlQuery = SqlQuery.from(OcrQueue.class).equal(OcrQueueInfo.FORMDEFINITIONID, fid).set(OcrQueueInfo.PRIORITY, type);
            commonMapper.updateByQuery(ocrQueueSqlQuery);
        }
        //????????????????????????
        task.setStatus(TaskInfoList.TaskStatus.FINISHED);
        task.setEndTime(System.currentTimeMillis());
        task.setUrl("finished");
        taskInfoList.update(task);
    }

    @Override
    public ResultEntity selectFunction() {
        //???????????????????????????
        List<OcrQueue> ocrQueues = commonMapper.selectByQuery(
                SqlQuery.from(OcrQueue.class, true)
                        .equal(OcrQueueInfo.STATUS, '1')
                        .groupBy(OcrQueueInfo.ARCHIVECODE)
                        .orderBy(OcrQueueInfo.CREATEDATE)
        );
        return ResultEntity.success(ocrQueues);
    }

    /**
     * ????????????????????????????????????
     *
     * @param formData ????????????
     */
    public void insetFolder(FormData formData, String registerId) {
        //?????????????????????
        List<FileStructure> fileStructures = commonMapper.selectByQuery(SqlQuery.from(FileStructure.class)
                .equal(FileStructureInfo.REGISTERID, registerId)
                .equal(FileStructureInfo.DEFAULT_STATE, 0)
                .equal(FileStructureInfo.ARCHIVESID, formData.get(ArchiveEntity.ID_COLUMN_NAME).toString()));
        if (fileStructures.size() == 0) {
            formData.put(ArchiveEntity.COLUMN_DISASSEMBLY_STATE, '1');
            formDataService.updateFormDataById(formData);
            List<TypeFile> typeFile = commonMapper.selectByQuery(SqlQuery.from(TypeFile.class)
                    .equal(TypeFileInfo.REGISTERID, registerId));
            //????????????????????????????????????
            if (typeFile.size() > 0) {
                for (TypeFile type : typeFile) {
                    String key = type.getYhCode();
                    String fileType = type.getFileName();
                    FileStructure data = new FileStructure();
                    String fond_code = formData.get(ArchiveEntity.COLUMN_FOND_CODE);//?????????
                    String archive_code = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);//??????
                    data.setArchives_item_number(key);
                    data.setFonds_identifier(fond_code);
                    data.setArchivers_category_code(formData.get(ArchiveEntity.COLUMN_CATEGORY_CODE));
                    data.setAj_archival_code(archive_code);
                    data.setArchival_code(archive_code + "-" + key);
                    data.setRegisterId(registerId);
                    data.setFile_type(fileType);
                    data.setArchivesID(formData.get(ArchiveEntity.ID_COLUMN_NAME));
                    data.setStatus("RECEIVE");
                    //?????????????????????????????????
                    data.setDefault_state("0");
                    data.setId(UUIDUtils.getUUID());
                    commonMapper.insert(data);
                }
            }else {
                //?????????????????????????????????
                String[] type = {"001", "002", "003", "004", "005"};
                String fileType="";
                for (String key:type){
                    switch (key) {
                        case "001":
                            fileType = "??????";
                            break;
                        case "002":
                            fileType = "??????";
                            break;
                        case "003":
                            fileType = "??????";
                            break;
                        case "004":
                            fileType = "?????????";
                            break;
                        case "005":
                            fileType = "??????";
                            break;
                    }
                    FileStructure data = new FileStructure();
                    String fond_code = formData.get(ArchiveEntity.COLUMN_FOND_CODE);//?????????
                    String archive_code = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);//??????
                    data.setArchives_item_number(key);
                    data.setFonds_identifier(fond_code);
                    data.setArchivers_category_code(formData.get(ArchiveEntity.COLUMN_CATEGORY_CODE));
                    data.setAj_archival_code(archive_code);
                    data.setArchival_code(archive_code + "-" + key);
                    data.setRegisterId(registerId);
                    data.setFile_type(fileType);
                    data.setStatus("RECEIVE");
                    data.setArchivesID(formData.get(ArchiveEntity.ID_COLUMN_NAME));
                    //?????????????????????????????????
                    data.setDefault_state("0");
                    data.setId(UUIDUtils.getUUID());
                    commonMapper.insert(data);
                }

            }

        }
    }

    /**
     * ??????ocr?????????????????????
     *
     * @param ocrQueue
     * @param startTime
     * @param endTime
     * @param name
     */
    public void operationLog(OcrQueue ocrQueue, Long startTime, Long endTime, String name) {
        ocrQueue.setStartTime(startTime);
        ocrQueue.setEndTime(startTime);
        ocrQueue.setName(name);
        commonMapper.updateIgnoreNullById(ocrQueue);

    }
}
