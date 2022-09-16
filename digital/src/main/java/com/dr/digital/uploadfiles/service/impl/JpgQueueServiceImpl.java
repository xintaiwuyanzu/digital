package com.dr.digital.uploadfiles.service.impl;

import com.dr.archive.common.dataClient.entity.DataBatch;
import com.dr.archive.common.dataClient.entity.DataBatchInfo;
import com.dr.digital.configManager.bo.LinkFlowPath;
import com.dr.digital.configManager.entity.FlowPath;
import com.dr.digital.configManager.entity.FlowPathDetail;
import com.dr.digital.configManager.entity.FlowPathDetailInfo;
import com.dr.digital.configManager.entity.FlowPathInfo;
import com.dr.digital.configManager.service.FondDataService;
import com.dr.digital.event.ArchiveDataStatusChangeEvent;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.ocr.service.OcrQueueService;
import com.dr.digital.processing.vo.ImgVo;
import com.dr.digital.register.controller.RegisterController;
import com.dr.digital.register.entity.Register;
import com.dr.digital.register.entity.RegisterInfo;
import com.dr.digital.register.service.RegisterService;
import com.dr.digital.register.service.ThreeInOneService;
import com.dr.digital.uploadfiles.entity.JpgQueue;
import com.dr.digital.uploadfiles.entity.JpgQueueInfo;
import com.dr.digital.uploadfiles.entity.MatchText;
import com.dr.digital.uploadfiles.service.JpgQueueService;
import com.dr.digital.uploadfiles.service.UploadFilesService;
import com.dr.digital.util.FileUtil;
import com.dr.digital.util.jpgImageUtil;
import com.dr.digital.util.UUIDUtils;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.entity.StatusEntity;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.form.core.service.FormDataService;
import com.dr.framework.common.service.CommonService;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.organise.service.OrganisePersonService;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class JpgQueueServiceImpl implements JpgQueueService {
    private Logger logger = LoggerFactory.getLogger(JpgQueueServiceImpl.class);
    @Autowired
    FormDataService formDataService;
    @Autowired
    CommonService commonService;
    @Autowired
    RegisterService registerService;
    @Resource
    CommonMapper commonMapper;
    @Autowired
    UploadFilesService uploadFilesService;
    @Autowired
    OcrQueueService ocrQueueService;
    @Autowired
    OrganisePersonService organisePersonService;
    @Autowired
    ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    ThreeInOneService threeInOneService;
    @Value("${filePath}")
    private String filePath;
    //查询地址的原文
    List<String> fileList = new ArrayList<>();
    Map<String, String> fileMap = new HashMap<>();
    CountDownLatch countDownLatch = new CountDownLatch(10);
    ExecutorService executorService = Executors.newFixedThreadPool(6);



    /**
     * 执行jpg队列
     *
     * @return
     */
    @Override
    public void implementIJpg() {
        //先查询是否有需要拆分的数据
        List<JpgQueue> jpgQueues = commonMapper.selectLimitByQuery(
                SqlQuery.from(JpgQueue.class)
                        .equal(JpgQueueInfo.STATUS, '0')
                        .orderBy(JpgQueueInfo.CREATEDATE),
                0, 1
        );
        if (jpgQueues.isEmpty()) {
            return;
        }
        //查询当前文件夹下面的tif数据
        String tifPath = String.join(File.separator, filePath, "tifold");
        final File tifOldFile = new File(tifPath);
        if (!tifOldFile.exists()) {
            tifOldFile.mkdirs();
        }
        List<String> directory = isDirectory(tifOldFile, Collections.synchronizedList(new ArrayList<>()));
        if (!directory.isEmpty()) {
            for (String str : directory) {
                doJPGAndSaveStatus(str);
            }
        }
    }

    private List<String> isDirectory(File file, List<String> fileList) {
        if (file.exists()) {
            if (file.isFile()) {
                fileList.add(file.getPath());
            } else {
                File[] list = file.listFiles();
                if (list.length != 0) {
                    for (int i = 0; i < list.length; i++) {
                        isDirectory(list[i], fileList);
                    }
                }
            }
        } else {
            System.out.println("文件不存在！");
        }
        return fileList;
    }

    private void doJPGAndSaveStatus(String str) {
        File file = new File(str);
        String suffix = file.getName().split("\\.")[1].toLowerCase();
        if ("tif".equals(suffix) || "pdf".equals(suffix)) {
            String fileName = file.getName().split("\\.")[0];
            SqlQuery<JpgQueue> sqlQuery = SqlQuery.from(JpgQueue.class)
                    .equal(JpgQueueInfo.STATUS, '0')
                    .equal(JpgQueueInfo.ARCHIVECODE, fileName)
                    .orderBy(JpgQueueInfo.CREATEDATE);
            //查询队列内的文件进行拆件
            List<JpgQueue> jpgQueues = commonMapper.selectByQuery(sqlQuery);
            if (!jpgQueues.isEmpty()) {
                commonMapper.updateByQuery(sqlQuery.set(JpgQueueInfo.STATUS, '1'));
                if ("tif".equals(suffix)) {
                    //将这个tif原文 拆分成jpg
                    tifTojPGXc(file, jpgQueues.get(0));
                } else if ("pdf".equals(suffix)) {
                    //将这个pdf原文 拆分成jpg
                    pdfToImageFile(file, jpgQueues.get(0));
                }
            }
        }
    }

    /**
     * 执行 tif拆分jpg操作
     *
     * @param file
     */
    @Async
    public void tifTojPGXc(File file, JpgQueue jpgQueue) {
        ImageInputStream input;
        if (file.isFile()) {
            try {
                input = ImageIO.createImageInputStream(file);//以图片输入流形式读取到tif
                if (input != null) {
                    ImageReader reader = ImageIO.getImageReaders(input).next();
                    reader.setInput(input);
                    int numPages = reader.getNumImages(true);
                    String filePower = null;
//                    StringBuffer dpi =new StringBuffer();
                   /* if (numPages > 0) {
                        for (int i=0;i<numPages;i++){
                           if (getTiffDPI(reader, i)!=null){
                            long[] dpiArr = getTiffDPI(reader, i);
                         }
                         dpi.append(dpiArr[0]+",");
                        }
                        //删除最后一个逗号
                  dpi.deleteCharAt(dpi.length()-1);
                        filePower = reader.getWidth(1) + " x " + reader.getHeight(1);
                    }*/
                    for (int i = 0; i < numPages; i++) {
                        BufferedImage image = reader.read(i, null);//取得第i页
                        String num = FileUtil.getNewFileName((i + 1));
                        String src = String.join(File.separator, filePath, "filePath", jpgQueue.getFondCode(), jpgQueue.getArchiveCode(), num + ".jpg");
                        File imgFile = new File(src);
                        if (!imgFile.getParentFile().exists()) {
                            imgFile.getParentFile().mkdirs();
                        }
                        if (!imgFile.exists()) {
                            imgFile.createNewFile();
                        }
                        ImageIO.write(image, "JPEG", imgFile);//保存图片
                        //后加入ocr队列
                        ocrQueueService.addOcrQueue(imgFile, jpgQueue);
                    }
                    File file1 = new File(String.join(File.separator, filePath, "tifbeifen", jpgQueue.getFondCode(), jpgQueue.getArchiveCode(), file.getName()));
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
                        jpgQueue.setStatus("2");//拆件完成
                        jpgQueue.setJpgExplain("档号原文匹配成功！");
                        jpgQueue.setFileName(file.getName());
                        jpgQueue.setFilePath(file.getPath());
                        jpgQueue.setFileSize((file.length() / 1024) + "");
                        jpgQueue.setFileYs(numPages + "");
                        jpgQueue.setFilePower("");
                        jpgQueue.setFileDpi("");
                        commonMapper.updateById(jpgQueue);
                        //查询需要更新的目录
                        List<FormData> formDataList = formDataService.selectFormData(jpgQueue.getFormDefinitionId(), (sqlQuery, formRelationWrapper) -> {
                            sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE), jpgQueue.getArchiveCode())
                                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '0')
                                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), '0')
                                    .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
                        });
                        if (!formDataList.isEmpty()) {
                            for (FormData formData : formDataList) {
                                formData.put(ArchiveEntity.COLUMN_SPLIT_STATE, "1");
                                formData.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "1");
                                formDataService.updateFormDataById(formData);
                            }
                        }
                        reader.dispose();
                        input.close();
                        inputChannel.close();
                        outputChannel.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            file.delete();
        }
    }

    //获取tiff dpi
    public static long[] getTiffDPI(ImageReader reader, int index) {
        long[] dpi = new long[2];
        IIOMetadata meta = null;
        try {
            meta = reader.getImageMetadata(index);
            org.w3c.dom.Node n = meta.getAsTree("javax_imageio_1.0");
            n = n.getFirstChild();
            while (n != null) {
                if (n.getNodeName().equals("Dimension")) {
                    org.w3c.dom.Node n2 = n.getFirstChild();
                    while (n2 != null) {
                        if (n2.getNodeName().equals("HorizontalPixelSize")) {
                            org.w3c.dom.NamedNodeMap nnm = n2.getAttributes();
                            org.w3c.dom.Node n3 = nnm.item(0);
                            float hps = Float.parseFloat(n3.getNodeValue());
                            dpi[0] = Math.round(25.4f / hps);
                        }
                        if (n2.getNodeName().equals("VerticalPixelSize")) {
                            org.w3c.dom.NamedNodeMap nnm = n2.getAttributes();
                            org.w3c.dom.Node n3 = nnm.item(0);
                            float vps = Float.parseFloat(n3.getNodeValue());
                            dpi[1] = Math.round(25.4f / vps);
                        }
                        n2 = n2.getNextSibling();
                    }
                }
                n = n.getNextSibling();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dpi;
    }

    /**
     * pdf 拆分jpg
     *
     * @throws Exception
     */
    public void pdfToImageFile(File file, JpgQueue jpgQueue) {
        PDDocument doc = null;
        InputStream stream = null;
        if (file.isFile()) {
            try {
                // 加载解析PDF文件
                stream = new FileInputStream(file);
                doc = PDDocument.load(stream);
                PDFRenderer pdfRenderer = new PDFRenderer(doc);
                PDPageTree pages = doc.getPages();
                int pageCount = pages.getCount();
                for (int i = 0; i < pageCount; i++) {
                    BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 200);
                    String num = FileUtil.getNewFileName((i + 1));
                    String src = String.join(File.separator, filePath, "filePath", jpgQueue.getFondCode(), jpgQueue.getArchiveCode(), num + ".jpg");
                    File imgFile = new File(src);
                    if (!imgFile.getParentFile().exists()) {
                        imgFile.getParentFile().mkdirs();
                    }
                    if (!imgFile.exists()) {
                        imgFile.createNewFile();
                    }
                    ImageIO.write(bim, "JPEG", imgFile);//保存图片
                    //后加入ocr队列
                    ocrQueueService.addOcrQueue(imgFile, jpgQueue);
                }
                File file1 = new File(String.join(File.separator, filePath, "tifbeifen", jpgQueue.getFondCode(), jpgQueue.getArchiveCode(), file.getName()));
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
                    jpgQueue.setStatus("2");//拆件完成
                    jpgQueue.setJpgExplain("档号原文匹配成功！");
                    jpgQueue.setFileName(file.getName());
                    jpgQueue.setFilePath(file.getPath());
                    jpgQueue.setFileSize((file.length() / 1024) + "");
                    jpgQueue.setFileYs(pageCount + "");
                    commonMapper.updateById(jpgQueue);
                    //查询需要更新的目录
                    List<FormData> formDataList = formDataService.selectFormData(jpgQueue.getFormDefinitionId(), (sqlQuery, formRelationWrapper) -> {
                        sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE), jpgQueue.getArchiveCode())
                                .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '0')
                                .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), '0')
                                .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
                    });
                    if (!formDataList.isEmpty()) {
                        for (FormData formData : formDataList) {
                            formData.put(ArchiveEntity.COLUMN_SPLIT_STATE, "1");
                            formData.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "1");
                            formDataService.updateFormDataById(formData);
                        }
                    }
                    inputChannel.close();
                    outputChannel.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    doc.close();
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            file.delete();
        }
    }

    /**
     * 旧添加jpg 队列
     *
     * @param formDefinitionId
     * @param registerId
     * @param person
     */
    @Override
    public void addJpgQueue(String formDefinitionId, String registerId, String batchName, String batchNo, Person person) {
        //1、查询需要添加jpg队列内的数据
        List<FormData> formDataList = formDataService.selectFormData(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
            sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '0')
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), '0')
                    .orderBy(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
        //2、将数据添加到队列内
        if (formDataList.size() > 0) {
            for (FormData formData : formDataList) {
                addJpgQueueLb(formDefinitionId, formData, registerId, batchName, batchNo, person);
            }
        }
        //添加完jpg任务后，马上唤醒拆分队列
        implementIJpg();
    }

    /**
     * 新添加jpg 队列
     *
     * @param formDefinitionId
     * @param registerId
     * @param person
     */
    public void addJpgQueue(String formDefinitionId, String registerId, String batchName, String batchNo, Person person, FormData formData) {
        addJpgQueueLb(formDefinitionId, formData, registerId, batchName, batchNo, person);
        //添加完jpg任务后，马上唤醒拆分队列
        implementIJpg();
    }

    public JpgQueue addJpgQueueLb(String formDefinitionId, FormData formData, String registerId, String batchName, String batchNo, Person person) {
        JpgQueue jpgQueue = new JpgQueue();
        jpgQueue.setSystem_code("INSPUR-DZZW-MACHINING");
        jpgQueue.setFormDefinitionId(formDefinitionId);
        jpgQueue.setBatchID(registerId);
        jpgQueue.setBatchName(batchName);
        jpgQueue.setBatchNo(batchNo);
        jpgQueue.setFondCode(formData.get(ArchiveEntity.COLUMN_FOND_CODE));
        jpgQueue.setArchiveCode(formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        jpgQueue.setStatus("0"); //等待拆件
        jpgQueue.setCreatePerson(person.getId());
        jpgQueue.setPersonName(person.getUserName());
        jpgQueue.setCreateDate(System.currentTimeMillis());
        jpgQueue.setId(UUID.randomUUID().toString());
        commonMapper.insert(jpgQueue);
        return jpgQueue;
    }


    @Override
    public void tiffToJpgByPath(String formDefinitionId, String registerId, String type, Person person) {

    }

    @Override
    public List<JpgQueue> getArchiveCodeByJpg(String archiveCode) {
        SqlQuery<JpgQueue> sqlQuery = SqlQuery.from(JpgQueue.class).equal(JpgQueueInfo.ARCHIVECODE, archiveCode)
                .equal(JpgQueueInfo.STATUS, 2);
        List<JpgQueue> jpgQueues = commonService.selectList(sqlQuery);
        return jpgQueues;
    }

    //存放第一次请求的档号
    List<String> jpgQueueList = new ArrayList<>();

    //int i = 5;
    @Override
    public ResultEntity getMatchText(MatchText matchText, Person person) {
        //这里查出该批次下的未拆分的目录数据
        List<FormData> formDataList = formDataService.selectFormData(matchText.getFormDefinitionId(), (sqlQuery, formRelationWrapper) -> {
            sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), "RECEIVE")
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '0')
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), '0')
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.DATA_CLEANING),"0")
                    .orderBy(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
        //更新数据清洗
        formDataService.updateFormDataBySqlBuilder(matchText.getFormDefinitionId(),(sqlQuery, formRelationWrapper) -> {
                    sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), "RECEIVE")
                            .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '0')
                            .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), '0')
                            .equal(formRelationWrapper.getColumn(ArchiveEntity.DATA_CLEANING),"0")
                            .set(formRelationWrapper.getColumn(ArchiveEntity.DATA_CLEANING),"1");});

        if (formDataList.size() > 0) {
            if ("CLIENT".equals(matchText.getFileLocations())) {
                DataBatch dataBatch = commonMapper.selectOneByQuery(SqlQuery.from(DataBatch.class).equal(DataBatchInfo.ID, matchText.getClientBatchId()));
                String filePath = dataBatch.getClientPath();
                return splitJpg(matchText, person, formDataList, filePath);
            } else if ("SERVER".equals(matchText.getFileLocations())) {
                String filePath = matchText.getFilePath();
                return splitJpg(matchText, person, formDataList, filePath);
            } else {
                //失败恢复数据
                formDataService.updateFormDataBySqlBuilder(matchText.getFormDefinitionId(),(sqlQuery, formRelationWrapper) -> {
                    sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), "RECEIVE")
                            .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '0')
                            .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), '0')
                            .equal(formRelationWrapper.getColumn(ArchiveEntity.DATA_CLEANING),"1")
                            .set(formRelationWrapper.getColumn(ArchiveEntity.DATA_CLEANING),"0");});
                return ResultEntity.success("请选择处理方式");
            }
        } else {
            return ResultEntity.success("暂无需要处理的档案");
        }
    }

    public ResultEntity splitJpg(MatchText matchText, Person person, List<FormData> formDataList, String filePach) {
        //查询地址的原文
        Map<String, String> map = SplitJpg(matchText, new File(filePach), person);
        if (!map.isEmpty()) {
            //查出该批次的自动化环节
            FlowPathDetail flowPathDetail = commonMapper.selectOneByQuery(SqlQuery.from(FlowPathDetail.class)
                    .equal(FlowPathDetailInfo.FORMDEFINITIONID, matchText.getFormDefinitionId()));
            //批次内环节名
            String flowBatchName = flowPathDetail.getFlowBatchName();
            for (FormData formData : formDataList) {
                executorService.execute(() ->
                        fileSplit(map, formData, person, matchText,flowBatchName,flowBatchName)
                );
            }
            return ResultEntity.success("正在拼命处理中，请刷新查看结果");
        } else {
            //未查询出原文修改字段。
            formDataService.updateFormDataBySqlBuilder(matchText.getFormDefinitionId(),(sqlQuery, formRelationWrapper) -> {
                sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), "RECEIVE")
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '0')
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), '0')
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.DATA_CLEANING),"1")
                        .set(formRelationWrapper.getColumn(ArchiveEntity.DATA_CLEANING),"0");});
            return ResultEntity.error("未查询到档案原文，请检查文件路径");
        }
    }


    //返回档号
    public Map<String, String> SplitJpg(MatchText matchText, File tifOldFile, Person person) {

        //获取文件夹下的档案原件名称
        fileMap = new HashMap<>();
        //判断地址是否为空
        if (tifOldFile.exists()) {
            //获取地址下文件名称kye,文件地址value
            return isDirectory(tifOldFile);
        } else {
            return fileMap;
        }
    }


    public Map<String, String> isDirectory(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                logger.info("测试。。。。。。。。。。。。。。。。。。{}",file.getName());
                String suffix = file.getName().substring(file.getName().lastIndexOf(".")).toLowerCase();
                String key = file.getName().substring(0, file.getName().lastIndexOf(".")) + suffix;
                String value = file.getPath();
                fileMap.put(key, value);
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
        return fileMap;
    }

    /**
     * 原文拆jpg
     * @param map
     * @param formData
     * @param person
     * @param matchText
     * @param ocrOrChaiJian 判断是否有ocr和chaijian
     */
    public void fileSplit(Map<String, String> map, FormData formData, Person person, MatchText matchText,String ocrOrChaiJian,String flowPathName) {
        //获取文件的类型
        String file = map.get(formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + ".tif");
        String fileJpg = map.get(formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + ".pdf");
        //tif
        if (file != null && !file.isEmpty()) {
            Register register = commonMapper.selectOneByQuery(SqlQuery.from(Register.class)
                    .equal(RegisterInfo.FORMDEFINITIONID, matchText.getFormDefinitionId()));
            // addJpgQueue(matchText.getFormDefinitionId(),register.getId(),register.getBatch_name(),register.getBatch_no(),person,formData);
            //加入队列
            JpgQueue jpgQueue = addJpgQueueLb(matchText.getFormDefinitionId(), formData, register.getId(), register.getBatch_name(), register.getBatch_no(), person);
            tifTojPGXc(file, matchText, register, person, formData, jpgQueue,ocrOrChaiJian,flowPathName);

        }
        //pdf
        else if (fileJpg != null && !fileJpg.isEmpty()) {
            Register register = commonMapper.selectOneByQuery(SqlQuery.from(Register.class)
                    .equal(RegisterInfo.FORMDEFINITIONID, matchText.getFormDefinitionId()));
            //加入队列
            //addJpgQueue(matchText.getFormDefinitionId(),register.getId(),register.getBatch_name(),register.getBatch_no(),person,formData);
            JpgQueue queue = addJpgQueueLb(matchText.getFormDefinitionId(), formData, register.getId(), register.getBatch_name(), register.getBatch_no(), person);
            pdfToImage(fileJpg, formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE), matchText, register, person, formData, queue,ocrOrChaiJian,flowPathName);
            //拆分状态更新
            formData.put(ArchiveEntity.COLUMN_SPLIT_STATE, "1");
            formData.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "1");
            formDataService.updateFormDataById(formData);
        }else {
            //没找到原文，要打开数据清洗的权限
            formDataService.updateFormDataBySqlBuilder(matchText.getFormDefinitionId(),(sqlQuery, formRelationWrapper) -> {
                sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), "RECEIVE")
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '0')
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), '0')
                        .equal(formRelationWrapper.getColumn(ArchiveEntity.DATA_CLEANING),"1")
                        .set(formRelationWrapper.getColumn(ArchiveEntity.DATA_CLEANING),"0");});
        }
    }

    /**
     * 执行 tif拆分jpg操作
     *
     * @param str
     * @param matchText
     * @param register
     * @param person
     * @param formData
     * @param queue
     */
    @Async
    public void  tifTojPGXc(String str, MatchText matchText, Register register, Person person, FormData formData, JpgQueue queue,String ocrOrChaiJian,String flowPathName) {
        File file = new File(str);
        ImgVo imgVo = new ImgVo();
        imgVo.setFilePath(file.getAbsolutePath());
        String name = file.getName().substring(0, file.getName().lastIndexOf("."));
        imgVo.setFileName(name);
        ImageInputStream input;
        try {
            if (file.isFile()) {
                input = ImageIO.createImageInputStream(file);//以图片输入流形式读取到tif
                if (input != null) {
                    ImageReader reader = ImageIO.getImageReaders(input).next();
                    reader.setInput(input);
                    int numPages = reader.getNumImages(true);
                    String path = String.join(File.separator, imgVo.getFileName().substring(0, imgVo.getFileName().indexOf("-")), imgVo.getFileName());
                    String filePower = null;
                    String dpi = null;
                    StringBuffer fileRGB = new StringBuffer();
                    if (numPages > 0) {
//                        if (getTiffDPI(reader,1)!=null){
//                            long[] dpiArr = getTiffDPI(reader,1);
//                            dpi = dpiArr[0]+"";
//                        }
                    }
                    queue.setStatus("1");
                    queue.setFileYs(numPages + "");
                    commonMapper.updateIgnoreNullById(queue);
                    for (int i = 0; i < numPages; i++) {
                        BufferedImage image = reader.read(i, null);//取得第i页
                        String num = getNewFileName((i + 1));
                        String s1 = String.join(File.separator, filePath, "filePath", path, num + ".jpg");
                        File imgFile = new File(s1);
                        if (!imgFile.getParentFile().exists()) {
                            imgFile.getParentFile().mkdirs();
                        }
                        if (!imgFile.exists()) {
                            imgFile.createNewFile();
                        }
                        ImageIO.write(image, "JPEG", imgFile);//保存图片
//                        String rgb = jpgImageUtil.ImageRGB(s1);
//                        fileRGB.append(rgb+",");
                        String resolution = jpgImageUtil.resolution(s1);
                        JpgQueue jpgQueue = new JpgQueue();
//                        jpgQueue.setFileRGB(rgb);
                        jpgQueue.setFilePower(resolution);
                        jpgQueue.setBatchID(register.getId());
                        jpgQueue.setBatchNo(register.getBatch_no());
                        jpgQueue.setBatchName(register.getBatch_name());
                        jpgQueue.setFormDefinitionId(matchText.getFormDefinitionId());
                        jpgQueue.setFormDateId(formData.getId());
                        jpgQueue.setFondCode(formData.get(ArchiveEntity.COLUMN_FOND_CODE));
                        jpgQueue.setArchiveCode(formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE));
                        jpgQueue.setCreatePerson(person.getId());
                        jpgQueue.setPersonName(person.getUserName());
                        //判断是否有选择ocr环节，没有ocr环节的不做ocr和拆件
                        if (ocrOrChaiJian.indexOf(LinkFlowPath.OCR)!=-1){
                            //加入ocr队列并唤醒ocr线，判断是否有拆件，没有的话ocr结束就提交到人工环节
                            ocrQueueService.addOcrQueue(imgFile, jpgQueue);
                        }else {
                            //识别状态改为1，表示不ocr了，jpg拆tif之后就提交到人工环节
                            formData.put(ArchiveEntity.COLUMN_DISTINGUISH_STATE,1);
                        }
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
                        queue.setUpdateDate(UUIDUtils.currentTimeMillis());
                        queue.setStatus("2");//拆件完成
                        queue.setJpgExplain("档号原文匹配成功！");
                        queue.setFileName(file.getName());
                        queue.setFilePath(file.getPath());
                        queue.setFileSize((file.length() / 1024) + "");
                        queue.setFileYs(numPages + "");
                        queue.setFilePower("");
                        queue.setFileDpi("");
                        queue.setFormDateId(formData.getId());
//                        fileRGB.deleteCharAt(fileRGB.length()-1);
//                        queue.setFileRGB(fileRGB.toString());
                        queue.setFileType("tif");
                        commonMapper.updateById(queue);
                        //拆分状态更新
                        formData.put(ArchiveEntity.COLUMN_SPLIT_STATE, "1");
                        formData.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "1");
                        formDataService.updateFormDataIgnoreNullById(formData);
                        //没有选择ocr环节，提交到人工,增加操作记录
                        if (ocrOrChaiJian.indexOf(LinkFlowPath.OCR)==-1){
                            String link = registerService.manualLink(flowPathName);
                            BaseQuery query = new BaseQuery();
                            query.setFondId(register.getId());
                            query.setFormDefinitionId(register.getFormDefinitionId());
                            if (!formData.get(ArchiveEntity.COLUMN_STATUS).equals(link)){
                                registerService.updateDataDetail(person,formData,link,"",query,"");
                            }
                        }
                        reader.dispose();
                        input.close();
                        inputChannel.close();
                        outputChannel.close();

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //根据需求是否删除原文
        if (matchText.isDeleteFile()) {
            file.delete();
        }

    }


    /**
     * pdf 拆分jpg
     *
     * @throws Exception
     */
    public void pdfToImage(String str, String fileName, MatchText matchText, Register register, Person person, FormData formData, JpgQueue queue,String ocrOrChaiJian,String flowPathName) {
        File file = new File(str);
        PDDocument doc = null;
        InputStream stream = null;
        try {
            // pdf路径
            stream = new FileInputStream(file);
            // 加载解析PDF文件
            doc = PDDocument.load(stream);
            PDFRenderer pdfRenderer = new PDFRenderer(doc);
            PDPageTree pages = doc.getPages();
            int pageCount = pages.getCount();
            StringBuffer fileRGB = new StringBuffer();
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
                    // 不存在则创建父目录及子文件
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                ImageIO.write(bim, "JPEG", imgFile);//保存图片
//                String rgb = jpgImageUtil.ImageRGB(s1);
//                fileRGB.append(rgb+",");
                String resolution = jpgImageUtil.resolution(s1);
                //dpi目前获取到的数据是-1，图片被修改或者被压缩过都是-1，另存为可以正常显示，或者编辑图片后另存为即可。
                //int dpi = ImageUItl.getDpi(imgFile);
                JpgQueue jpgQueue = new JpgQueue();
//                jpgQueue.setFileRGB(rgb);
                jpgQueue.setFilePower(resolution);
                jpgQueue.setBatchID(register.getId());
                jpgQueue.setBatchNo(register.getBatch_no());
                jpgQueue.setBatchName(register.getBatch_name());
                jpgQueue.setFormDateId(formData.getId());
                jpgQueue.setFormDefinitionId(matchText.getFormDefinitionId());
                jpgQueue.setFondCode(formData.get(ArchiveEntity.COLUMN_FOND_CODE));
                jpgQueue.setArchiveCode(formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE));
                jpgQueue.setCreatePerson(person.getId());
                jpgQueue.setPersonName(person.getUserName());
                //判断是否有选择ocr环节，没有ocr环节的不做ocr和拆件
                if (ocrOrChaiJian.indexOf(LinkFlowPath.OCR)!=-1){
                    //加入ocr队列并唤醒ocr线，判断是否有拆件，没有的话ocr结束就提交到人工环节
                    ocrQueueService.addOcrQueue(imgFile, jpgQueue);
                }else {
                    //识别状态改为1，表示不ocr了，jpg拆tif之后就提交到人工环节
                    formData.put(ArchiveEntity.COLUMN_DISTINGUISH_STATE,1);
                }
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
                queue.setStatus("2");//拆件完成
                queue.setUpdateDate(UUIDUtils.currentTimeMillis());
                queue.setJpgExplain("档号原文匹配成功！");
                queue.setFileName(file.getName());
                queue.setFilePath(file.getPath());
                queue.setFileSize((file.length() / 1024) + "");
                queue.setFileYs(pageCount + "");
                queue.setFileType("pdf");
//                fileRGB.deleteCharAt(fileRGB.length()-1);
//                queue.setFileRGB(rgb);
                commonMapper.updateById(queue);
                //拆分状态更新
                formData.put(ArchiveEntity.COLUMN_SPLIT_STATE, "1");
                formData.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "1");
                formDataService.updateFormDataIgnoreNullById(formData);
                //没有选择ocr环节，提交到人工,增加操作记录
                if (ocrOrChaiJian.indexOf(LinkFlowPath.OCR)==-1){
                    String link = registerService.manualLink(flowPathName);
                    BaseQuery query = new BaseQuery();
                    query.setFondId(register.getId());
                    query.setFormDefinitionId(register.getFormDefinitionId());
                    registerService.updateDataDetail(person,formData,link,"",query,"");
                }
                inputChannel.close();
                outputChannel.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                doc.close();
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (matchText.isDeleteFile()) {
            file.delete();
        }

    }


    /**
     * 更换原文名称的位数
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

    @Override
    public ResultEntity selectFunction() {
        //先查询出指定的数据
        List<JpgQueue> jpgQueues = commonMapper.selectByQuery(
                SqlQuery.from(JpgQueue.class)
                        .equal(JpgQueueInfo.STATUS, '1')
                        .groupBy(JpgQueueInfo.ARCHIVECODE)
                        .orderBy(JpgQueueInfo.CREATEDATE)
        );
        return ResultEntity.success(jpgQueues);
    }

    @Override
    public void test() {
        implementIJpg();
    }

//    /**
//     * 因为没有元数据的目录导入检验，先做最基础的非空判断
//     *
//     * @param f 批次详情表里面的具体数据
//     * @return
//     */
//    public boolean isNullJudge(FormData f) {
//        StringBuffer sb = new StringBuffer();
//        if (StringUtils.isEmpty(f.get("archival_code"))){
//           sb.append("档案检测未通过");
//        }
//        if (StringUtils.isEmpty(f.get("archival_code")) || StringUtils.isEmpty(f.get("fonds_identifier")) || StringUtils.isEmpty(f.get("archivers_year"))
//                || StringUtils.isEmpty(f.get("total_number_of_pages"))) {
//            return false;
//        }
//        formDataService.updateFormDataById(f);
//        return true;
//    }

//    /**
//     * 把jpg队列的数据和档案目录数据做对比，判断是否相等
//     *
//     * @param archivalCode 当前档号
//     * @param formData     当前环节档号
//     * @return
//     */
//    public boolean isEquals(String archivalCode, List<FormData> formData) {
//        List<JpgQueue> archiveCodeByJpg = this.getArchiveCodeByJpg(archivalCode);
//        //没找到队列
//        if (archiveCodeByJpg.size() < 1) {
//            return false;
//        } else {
//            for (FormData formDatum : formData) {
//                //找到当前环节目录档号对应的那条数据
//                if (formDatum.get("archival_code").equals(archivalCode)) {
//                    //当前档号对象和jpg队列对象做对比
//                    //对比全宗号是否相等
//                    if (!formDatum.get("fonds_identifier").equals(archiveCodeByJpg.get(0).getFondCode())) {
//                        return false;
//                        //对比页数是否相等
//                    } else if (!formDatum.get("total_number_of_pages").equals(archiveCodeByJpg.get(0).getFileYs())) {
//                        return false;
//                    }
//
//                    return true;
//                }
//                ;
//            }
//        }
//        return true;
//    }

}
