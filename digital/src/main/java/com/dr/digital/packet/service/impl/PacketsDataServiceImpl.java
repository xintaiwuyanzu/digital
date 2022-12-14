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
    //?????????
    ExecutorService executorService = Executors.newFixedThreadPool(1);

    private static final Logger logger =  LoggerFactory.getLogger(PacketsDataServiceImpl.class);
    /**
     * ????????????
     *
     * @param formDefinitionId
     * @param formDataId       ?????????????????????????????????????????????
     */
    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void packet(String formDefinitionId, String formDataId) {
        packet(archiveDataManager.selectOneFormData(formDefinitionId, formDataId));
    }

    /**
     * ????????????
     *
     * @param formDataList
     * @param registerId
     */
    @Override
    public ResultEntity onlineHandover(List<FormData> formDataList, String registerId, String formDefinitionId, Person person) {
        //??????????????????
        Register register = registerService.selectById(registerId);
        //???????????????????????????
        register.setSend_number(String.valueOf(formDataList.size()));
        register.setSend_time(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
        register.setTransactor(person.getUserName());
        String xmlPath = String.join(File.separator, filePath, "zipPack", register.getBatch_no() + ".xml");
        onlineHandoverXml(formDataList, register, formDefinitionId, xmlPath, person);
        //??????????????? ???????????????
        ArchiveReceiveBo resultJson = new ArchiveReceiveBo();
        resultJson.setXmlPath(xmlPath);
        resultJson.setArrange(register.getArrange());
        resultJson.setClassify(register.getCode());
        List<ArchiveReceiveBo.ArchiveFileInfo> list = new ArrayList<>();
        for (FormData formData : formDataList) {
            ArchiveReceiveBo.ArchiveFileInfo zlJson = new ArchiveReceiveBo.ArchiveFileInfo();
            String targetPath = String.join(File.separator, filePath, "zipPack", formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + ".zip");
            zlJson.setSystemNum("INSPUR-DZZW-MACHINING");
            zlJson.setSystemName("?????????????????????");
            zlJson.setBusinessId(formData.get(ArchiveEntity.ID_COLUMN_NAME));
            zlJson.setOfdName(formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + ".zip");
            zlJson.setPath(targetPath);
            list.add(zlJson);
        }

        resultJson.setFiles(list);
        //?????????????????????????????????
        String resultEntity = getReceiveDownload(resultJson);
        if (!StringUtils.isEmpty(resultEntity)) {
            //??????????????????
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
            //????????????????????????????????????
            register.setHandoverStatus("1");
            commonMapper.updateIgnoreNullById(register);
            return ResultEntity.success("????????????");
        } else {
            return ResultEntity.error("?????????????????????????????????");
        }
    }

    /**
     * ???????????? xml???????????????
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
            rss.setAttribute("title", "????????????");
            Document document = new Document(rss);
            Element channel = new Element("system_code");
            channel.setAttribute("title", "????????????");
            channel.setText("INSPUR-DZZW-MACHINING");
            rss.addContent(channel);
            Element channel0 = new Element("batch_no");
            channel0.setAttribute("title", "?????????");
            channel0.setText(register.getBatch_no());
            rss.addContent(channel0);
            Element channel1 = new Element("batch_name");
            channel1.setAttribute("title", "?????????");
            channel1.setText(register.getBatch_name());
            rss.addContent(channel1);
            Element channel2 = new Element("send_time");
            channel2.setAttribute("title", "??????????????????");
            channel2.setText(register.getSend_time());
            rss.addContent(channel2);
            Element channel3 = new Element("send_number");
            channel3.setAttribute("title", "???????????????????????????");
            channel3.setText(register.getSend_number());
            rss.addContent(channel3);
            Element channel4 = new Element("standard");
            channel4.setAttribute("title", "????????????");
            channel4.setText(register.getCode());
            rss.addContent(channel4);

            Element directories = new Element("directories");
            directories.setAttribute("title", "??????");
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
                        fondsChannel.setAttribute("title", "????????????");
                        fondsChannel.setText(formDataList.get(i).getString(formField.getFieldCode()));
                        folder.addContent(fondsChannel);
                    }
                }
                Element regionChannel = new Element("region_code");
                regionChannel.setAttribute("title", "??????????????????");
                regionChannel.setText(register.getRegion_code());
                folder.addContent(regionChannel);
                Element socialChannel = new Element("social_code");
                socialChannel.setAttribute("title", "????????????");
                socialChannel.setText(register.getSocial_code());
                folder.addContent(socialChannel);
                //???????????????
                String targetPath = filePath + File.separator + "zipPack" + File.separator + formDataList.get(i).get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + formDataList.get(i).get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + ".zip";
                File targetFile = new File(targetPath);
                if (targetFile.isFile()) {
                    Element zjChannel = new Element("size");
                    zjChannel.setAttribute("title", "????????????????????????");
                    zjChannel.setText(targetFile.length() + "");
                    folder.addContent(zjChannel);
                    zZiJie += targetFile.length();
                    Element rjChannel = new Element("software_environment");
                    rjChannel.setAttribute("title", "????????????");
                    rjChannel.setText(targetPath);
                    folder.addContent(rjChannel);
                }
                directories.addContent(folder);
            }
            rss.addContent(directories);
            Element channel5 = new Element("send_size");
            channel5.setAttribute("title", "???????????????????????????");
            channel5.setText(zZiJie + "");
            rss.addContent(channel5);
            Element channel6 = new Element("transactor");
            channel6.setAttribute("title", "?????????");
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
     * ??????????????????
     *
     * @param jsonData
     * @return
     */
    public String getReceiveDownload(ArchiveReceiveBo jsonData) {
        return packetsClient.getReceiveDownload(jsonData);
    }

    /**
     * ??????????????????
     *
     * @param archiveCallback
     */
    @Override
    public ResultEntity archivingResult(ArchiveCallback archiveCallback) {
        Assert.notNull(archiveCallback, "???????????????????????????");
        //?????????????????????????????????????????????
        SqlQuery<HandoverRecord> sqlQuery = SqlQuery.from(HandoverRecord.class);
        sqlQuery.equal(HandoverRecordInfo.BATCH_NAME, archiveCallback.getBatch_name());
        HandoverRecord handoverRecord = commonMapper.selectOneByQuery(sqlQuery);
        Assert.notNull(handoverRecord, "???????????????????????????????????????");
        Register register = registerService.selectById(handoverRecord.getRegisterId());
        ArchiveDataQuery queryArchive = new ArchiveDataQuery();
        queryArchive.setFormDefinitionId(handoverRecord.getFormDefinitionId());
        queryArchive.setFondId(register.getId());
        //??????????????????????????????????????????
        List<FormData> list = archiveDataManager.findDataByQuery(queryArchive);
        //?????????????????????????????????
        if (archiveCallback.isSuccess()) {
            //?????????????????? ???????????? ????????? txt????????????
            for (FormData formData : list) {
                //??????filepath??????????????????
                File pathFile = new File(String.join(File.separator, filePath, "filePath", formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE)));
                deleteFile(pathFile);
                //?????????????????????????????????
                File sltFile = new File(String.join(File.separator, filePath, "fileThumbnailPath", formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE)));
                deleteFile(sltFile);
                //??????txt??????
                File txtFile = new File(String.join(File.separator, filePath, "txt", formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE)));
                deleteFile(txtFile);
                //?????? split ??????
                File splitFile = new File(String.join(File.separator, filePath, "split", formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE)));
                deleteFile(splitFile);
                //?????? ????????????
                File packFile = new File(String.join(File.separator, filePath, "zipPack", formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE)));
                deleteFile(packFile);
            }
            //2??????????????????????????????????????????
            register.setHandoverStatus("2");
            registerService.updateById(register);
            //??????????????????
            //archiveFormDefinitionService.deleteForm(handoverRecord.getFormDefinitionId());
            //?????????????????????
            // commonMapper.deleteByQuery(SqlQuery.from(Category.class).equal(CategoryInfo.REGISTERID, register.getId()));
            //?????????????????????
            // registerService.deleteById(register.getId());
        } else {//?????????????????????????????????
            register.setHandoverStatus("3"); //????????????????????????
            registerService.updateById(register);
        }
        return ResultEntity.success();
    }

    /**
     * ?????????????????????????????????
     *
     * @param file
     */
    public void deleteFile(File file) {
        //??????????????????null?????????????????????
        if (file == null || !file.exists()) {
            return;
        }
        //?????????????????????????????????????????????
        File[] files = file.listFiles();
        //?????????????????????????????????
        for (File f : files) {
            //????????????????????????????????????,????????????????????????
            if (f.isDirectory()) {
                deleteFile(f);
            } else {
                f.delete();
            }
        }
        //??????????????????  for????????????????????????????????????????????????
        file.delete();
    }

    /**
     * ????????????
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
     * ????????????
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
     * ????????????
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
     * ????????????????????????
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
     * ??????
     *
     * @param formData
     */
    public void packet(FormData formData) {
        List<FormData> formDataList = new ArrayList<>();
        formDataList.add(formData);
        PacketRecord packetRecord = new PacketRecord();
        //?????????????????????
        List<FormField> defaultField = getDefaultField(formData.getFormDefinitionId());
        if (defaultField.size() > 0) {
//            packetXml(formData, defaultField);
            packetXml(formData);
            packetRecord.setSuccess("1");
            packetRecord.setMessage("?????????????????????");
        } else {
            packetRecord.setSuccess("0");
            packetRecord.setMessage("???????????????????????????????????????????????????");
        }
        //??????????????????
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
        //TODO ???????????????????????????
        /*PacketRecord packetRecord = new PacketRecord();
        packetRecord.setTitle(formData.getString(ArchiveEntity.COLUMN_TITLE));
        packetRecord.setArchiveCode(formData.getString(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        packetRecord.setFondCode(formData.getString(ArchiveEntity.COLUMN_FOND_CODE));
        packetRecord.setFormDataId(formData.getId());
        packetRecord.setFormDefinitionId(formData.getFormDefinitionId());
        CommonService.bindCreateInfo(packetRecord);
        commonMapper.insert(packetRecord);
        //??????????????????
        String targetPath = filePath + File.separator + "zipPack" + File.separator + formData.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
        //????????????
        String ofdPath = filePath + File.separator + "ofd" + File.separator + formData.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
        File file = new File(ofdPath);
        //??????fileLists????????????????????????????????????OFD??????
        fileLists = new ArrayList<>();
        List<String> directory = isDirectory(file);
        for (String s : directory) {
            try {
                String realTargetPath = targetPath;
                //???????????????ofd??????
                File ofdFile = new File(s);
                String dirName = ofdFile.getParentFile().getName();
                String fileName = ofdFile.getName();
                //????????????????????????????????????????????????
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
        //??????xml??????????????????????????????????????????????????????xml??????
        buildXML(formDataList, targetPath, directory);
        //??????zip???
        try {
            ZipUtil.toZip(targetPath, targetPath + ".zip", true);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    /**
     * ??????xml??????
     */
    public void xmlGenerateFile(FormData formData) {
        List<FormField> defaultField = getDefaultField(formData.getFormDefinitionId());

        if (defaultField.size() > 0) {
            try {
                String fond_code = formData.get(ArchiveEntity.COLUMN_FOND_CODE);
                String archive_code = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
                // 1????????????????????????  base_info title="?????????????????????"
                Element rss = new Element("base_info");
                // 2????????????????????????
                rss.setAttribute("title", "?????????????????????");
                // 3???????????????document??????
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
                //4?????????????????????
                String targetPath = String.join(File.separator, filePath, "zipPack", fond_code, archive_code);
                //5???????????????
                String ofdPath = String.join(File.separator, filePath, "ofd", fond_code, archive_code);
                File file = new File(ofdPath);
                //??????fileLists????????????????????????????????????OFD??????
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
                                        file_code.setAttribute("title", "??????????????????");
                                        file_code.setText("A");

                                        Element file_standard_name = new Element("file_standard_name");
                                        file_standard_name.setAttribute("title", "????????????????????????");
                                        file_standard_name.setText(formData.getString(ArchiveEntity.COLUMN_TITLE));

                                        Element file_actual_name = new Element("file_actual_name");
                                        file_actual_name.setAttribute("title", "??????????????????");
                                        file_actual_name.setText(files[i].getName());

                                        Element format_information = new Element("format_information");
                                        format_information.setAttribute("title", "??????????????????");
                                        format_information.setText(files[i].getName().substring(files[i].getName().lastIndexOf(".") + 1));

                                        Element computer_file_name = new Element("computer_file_name");
                                        computer_file_name.setAttribute("title", "??????????????????????????????");
                                        computer_file_name.setText(files[i].getName().substring(0, files[i].getName().lastIndexOf(".")));

                                        Element computer_file_size = new Element("computer_file_size");
                                        computer_file_size.setAttribute("title", "?????????????????????????????????");
                                        computer_file_size.setText(files[i].length() + "");

                                        Element computer_file_creation_time = new Element("computer_file_creation_time");
                                        computer_file_creation_time.setAttribute("title", "?????????????????????????????????");
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
                                file_code.setAttribute("title", "??????????????????");
                                file_code.setText("A");

                                Element file_standard_name = new Element("file_standard_name");
                                file_standard_name.setAttribute("title", "????????????????????????");
                                file_standard_name.setText(formData.getString(ArchiveEntity.COLUMN_TITLE));

                                Element file_actual_name = new Element("file_actual_name");
                                file_actual_name.setAttribute("title", "??????????????????");
                                file_actual_name.setText(listFile.getName());

                                Element format_information = new Element("format_information");
                                format_information.setAttribute("title", "??????????????????");
                                format_information.setText(listFile.getName().substring(listFile.getName().lastIndexOf(".") + 1));

                                Element computer_file_name = new Element("computer_file_name");
                                computer_file_name.setAttribute("title", "??????????????????????????????");
                                computer_file_name.setText(listFile.getName().substring(0, listFile.getName().lastIndexOf(".")));

                                Element computer_file_size = new Element("computer_file_size");
                                computer_file_size.setAttribute("title", "?????????????????????????????????");
                                computer_file_size.setText(listFile.length() + "");

                                Element computer_file_creation_time = new Element("computer_file_creation_time");
                                computer_file_creation_time.setAttribute("title", "?????????????????????????????????");
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
                // ????????????Tab?????????
                format.setIndent("	");
                format.setEncoding("UTF-8");

                // 6?????????XMLOutputter?????????
                XMLOutputter outputer = new XMLOutputter(format);
                String property = System.getProperty("user.dir");
                // 7?????????outputer???document?????????xml??????
                File xmlFile = new File(targetPath+ File.separator + archive_code + ".xml");
                if (!xmlFile.exists()) {
                    //?????????????????????????????????
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
     * ?????????????????????xml???????????????????????????
     *
     * @param formData
     */
    public void packetXml(FormData formData) {
        try {
            String fond_code = formData.get(ArchiveEntity.COLUMN_FOND_CODE);
            String archive_code = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
            String targetPath = String.join(File.separator, filePath, "zipPack", fond_code, archive_code);
            String xmlPath = String.join(File.separator, targetPath,archive_code + ".xml");
            //??????????????????????????????????????????????????????xml
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
     * ??????????????????xml?????????????????????
     *
     * @param formData
     * @param formFields
     */
    public void packetXml(FormData formData, List<FormField> formFields) {
        try {
            String fond_code = formData.get(ArchiveEntity.COLUMN_FOND_CODE);
            String archive_code = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
            // 1????????????????????????  base_info title="?????????????????????"
            Element rss = new Element("base_info");
            // 2????????????????????????
            rss.setAttribute("title", "?????????????????????");
            // 3???????????????document??????
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
            //4?????????????????????
            String targetPath = String.join(File.separator, filePath, "zipPack", fond_code, archive_code);
            //5???????????????
            String ofdPath = String.join(File.separator, filePath, "ofd", fond_code, archive_code);
            File file = new File(ofdPath);
            //??????fileLists????????????????????????????????????OFD??????
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
                                    file_code.setAttribute("title", "??????????????????");
                                    file_code.setText("A");

                                    Element file_standard_name = new Element("file_standard_name");
                                    file_standard_name.setAttribute("title", "????????????????????????");
                                    file_standard_name.setText(formData.getString(ArchiveEntity.COLUMN_TITLE));

                                    Element file_actual_name = new Element("file_actual_name");
                                    file_actual_name.setAttribute("title", "??????????????????");
                                    file_actual_name.setText(files[i].getName());

                                    Element format_information = new Element("format_information");
                                    format_information.setAttribute("title", "??????????????????");
                                    format_information.setText(files[i].getName().substring(files[i].getName().lastIndexOf(".") + 1));

                                    Element computer_file_name = new Element("computer_file_name");
                                    computer_file_name.setAttribute("title", "??????????????????????????????");
                                    computer_file_name.setText(files[i].getName().substring(0, files[i].getName().lastIndexOf(".")));

                                    Element computer_file_size = new Element("computer_file_size");
                                    computer_file_size.setAttribute("title", "?????????????????????????????????");
                                    computer_file_size.setText(files[i].length() + "");

                                    Element computer_file_creation_time = new Element("computer_file_creation_time");
                                    computer_file_creation_time.setAttribute("title", "?????????????????????????????????");
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
                            file_code.setAttribute("title", "??????????????????");
                            file_code.setText("A");

                            Element file_standard_name = new Element("file_standard_name");
                            file_standard_name.setAttribute("title", "????????????????????????");
                            file_standard_name.setText(formData.getString(ArchiveEntity.COLUMN_TITLE));

                            Element file_actual_name = new Element("file_actual_name");
                            file_actual_name.setAttribute("title", "??????????????????");
                            file_actual_name.setText(listFile.getName());

                            Element format_information = new Element("format_information");
                            format_information.setAttribute("title", "??????????????????");
                            format_information.setText(listFile.getName().substring(listFile.getName().lastIndexOf(".") + 1));

                            Element computer_file_name = new Element("computer_file_name");
                            computer_file_name.setAttribute("title", "??????????????????????????????");
                            computer_file_name.setText(listFile.getName().substring(0, listFile.getName().lastIndexOf(".")));

                            Element computer_file_size = new Element("computer_file_size");
                            computer_file_size.setAttribute("title", "?????????????????????????????????");
                            computer_file_size.setText(listFile.length() + "");

                            Element computer_file_creation_time = new Element("computer_file_creation_time");
                            computer_file_creation_time.setAttribute("title", "?????????????????????????????????");
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
            // ????????????Tab?????????
            format.setIndent("	");
            format.setEncoding("UTF-8");

            // 6?????????XMLOutputter?????????
            XMLOutputter outputer = new XMLOutputter(format);
            String property = System.getProperty("user.dir");
            // 7?????????outputer???document?????????xml??????
            File xmlFile = new File(targetPath + File.separator + "?????????????????????.xml");
            if (!xmlFile.exists()) {
                //?????????????????????????????????
                xmlFile.getParentFile().mkdir();
            }
            OutputStream outputStream = new FileOutputStream(xmlFile);
            outputer.output(document, outputStream);
            outputStream.close();
            ZipUtil.toZip(targetPath, targetPath + ".zip", true);
            System.out.println("????????????");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("????????????");
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param formDefinitionId
     * @return
     */
    public List<FormField> getDefaultField(String formDefinitionId) {
        //?????????????????????oneMetadata
        Metadata oneMetadata = commonMapper.selectOneByQuery(SqlQuery.from(Metadata.class).equal(MetadataInfo.FORMDEFINITIONID, formDefinitionId));
        List<FormField> formFields = new ArrayList<>();
        //??????id???????????????id
        List<MataDataInfo> categoryMetadata = configManagerClient.getCategoryMetadata(oneMetadata.getCode(), oneMetadata.getClassify(), oneMetadata.getArrange(), oneMetadata.getBatch_id());
        if (categoryMetadata.size() > 0) {
            MataDataInfo mataDataInfo1 = categoryMetadata.get(0);
            List<FormField> fieldList = archiveFormDefinitionService.findFieldList(formDefinitionId);
            fieldList.forEach(item -> {
                item.setDescription(StatusEntity.STATUS_DISABLE_STR);
                item.setFieldCode(item.getFieldCode().toLowerCase());
            });
            //???????????????????????????
            List<String> collect1 = mataDataInfo1.getMetadata().stream().map(mataData -> mataData.geteName().toLowerCase()).collect(Collectors.toList());
            //????????????????????????????????????
            List<String> collect2 = mataDataInfo1.getMetadata().stream().map(mataData -> mataData.getName()).collect(Collectors.toList());
            //????????????????????????????????????????????????????????????????????????????????????????????????
            List<FormField> collect3 = fieldList.stream().filter(formField -> collect1.contains(formField.getFieldAliasStr())).collect(Collectors.toList());
            //?????????????????????????????????????????????
            List<FormField> collect4 = fieldList.stream().filter(formField -> collect1.contains(formField.getFieldCode().toLowerCase())).collect(Collectors.toList());
            //???????????????????????????????????????????????????
            List<FormField> collect5 = fieldList.stream().filter(formField -> collect2.contains(formField.getLabel())).collect(Collectors.toList());
            //??????
            collect5.addAll(collect4);
            collect5.addAll(collect3);
            //??????
            List<FormField> collect6 = collect5.stream().distinct().collect(Collectors.toList());
            formFields.addAll(collect6);
            Set<String> set = new HashSet<>();
            //?????????????????????????????????????????????
            for (FormField formField : collect6) {
                set.add(formField.getFieldCode());
                if (!StringUtils.isEmpty(formField.getFieldAliasStr())) {
                    set.add(formField.getFieldAliasStr());
                }
            }
            List<MataData> metadata = mataDataInfo1.getMetadata();
            //??????????????????
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
     * ??????xml??????
     *
     * @param formDataList
     * @param targetPath   ?????????????????????
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
     * ????????????????????????????????????????????????
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
                    relative = File.separator + "????????????";
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
     * ???List<FormData>???????????????List<Map<String, Object>>
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
     * ???????????????????????????????????????
     *
     * @param path            ????????????
     * @param ifCreateNewFile ??????????????????
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
     * ???????????????????????????????????????
     *
     * @param file
     * @return
     */
    List<String> fileLists = new ArrayList<>();

    public List<String> isDirectory(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                String fileName = file.getName().toUpperCase();
                // ???????????????
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
            System.out.println("??????????????????");
        }
        return fileLists;
    }

}
