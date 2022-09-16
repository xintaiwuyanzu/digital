package com.dr.digital.packet.service.impl;

import com.dr.digital.configManager.bo.MataData;
import com.dr.digital.configManager.bo.MataDataInfo;
import com.dr.digital.configManager.bo.Metadata;
import com.dr.digital.configManager.bo.MetadataInfo;
import com.dr.digital.configManager.service.ConfigManagerClient;
import com.dr.digital.manage.form.service.ArchiveDataManager;
import com.dr.digital.manage.form.service.ArchiveFormDefinitionService;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.model.query.ArchiveDataQuery;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.packet.entity.*;
import com.dr.digital.packet.service.PacketsClient;
import com.dr.digital.packet.service.PacketsDataService;
import com.dr.digital.register.entity.Register;
import com.dr.digital.register.service.RegisterService;
import com.dr.digital.util.FileUtil;
import com.dr.digital.util.PacketDataParserUtil;
import com.dr.digital.util.UUIDUtils;
import com.dr.digital.util.ZipUtil;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.IdEntity;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.entity.StatusEntity;
import com.dr.framework.common.file.autoconfig.CommonFileConfig;
import com.dr.framework.common.file.model.FileInfo;
import com.dr.framework.common.file.service.CommonFileService;
import com.dr.framework.common.file.service.impl.DefaultFileHandler;
import com.dr.framework.common.form.core.entity.FormField;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.form.core.service.FormDataService;
import com.dr.framework.common.form.core.service.FormDefinitionService;
import com.dr.framework.common.form.core.service.FormNameGenerator;
import com.dr.framework.common.form.engine.model.core.FormModel;
import com.dr.framework.common.service.DataBaseService;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.jdbc.Relation;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

import static com.dr.framework.common.form.util.Constants.MODULE_NAME;

