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
    //全局正在识别的档号
    List<String> ocrCodes = Collections.synchronizedList(new ArrayList<>());
    //线程池
    ThreadPoolExecutor executorService;

    /**
     * 暂停ocr服务
     */
    @Override
    @Async
    public void updateStatus() {
        //先查询出指定的数据
        List<OcrQueue> ocrQueues = commonMapper.selectByQuery(
                SqlQuery.from(OcrQueue.class)
                        .equal(OcrQueueInfo.STATUS, '0')
                        .notIn(OcrQueueInfo.ARCHIVECODE, ocrCodes)
                        .orderBy(OcrQueueInfo.CREATEDATE)
        );
        if (ocrQueues.size() > 0) {
            for (OcrQueue ocrQueue : ocrQueues) {
                ocrQueue.setStatus("4");//暂停
                commonMapper.updateIgnoreNullById(ocrQueue);
            }
        }
    }

    /**
     * 选择启动
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
        //再次尝试唤醒ocr任务队列
        implementOcr();
    }

    /**
     * 查询转换服务
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
                ocrQueue.setStatus("0");//等待
                commonMapper.updateIgnoreNullById(ocrQueue);
            }
        }
        //再次尝试唤醒ocr任务队列
        implementOcr();
    }

    /**
     * 暂停重启服务
     */
    @Override
    @Async
    public void allOcrStart() {
        //先查询出指定的数据
        List<OcrQueue> ocrQueues = commonMapper.selectByQuery(
                SqlQuery.from(OcrQueue.class)
                        .equal(OcrQueueInfo.STATUS, '4')
                        .orderBy(OcrQueueInfo.CREATEDATE)
        );
        if (ocrQueues.size() > 0) {
            for (OcrQueue ocrQueue : ocrQueues) {
                ocrQueue.setStatus("0");//等待
                commonMapper.updateIgnoreNullById(ocrQueue);
            }
        }
        //再次尝试唤醒ocr任务队列
        implementOcr();
    }


    /**
     * 执行ocr 队列
     */
    @Async
    @Override
    public void implementOcr() {
        //判断线程池是否启动
        synchronized (executorService) {
            while (executorService.getActiveCount() < THREAD_POOL_SIZE) {
                executorService.execute(this::selectAndOcr);
            }
        }
    }

    @Transactional
    protected void selectAndOcr() {
        //矫正数据
        correct();
        List<OcrQueue> ocrQueueFiles;
        OcrQueue ocrQueueOne;
        synchronized (queryLock) {
            //先查询出指定的数据
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
            //根据第一条数据，查询目录下的数据
            ocrQueueOne = ocrQueues.get(0);
            SqlQuery<OcrQueue> sqlQuery = SqlQuery.from(OcrQueue.class)
                    .equal(OcrQueueInfo.FONDCODE, ocrQueueOne.getFondCode())
                    .equal(OcrQueueInfo.STATUS, '0')
                    .equal(OcrQueueInfo.ARCHIVECODE, ocrQueueOne.getArchiveCode())
                    .equal(OcrQueueInfo.BATCHID, ocrQueueOne.getBatchID());
            ocrQueueFiles = commonMapper.selectByQuery(sqlQuery);
            commonMapper.updateByQuery(sqlQuery.set(OcrQueueInfo.STATUS, '1'));
            //所有的都变为1的时候仔添加到list
            ocrCodes.add(ocrQueueOne.getArchiveCode());
        }
        boolean success = true;
        //更新这些数据为正在执行
        for (OcrQueue ocrQueue : ocrQueueFiles) {
            //逐条执行ocr任务
            success = success & doOcrAndSaveStatus(ocrQueue);
            //更新ocr结果和关联业务状态
        }
        if (success) {
            //如果成功，更新目录状态
            if (ocrQueueOne.getFormDateId() != null && ocrQueueOne.getFormDefinitionId() != null) {
                FormData formData = formDataService.selectOneFormData(ocrQueueOne.getFormDefinitionId(), ocrQueueOne.getFormDateId());
                formData.put(ArchiveEntity.COLUMN_DISTINGUISH_STATE, '1');
                formDataService.updateFormDataIgnoreNullById(formData);

                //判断ocr队列的批次是否有拆件环节，如果没有，在ocr之后就提交到人工环节
                FlowPathDetail flowPathDetails = commonMapper.selectOneByQuery(SqlQuery.from(FlowPathDetail.class)
                        .equal(FlowPathDetailInfo.FORMDEFINITIONID, ocrQueueOne.getFormDefinitionId()));
                //判断第一个人工环节
                String link = registerService.manualLink(flowPathDetails.getFlowBatchName());
                //当前ocr条数
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
//                ocr完成总页数和页数相同说明全部ocr完，可以提交了
                if (ocrYs.equals(jpgQueue.getFileYs())){
                    //判断是否有拆件，有的话ocr结束后拆件，并提交到人工，没有的话就直接提交了
                    if (!flowPathDetails.getFlowBatchName().contains(LinkFlowPath.CHAIJIAN)){
                        BaseQuery query = new BaseQuery();
                        query.setFondId(ocrQueueOne.getBatchID());
                        query.setFormDefinitionId(ocrQueueOne.getFormDefinitionId());
                        Person person = new Person();
                        person.setUserName("超级管理员");
                        person.setUserCode("admin");
                        //没有自动拆件，但是有手动拆件，自动生成包结构
                        if (flowPathDetails.getFlowBatchName().contains(LinkFlowPath.WSSPLIT)){
                            // ocr后初始化手动拆件文件夹 先注释掉，首先要文件结构要从智能归档查，可以改完，默认ocr结束后，就根据智能归档生成了文件夹，自动拆件只是分区域
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
        //再次尝试唤醒ocr任务队列
        implementOcr();
    }

    /**
     * 单条执行ocr识别
     *
     * @param ocrQueue
     * @return
     */
    private boolean doOcrAndSaveStatus(OcrQueue ocrQueue) {
        //带有重试机制执行ocr识别
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
                        //重试第二次还是失败，判断为ocr崩溃，等待5分钟
                        Thread.sleep(5 * 60 * 1000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
                count++;
            }
        }
        if (success) {
            //更新列表数据
            ocrQueue.setStatus("2");//识别成功


            //更新时间
            ocrQueue.setUpdateDate(UUIDUtils.currentTimeMillis());
            commonMapper.updateIgnoreNullById(ocrQueue);
        } else {
            //如果失败，更新状态为失败
            ocrQueue.setStatus("3");//识别失败
            commonMapper.updateIgnoreNullById(ocrQueue);
        }
        return success;
    }

    /**
     * 调用ocr接口执行ocr识别
     *
     * @param ocrQueue
     * @return 成功返回true
     */
    private boolean doOcr(OcrQueue ocrQueue) {
        String registerId = ocrQueue.getBatchID();
        //获取转换文件jpg的地址
        String fileDz = ocrQueue.getFilePath();
        String fondCode = ocrQueue.getFondCode();
        String archiveCode = ocrQueue.getArchiveCode();
        if (!StringUtils.isEmpty(fileDz)) {
            String code = "";
            String message = "";
            String fhAge = "表格识别";
            long startTime = 0;
            long endTime = 0;
            //获取图片
            File imageFile = new File(fileDz);
            //获取转换后txt文件存放地址
            File pathFile = new File(String.join(File.separator, filePath, "txt", fondCode, archiveCode));
            if (!pathFile.exists()) {
                pathFile.mkdirs();
            }
            File textFile = new File(String.join(File.separator, filePath, "txt", fondCode, archiveCode), ocrQueue.getFileName() + ".txt");
            logger.info("生成txt文件：{}", textFile.getAbsolutePath());
            try {
                String img = Base64Utils.encodeToString(FileUtils.readFileToByteArray(imageFile));
                //更换最新使用 表格识别如果不成功 则更换 通用文字识别
                startTime = System.currentTimeMillis();
                Thread.sleep(1000);
//                logger.info("img========：{}",img);
                TableResultEntity tableResultEntity = ocrService.shiBieOcrTable(img);
                endTime = System.currentTimeMillis();
                logger.info("tableResultEntity========：{}", tableResultEntity);
                if ("0".equals(tableResultEntity.getCode())) {
                    if (!textFile.getParentFile().exists()) {
                        textFile.getParentFile().mkdirs();
                    }
//                    logger.info("准备写txt文件：{}",textFile.getAbsolutePath());
                    try (OutputStream stream = new FileOutputStream(textFile)) {
                        logger.info("tableResultEntity.getData()============：{}", tableResultEntity.getData());
                        objectMapper.writeValue(stream, tableResultEntity.getData());
                        code = tableResultEntity.getCode();
                        message = tableResultEntity.getMessage();
                    } catch (Exception e) {
                        logger.warn("写入txt文件失败", e);
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
                            fhAge = "文字识别";
                        } catch (Exception e) {
                            logger.warn("写入txt文件失败", e);
                        }
                    } else {//如果再不成功增加识别错误信息、并将原文存在错误列表内、复制失败的原文
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
                //添加转换记录
                uploadFilesService.insetOcrRecord(archiveCode, code, message, imageFile.getName(), imageFile.getPath(), fhAge, (endTime - startTime) + "", startTime, endTime, imageFile.length() / 1024 + "", registerId);
                operationLog(ocrQueue, startTime, endTime, imageFile.getName());
            }
            return true;
        }
        return false;
    }

    /**
     * 添加到 ocr队列
     *
     * @param formDefinitionId
     * @param registerId
     * @param person
     */
    public void ocrQueueLb(String formDefinitionId, String registerId, String batchName, String batchNo, Person person) {
        //1、查询需要添加ocr队列内的数据
        List<FormData> formDataList = formDataService.selectFormData(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
            sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), '1')
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '1')
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_DISTINGUISH_STATE), '0')
                    .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
        //2、将数据添加到队列内
        if (formDataList.size() > 0) {
            for (FormData formData : formDataList) {
                addOcrQueueLb(formDefinitionId, formData, registerId, batchName, batchNo, person);
            }
        }
    }

    /**
     * 根据目录信息创建队列数据
     *
     * @param formData
     * @param registerId
     */
    public void addOcrQueueLb(String formDefinitionId, FormData formData, String registerId, String batchName, String batchNo, Person person) {
        String fond_code = formData.get(ArchiveEntity.COLUMN_FOND_CODE);//全宗号
        String archive_code = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);//档号
        if (!StringUtils.isEmpty(fond_code) && !StringUtils.isEmpty(archive_code)) {
            //获取目录数据的jpg原文信息
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
        //添加完ocr任务后，马上唤醒识别队列
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
            ocrQueue.setStatus("0"); //未识别
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
            ocrQueue.setStatus("0"); //未识别
            ocrQueue.setCreatePerson(jpgQueue.getCreatePerson());
            ocrQueue.setPersonName(jpgQueue.getPersonName());
            ocrQueue.setCreateDate(System.currentTimeMillis());
            ocrQueue.setId(UUID.randomUUID().toString());
            commonMapper.insert(ocrQueue);
            //唤醒ocr队列
            implementOcr();
            return ocrQueue;
        }
        return null;
    }

    /**
     * 查询文件夹下面的图片
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
                // 只检测图片
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
            System.out.println("文件不存在！");
        }
        return fileList3;
    }

    //创建线程池
    @Override
    public void afterPropertiesSet() {
        ThreadFactory threadFactory = new CustomizableThreadFactory("ocr识别线程");
        executorService = new ThreadPoolExecutor(1, THREAD_POOL_SIZE, 0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(THREAD_POOL_SIZE), threadFactory);
        implementOcr();
    }

    //销毁线程池
    @Override
    public void destroy() {
        executorService.shutdown();
    }

    /**
     * 矫正数据
     */
    public void correct() {
        //先查询出指定的数据
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
                    //启动
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
        //更新任务状态信息
        task.setStatus(TaskInfoList.TaskStatus.FINISHED);
        task.setEndTime(System.currentTimeMillis());
        task.setUrl("finished");
        taskInfoList.update(task);
    }

    @Override
    public ResultEntity selectFunction() {
        //先查询出指定的数据
        List<OcrQueue> ocrQueues = commonMapper.selectByQuery(
                SqlQuery.from(OcrQueue.class, true)
                        .equal(OcrQueueInfo.STATUS, '1')
                        .groupBy(OcrQueueInfo.ARCHIVECODE)
                        .orderBy(OcrQueueInfo.CREATEDATE)
        );
        return ResultEntity.success(ocrQueues);
    }

    /**
     * 初始化手动拆件文件夹结构
     *
     * @param formData 批次信息
     */
    public void insetFolder(FormData formData, String registerId) {
        //先查询是否存在
        List<FileStructure> fileStructures = commonMapper.selectByQuery(SqlQuery.from(FileStructure.class)
                .equal(FileStructureInfo.REGISTERID, registerId)
                .equal(FileStructureInfo.DEFAULT_STATE, 0)
                .equal(FileStructureInfo.ARCHIVESID, formData.get(ArchiveEntity.ID_COLUMN_NAME).toString()));
        if (fileStructures.size() == 0) {
            formData.put(ArchiveEntity.COLUMN_DISASSEMBLY_STATE, '1');
            formDataService.updateFormDataById(formData);
            List<TypeFile> typeFile = commonMapper.selectByQuery(SqlQuery.from(TypeFile.class)
                    .equal(TypeFileInfo.REGISTERID, registerId));
            //判断是否智能归档是否配置
            if (typeFile.size() > 0) {
                for (TypeFile type : typeFile) {
                    String key = type.getYhCode();
                    String fileType = type.getFileName();
                    FileStructure data = new FileStructure();
                    String fond_code = formData.get(ArchiveEntity.COLUMN_FOND_CODE);//全宗号
                    String archive_code = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);//档号
                    data.setArchives_item_number(key);
                    data.setFonds_identifier(fond_code);
                    data.setArchivers_category_code(formData.get(ArchiveEntity.COLUMN_CATEGORY_CODE));
                    data.setAj_archival_code(archive_code);
                    data.setArchival_code(archive_code + "-" + key);
                    data.setRegisterId(registerId);
                    data.setFile_type(fileType);
                    data.setArchivesID(formData.get(ArchiveEntity.ID_COLUMN_NAME));
                    data.setStatus("RECEIVE");
                    //自动生成文件夹识别字段
                    data.setDefault_state("0");
                    data.setId(UUIDUtils.getUUID());
                    commonMapper.insert(data);
                }
            }else {
                //默认智能归档文件夹位置
                String[] type = {"001", "002", "003", "004", "005"};
                String fileType="";
                for (String key:type){
                    switch (key) {
                        case "001":
                            fileType = "封面";
                            break;
                        case "002":
                            fileType = "正文";
                            break;
                        case "003":
                            fileType = "附件";
                            break;
                        case "004":
                            fileType = "办理单";
                            break;
                        case "005":
                            fileType = "底稿";
                            break;
                    }
                    FileStructure data = new FileStructure();
                    String fond_code = formData.get(ArchiveEntity.COLUMN_FOND_CODE);//全宗号
                    String archive_code = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);//档号
                    data.setArchives_item_number(key);
                    data.setFonds_identifier(fond_code);
                    data.setArchivers_category_code(formData.get(ArchiveEntity.COLUMN_CATEGORY_CODE));
                    data.setAj_archival_code(archive_code);
                    data.setArchival_code(archive_code + "-" + key);
                    data.setRegisterId(registerId);
                    data.setFile_type(fileType);
                    data.setStatus("RECEIVE");
                    data.setArchivesID(formData.get(ArchiveEntity.ID_COLUMN_NAME));
                    //自动生成文件夹识别字段
                    data.setDefault_state("0");
                    data.setId(UUIDUtils.getUUID());
                    commonMapper.insert(data);
                }

            }

        }
    }

    /**
     * 整合ocr批次记录信息。
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
