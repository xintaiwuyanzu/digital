package com.dr.digital.uploadfiles.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.dr.digital.configManager.bo.LinkFlowPath;
import com.dr.digital.manage.form.entity.FileStructure;
import com.dr.digital.manage.form.entity.FileStructureInfo;
import com.dr.digital.manage.form.service.ArchiveDataManager;
import com.dr.digital.manage.form.service.impl.DefaultArchiveDataManager;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.manage.task.entity.ArchiveBatch;
import com.dr.digital.manage.task.entity.YuanWenBatchDetail;
import com.dr.digital.manage.task.service.ArchiveBatchService;
import com.dr.digital.ocr.bo.TemplateBo;
import com.dr.digital.ocr.entity.*;
import com.dr.digital.ocr.service.OcrGeneralClient;
import com.dr.digital.ocr.service.OcrService;
import com.dr.digital.ocr.service.OcrTemplateClient;
import com.dr.digital.ofd.OfdConfig;
import com.dr.digital.ofd.entity.OfdRecord;
import com.dr.digital.ofd.service.OfdClient;
import com.dr.digital.ofd.service.TokenClient;
import com.dr.digital.ofd.service.impl.OfdConversion;
import com.dr.digital.processing.vo.ImgVo;
import com.dr.digital.template.entity.Template;
import com.dr.digital.uploadfiles.entity.UploadFiles;
import com.dr.digital.uploadfiles.entity.UploadFilesInfo;
import com.dr.digital.uploadfiles.mapper.ImportMapper;
import com.dr.digital.uploadfiles.service.UploadFilesService;
import com.dr.digital.util.*;
import com.dr.digital.wssplit.entity.WssplitTagging;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.entity.StatusEntity;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.form.core.service.FormDataService;
import com.dr.framework.common.service.CommonService;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.dr.framework.util.DateTimeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.beans.Transient;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class UploadFilesServiceImpl implements UploadFilesService {
    private Logger logger = LoggerFactory.getLogger(UploadFilesServiceImpl.class);
    @Autowired
    FormDataService formDataService;
    @Autowired
    CommonService commonService;
    @Autowired
    CommonMapper commonMapper;
    @Autowired
    ImportMapper importMapper;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    OcrGeneralClient ocrGeneralClient;
    @Autowired
    OcrTemplateClient ocrTemplateClient;
    @Autowired
    @Lazy
    OcrService ocrService;
    @Autowired
    ArchiveBatchService archiveBatchService;
    @Autowired
    TokenClient tokenClient;
    @Autowired
    OfdClient ofdClient;
    @Autowired
    ArchiveDataManager dataManager;
    @Autowired
    DefaultArchiveDataManager defaultArchiveDataManager;
    @Autowired
    OfdConfig ofdConfig;
    @Value("${filePath}")
    private String filePath;
    @Value("${ofd.baseIp}")
    private String ofdUrl;
    //???????????????jpg
    ExecutorService executorService = Executors.newFixedThreadPool(8);
    //OFD??????
    ExecutorService executor = Executors.newFixedThreadPool(1);
    CountDownLatch countDownLatch = new CountDownLatch(10);

    @Override
    public List<UploadFiles> findUploadFilesByBussinessId(String aId) {
        SqlQuery<UploadFiles> sqlQuery = SqlQuery.from(UploadFiles.class);
        sqlQuery.equal(UploadFilesInfo.BUSSINESSID, aId).equal(UploadFilesInfo.FILESTATUS, "1");
        sqlQuery.orderBy(UploadFilesInfo.ORDERBY);
        return commonMapper.selectByQuery(sqlQuery);
    }

    @Override
    public Long findImgCountById(String aId) {
        SqlQuery<UploadFiles> sqlQuery = SqlQuery.from(UploadFiles.class,false).column(UploadFilesInfo.ID);
        sqlQuery.equal(UploadFilesInfo.BUSSINESSID, aId).equal(UploadFilesInfo.FILESTATUS, "1");
        sqlQuery.orderBy(UploadFilesInfo.ORDERBY);
        return commonService.countByQuery(sqlQuery);
    }
    @Override
    public Integer findUploadFilesOrderMaxByBussinessId(String aId) {
        UploadFiles u = commonMapper.selectOneByQuery(SqlQuery.from(UploadFiles.class, false).max(UploadFilesInfo.ORDERBY).equal(UploadFilesInfo.BUSSINESSID, aId));
        if (u != null) {
            return u.getOrder();
        } else {
            return 0;
        }
    }

    String nextid = "";

    /**
     * ??????
     *
     * @param multipartfile
     * @param formData
     * @param filesOrder
     * @param newFilePath
     */
    @Override
    public void uploadFiles(MultipartFile multipartfile, FormData formData, Integer filesOrder, String newFilePath) {
        Assert.notNull(formData, "????????????????????????");
        UploadFiles uploadFiles = new UploadFiles();
        String id = UUID.randomUUID().toString();
        if (filesOrder == 0) {
            nextid = id;
            id = "";
            uploadFiles.setOrder(1);
            uploadFiles.setSrcName(String.format("%03d", 1) + ".jpg");
        } else {
            String v = "";
            v = id;
            id = nextid;
            nextid = v;
            uploadFiles.setOrder(findUploadFilesByBussinessId(formData.getId()).size() + 1);
            uploadFiles.setSrcName(String.format("%03d", findUploadFilesByBussinessId(formData.getId()).size() + 1) + ".jpg");
        }
        File file = new File(String.join(File.separator, filePath, "filePath", newFilePath, uploadFiles.getSrcName()));
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
                multipartfile.transferTo(file);
                if (StringUtils.isEmpty(id)) {
                    uploadFiles.setParentId(id);
                } else {
                    UploadFiles uploadFilesByParentId = findUploadFilesByParentId(id);
                    if (uploadFilesByParentId != null) {
                        uploadFiles.setParentId(uploadFilesByParentId.getNextId());
                    } else {
                        uploadFiles.setParentId(id);
                    }
                }
                ImageUtil.thumbnailImageWindows(file.getPath(), 300, 325, filePath + File.separator + "fileThumbnailPath" + File.separator + newFilePath, false);
                uploadFiles.setThumbnailPath("fileThumbnailPath" + File.separator + newFilePath + File.separator + uploadFiles.getSrcName());
                uploadFiles.setThumbnailAbsolutePath(filePath + File.separator + "fileThumbnailPath" + File.separator + newFilePath + File.separator + uploadFiles.getSrcName());
                uploadFiles.setFilePath("filePath" + file.separator + newFilePath + File.separator + uploadFiles.getSrcName());
                uploadFiles.setAbsolutePath(file.getPath());
                uploadFiles.setNextId(nextid);
                uploadFiles.setFileSize(getPrintSize(file.length()));
                uploadFiles.setBatch(1);
                addUploadFiles(uploadFiles, formData.getId(), new Person());
                //????????????????????? ????????????
                formData.put(ArchiveEntity.COLUMN_TRANSITION_STATE, '1');
                formData.put(ArchiveEntity.COLUMN_SPLIT_STATE, '1');
                formDataService.updateFormDataById(formData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ??????
     *
     * @param multipartfile
     * @param formData
     * @param newFilePath
     * @param id
     */
    @Override
    public void creatPlug(MultipartFile multipartfile, FormData formData, String newFilePath, String id) {
        Assert.notNull(formData, "????????????????????????");
        //?????????????????????????????????????????????
        UploadFiles uploadFile = findUploadFilesOneById(id);
        //?????? ??????????????????????????????
        UploadFiles fistFile = importMapper.findUploadFilesByNullParentId(formData.getId());
        id = UUID.randomUUID().toString();
        //??????parentId?????????????????????????????????
        if (StringUtils.isEmpty(uploadFile.getParentId())) {
            //???????????????????????????
            fistFile.setNextId(uploadFile.getId());
            fistFile.setBatch(2);//2????????????
            fistFile.setId(id);
            //??????????????????
            uploadFile.setParentId(fistFile.getNextId());
            clientUpdateFilesById(uploadFile, new Person());
            commonMapper.insert(fistFile);
            //???????????????????????????
            updateUpload(fistFile, newFilePath);
            //?????????????????????
            ImgVo imgVo = new ImgVo();
            imgVo.setFilePath(fistFile.getFilePath());
            String sum = FileUtil.getFileNameNoEx(fistFile.getSrcName());
            imgVo.setFileName(String.format("%03d", Integer.parseInt(sum) + 1) + ".jpg");
            updateFileVolumesImg(imgVo, new Person());
            imgVo.setFilePath(fistFile.getThumbnailPath());
            updateFileVolumesImg(imgVo, new Person());
            File file = new File(uploadFile.getAbsolutePath());
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    multipartfile.transferTo(file);
                    ImageUtil.thumbnailImageWindows(file.getPath(), 300, 325, filePath + File.separator + "fileThumbnailPath" + File.separator + newFilePath, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            while (true) {
                if (!fistFile.getNextId().equals(uploadFile.getParentId())) {
                    fistFile = findUploadFilesByParentId(fistFile.getNextId());
                } else {
                    break;
                }
            }
            //??????????????????
            uploadFile.setParentId(id);
            clientUpdateFilesById(uploadFile, new Person());
            //?????????????????????
            uploadFile.setParentId(fistFile.getNextId());
            uploadFile.setNextId(id);
            uploadFile.setBatch(2);//2????????????
            uploadFile.setId(UUID.randomUUID().toString());
            commonMapper.insert(uploadFile);
            //???????????????????????????
            updateUpload(uploadFile, newFilePath);
            //?????????????????????
            ImgVo imgVo = new ImgVo();
            imgVo.setFilePath(uploadFile.getFilePath());
            String sum = FileUtil.getFileNameNoEx(uploadFile.getSrcName());
            imgVo.setFileName(String.format("%03d", Integer.parseInt(sum) + 1) + ".jpg");
            updateFileVolumesImg(imgVo, new Person());
            imgVo.setFilePath(uploadFile.getThumbnailPath());
            updateFileVolumesImg(imgVo, new Person());
            File file = new File(uploadFile.getAbsolutePath());
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    multipartfile.transferTo(file);
                    ImageUtil.thumbnailImageWindows(file.getPath(), 300, 325, filePath + File.separator + "fileThumbnailPath" + File.separator + newFilePath, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void creatSweeping(MultipartFile multipartfile, FormData formData, String newFilePath, String id) {
        Assert.notNull(formData, "????????????????????????");
        Assert.isTrue(!StringUtils.isEmpty(id), "??????Id???????????????");
        //??????????????????
        UploadFiles firstUpload = findUploadFilesOneById(id);
        String nextId = firstUpload.getNextId();
        UploadFiles uploadFilesByParentId = findUploadFilesByParentId(nextId);
        id = UUID.randomUUID().toString();
    }

    /**
     * ??????
     *
     * @param multipartfile
     * @param formData
     * @param newFilePath
     * @param id
     */
    @Override
    public void creatSaul(MultipartFile multipartfile, FormData formData, String newFilePath, String id, UploadFiles uploadFile) {
        Assert.notNull(formData, "????????????????????????");
        Assert.isTrue(!StringUtils.isEmpty(id), "????????????Id???????????????");
        File file = new File(uploadFile.getAbsolutePath());
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                //??????????????????
                file.createNewFile();
                multipartfile.transferTo(file);
                //?????????????????????
                ImageUtil.thumbnailImageWindows(file.getPath(), 300, 325, filePath + File.separator + "fileThumbnailPath" + File.separator + newFilePath, false);
                uploadFile.setFileSize(Long.toString(file.length()));
                uploadFile.setBatch(4);//??????
                addUploadFiles(uploadFile, formData.getId(), new Person());
                // updateUpload(uploadFile, newFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ?????? ?????????????????????????????????
     *
     * @param ldUploadFiles
     */
    private void updateUpload(UploadFiles ldUploadFiles, String newFilePath) {
        UploadFiles fistFile = findUploadFilesByParentId(ldUploadFiles.getNextId());
        File file = new File("aa");//??????????????????file.separator
        if (fistFile != null) {
            String sum = FileUtil.getFileNameNoEx(fistFile.getSrcName());
            fistFile.setParentId(ldUploadFiles.getNextId());
            fistFile.setSrcName(String.format("%03d", Integer.parseInt(sum) + 1) + ".jpg");
            fistFile.setOrder(Integer.parseInt(sum) + 1);
            fistFile.setFilePath("filePath" + file.separator + newFilePath + fistFile.getSrcName());
            fistFile.setAbsolutePath(filePath + newFilePath + fistFile.getSrcName());
            fistFile.setThumbnailPath("fileThumbnailPath" + file.separator + newFilePath + fistFile.getSrcName());
            fistFile.setThumbnailAbsolutePath(filePath + File.separator + "fileThumbnailPath" + File.separator + newFilePath + fistFile.getSrcName());
            clientUpdateFilesById(fistFile, new Person());
            updateUpload(fistFile, newFilePath);
        }
    }

    public String clientUpdateFilesById(UploadFiles uploadFiles, Person aPerson) {
        uploadFiles.setUpdateDate(DateTimeUtils.getMillis());
        uploadFiles.setUpdatePerson(aPerson.getId());
        commonMapper.updateIgnoreNullById(uploadFiles);
        return "0";
    }

    /**
     * ??????????????????
     *
     * @param size
     * @return
     */
    public String getPrintSize(long size) {
        // ?????????????????????1024???????????????B???????????????????????????1024??????3?????????????????????
        if (size < 1024) {
            return String.valueOf(size) + "B";
        } else {
            size = size / 1024;
        }
        // ????????????????????????1024???????????????1024?????????????????????KB????????????
        // ??????????????????????????????????????????????????????
        // ?????????????????????
        if (size < 1024) {
            return String.valueOf(size) + "KB";
        } else {
            size = size / 1024;
        }
        if (size < 1024) {
            // ???????????????MB?????????????????????????????????1????????????
            // ????????????????????????100???????????????
            size = size * 100;
            return String.valueOf((size / 100)) + "." + String.valueOf((size % 100)) + "MB";
        } else {
            // ??????????????????GB????????????????????????1024?????????????????????
            size = size * 100 / 1024;
            return String.valueOf((size / 100)) + "." + String.valueOf((size % 100)) + "GB";
        }
    }

    /**
     * ???????????????id????????????
     *
     * @param nextId
     * @return
     */
    @Override
    public UploadFiles findUploadFilesByParentId(String nextId) {
        SqlQuery<UploadFiles> sqlQuery = SqlQuery.from(UploadFiles.class);
        sqlQuery.equal(UploadFilesInfo.PARENTID, nextId);
        return commonMapper.selectOneByQuery(sqlQuery);
    }

    public String addUploadFiles(UploadFiles uploadFiles, String archivesId, Person aPerson) {
        uploadFiles.setId(UUID.randomUUID().toString());
        uploadFiles.setCreateDate(DateTimeUtils.getMillis());
        uploadFiles.setCreatePerson(aPerson.getId());
        uploadFiles.setUpdateDate(DateTimeUtils.getMillis());
        uploadFiles.setBussinessId(archivesId);
        // 0 ?????????  1 ????????????
        uploadFiles.setFilesTatus("1");
        commonMapper.insert(uploadFiles);
        return "0";
    }

    @Override
    public UploadFiles findUploadFilesOneById(String Id) {
        SqlQuery<UploadFiles> sqlQuery = SqlQuery.from(UploadFiles.class);
        sqlQuery.equal(UploadFilesInfo.ID, Id);
        return commonMapper.selectOneByQuery(sqlQuery);
    }

    /**
     * ??????????????????
     *
     * @param formData
     * @param registerId
     * @param person
     * @throws Exception
     */
    @Override
    public void fileSplitWrit(FormData formData, String registerId, Person person) {
        //?????????????????? ????????????????????????
        SqlQuery<FileStructure> sqlQuery = SqlQuery.from(FileStructure.class)
                .equal(FileStructureInfo.ARCHIVERS_CATEGORY_CODE, formData.get(ArchiveEntity.COLUMN_CATEGORY_CODE) + "")
                .equal(FileStructureInfo.AJ_ARCHIVAL_CODE, formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "")
                .equal(FileStructureInfo.REGISTERID, registerId);
        List<FileStructure> VolumesList = commonMapper.selectByQuery(sqlQuery);
        if (VolumesList.size() > 0) {
            for (FileStructure fileStructure : VolumesList) {
                int num = 1;
                String JH;
                if (!StringUtils.isEmpty(fileStructure.getArchives_item_number())) {
                    JH = fileStructure.getArchives_item_number();
                } else {
                    JH = "000";
                }
                //???????????????????????????
                String filepath = String.join(File.separator, filePath, "split", fileStructure.getFonds_identifier(), fileStructure.getAj_archival_code(), JH);
                File fileVolumes = new File(filepath);
                if (!fileVolumes.exists()) {
                    fileVolumes.mkdirs();
                } else {
                    File[] yFileList = fileVolumes.listFiles();
                    for (File yFile : yFileList) {
                        yFile.delete();
                    }
                }
                //?????????????????????
                String wfPath = String.join(File.separator, filePath, "filePath", fileStructure.getFonds_identifier(), fileStructure.getAj_archival_code());
                File file = new File(wfPath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                //???????????????txt??????
                String txtPath = String.join(File.separator, filePath, "txt", fileStructure.getFonds_identifier(), fileStructure.getAj_archival_code());
                File txtFile = new File(txtPath);
                if (!txtFile.exists()) {
                    txtFile.mkdirs();
                }
                String[] fileList = file.list();
                if (fileList.length != 0) {
                    for (int i = 0; i < fileList.length; i++) {
                        File readFile = new File(file + File.separator + fileList[i]);
                        if (!readFile.isDirectory()) {
                            String houZu = fileList[i].substring(fileList[i].indexOf("."));
                            //????????????
                            int CatalogNum = 0;
                            if (!StringUtils.isEmpty(formData.get(ArchiveEntity.COLUMN_CATALOG_NUM))) {
                                CatalogNum = Integer.parseInt(formData.get(ArchiveEntity.COLUMN_CATALOG_NUM));
                            }
                            int YS = Integer.parseInt(StringUtils.isEmpty(fileStructure.getTotal_number_of_pages()) ? "0" : fileStructure.getTotal_number_of_pages());
                            int YH = Integer.parseInt(StringUtils.isEmpty(fileStructure.getPage_number()) ? "0" : fileStructure.getPage_number());
                            int Jh = Integer.parseInt(JH);
                            int JS = YH + YS - 1;
                            if (1 == YH) {
                                JS += CatalogNum;
                            } else {
                                YH += CatalogNum;
                                JS += CatalogNum;
                            }
                            if (!readFile.getName().contains(".DS_Store")) {
                                int centre = 0;
                                if (readFile.getName().contains("_")) {
                                    centre = Integer.parseInt(readFile.getName().substring(0, readFile.getName().lastIndexOf("_")).replaceAll("^(0+)", ""));
                                } else {
                                    centre = Integer.parseInt(readFile.getName().substring(0, readFile.getName().lastIndexOf(".")).replaceAll("^(0+)", ""));
                                }
                                if (YH <= centre && centre <= JS) {
                                    String photoName = getNewFileName(num);
                                    FileChannel inputChannel = null;
                                    FileChannel outputChannel = null;
                                    try {
                                        File file1 = new File(String.join(File.separator, filePath, "split", fileStructure.getFonds_identifier(), fileStructure.getAj_archival_code(), JH, photoName + houZu));
                                        if (!file1.getParentFile().exists()) {
                                            file1.getParentFile().mkdirs();
                                        }
                                        if (!file1.exists()) {
                                            file1.createNewFile();
                                        }
                                        inputChannel = new FileInputStream(readFile).getChannel();
                                        outputChannel = new FileOutputStream(file1).getChannel();
                                        outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } finally {
                                        try {
                                            inputChannel.close();
                                            outputChannel.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    num++;
                                }
                            }
                        }
                    }
                }
                num = 1;
                String[] txtFileList = txtFile.list();
                if (txtFileList.length != 0) {
                    for (int i = 0; i < txtFileList.length; i++) {
                        File readFile = new File(txtFile + File.separator + txtFileList[i]);
                        if (!readFile.isDirectory()) {
                            String houZu = txtFileList[i].substring(txtFileList[i].indexOf("."));
                            //????????????
                            int CatalogNum = 0;
                            if (!StringUtils.isEmpty(formData.get(ArchiveEntity.COLUMN_CATALOG_NUM))) {
                                CatalogNum = Integer.parseInt(formData.get(ArchiveEntity.COLUMN_CATALOG_NUM));
                            }
                            int YS = Integer.parseInt(StringUtils.isEmpty(fileStructure.getTotal_number_of_pages()) ? "0" : fileStructure.getTotal_number_of_pages());
                            int YH = Integer.parseInt(StringUtils.isEmpty(fileStructure.getPage_number()) ? "0" : fileStructure.getPage_number());
                            int Jh = Integer.parseInt(JH);
                            int JS = YH + YS - 1;
                            if (1 == YH) {
                                JS += CatalogNum;
                            } else {
                                YH += CatalogNum;
                                JS += CatalogNum;
                            }
                            if (!readFile.getName().contains(".DS_Store")) {
                                int centre = 0;
                                if (readFile.getName().contains("_")) {
                                    centre = Integer.parseInt(readFile.getName().substring(0, readFile.getName().lastIndexOf("_")).replaceAll("^(0+)", ""));
                                } else {
                                    centre = Integer.parseInt(readFile.getName().substring(0, readFile.getName().lastIndexOf(".")).replaceAll("^(0+)", ""));
                                }
                                if (YH <= centre && centre <= JS) {
                                    String photoName = getNewFileName(num);
                                    FileChannel inputChannel = null;
                                    FileChannel outputChannel = null;
                                    try {
                                        File file1 = new File(String.join(File.separator, filePath, "split", fileStructure.getFonds_identifier(), fileStructure.getAj_archival_code(), JH, photoName + houZu));
                                        if (!file1.getParentFile().exists()) {
                                            file1.getParentFile().mkdirs();
                                        }
                                        if (!file1.exists()) {
                                            file1.createNewFile();
                                        }
                                        inputChannel = new FileInputStream(readFile).getChannel();
                                        outputChannel = new FileOutputStream(file1).getChannel();
                                        outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } finally {
                                        try {
                                            inputChannel.close();
                                            outputChannel.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    num++;
                                }
                            }
                        }
                    }
                }
            }
            formDataService.updateFormDataById(formData);
        } else {
            // ???????????????
            String fond_code = formData.get(ArchiveEntity.COLUMN_FOND_CODE);
            String archive_code = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
            //????????????????????????jpg??????
            String wfPath2 = String.join(File.separator, filePath, "filePath", fond_code, archive_code);
            File file = new File(wfPath2);
            if (!file.exists()) {
                file.mkdirs();
            }
            //??????????????????????????? ???????????????????????????????????????
            FileStructure data = new FileStructure();
            data.setArchives_item_number("001");
            data.setFonds_identifier(fond_code);
            data.setArchivers_category_code(formData.get(ArchiveEntity.COLUMN_CATEGORY_CODE));
            data.setAj_archival_code(archive_code);
            data.setArchival_code(archive_code);
            data.setPage_number("001");
            data.setTotal_number_of_pages((file.list().length) + "");
            data.setRegisterId(registerId);
            data.setFile_type(archive_code);
            data.setStatus("VOLUMES");
            data.setId(UUIDUtils.getUUID());
            commonMapper.insert(data);
            //????????????
            fileSplitWrit(formData, registerId, person);
        }
    }

    @Transient
    @Override
    public String deleteUploadFilesById(String aId) {
        logger.info("deleteFileById???aId:" + aId);
        commonMapper.deleteByQuery(SqlQuery.from(UploadFiles.class).equal(UploadFilesInfo.ID, aId));
        logger.info("????????????");
        return "0";
    }

    /**
     * ????????????????????????
     *
     * @param uploadFiles
     * @param filePaths
     * @param flag
     */
    @Override
    public void deleteAllByBussinessId(UploadFiles uploadFiles, String filePaths, String flag) {
        try {
            new File(uploadFiles.getAbsolutePath()).delete();
            new File(uploadFiles.getThumbnailAbsolutePath()).delete();
            deleteUploadFilesById(uploadFiles.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ?????????????????????pdf ?????????ofd??????
     *
     * @param formData
     * @param childFormId
     * @param registerId
     * @param person
     * @return
     * @throws Exception
     */
    @Override
    public boolean photoMergeToPDFToOFD(FormData formData, String childFormId, String registerId, Person person) throws Exception {
        //??????????????????????????????????????? ????????????????????????????????????
        if (!StringUtils.isEmpty(childFormId)) {
            List<FormData> formList = formDataService.selectFormData(childFormId, (sqlQuery, formRelationWrapper) -> {
                sqlQuery.like(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_AJDH), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE))
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), formData.get(ArchiveEntity.COLUMN_STATUS) + "")
                        .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
            });
            if (formList.size() > 0) {
                for (FormData volumesList : formList) {
                    int num = 1;
                    String JH;
                    if (!StringUtils.isEmpty(volumesList.get(ArchiveEntity.COLUMN_JH))) {
                        JH = volumesList.get(ArchiveEntity.COLUMN_JH);
                    } else {
                        JH = "000";
                    }
                    File fileVolumes = new File(String.join(File.separator, filePath, "split", volumesList.get(ArchiveEntity.COLUMN_FOND_CODE), volumesList.get(ArchiveEntity.COLUMN_AJDH), JH));
                    if (!fileVolumes.exists()) {
                        fileVolumes.mkdirs();
                    }
                    File[] imageFiles = fileVolumes.listFiles();
                    if (imageFiles.length > 0) {
                        //????????????pdf
                        String pdfName = "??????";
                        if (!"".equals(volumesList.get(ArchiveEntity.COLUMN_FILE_TYPE))) {
                            pdfName = volumesList.get(ArchiveEntity.COLUMN_FILE_TYPE);
                        }
                        final String outPDF = String.join(File.separator, filePath, "pdf", volumesList.get(ArchiveEntity.COLUMN_FOND_CODE), volumesList.get(ArchiveEntity.COLUMN_AJDH), pdfName);
                        final File file = new File(outPDF);
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        final String pdfUrl = Img2PdfUtil.imagesToPdf(outPDF, fileVolumes.getAbsolutePath(), volumesList.get(ArchiveEntity.COLUMN_ARCHIVE_CODE));
                        //todo ??????????????????pdf??????ocr????????????pdf
                        //??????pdf??????????????????ofd?????????????????????pdf??????????????????ofd
                        /*long timestamp = System.currentTimeMillis();
                        TokenInfo tokenInfo = new TokenInfo(timestamp, ofdConfig.getClientId(), ofdConfig.getClientSecret(), ofdConfig.getApiServerName());
                        TokenResult tokenResult = tokenClient.token(tokenInfo);
                        FileByteInfo fileByteInfo = new FileByteInfo(UUIDUtils.getUUID(), pdfUrl, ofdConfig.getTargetPath(), tokenResult.getData().getAuthToken());
                        FileStreamResult fileStreamResult = ofdClient.turnOFD(fileByteInfo);
                        //??????ofd????????????
                        insetOfdRecord(volumesList.get(ArchiveEntity.COLUMN_AJDH), fileStreamResult.getCode(), fileStreamResult.getMsg(), fileStreamResult.getData(), volumesList.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + ".ofd", pdfUrl);
                        //???????????? ?????????????????????????????????
                        if ("0".equals(fileStreamResult.getCode())) {
                            formData.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "3");
                            formDataService.updateFormDataById(formData);
                            volumesList.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "3");
                            formDataService.updateFormDataById(volumesList);
                        } else {
                            formData.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "2");
                            formDataService.updateFormDataById(formData);
                            volumesList.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "2");
                            formDataService.updateFormDataById(volumesList);
                        }*/
                        formData.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "3");
                        formDataService.updateFormDataById(formData);
                        volumesList.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "3");
                        formDataService.updateFormDataById(volumesList);
                    }
                }
            }
        } else {
            //????????????????????? ??????????????? ????????????????????? ??????????????????
            //???????????????????????????
            final String outPDF = filePath + File.separator + "pdf" + File.separator + formData.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
            final File file = new File(outPDF);
            if (!file.exists()) {
                file.mkdirs();
            }
            File fileVolumes = new File(filePath + File.separator + "split" + File.separator + formData.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE));
            if (!fileVolumes.exists()) {
                fileVolumes.mkdirs();
            }
           /* //????????????pdf
            final String pdfUrl = Img2PdfUtil.imagesToPdf(outPDF, fileVolumes.getAbsolutePath(), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE));
            //todo ???pdf?????????????????????ofd?????????
            long timestamp = System.currentTimeMillis();
            TokenInfo tokenInfo = new TokenInfo(timestamp, ofdConfig.getClientId(), ofdConfig.getClientSecret(), ofdConfig.getApiServerName());
            TokenResult tokenResult = tokenClient.token(tokenInfo);
            FileByteInfo fileByteInfo = new FileByteInfo(UUIDUtils.getUUID(), pdfUrl, ofdConfig.getTargetPath(), tokenResult.getData().getAuthToken());
            FileStreamResult fileStreamResult = ofdClient.turnOFD(fileByteInfo);
            //??????ofd????????????
            insetOfdRecord(formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE), fileStreamResult.getCode(), fileStreamResult.getMsg(), fileStreamResult.getData(), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + ".ofd", pdfUrl);
            //???????????? ?????????????????????????????????
            if ("0".equals(fileStreamResult.getCode())) {
                formData.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "3");
                formDataService.updateFormDataById(formData);
            }*/
        }
        return false;
    }

    /**
     * ?????????????????????pdf
     *
     * @param formData
     * @param childFormId
     * @param registerId
     * @param person
     * @return
     * @throws Exception
     */
    @Override
    public boolean photoMergeToPDF(FormData formData, String childFormId, String registerId, Person person) throws Exception {
        //??????????????????????????????????????? ????????????????????????????????????
        if (!StringUtils.isEmpty(childFormId)) {
            List<FormData> formList = formDataService.selectFormData(childFormId, (sqlQuery, formRelationWrapper) -> {
                sqlQuery.like(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_AJDH), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE))
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), formData.get(ArchiveEntity.COLUMN_STATUS) + "")
                        .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
            });
            if (formList.size() > 0) {
                for (FormData volumesList : formList) {
                    int num = 1;
                    String JH;
                    if (!StringUtils.isEmpty(volumesList.get(ArchiveEntity.COLUMN_JH))) {
                        JH = volumesList.get(ArchiveEntity.COLUMN_JH);
                    } else {
                        JH = "000";
                    }
                    File fileVolumes = new File(filePath + File.separator + "split" + File.separator + volumesList.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + volumesList.get(ArchiveEntity.COLUMN_AJDH) + File.separator + JH);
                    if (!fileVolumes.exists()) {
                        fileVolumes.mkdirs();
                    }
                    File[] imageFiles = fileVolumes.listFiles();
                    int len = imageFiles.length;
                    if (len > 0) {
                        //????????????pdf
                        String pdfName = "??????";
                        if (!"".equals(volumesList.get(ArchiveEntity.COLUMN_FILE_TYPE))) {
                            pdfName = volumesList.get(ArchiveEntity.COLUMN_FILE_TYPE);
                        }
                        final String outPDF = filePath + File.separator + "pdf" + File.separator + volumesList.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + volumesList.get(ArchiveEntity.COLUMN_AJDH) + File.separator + pdfName;
                        final File file = new File(outPDF);
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        Img2PdfUtil.imagesToPdf(outPDF, fileVolumes.getAbsolutePath(), volumesList.get(ArchiveEntity.COLUMN_ARCHIVE_CODE));
                        //???????????? ?????????????????????????????????
                        formData.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "2");
                        formDataService.updateFormDataById(formData);
                        volumesList.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "2");
                        formDataService.updateFormDataById(volumesList);
                    }
                }
            }
        } else {
            //????????????????????? ??????????????? ????????????????????? ??????????????????
            //???????????????????????????
            final String outPDF = filePath + File.separator + "pdf" + File.separator + formData.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
            final File file = new File(outPDF);
            if (!file.exists()) {
                file.mkdirs();
            }
            File fileVolumes = new File(filePath + File.separator + "split" + File.separator + formData.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE));
            if (!fileVolumes.exists()) {
                fileVolumes.mkdirs();
            }
            String pdfName = "";
            if (!"".equals(formData.get(ArchiveEntity.COLUMN_FILE_TYPE))) {
                pdfName = formData.get(ArchiveEntity.COLUMN_FILE_TYPE);
            } else {
                pdfName = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
            }
            //????????????pdf
            Img2PdfUtil.imagesToPdf(outPDF, fileVolumes.getAbsolutePath(), pdfName);
            //???????????? ?????????????????????????????????
            formData.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "2");
            formDataService.updateFormDataById(formData);
        }
        return false;
    }

    /**
     * ?????????txt????????????????????????ODF???
     *
     * @param formData
     * @param registerId
     * @param person
     * @return
     * @throws Exception
     */
    @Override
    public boolean photoMergeToOfd(FormData formData, String registerId, Person person) throws Exception {
        //?????????????????? ????????????????????????
        SqlQuery<FileStructure> sqlQuery = SqlQuery.from(FileStructure.class)
                .equal(FileStructureInfo.ARCHIVERS_CATEGORY_CODE, formData.get(ArchiveEntity.COLUMN_CATEGORY_CODE) + "")
                .equal(FileStructureInfo.AJ_ARCHIVAL_CODE, formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "")
                .equal(FileStructureInfo.REGISTERID, registerId);
        List<FileStructure> VolumesList = commonMapper.selectByQuery(sqlQuery);
        if (VolumesList.size() > 0) {
            for (FileStructure fileStructure : VolumesList) {
                String fond_code = fileStructure.getFonds_identifier();
                String archive_code = fileStructure.getAj_archival_code();
                String JH;
                if (!StringUtils.isEmpty(fileStructure.getArchives_item_number())) {
                    JH = fileStructure.getArchives_item_number();
                } else {
                    JH = "000";
                }
                //??????????????????????????????txt?????????????????????
                File fileVolumes = new File(String.join(File.separator, filePath, "split", fond_code, archive_code, JH));
                if (!fileVolumes.exists()) {
                    fileVolumes.mkdirs();
                }
                String pdfName = "??????";
                if (!"".equals(fileStructure.getFile_type())) {
                    pdfName = fileStructure.getFile_type();
                }
                //?????????????????????ofd????????????
                File ofdFile = new File(String.join(File.separator, filePath, "ofd", fileStructure.getFonds_identifier(), fileStructure.getAj_archival_code(), pdfName));
                if (!ofdFile.exists()) {
                    ofdFile.mkdirs();
                } else {
                    File[] yFileList = ofdFile.listFiles();
                    for (File yFile : yFileList) {
                        yFile.delete();
                    }
                }
                //????????????
                OfdConversion ofdTxt = new OfdConversion();
                int ret = ofdTxt.init();
                if (ret == 0) {
                    ofdTxt.imgOfdLangChAo(fileVolumes.getPath(), ofdFile.getPath() + File.separator + fileStructure.getArchival_code() + ".ofd");
                    ofdTxt.destroy();
                    //??????ofd????????????
                    insetOfdRecord(fileStructure.getAj_archival_code(), String.valueOf(ret), "", "", fileStructure.getArchival_code() + ".ofd", ofdFile.getPath());
                    formData.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "3");
                    formDataService.updateFormDataById(formData);
                }
            }
        }
        return false;
    }

    /**
     * @param path ???????????????
     * @return
     */
    @Override
    public Map<String, String> addXMLFile(String path) {
        String url = String.join(File.separator, filePath, "tifold", Constants.ofd_addXMLFile);
        String response = null;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("file", path);
        jsonObject.put("xml", "");
        jsonObject.put("save", "");
        jsonObject.put("name", "");
        try {
            response = FaceHttpUtil.sendHttpPost(url, jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jo = JSONObject.parseObject(response);
        String returncode = jo.getString("returncode");
        String returnmsg = jo.getString("returnmsg");
        String data = jo.getString("data");
        final HashMap<String, String> map = new HashMap<>();
        map.put("returncode", returncode);
        map.put("returnmsg", returnmsg);
        map.put("data", data);
        return map;
    }

    /**
     * @param src  ???????????????
     * @param save ??????????????????
     * @return
     */
    @Override
    public Map<String, String> mergeBatchPage(String src, String save) {
        String url = ofdUrl + Constants.ofd_mergeBatchPage;
        String response = null;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("src", src);
        jsonObject.put("save", save);
        try {
            response = FaceHttpUtil.sendHttpPost(url, jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jo = JSONObject.parseObject(response);
        String returncode = jo.getString("returncode");
        String returnmsg = jo.getString("returnmsg");
        String data = jo.getString("data");
        final HashMap<String, String> map = new HashMap<>();
        map.put("returncode", returncode);
        map.put("returnmsg", returnmsg);
        map.put("data", data);
        return map;
    }

    /**
     * ??????????????????
     *
     * @param imgVo
     * @param person
     */
    @Override
    public void updateFileVolumesImg(ImgVo imgVo, Person person) {
        Assert.notNull(imgVo.getFilePath(), "????????????????????????");
        Assert.notNull(imgVo.getFileName(), "????????????????????????");
        String filePaths = imgVo.getFilePath().substring(0, imgVo.getFilePath().lastIndexOf(File.separator));
        String fileHao = imgVo.getFilePath().substring(imgVo.getFilePath().lastIndexOf(File.separator) + 1);
        int fileHm = Integer.parseInt(fileHao.substring(0, fileHao.indexOf(".")));
        String houZu = fileHao.substring(fileHao.indexOf("."));
        int newFileHm = Integer.parseInt(imgVo.getFileName().substring(0, imgVo.getFileName().indexOf(".")));
        //???????????????????????????????????????????????????
        if (fileHm != newFileHm) {
            if (filePaths.contains("filePath")) {
                filePaths = filePath + File.separator + "filePath" + File.separator + filePaths.substring(filePaths.indexOf(File.separator) + 1);
            } else if (filePaths.contains("splitPath")) {
                filePaths = filePath + File.separator + "split" + File.separator + filePaths.substring(filePaths.indexOf(File.separator) + 1);
            } else if (filePaths.contains("fileThumbnailPath")) {
                filePaths = filePath + File.separator + "fileThumbnailPath" + File.separator + filePaths.substring(filePaths.indexOf(File.separator) + 1);
            }
            File readFile = new File(filePaths);
            if (readFile.exists()) {
                int cha = newFileHm - fileHm;
                String[] fileList = readFile.list();
                if (fileList.length >= 0) {
                    if (cha >= 0) {
                        for (int i = fileList.length - 1; i >= 0; i--) {
                            int fileH = Integer.parseInt(fileList[i].substring(0, fileList[i].indexOf(".")));
                            if (fileH >= fileHm) {
                                File folder = new File(filePaths + File.separator + fileList[i]);
                                File newDir = new File(filePaths + File.separator + getNewFileName(fileH + cha) + houZu);
                                if (!folder.isDirectory()) {
                                    folder.renameTo(newDir);
                                }
                            }
                        }
                    } else {
                        for (int i = 0; i < fileList.length; i++) {
                            int fileH = Integer.parseInt(fileList[i].substring(0, fileList[i].indexOf(".")));
                            if (fileH >= fileHm) {
                                File folder = new File(filePaths + File.separator + fileList[i]);
                                File newDir = new File(filePaths + File.separator + getNewFileName(fileH + cha) + houZu);
                                if (!folder.isDirectory()) {
                                    folder.renameTo(newDir);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * ???????????????????????????
     *
     * @param num
     * @return
     */
    public String getNewFileName(int num) {
        String photoName = "";
        if (num < 10) {
            photoName = "00" + num;
        } else if (num >= 10 && num < 100) {
            photoName = "0" + num;
        } else {
            photoName = "" + num;
        }
        return photoName;
    }

    /**
     * ??????????????????
     *
     * @param imgVo
     * @param contents
     * @param person
     */
    @Override
    public void updateFileImg(ImgVo imgVo, String contents, Person person) {
        Assert.isTrue(!StringUtils.isEmpty(contents), "???????????????????????????");
        contents = contents.substring(contents.indexOf(",") + 1);
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] b = decoder.decodeBuffer(contents);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {//??????????????????
                    b[i] += 256;
                }
            }
            String filePaths = imgVo.getFilePath();
            if (filePaths.indexOf("filePath") != -1) {
                filePaths = filePath + File.separator + "filePath" + File.separator + filePaths.substring(filePaths.indexOf(File.separator) + 1);
            } else if (filePaths.indexOf("splitPath") != -1) {
                filePaths = filePath + File.separator + "split" + File.separator + filePaths.substring(filePaths.indexOf(File.separator) + 1);
            }
            OutputStream out = new FileOutputStream(filePaths);
            out.write(b);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * tiff???????????????jpg?????????????????????
     * param imgVo
     */
    @Override
    public void tiffToJpg(ImgVo imgVo, String batchId) {
        File file = new File(imgVo.getFilePath());
        ImageInputStream input;
        try {
            if (file.isFile()) {
                input = ImageIO.createImageInputStream(file);//?????????????????????????????????tif
                if (input != null) {
                    ImageReader reader = ImageIO.getImageReaders(input).next();
                    reader.setInput(input);
                    int count = reader.getNumImages(true);
                    for (int i = 0; i < count; i++) {
                        BufferedImage image = reader.read(i, null);//?????????i???
                        String num = getNewFileName((i + 1));
                        try {
                            //?????????????????? filepath+?????????+??????+?????????
                            File file1 = new File(String.join(File.separator, filePath, "filePath", imgVo.getFileName().substring(0, imgVo.getFileName().indexOf("-")), imgVo.getFileName().substring(0, imgVo.getFileName().indexOf(".")), num + ".jpg"));
                            if (!file1.getParentFile().exists()) {
                                file1.getParentFile().mkdirs();
                            }
                            if (!file1.exists()) {
                                file1.createNewFile();
                            }
                            ImageIO.write(image, "JPEG", file1);//????????????

                            YuanWenBatchDetail yuanWenBatchDetail = new YuanWenBatchDetail();
                            yuanWenBatchDetail.setImgSize(FileUtil.getPrintSize(file1.length()));
                            yuanWenBatchDetail.setArchival_code(imgVo.getFileName().substring(0, imgVo.getFileName().indexOf(".")));
                            yuanWenBatchDetail.setImgName(getNewFileName(i + 1) + ".jpg");
                            yuanWenBatchDetail.setSplit(StatusEntity.STATUS_ENABLE_STR);
                            yuanWenBatchDetail.setId(UUIDUtils.getUUID());
                            yuanWenBatchDetail.setBatchId(batchId);
                            CommonService.bindCreateInfo(yuanWenBatchDetail);
                            commonMapper.insert(yuanWenBatchDetail);

                            File file2 = new File(String.join(File.separator, filePath, "tifbeifen", imgVo.getFileName().substring(0, imgVo.getFileName().indexOf("-")), imgVo.getFileName().substring(0, imgVo.getFileName().indexOf(".")), imgVo.getFileName().substring(0, imgVo.getFileName().indexOf(".")) + ".tif"));
                            if (!file2.getParentFile().exists()) {
                                file2.getParentFile().mkdirs();
                            }
                            if (!file2.exists()) {
                                file2.createNewFile();
                            }
                            FileChannel inputChannel = null;
                            FileChannel outputChannel = null;
                            try {
                                String path = imgVo.getFilePath();
                                inputChannel = new FileInputStream(new File(path)).getChannel();
                                outputChannel = new FileOutputStream(file2).getChannel();
                                outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
                            } finally {
                                inputChannel.close();
                                outputChannel.close();
                                reader.dispose();
                                input.close();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }

    @Override
    @Async
    public void tiffToJpgByPath(Person person) {
        ArchiveBatch batch = archiveBatchService.newBatchJpgByPath("IMP_YUANWEN", new BaseQuery(), person);
        final File tifOldFile = new File(String.join(File.separator, filePath, "tifold"));
        if (!tifOldFile.getParentFile().exists()) {
            tifOldFile.getParentFile().mkdirs();
        }
        if (!tifOldFile.exists()) {
            try {
                tifOldFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //???????????????????????????
        List<File> orderFiles = FileUtil.orderByName(tifOldFile);
        if (orderFiles.size() < 1) {
            batch.setBeizhu("???????????????????????????");
            batch.setStatus(StatusEntity.STATUS_DISABLE_STR);
            batch.setEndDate(System.currentTimeMillis());
            commonMapper.updateById(batch);
        }
        for (File file : orderFiles) {
            executorService.execute(() -> tifTojPGXc(file, batch, person));
        }
    }

    /**
     * ?????? tif??????jpg??????
     *
     * @param file
     * @param batch
     * @param person
     */
    @Async
    public void tifTojPGXc(File file, ArchiveBatch batch, Person person) {
        ImgVo imgVo = new ImgVo();
        imgVo.setFilePath(file.getAbsolutePath());
        String name = file.getName().substring(0, file.getName().lastIndexOf("."));
        imgVo.setFileName(name);
        ImageInputStream input;
        try {
            if (file.isFile()) {
                input = ImageIO.createImageInputStream(file);//?????????????????????????????????tif
                if (input != null) {
                    ImageReader reader = ImageIO.getImageReaders(input).next();
                    reader.setInput(input);
                    int count = reader.getNumImages(true);
                    String path = String.join(File.separator, imgVo.getFileName().substring(0, imgVo.getFileName().indexOf("-")), imgVo.getFileName());
                    for (int i = 0; i < count; i++) {
                        BufferedImage image = reader.read(i, null);//?????????i???
                        String num = getNewFileName((i + 1));
                        String s1 = String.join(File.separator, filePath, "filePath", path, num + ".jpg");
                        File imgFile = new File(s1);
                        if (!imgFile.getParentFile().exists()) {
                            imgFile.getParentFile().mkdirs();
                        }
                        if (!imgFile.exists()) {
                            imgFile.createNewFile();
                        }
                        ImageIO.write(image, "JPEG", imgFile);//????????????
                        YuanWenBatchDetail yuanWenBatchDetail = new YuanWenBatchDetail();
                        yuanWenBatchDetail.setImgSize(FileUtil.getPrintSize(imgFile.length()));
                        yuanWenBatchDetail.setArchival_code(imgVo.getFileName());
                        yuanWenBatchDetail.setImgName(getNewFileName(i + 1) + ".jpg");
                        yuanWenBatchDetail.setSplit(StatusEntity.STATUS_ENABLE_STR);
                        yuanWenBatchDetail.setMuLuYeShu("");
                        yuanWenBatchDetail.setId(UUIDUtils.getUUID());
                        yuanWenBatchDetail.setBatchId(batch.getId());
                        yuanWenBatchDetail.setCreatePerson(person.getId());
                        yuanWenBatchDetail.setCreateDate(System.currentTimeMillis());
                        commonMapper.insert(yuanWenBatchDetail);
                    }
                    File file1 = new File(filePath + File.separator + "tifbeifen" + File.separator + path + ".tif");
                    if (!file1.getParentFile().exists()) {
                        file1.getParentFile().mkdirs();
                    }
                    if (!file1.exists()) {
                        file1.createNewFile();
                    }
                    FileChannel inputChannel = null;
                    FileChannel outputChannel = null;
                    try {
                        inputChannel = new FileInputStream(file).getChannel();
                        outputChannel = new FileOutputStream(file1).getChannel();
                        outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
                    } finally {
                        batch.setStatus(StatusEntity.STATUS_ENABLE_STR);
                        batch.setEndDate(System.currentTimeMillis());
                        commonMapper.updateById(batch);
                        reader.dispose();
                        input.close();
                        inputChannel.close();
                        outputChannel.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            batch.setStatus(StatusEntity.STATUS_DISABLE_STR);
            batch.setEndDate(System.currentTimeMillis());
            commonMapper.updateById(batch);
        } finally {
            batch.setStatus(StatusEntity.STATUS_ENABLE_STR);
            batch.setEndDate(System.currentTimeMillis());
            commonMapper.updateById(batch);
        }
        file.delete();
    }

    /**
     * pdf ??????jpg
     *
     * @throws Exception
     */
    public void pdfToImageFile(File file, ArchiveBatch batch, String fileName, Person person) {
        PDDocument doc = null;
        InputStream stream = null;
        try {
            // pdf??????
            stream = new FileInputStream(file);
            // ????????????PDF??????
            doc = PDDocument.load(stream);
            PDFRenderer pdfRenderer = new PDFRenderer(doc);
            PDPageTree pages = doc.getPages();
            int pageCount = pages.getCount();
            String path = fileName.substring(0, fileName.indexOf("-")) + File.separator + fileName;
            for (int i = 0; i < pageCount; i++) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 200);
                String num = FileUtil.getNewFileName((i + 1));
                String s1 = String.join(File.separator, filePath, "filePath", path, num + ".jpg");
                File imgFile = new File(s1);
                if (!imgFile.getParentFile().exists()) {
                    imgFile.getParentFile().mkdirs();
                }
                if (!imgFile.exists()) {
                    imgFile.createNewFile();
                }
                if (!file.getParentFile().exists()) {
                    // ???????????????????????????????????????
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                ImageIO.write(bim, "JPEG", imgFile);//????????????
                YuanWenBatchDetail yuanWenBatchDetail = new YuanWenBatchDetail();
                yuanWenBatchDetail.setImgSize(FileUtil.getPrintSize(imgFile.length()));
                yuanWenBatchDetail.setArchival_code(fileName);
                yuanWenBatchDetail.setImgName(num + ".jpg");
                yuanWenBatchDetail.setSplit(StatusEntity.STATUS_ENABLE_STR);
                yuanWenBatchDetail.setId(UUIDUtils.getUUID());
                yuanWenBatchDetail.setBatchId(batch.getId());
                yuanWenBatchDetail.setCreatePerson(person.getId());
                yuanWenBatchDetail.setCreateDate(System.currentTimeMillis());
                commonMapper.insert(yuanWenBatchDetail);
            }
            File file1 = new File(String.join(File.separator, filePath, "tifbeifen", path + ".pdf"));
            if (!file1.getParentFile().exists()) {
                file1.getParentFile().mkdirs();
            }
            if (!file1.exists()) {
                file1.createNewFile();
            }
            FileChannel inputChannel = null;
            FileChannel outputChannel = null;
            try {
                inputChannel = new FileInputStream(file).getChannel();
                outputChannel = new FileOutputStream(file1).getChannel();
                outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            } finally {
                batch.setStatus(StatusEntity.STATUS_ENABLE_STR);
                batch.setEndDate(System.currentTimeMillis());
                commonMapper.updateById(batch);
                inputChannel.close();
                outputChannel.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                batch.setStatus(StatusEntity.STATUS_ENABLE_STR);
                batch.setEndDate(System.currentTimeMillis());
                commonMapper.updateById(batch);
                doc.close();
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        file.delete();
    }

    /**
     * ??????ocr????????????
     *
     * @param dirName
     * @param code
     * @param message
     * @param fileName
     * @param path
     */
    public void insetOcrRecord(String dirName, String code, String message, String fileName, String path, String name, String ysTime, long startTime, long endTime, String ImgKb, String registerId) {
        OcrRecord ocrRecord = new OcrRecord();
        ocrRecord.setRegisterId(registerId);
        ocrRecord.setId(UUIDUtils.getUUID());
        ocrRecord.setCreateDate(System.currentTimeMillis());
        ocrRecord.setArchiveCode(dirName);
        ocrRecord.setName(name);
        ocrRecord.setCode(code);
        ocrRecord.setDescription(ysTime);
        ocrRecord.setStartTime(startTime);
        ocrRecord.setEndTime(endTime);
        ocrRecord.setFileKb(ImgKb);
        ocrRecord.setMessage(message);
        ocrRecord.setFileName(fileName);
        ocrRecord.setFilePath(path);
        commonMapper.insert(ocrRecord);
    }

    /**
     * ??????ofd????????????
     *
     * @param dirName
     * @param code
     * @param message
     * @param data
     * @param fileName
     * @param path
     */
    public void insetOfdRecord(String dirName, String code, String message, String data, String fileName, String path) {
        OfdRecord ofdRecord = new OfdRecord();
        ofdRecord.setId(UUIDUtils.getUUID());
        ofdRecord.setCreateDate(System.currentTimeMillis());
        ofdRecord.setArchiveCode(dirName);
        ofdRecord.setCode(code);
        ofdRecord.setMessage(message);
//        ofdRecord.setFhData(data);
        ofdRecord.setFileName(fileName);
        ofdRecord.setFilePath(path);
        commonMapper.insert(ofdRecord);
    }

    /**
     * OCR ??????????????????
     *
     * @param img
     * @return
     */
    public Map<String, String> getTempData(String img) {
        Map<String, String> result = new HashMap<>();
        TemplateBo templateBo = new TemplateBo(true, true, img);
        TemplateResultEntity templateResultEntity = ocrTemplateClient.template(templateBo);
        logger.info("???????????????????????????:{}", templateResultEntity.getData().getResults());
        List<Template> templateList = commonService.selectList(SqlQuery.from(Template.class));
        //????????????????????????????????????map????????????????????????
        Map<String, Template> map = new HashMap();
        for (Template template : templateList) {
            map.put(template.getField(), template);
        }
        //?????????????????????????????????????????????????????????????????????????????????result????????????????????????form??????
        List list = templateResultEntity.getData().getResults();
        for (int i = 0; i < list.size(); i++) {
            Results results = (Results) list.get(i);
            if (map.containsKey(results.getField_name())) {
                Template template2 = map.get(results.getField_name());
                result.put(template2.getFieldval(), results.getResults().get(0));
            }
        }
        return result;
    }

    /**
     * ????????????????????? tif???pdf?????????
     */
    List<String> fileList = new ArrayList<>();

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

    /**
     * ???????????????????????? jpg ??????
     *
     * @param file
     * @return
     */
    List<String> fileList3 = new ArrayList<>();

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

    /**
     * ??????ofd?????? ????????????????????????
     *
     * @param dataList
     * @param formDefinitionId
     * @param registerId
     * @param person
     */
    @Override
    public void mergeConversion(List<FormData> dataList, String formDefinitionId, String registerId, Person person) {
        if (dataList.size() > 0) {
            for (FormData formDataOne : dataList) {
                //????????????????????????
                WssplitTagging wssplitTagging = new WssplitTagging();
                wssplitTagging.setFormDefinitionId(formDefinitionId);
                wssplitTagging.setArchivesId(formDataOne.get(ArchiveEntity.ID_COLUMN_NAME));
                ResultEntity resultEntity = defaultArchiveDataManager.uniquenessJudge(wssplitTagging);
                //?????????????????????????????????
                 if (resultEntity.isSuccess()) {
                    executorService.execute(() -> conversion(formDataOne, registerId, person));
                }
            }
        }
    }

    public void conversion(FormData formData, String registerId, Person person) {
        try {
            //???????????? ???????????????????????? ???????????????????????????
            fileSplitWrit(formData, registerId, person);
            //???????????????????????????
            photoMergeToOfd(formData, registerId, person);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ???????????????jpg ??????
     *
     * @param formDefinitionId
     * @param person
     * @return
     */
    @Override
    @Async
    public void tiffToJpgByPath(String formDefinitionId, Person person) {
        //???????????????????????????????????????????????????
        List<FormData> formDataList = formDataService.selectFormData(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
            sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), "RECEIVE")
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '0')
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), '0')
                    .orderBy(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
        //????????????????????????
        ArchiveBatch batch = archiveBatchService.newBatchJpgByPath("IMP_YUANWEN", new BaseQuery(), person);
        batch.setFormid(formDefinitionId);
        if (formDataList.size() > 0) {
            //?????????????????? ????????????????????? tif
            String tifPath = String.join(File.separator, filePath, "tifold", formDataList.get(0).get(ArchiveEntity.COLUMN_FOND_CODE).toString());
            final File tifOldFile = new File(tifPath);
            if (!tifOldFile.exists()) {
                tifOldFile.mkdirs();
            }
            fileList = new ArrayList<>();
            List<String> directory = isDirectory(tifOldFile);
            if (directory.size() > 0) {
                for (String str : directory) {
                    executorService.execute(() -> fileSplit(str, formDefinitionId, person, batch));
                }
            } else {
                batch.setBeizhu("???????????????????????????");
                batch.setStatus(StatusEntity.STATUS_DISABLE_STR);
                batch.setEndDate(System.currentTimeMillis());
                commonMapper.updateById(batch);
            }
        } else {
            batch.setBeizhu("???????????????????????????");
            batch.setStatus(StatusEntity.STATUS_DISABLE_STR);
            batch.setEndDate(System.currentTimeMillis());
            commonMapper.updateById(batch);
        }
    }

    /**
     * ???????????????????????????????????????ps,?????????????????????????????????????????????????????????????????????
     */
    public void fileSplit(String str, String formDefinitionId, Person person, ArchiveBatch batch) {
        File file = new File(str);
        String suffix = file.getName().split("\\.")[1].toLowerCase();
        if ("tif".equals(suffix) || "pdf".equals(suffix)) {
            String fileName = file.getName().split("\\.")[0];
            //??????????????????????????????
            List<FormData> formDataList = formDataService.selectFormData(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
                sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '0')
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE), fileName)
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), "RECEIVE")
                        .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
            });
            if (formDataList.size() > 0) {
                if ("tif".equals(suffix)) {
                    //?????????tif?????? ?????????jpg
                    tifTojPGXc(file, batch, person);
                } else if ("pdf".equals(suffix)) {
                    //?????????pdf?????? ?????????jpg
                    pdfToImageFile(file, batch, fileName, person);
                }
                //??????????????????
                for (FormData formData : formDataList) {
                    formData.put(ArchiveEntity.COLUMN_SPLIT_STATE, "1");
                    formData.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "1");
                    formDataService.updateFormDataById(formData);
                }
            } else {
                batch.setBeizhu("?????????????????????????????????");
                batch.setStatus(StatusEntity.STATUS_DISABLE_STR);
                batch.setEndDate(System.currentTimeMillis());
                commonMapper.updateById(batch);
            }
        }
    }

    /**
     * ???????????? ocr??????
     *
     * @param formDefinitionId
     * @param person
     */
    @Override
    public void batchJpgToTxt(String formDefinitionId, String registerId, Person person) {
        //???????????????????????????ocr???jpg ??????
        List<FormData> formDataList = formDataService.selectFormData(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
            sqlQuery.in(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), "RECEIVE", "WSSPLIT")
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), '1')
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '1')
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_DISTINGUISH_STATE), '0')
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_DISASSEMBLY_STATE), '0')
                    .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
        if (formDataList.size() > 0) {
            for (FormData formDataTw : formDataList) {
                try {
                    //????????????
                    batchImageToText(formDataTw, registerId);
                    countDownLatch.await();
                    //??????????????????????????????
                    formDataTw.put(ArchiveEntity.COLUMN_DISTINGUISH_STATE, '1');
                    formDataService.updateFormDataById(formDataTw);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * ???????????????ofd
     */
    @Override
    public void formDataOneToOfd(FormData formData, String registerId, Person person) {
        executor.execute(() -> conversion(formData, registerId, person));
    }

    /**
     * ??????????????????OFD??????
     *
     * @param formDefinitionId
     * @param registerId
     * @param person
     */
    @Override
    public void JpgAndTxtToOfd(String formDefinitionId, String registerId, Person person) {
        //??????????????????????????????????????????
        List<FormData> formDataList = formDataService.selectFormData(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
            sqlQuery.in(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), LinkFlowPath.RECEIVE,LinkFlowPath.VOLUMES,LinkFlowPath.OVER)
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), '1')
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '1')
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_DISTINGUISH_STATE), '1')
                    .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
        if (formDataList.size() > 0) {
            for (FormData formDataOne : formDataList) {
                executor.execute(() -> conversion(formDataOne, registerId, person));
            }
        }
    }

    @Async
    public void batchImageToText(FormData formData, String registerId) {
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
                executorService.execute(() -> imageToText(s, fond_code, archive_code, registerId));
            }
        } else {
            countDownLatch = new CountDownLatch(1);
            countDownLatch.countDown();
        }
    }

    /**
     * ?????????txt??????
     *
     * @param str
     * @param fond_code
     * @param archive_code
     */
    private void imageToText(String registerId, String str, String fond_code, String archive_code) {
        File imageFile = new File(str);
        String code = "0";
        String message = "";
        String fhAge = "????????????";
        long startTime = 0;
        long endTime = 0;
        if (imageFile.exists()) {
            String fileName = imageFile.getName().split("\\.")[0];
            File pathFile = new File(String.join(File.separator, filePath, "txt", fond_code, archive_code));
            if (!pathFile.exists()) {
                pathFile.mkdirs();
            }
            //??????txt????????????
            File textFile = new File(String.join(File.separator, filePath, "txt", fond_code, archive_code), fileName + ".txt");
            try {
                String img = Base64Utils.encodeToString(FileUtils.readFileToByteArray(imageFile));
                Thread.sleep(2000);
                //?????????????????? ????????????
                startTime = System.currentTimeMillis();
                Thread.sleep(1000);
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
                        fhAge = "????????????";
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
            } finally {
                //??????????????????
                insetOcrRecord(archive_code, code, message, imageFile.getName(), imageFile.getPath(), fhAge, (endTime - startTime) + "", startTime, endTime, imageFile.length() / 1024 + "", registerId);
                countDownLatch.countDown();
            }
        }
    }

}