/**
 * @Author: caor
 * @Date: 2021-11-23 15:47
 * @Description:
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class PacketsDataServiceImpl implements PacketsDataService {
    @Autowired
    ArchiveFormDefinitionService archiveFormDefinitionService;
    @Autowired
    ArchiveDataManager archiveDataManager;
    @Autowired
    CommonFileService commonFileService;
    @Autowired
    CommonFileConfig commonFileConfig;
    @Autowired
    DefaultFileHandler defaultFileHandler;
    @Value("${filePath}")
    private String filePath;
    @Autowired
    FormDefinitionService formDefinitionService;
    @Autowired
    DataBaseService dataBaseService;
    @Autowired
    FormNameGenerator formNameGenerator;
    @Autowired
    CommonMapper commonMapper;
    @Autowired
    ConfigManagerClient configManagerClient;
    @Autowired
    RegisterService registerService;
    @Autowired
    FormDataService formDataService;
    @Autowired
    PacketsClient packetsClient;
    //线程池
    ExecutorService executorService = Executors.newFixedThreadPool(1);

    private static final Logger logger =  LoggerFactory.getLogger(PacketsDataServiceImpl.class);
    /**
     * 单个打包
     *
     * @param formDefinitionId
     * @param formDataId       打包路径，若为空则默认创建路径
     */
    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void packet(String formDefinitionId, String formDataId) {
        packet(archiveDataManager.selectOneFormData(formDefinitionId, formDataId));
    }

    /**
     * 在线移交
     *
     * @param formDataList
     * @param registerId
     */
    @Override
    public ResultEntity onlineHandover(List<FormData> formDataList, String registerId, String formDefinitionId, Person person) {
        //查询批次信息
        Register register = registerService.selectById(registerId);
        //归档信息包数据回填
        register.setSend_number(String.valueOf(formDataList.size()));
        register.setSend_time(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
        register.setTransactor(person.getUserName());
        String xmlPath = String.join(File.separator, filePath, "zipPack", register.getBatch_no() + ".xml");
        onlineHandoverXml(formDataList, register, formDefinitionId, xmlPath, person);
        //将数据传到 管理系统内
        ArchiveReceiveBo resultJson = new ArchiveReceiveBo();
        resultJson.setXmlPath(xmlPath);
        resultJson.setArrange(register.getArrange());
        resultJson.setClassify(register.getCode());
        List<ArchiveReceiveBo.ArchiveFileInfo> list = new ArrayList<>();
        for (FormData formData : formDataList) {
            ArchiveReceiveBo.ArchiveFileInfo zlJson = new ArchiveReceiveBo.ArchiveFileInfo();
            String targetPath = String.join(File.separator, filePath, "zipPack", formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + ".zip");
            zlJson.setSystemNum("INSPUR-DZZW-MACHINING");
            zlJson.setSystemName("数字化加工工具");
            zlJson.setBusinessId(formData.get(ArchiveEntity.ID_COLUMN_NAME));
            zlJson.setOfdName(formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + ".zip");
            zlJson.setPath(targetPath);
            list.add(zlJson);
        }

        resultJson.setFiles(list);
        //给档案室系统传输过去。
        String resultEntity = getReceiveDownload(resultJson);
        if (!StringUtils.isEmpty(resultEntity)) {
            //增加移交记录
            HandoverRecord handoverRecord = new HandoverRecord();
            handoverRecord.setRegisterId(register.getId());
            handoverRecord.setBatch_no(register.getBatch_no());
            handoverRecord.setBatch_name(register.getBatch_name());
            handoverRecord.setSend_number(register.getSend_number());
            handoverRecord.setSend_size(register.getSend_size());
            handoverRecord.setSend_time(register.getSend_time());
            handoverRecord.setTransactor(person.getUserName());
            handoverRecord.setFormDefinitionId(formDefinitionId);
            handoverRecord.setCode("200");
            handoverRecord.setMessage(resultEntity);
            handoverRecord.setSuccess("1");
            handoverRecord.setId(UUIDUtils.getUUID());
            commonMapper.insert(handoverRecord);
            //移交，禁用该批次所有功能
            register.setHandoverStatus("1");
            commonMapper.updateIgnoreNullById(register);
            return ResultEntity.success("移交成功");
        } else {
            return ResultEntity.error("移交异常请联系管理员！");
        }
    }

    /**
     * 生成移交 xml数据（外）
     *
     * @param formDataList
     * @param register
     * @param formDefinitionId
     * @param xmlPath
     * @param person
     */
    public void onlineHandoverXml(List<FormData> formDataList, Register register, String formDefinitionId, String xmlPath, Person person) {
        try {
            Element rss = new Element("transfer_info");
            rss.setAttribute("title", "移交目录");
            Document document = new Document(rss);
            Element channel = new Element("system_code");
            channel.setAttribute("title", "系统编码");
            channel.setText("INSPUR-DZZW-MACHINING");
            rss.addContent(channel);
            Element channel0 = new Element("batch_no");
            channel0.setAttribute("title", "批次号");
            channel0.setText(register.getBatch_no());
            rss.addContent(channel0);
            Element channel1 = new Element("batch_name");
            channel1.setAttribute("title", "批次名");
            channel1.setText(register.getBatch_name());
            rss.addContent(channel1);
            Element channel2 = new Element("send_time");
            channel2.setAttribute("title", "数据交换时间");
            channel2.setText(register.getSend_time());
            rss.addContent(channel2);
            Element channel3 = new Element("send_number");
            channel3.setAttribute("title", "归档信息包移交数量");
            channel3.setText(register.getSend_number());
            rss.addContent(channel3);
            Element channel4 = new Element("standard");
            channel4.setAttribute("title", "执行标准");
            channel4.setText(register.getCode());
            rss.addContent(channel4);

            Element directories = new Element("directories");
            directories.setAttribute("title", "目录");
            List<FormField> defaultField = getDefaultField(formDefinitionId);
            int zZiJie = 0;
            for (int i = 0; i < formDataList.size(); i++) {
                Element folder = new Element("directory");
                folder.setAttribute("id", (i + 1) + "");
                for (FormField formField : defaultField) {
                    if ("fonds_identifier".equals(formField.getFieldCode())) {
                        Element fondsChannel = new Element(formField.getFieldCode());
                        fondsChannel.setAttribute("title", formField.getLabel());
                        fondsChannel.setText(formDataList.get(i).getString(formField.getFieldCode()));
                        folder.addContent(fondsChannel);
                    }
                    if ("fonds_constituting_unit_name".equals(formField.getFieldCode())) {
                        Element fondsChannel = new Element(formField.getFieldCode());
                        fondsChannel.setAttribute("title", formField.getLabel());
                        fondsChannel.setText(formDataList.get(i).getString(formField.getFieldCode()));
                        folder.addContent(fondsChannel);
                    }
                    if ("archivers_category_code".equals(formField.getFieldCode())) {
                        Element fondsChannel = new Element("category_code");
                        fondsChannel.setAttribute("title", formField.getLabel());
                        fondsChannel.setText(formDataList.get(i).getString(formField.getFieldCode()));
                        folder.addContent(fondsChannel);
                    }
                    if ("archivers_year".equals(formField.getFieldCode())) {
                        Element fondsChannel = new Element(formField.getFieldCode());
                        fondsChannel.setAttribute("title", formField.getLabel());
                        fondsChannel.setText(formDataList.get(i).getString(formField.getFieldCode()));
                        folder.addContent(fondsChannel);
                    }
                    if ("archival_code".equals(formField.getFieldCode())) {
                        Element fondsChannel = new Element(formField.getFieldCode());
                        fondsChannel.setAttribute("title", formField.getLabel());
                        fondsChannel.setText(formDataList.get(i).getString(formField.getFieldCode()));
                        folder.addContent(fondsChannel);
                    }
                    if ("title".equals(formField.getFieldCode())) {
                        Element fondsChannel = new Element(formField.getFieldCode());
                        fondsChannel.setAttribute("title", formField.getLabel());
                        fondsChannel.setText(formDataList.get(i).getString(formField.getFieldCode()));
                        folder.addContent(fondsChannel);
                    }
                    if ("date_time".equals(formField.getFieldCode())) {
                        Element fondsChannel = new Element(formField.getFieldCode());
                        fondsChannel.setAttribute("title", "归档时间");
                        fondsChannel.setText(formDataList.get(i).getString(formField.getFieldCode()));
                        folder.addContent(fondsChannel);
                    }
                }
                Element regionChannel = new Element("region_code");
                regionChannel.setAttribute("title", "行政区划编码");
                regionChannel.setText(register.getRegion_code());
                folder.addContent(regionChannel);
                Element socialChannel = new Element("social_code");
                socialChannel.setAttribute("title", "机构编码");
                socialChannel.setText(register.getSocial_code());
                folder.addContent(socialChannel);
                //查询压缩包
                String targetPath = filePath + File.separator + "zipPack" + File.separator + formDataList.get(i).get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + formDataList.get(i).get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + ".zip";
                File targetFile = new File(targetPath);
                if (targetFile.isFile()) {
                    Element zjChannel = new Element("size");
                    zjChannel.setAttribute("title", "归档信息包字节数");
                    zjChannel.setText(targetFile.length() + "");
                    folder.addContent(zjChannel);
                    zZiJie += targetFile.length();
                    Element rjChannel = new Element("software_environment");
                    rjChannel.setAttribute("title", "软件环境");
                    rjChannel.setText(targetPath);
                    folder.addContent(rjChannel);
                }
                directories.addContent(folder);
            }
            rss.addContent(directories);
            Element channel5 = new Element("send_size");
            channel5.setAttribute("title", "归档信息包总字节数");
            channel5.setText(zZiJie + "");
            rss.addContent(channel5);
            Element channel6 = new Element("transactor");
            channel6.setAttribute("title", "移交人");
            channel6.setText(person.getUserName());
            rss.addContent(channel6);

            Format format = Format.getCompactFormat();
            format.setIndent("	");
            format.setEncoding("UTF-8");
            File xmlFile = new File(xmlPath);
            XMLOutputter outputer = new XMLOutputter(format);
            OutputStream outputStream = new FileOutputStream(xmlFile);
            outputer.output(document, outputStream);
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 移交传输函数
     *
     * @param jsonData
     * @return
     */
    public String getReceiveDownload(ArchiveReceiveBo jsonData) {
        return packetsClient.getReceiveDownload(jsonData);
    }

    /**
     * 移交回调函数
     *
     * @param archiveCallback
     */
    @Override
    public ResultEntity archivingResult(ArchiveCallback archiveCallback) {
        Assert.notNull(archiveCallback, "回调参数不能为空！");
        //查询移交记录内是否有该移交记录
        SqlQuery<HandoverRecord> sqlQuery = SqlQuery.from(HandoverRecord.class);
        sqlQuery.equal(HandoverRecordInfo.BATCH_NAME, archiveCallback.getBatch_name());
        HandoverRecord handoverRecord = commonMapper.selectOneByQuery(sqlQuery);
        Assert.notNull(handoverRecord, "未查询到该批次的移交记录！");
        Register register = registerService.selectById(handoverRecord.getRegisterId());
        ArchiveDataQuery queryArchive = new ArchiveDataQuery();
        queryArchive.setFormDefinitionId(handoverRecord.getFormDefinitionId());
        queryArchive.setFondId(register.getId());
        //查询选择的门类的所以档案数据
        List<FormData> list = archiveDataManager.findDataByQuery(queryArchive);
        //成功则进行删除过程数据
        if (archiveCallback.isSuccess()) {
            //根据内容删除 过程中的 图片和 txt文本数据
            for (FormData formData : list) {
                //删除filepath下的路径文件
                File pathFile = new File(String.join(File.separator, filePath, "filePath", formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE)));
                deleteFile(pathFile);
                //删除扫描文件路径的文件
                File sltFile = new File(String.join(File.separator, filePath, "fileThumbnailPath", formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE)));
                deleteFile(sltFile);
                //删除txt文本
                File txtFile = new File(String.join(File.separator, filePath, "txt", formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE)));
                deleteFile(txtFile);
                //删除 split 文件
                File splitFile = new File(String.join(File.separator, filePath, "split", formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE)));
                deleteFile(splitFile);
                //删除 打包数据
                File packFile = new File(String.join(File.separator, filePath, "zipPack", formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE)));
                deleteFile(packFile);
            }
            //2代表成功，禁用该批次所有功能
            register.setHandoverStatus("2");
            registerService.updateById(register);
            //删除表单定义
            //archiveFormDefinitionService.deleteForm(handoverRecord.getFormDefinitionId());
            //删除门类关联表
            // commonMapper.deleteByQuery(SqlQuery.from(Category.class).equal(CategoryInfo.REGISTERID, register.getId()));
            //删除批次信息表
            // registerService.deleteById(register.getId());
        } else {//失败则可以进行退回操作
            register.setHandoverStatus("3"); //代表可退回操作。
            registerService.updateById(register);
        }
        return ResultEntity.success();
    }

    /**
     * 删除文件夹下的所有文件
     *
     * @param file
     */
    public void deleteFile(File file) {
        //判断文件不为null或文件目录存在
        if (file == null || !file.exists()) {
            return;
        }
        //取得这个目录下的所有子文件对象
        File[] files = file.listFiles();
        //遍历该目录下的文件对象
        for (File f : files) {
            //判断子目录是否存在子目录,如果是文件则删除
            if (f.isDirectory()) {
                deleteFile(f);
            } else {
                f.delete();
            }
        }
        //删除空文件夹  for循环已经把上一层节点的目录清空。
        file.delete();
    }

    /**
     * 批量打包
     *
     * @param query
     * @param queryContent
     * @param command
     * @param formDataId
     * @param formDefinitionId
     * @param registerId
     */
    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void packetAll(BaseQuery query, String queryContent, String command, String formDataId, String formDefinitionId, String registerId) {
        switch (command) {
            case "select":
                packetSelect(formDataId, formDefinitionId);
                break;
            case "all":
                packetAll(formDefinitionId);
                break;
            case "query":
                packetQuery(query, queryContent);
                break;
        }
    }

    /**
     * 打包选中
     *
     * @param formDataId
     * @param formDefinitionId
     */
    public void packetSelect(String formDataId, String formDefinitionId) {
        for (String dataId : formDataId.split(",")) {
            executorService.execute(() -> packet(archiveDataManager.selectOneFormData(formDefinitionId, dataId)));
        }
    }

    /**
     * 打包所有
     *
     * @param formDefinitionId
     */
    public void packetAll(String formDefinitionId) {
        FormModel formModel = formDefinitionService.selectFormDefinitionById(formDefinitionId);
        Relation tableInfo = dataBaseService.getTableInfo(formNameGenerator.genTableName(formModel), MODULE_NAME);
        List<String> over = commonMapper.selectByQuery(SqlQuery.from(tableInfo, false)
                .column(tableInfo.getColumn(IdEntity.ID_COLUMN_NAME))
                .equal(tableInfo.getColumn(ArchiveEntity.COLUMN_STATUS), "OVER").setReturnClass(String.class));
        for (String formDataId : over) {
            executorService.execute(() -> packet(archiveDataManager.selectOneFormData(formDefinitionId, formDataId)));
        }
    }

    /**
     * 根据条件查询打包
     *
     * @param query
     * @param queryContent
     */
    public void packetQuery(BaseQuery query, String queryContent) {
        query.parseQuery(queryContent);
        List<FormData> dataList = archiveDataManager.findDataByQuery(query);
        for (FormData formData : dataList) {
            executorService.execute(() -> packet(formData));
        }
    }

    /**
     * 打包
     *
     * @param formData
     */
    public void packet(FormData formData) {
        List<FormData> formDataList = new ArrayList<>();
        formDataList.add(formData);
        PacketRecord packetRecord = new PacketRecord();
        //默认元数据结构
        List<FormField> defaultField = getDefaultField(formData.getFormDefinitionId());
        if (defaultField.size() > 0) {
//            packetXml(formData, defaultField);
            packetXml(formData);
            packetRecord.setSuccess("1");
            packetRecord.setMessage("封包成功！！！");
        } else {
            packetRecord.setSuccess("0");
            packetRecord.setMessage("智能归档配置系统元数据未配置！！！");
        }
        //增加封包记录
        packetRecord.setTitle(formData.getString(ArchiveEntity.COLUMN_TITLE));
        packetRecord.setArchiveCode(formData.getString(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        packetRecord.setFondCode(formData.getString(ArchiveEntity.COLUMN_FOND_CODE));
        packetRecord.setFormDataId(formData.getId());
        packetRecord.setFormDefinitionId(formData.getFormDefinitionId());
        packetRecord.setId(UUIDUtils.getUUID());
        packetRecord.setCreateDate(UUIDUtils.currentTimeMillis());
        commonMapper.insert(packetRecord);
        formData.put(ArchiveEntity.COLUMN_PACKET_STATE, "1");
        formDataService.updateFormDataById(formData);
        //TODO 原来的代码先注释掉
        /*PacketRecord packetRecord = new PacketRecord();
        packetRecord.setTitle(formData.getString(ArchiveEntity.COLUMN_TITLE));
        packetRecord.setArchiveCode(formData.getString(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        packetRecord.setFondCode(formData.getString(ArchiveEntity.COLUMN_FOND_CODE));
        packetRecord.setFormDataId(formData.getId());
        packetRecord.setFormDefinitionId(formData.getFormDefinitionId());
        CommonService.bindCreateInfo(packetRecord);
        commonMapper.insert(packetRecord);
        //获取存储路径
        String targetPath = filePath + File.separator + "zipPack" + File.separator + formData.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
        //获取原文
        String ofdPath = filePath + File.separator + "ofd" + File.separator + formData.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
        File file = new File(ofdPath);
        //置空fileLists用于获取文件夹下面的所有OFD文件
        fileLists = new ArrayList<>();
        List<String> directory = isDirectory(file);
        for (String s : directory) {
            try {
                String realTargetPath = targetPath;
                //获取所有的ofd原文
                File ofdFile = new File(s);
                String dirName = ofdFile.getParentFile().getName();
                String fileName = ofdFile.getName();
                //根据目录数据将原文复制到指定位置
                realTargetPath = realTargetPath + File.separator + dirName;
                File targetFile = new File(realTargetPath);
                if (!targetFile.exists()) {
                    targetFile.mkdirs();
                }
                OutputStream os = new FileOutputStream(realTargetPath + File.separator + fileName);
                InputStream is = new FileInputStream(ofdFile);
                FileCopyUtils.copy(is, os);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //生成xml文件，把原文列表也放到基本信息元数据xml中去
        buildXML(formDataList, targetPath, directory);
        //生成zip包
        try {
            ZipUtil.toZip(targetPath, targetPath + ".zip", true);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    /**
     * 生成xml文件
     */
    public void xmlGenerateFile(FormData formData) {
        List<FormField> defaultField = getDefaultField(formData.getFormDefinitionId());

        if (defaultField.size() > 0) {
            try {
                String fond_code = formData.get(ArchiveEntity.COLUMN_FOND_CODE);
                String archive_code = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
                // 1、生成一个根节点  base_info title="基本信息元数据"
                Element rss = new Element("base_info");
                // 2、为节点添加属性
                rss.setAttribute("title", "基本信息元数据");
                // 3、生成一个document对象
                Document document = new Document(rss);
                for (FormField formField : defaultField) {
                    if (!StringUtils.isEmpty(formField.getFieldAliasStr())) {
                        Element channel = new Element(formField.getFieldAliasStr());
                        channel.setAttribute("title", formField.getLabel());
                        channel.setText(formData.getString(formField.getFieldCode()));
                        rss.addContent(channel);
                    } else {
                        Element channel = new Element(formField.getFieldCode());
                        channel.setAttribute("title", formField.getLabel());
                        channel.setText(formData.getString(formField.getFieldCode()));
                        rss.addContent(channel);
                    }
                }
                //4、获取存储路径
                String targetPath = String.join(File.separator, filePath, "zipPack", fond_code, archive_code);
                //5、获取原文
                String ofdPath = String.join(File.separator, filePath, "ofd", fond_code, archive_code);
                File file = new File(ofdPath);
                //置空fileLists用于获取文件夹下面的所有OFD文件
                fileLists = new ArrayList<>();
                Integer count = 1;
                if (!StringUtils.isEmpty(file.listFiles())) {
                    Element fileset = new Element("fileset");
                    for (File listFile : file.listFiles()) {
                        Element folder = new Element("folder");
                        if (!listFile.getName().contains(".DS_Store")) {
                            if (!listFile.getName().contains(".ofd")) {
                                File[] files = new File(listFile.getPath()).listFiles();
                                if (files.length > 0) {
                                    File targetFile = new File(targetPath + File.separator + listFile.getName());
                                    if (!targetFile.exists()) {
                                        targetFile.mkdirs();
                                    }
                                    for (int i = 0; i < files.length; i++) {
                                        OutputStream os = new FileOutputStream(targetPath + File.separator + listFile.getName() + File.separator + files[i].getName());
                                        InputStream is = new FileInputStream(files[i].getPath());
                                        FileCopyUtils.copy(is, os);

                                        folder.setAttribute("id", FileUtil.getNewFileName(count));
                                        folder.setAttribute("title", listFile.getName());
                                        Element file1 = new Element("file");
                                        file1.setAttribute("id", FileUtil.getNewFileName(i + 1));

                                        Element file_code = new Element("file_code");
                                        file_code.setAttribute("title", "归档文件类型");
                                        file_code.setText("A");

                                        Element file_standard_name = new Element("file_standard_name");
                                        file_standard_name.setAttribute("title", "归档文件目录名称");
                                        file_standard_name.setText(formData.getString(ArchiveEntity.COLUMN_TITLE));

                                        Element file_actual_name = new Element("file_actual_name");
                                        file_actual_name.setAttribute("title", "归档文件名称");
                                        file_actual_name.setText(files[i].getName());

                                        Element format_information = new Element("format_information");
                                        format_information.setAttribute("title", "归档文件格式");
                                        format_information.setText(files[i].getName().substring(files[i].getName().lastIndexOf(".") + 1));

                                        Element computer_file_name = new Element("computer_file_name");
                                        computer_file_name.setAttribute("title", "归档文件计算机文件名");
                                        computer_file_name.setText(files[i].getName().substring(0, files[i].getName().lastIndexOf(".")));

                                        Element computer_file_size = new Element("computer_file_size");
                                        computer_file_size.setAttribute("title", "归档文件计算机文件大小");
                                        computer_file_size.setText(files[i].length() + "");

                                        Element computer_file_creation_time = new Element("computer_file_creation_time");
                                        computer_file_creation_time.setAttribute("title", "归档文件计算机形成时间");
                                        computer_file_creation_time.setText(formData.getString(ArchiveEntity.COLUMN_FILETIME));

                                        file1.addContent(file_code);
                                        file1.addContent(file_standard_name);
                                        file1.addContent(file_actual_name);
                                        file1.addContent(format_information);
                                        file1.addContent(computer_file_name);
                                        file1.addContent(computer_file_size);
                                        file1.addContent(computer_file_creation_time);

                                        folder.addContent(file1);
                                    }
                                    count++;
                                    fileset.addContent(folder);
                                }
                            } else {
                                File targetFile = new File(targetPath);
                                if (!targetFile.exists()) {
                                    targetFile.mkdirs();
                                }
                                OutputStream os = new FileOutputStream(targetPath + File.separator + listFile.getName());
                                InputStream is = new FileInputStream(listFile.getPath());
                                FileCopyUtils.copy(is, os);

                                folder.setAttribute("id", FileUtil.getNewFileName(count));
                                folder.setAttribute("title", listFile.getName());
                                Element file1 = new Element("file");
                                file1.setAttribute("id", FileUtil.getNewFileName(1));

                                Element file_code = new Element("file_code");
                                file_code.setAttribute("title", "归档文件类型");
                                file_code.setText("A");

                                Element file_standard_name = new Element("file_standard_name");
                                file_standard_name.setAttribute("title", "归档文件目录名称");
                                file_standard_name.setText(formData.getString(ArchiveEntity.COLUMN_TITLE));

                                Element file_actual_name = new Element("file_actual_name");
                                file_actual_name.setAttribute("title", "归档文件名称");
                                file_actual_name.setText(listFile.getName());

                                Element format_information = new Element("format_information");
                                format_information.setAttribute("title", "归档文件格式");
                                format_information.setText(listFile.getName().substring(listFile.getName().lastIndexOf(".") + 1));

                                Element computer_file_name = new Element("computer_file_name");
                                computer_file_name.setAttribute("title", "归档文件计算机文件名");
                                computer_file_name.setText(listFile.getName().substring(0, listFile.getName().lastIndexOf(".")));

                                Element computer_file_size = new Element("computer_file_size");
                                computer_file_size.setAttribute("title", "归档文件计算机文件大小");
                                computer_file_size.setText(listFile.length() + "");

                                Element computer_file_creation_time = new Element("computer_file_creation_time");
                                computer_file_creation_time.setAttribute("title", "归档文件计算机形成时间");
                                computer_file_creation_time.setText(formData.getString(ArchiveEntity.COLUMN_FILETIME));

                                file1.addContent(file_code);
                                file1.addContent(file_standard_name);
                                file1.addContent(file_actual_name);
                                file1.addContent(format_information);
                                file1.addContent(computer_file_name);
                                file1.addContent(computer_file_size);
                                file1.addContent(computer_file_creation_time);

                                folder.addContent(file1);

                                fileset.addContent(folder);
                            }
                        }
                    }
                    rss.addContent(fileset);
                }

                Format format = Format.getCompactFormat();
                // 设置换行Tab或空格
                format.setIndent("	");
                format.setEncoding("UTF-8");

                // 6、创建XMLOutputter的对象
                XMLOutputter outputer = new XMLOutputter(format);
                String property = System.getProperty("user.dir");
                // 7、利用outputer将document转换成xml文档
                File xmlFile = new File(targetPath+ File.separator + archive_code + ".xml");
                if (!xmlFile.exists()) {
                    //文件可能不存在需要创建
                    xmlFile.getParentFile().mkdirs();
                }
                OutputStream outputStream = new FileOutputStream(xmlFile);
                outputer.output(document, outputStream);
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 新打包，已经有xml文件，打包成压缩包
     *
     * @param formData
     */
    public void packetXml(FormData formData) {
        try {
            String fond_code = formData.get(ArchiveEntity.COLUMN_FOND_CODE);
            String archive_code = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
            String targetPath = String.join(File.separator, filePath, "zipPack", fond_code, archive_code);
            String xmlPath = String.join(File.separator, targetPath,archive_code + ".xml");
            //现在在提交到数字化成果的时候就会生成xml
            File xmlFile = new File(xmlPath);
            if (!xmlFile.exists()) {
                xmlFile.mkdirs();
                xmlGenerateFile(formData);
            }
            ZipUtil.toZip(targetPath, targetPath + ".zip", true);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 旧打包，生成xml文件，然后打包
     *
     * @param formData
     * @param formFields
     */
    public void packetXml(FormData formData, List<FormField> formFields) {
        try {
            String fond_code = formData.get(ArchiveEntity.COLUMN_FOND_CODE);
            String archive_code = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
            // 1、生成一个根节点  base_info title="基本信息元数据"
            Element rss = new Element("base_info");
            // 2、为节点添加属性
            rss.setAttribute("title", "基本信息元数据");
            // 3、生成一个document对象
            Document document = new Document(rss);
            for (FormField formField : formFields) {
                if (!StringUtils.isEmpty(formField.getFieldAliasStr())) {
                    Element channel = new Element(formField.getFieldAliasStr());
                    channel.setAttribute("title", formField.getLabel());
                    channel.setText(formData.getString(formField.getFieldCode()));
                    rss.addContent(channel);
                } else {
                    Element channel = new Element(formField.getFieldCode());
                    channel.setAttribute("title", formField.getLabel());
                    channel.setText(formData.getString(formField.getFieldCode()));
                    rss.addContent(channel);
                }
            }
            //4、获取存储路径
            String targetPath = String.join(File.separator, filePath, "zipPack", fond_code, archive_code);
            //5、获取原文
            String ofdPath = String.join(File.separator, filePath, "ofd", fond_code, archive_code);
            File file = new File(ofdPath);
            //置空fileLists用于获取文件夹下面的所有OFD文件
            fileLists = new ArrayList<>();
            Integer count = 1;
            if (!StringUtils.isEmpty(file.listFiles())) {
                Element fileset = new Element("fileset");
                for (File listFile : file.listFiles()) {
                    Element folder = new Element("folder");
                    if (!listFile.getName().contains(".DS_Store")) {
                        if (!listFile.getName().contains(".ofd")) {
                            File[] files = new File(listFile.getPath()).listFiles();
                            if (files.length > 0) {
                                File targetFile = new File(targetPath + File.separator + listFile.getName());
                                if (!targetFile.exists()) {
                                    targetFile.mkdirs();
                                }
                                for (int i = 0; i < files.length; i++) {
                                    OutputStream os = new FileOutputStream(targetPath + File.separator + listFile.getName() + File.separator + files[i].getName());
                                    InputStream is = new FileInputStream(files[i].getPath());
                                    FileCopyUtils.copy(is, os);

                                    folder.setAttribute("id", FileUtil.getNewFileName(count));
                                    folder.setAttribute("title", listFile.getName());
                                    Element file1 = new Element("file");
                                    file1.setAttribute("id", FileUtil.getNewFileName(i + 1));

                                    Element file_code = new Element("file_code");
                                    file_code.setAttribute("title", "归档文件类型");
                                    file_code.setText("A");

                                    Element file_standard_name = new Element("file_standard_name");
                                    file_standard_name.setAttribute("title", "归档文件目录名称");
                                    file_standard_name.setText(formData.getString(ArchiveEntity.COLUMN_TITLE));

                                    Element file_actual_name = new Element("file_actual_name");
                                    file_actual_name.setAttribute("title", "归档文件名称");
                                    file_actual_name.setText(files[i].getName());

                                    Element format_information = new Element("format_information");
                                    format_information.setAttribute("title", "归档文件格式");
                                    format_information.setText(files[i].getName().substring(files[i].getName().lastIndexOf(".") + 1));

                                    Element computer_file_name = new Element("computer_file_name");
                                    computer_file_name.setAttribute("title", "归档文件计算机文件名");
                                    computer_file_name.setText(files[i].getName().substring(0, files[i].getName().lastIndexOf(".")));

                                    Element computer_file_size = new Element("computer_file_size");
                                    computer_file_size.setAttribute("title", "归档文件计算机文件大小");
                                    computer_file_size.setText(files[i].length() + "");

                                    Element computer_file_creation_time = new Element("computer_file_creation_time");
                                    computer_file_creation_time.setAttribute("title", "归档文件计算机形成时间");
                                    computer_file_creation_time.setText(formData.getString(ArchiveEntity.COLUMN_FILETIME));

                                    file1.addContent(file_code);
                                    file1.addContent(file_standard_name);
                                    file1.addContent(file_actual_name);
                                    file1.addContent(format_information);
                                    file1.addContent(computer_file_name);
                                    file1.addContent(computer_file_size);
                                    file1.addContent(computer_file_creation_time);

                                    folder.addContent(file1);
                                }
                                count++;
                                fileset.addContent(folder);
                            }
                        } else {
                            File targetFile = new File(targetPath);
                            if (!targetFile.exists()) {
                                targetFile.mkdirs();
                            }
                            OutputStream os = new FileOutputStream(targetPath + File.separator + listFile.getName());
                            InputStream is = new FileInputStream(listFile.getPath());
                            FileCopyUtils.copy(is, os);

                            folder.setAttribute("id", FileUtil.getNewFileName(count));
                            folder.setAttribute("title", listFile.getName());
                            Element file1 = new Element("file");
                            file1.setAttribute("id", FileUtil.getNewFileName(1));

                            Element file_code = new Element("file_code");
                            file_code.setAttribute("title", "归档文件类型");
                            file_code.setText("A");

                            Element file_standard_name = new Element("file_standard_name");
                            file_standard_name.setAttribute("title", "归档文件目录名称");
                            file_standard_name.setText(formData.getString(ArchiveEntity.COLUMN_TITLE));

                            Element file_actual_name = new Element("file_actual_name");
                            file_actual_name.setAttribute("title", "归档文件名称");
                            file_actual_name.setText(listFile.getName());

                            Element format_information = new Element("format_information");
                            format_information.setAttribute("title", "归档文件格式");
                            format_information.setText(listFile.getName().substring(listFile.getName().lastIndexOf(".") + 1));

                            Element computer_file_name = new Element("computer_file_name");
                            computer_file_name.setAttribute("title", "归档文件计算机文件名");
                            computer_file_name.setText(listFile.getName().substring(0, listFile.getName().lastIndexOf(".")));

                            Element computer_file_size = new Element("computer_file_size");
                            computer_file_size.setAttribute("title", "归档文件计算机文件大小");
                            computer_file_size.setText(listFile.length() + "");

                            Element computer_file_creation_time = new Element("computer_file_creation_time");
                            computer_file_creation_time.setAttribute("title", "归档文件计算机形成时间");
                            computer_file_creation_time.setText(formData.getString(ArchiveEntity.COLUMN_FILETIME));

                            file1.addContent(file_code);
                            file1.addContent(file_standard_name);
                            file1.addContent(file_actual_name);
                            file1.addContent(format_information);
                            file1.addContent(computer_file_name);
                            file1.addContent(computer_file_size);
                            file1.addContent(computer_file_creation_time);

                            folder.addContent(file1);

                            fileset.addContent(folder);
                        }
                    }
                }
                rss.addContent(fileset);
            }

            Format format = Format.getCompactFormat();
            // 设置换行Tab或空格
            format.setIndent("	");
            format.setEncoding("UTF-8");

            // 6、创建XMLOutputter的对象
            XMLOutputter outputer = new XMLOutputter(format);
            String property = System.getProperty("user.dir");
            // 7、利用outputer将document转换成xml文档
            File xmlFile = new File(targetPath + File.separator + "基本信息元数据.xml");
            if (!xmlFile.exists()) {
                //文件可能不存在需要创建
                xmlFile.getParentFile().mkdir();
            }
            OutputStream outputStream = new FileOutputStream(xmlFile);
            outputer.output(document, outputStream);
            outputStream.close();
            ZipUtil.toZip(targetPath, targetPath + ".zip", true);
            System.out.println("打包成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("打包失败");
        }
    }

    /**
     * 筛选原数据字段打包用
     *
     * @param formDefinitionId
     * @return
     */
    public List<FormField> getDefaultField(String formDefinitionId) {
        //筛选字段打包用oneMetadata
        Metadata oneMetadata = commonMapper.selectOneByQuery(SqlQuery.from(Metadata.class).equal(MetadataInfo.FORMDEFINITIONID, formDefinitionId));
        List<FormField> formFields = new ArrayList<>();
        //此处id为元数据的id
        List<MataDataInfo> categoryMetadata = configManagerClient.getCategoryMetadata(oneMetadata.getCode(), oneMetadata.getClassify(), oneMetadata.getArrange(), oneMetadata.getBatch_id());
        if (categoryMetadata.size() > 0) {
            MataDataInfo mataDataInfo1 = categoryMetadata.get(0);
            List<FormField> fieldList = archiveFormDefinitionService.findFieldList(formDefinitionId);
            fieldList.forEach(item -> {
                item.setDescription(StatusEntity.STATUS_DISABLE_STR);
                item.setFieldCode(item.getFieldCode().toLowerCase());
            });
            //取出查询的所有字段
            List<String> collect1 = mataDataInfo1.getMetadata().stream().map(mataData -> mataData.geteName().toLowerCase()).collect(Collectors.toList());
            //取出查询出的所有字段名称
            List<String> collect2 = mataDataInfo1.getMetadata().stream().map(mataData -> mataData.getName()).collect(Collectors.toList());
            //拿智能归档配置系统中的字段与表中的字段别名对比，如果相同就拿出来
            List<FormField> collect3 = fieldList.stream().filter(formField -> collect1.contains(formField.getFieldAliasStr())).collect(Collectors.toList());
            //与表中字段进行匹配相同就拿出来
            List<FormField> collect4 = fieldList.stream().filter(formField -> collect1.contains(formField.getFieldCode().toLowerCase())).collect(Collectors.toList());
            //与表中字段名进行匹配匹配到就拿出来
            List<FormField> collect5 = fieldList.stream().filter(formField -> collect2.contains(formField.getLabel())).collect(Collectors.toList());
            //合并
            collect5.addAll(collect4);
            collect5.addAll(collect3);
            //去重
            List<FormField> collect6 = collect5.stream().distinct().collect(Collectors.toList());
            formFields.addAll(collect6);
            Set<String> set = new HashSet<>();
            //与智能归档配置系统中的字段合并
            for (FormField formField : collect6) {
                set.add(formField.getFieldCode());
                if (!StringUtils.isEmpty(formField.getFieldAliasStr())) {
                    set.add(formField.getFieldAliasStr());
                }
            }
            List<MataData> metadata = mataDataInfo1.getMetadata();
            //删除重复字段
            metadata.removeIf(item -> {
                return set.contains(item.geteName().toLowerCase());
            });
            for (MataData mataDataInfo : metadata) {
                FormField formField = new FormField();
                formField.setLabel(mataDataInfo.getName());
                formField.setFieldCode(mataDataInfo.geteName().toLowerCase());
                formField.setLabel(mataDataInfo.getName());
                formFields.add(formField);
            }
        }
        return formFields;
    }

    /**
     * 创建xml文件
     *
     * @param formDataList
     * @param targetPath   元数据存储路径
     */
    void buildXML(List<FormData> formDataList, String targetPath, List<String> fileList) {
        if (StringUtils.isEmpty(targetPath)) {
            targetPath = commonFileConfig.getUploadDir("temp");
        }
        targetPath += File.separator + PacketsDataService.BASIC_INFORMATION_METADATA;
        ifFileExit(targetPath, true);
        try (OutputStream outputStream = new FileOutputStream(targetPath)) {
            PacketDataParserUtil.writeData(fileList.toArray(new String[0]), converseList(formDataList).iterator(), "application/zip", outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据目录数据将原文复制到指定位置
     *
     * @param refId
     * @param refType
     * @param groupCode
     * @param targetPath
     */
    List<String> buildFile(String refId, String refType, String groupCode, String targetPath) {
        List<String> filePathList = new ArrayList<>();
        if (StringUtils.isEmpty(groupCode)) {
            groupCode = "default";
        }
        List<FileInfo> fileInfoList = commonFileService.list(refId, refType, groupCode);
        for (FileInfo fileInfo : fileInfoList) {
            String realTargetPath = targetPath;
            String relative = "";
            try {
                if (!StringUtils.isEmpty(fileInfo.getDescription())) {
                    relative = File.separator + fileInfo.getDescription();
                } else {
                    relative = File.separator + "结果文件";
                }
                realTargetPath += relative;
                ifFileExit(realTargetPath, false);
                defaultFileHandler.copyTo(fileInfo, realTargetPath + File.separator + fileInfo.getName());
                filePathList.add(relative + File.separator + fileInfo.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filePathList;
    }

    /**
     * 将List<FormData>转为相应的List<Map<String, Object>>
     *
     * @param formDataList
     * @return
     */
    private List<Map<String, Object>> converseList(List<FormData> formDataList) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (FormData formData : formDataList) {
            Map<String, Object> map = new HashMap<>();
            Set<Map.Entry<String, Serializable>> entrySet = formData.entrySet();
            for (Map.Entry<String, Serializable> item : entrySet) {
                String key = item.getKey();
                map.put(key, item.getValue());
                map.put(item.getKey(), item.getValue());
            }
            list.add(map);
        }
        return list;
    }

    /**
     * 根据路径创建文件夹，或文件
     *
     * @param path            文件路径
     * @param ifCreateNewFile 是否创建文件
     * @return
     */
    void ifFileExit(String path, boolean ifCreateNewFile) {
        File file = new File(path);
        if (file.isDirectory() && !file.exists()) {
            file.getParentFile().mkdirs();
        }
        if (ifCreateNewFile) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 查询文件夹下面所有有的文件
     *
     * @param file
     * @return
     */
    List<String> fileLists = new ArrayList<>();

    public List<String> isDirectory(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                String fileName = file.getName().toUpperCase();
                // 只检测图片
                if (fileName.indexOf(".ofd") != -1 || fileName.indexOf(".OFD") != -1) {
                    fileLists.add(file.getPath());
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
        return fileLists;
    }

}
