package com.dr.digital.resultTest.service.impl;

import com.dr.digital.configManager.bo.*;
import com.dr.digital.configManager.service.ConfigManagerClient;
import com.dr.digital.configManager.service.FlowPathService;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.packet.service.PacketsDataService;
import com.dr.digital.packet.service.impl.PacketsDataServiceImpl;
import com.dr.digital.register.entity.Register;
import com.dr.digital.register.service.CategoryService;
import com.dr.digital.register.service.RegisterService;
import com.dr.digital.register.service.impl.CategoryServiceImpl;
import com.dr.digital.resultTest.entity.*;
import com.dr.digital.resultTest.service.ResultTestService;
import com.dr.digital.uploadfiles.entity.JpgQueue;
import com.dr.digital.uploadfiles.entity.JpgQueueInfo;
import com.dr.digital.uploadfiles.service.JpgQueueService;
import com.dr.digital.util.XmlUtil;
import com.dr.digital.util.jpgImageUtil;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.core.entity.FormField;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.form.core.service.FormDataService;
import com.dr.framework.common.service.DefaultBaseService;
import com.dr.framework.core.organise.entity.Organise;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.dr.framework.core.web.annotations.Current;
import org.jxls.common.Context;
import org.jxls.transform.Transformer;
import org.jxls.util.JxlsHelper;
import org.ofdrw.converter.ImageMaker;
import org.ofdrw.reader.OFDReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Mr.Zhu
 * @date 2022/8/18 - 16:49
 */
@Service
public class ResultTestServiceImpl extends DefaultBaseService<ResultTest> implements ResultTestService {
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    PacketsDataServiceImpl packetsDataServiceImpl;
    @Autowired
    RegisterService registerService;
    @Autowired
    FormDataService formDataService;
    @Autowired
    JpgQueueService jpgQueueService;
    @Autowired
    FlowPathService flowPathService;
    @Autowired
    ConfigManagerClient configManagerClient;
    @Autowired
    CategoryService categoryService;
    @Value("${filePath}")
    private String filePath;
    //?????????????????????
    ResultTestStatistics rs;

    //xml??????
    ExecutorService executor = Executors.newFixedThreadPool(1);
    //??????????????????
    ExecutorService executorResult = Executors.newFixedThreadPool(1);

    private String resultType0 = "????????????";
    private String resultType1 = "???????????????";
    private String resultType2 = "???????????????????????????";

    /**
     * ????????????
     *
     * @param registerId
     * @param person
     */
    @Override
    public void startResult(String registerId, @Current Person person) {
        String formDefinitionId = registerService.selectById(registerId).getFormDefinitionId();
        //????????????ofd???????????????????????????3?????????????????????????????????
        List<FormData> overDate = formDataService.selectFormData(formDefinitionId, (sqlQuery, wrapper) -> {
            sqlQuery.equal(wrapper.getColumn(ArchiveEntity.COLUMN_STATUS), "OVER")
                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), "3")
//                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_PACKET_STATE),0)
                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_QUALITY_STATE), "0")
//                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_QUALITY_CONDUCT_STATE),"0")
                    .orderBy(wrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
        //??????????????????
        if (overDate.size() > 0) {
            commonMapper.deleteByQuery(SqlQuery.from(ResultMessage.class)
                    .equal(ResultMessageInfo.REGISTERID, registerId));
        }
        //??????????????????????????????????????????????????????
//        formDataService.updateFormDataBySqlBuilder(formDefinitionId,(sqlQuery, wrapper) -> {
//            sqlQuery.equal(wrapper.getColumn(ArchiveEntity.COLUMN_STATUS), "OVER")
//                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), "3")
////                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_PACKET_STATE),0)
//                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_QUALITY_STATE), "0")
//                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_QUALITY_CONDUCT_STATE),"0")
//                    .set(wrapper.getColumn(ArchiveEntity.COLUMN_QUALITY_CONDUCT_STATE),"1");});
        //???????????????
        rs = new ResultTestStatistics();
        rs.setRegisterId(registerId);
        rs.setFormDefinitionId(formDefinitionId);
        //?????????????????????????????????
        SqlQuery<ResultTestStatistics> sqlQuery = SqlQuery.from(ResultTestStatistics.class)
                .equal(ResultTestStatisticsInfo.REGISTERID, registerId)
                .equal(ResultTestStatisticsInfo.FORMDEFINITIONID, formDefinitionId);
        ResultTestStatistics resultTestStatistics = commonMapper.selectOneByQuery(sqlQuery);
        //???over?????????????????????
        for (FormData formData : overDate){
            executorResult.execute(()->{
                        resultContent(formData,registerId,formDefinitionId,person);
                if (resultTestStatistics != null) {
                    rs.setId(resultTestStatistics.getId());
                    commonMapper.updateById(rs);
                } else {
                    rs.setId(UUID.randomUUID().toString());
                    commonMapper.insert(rs);
                }
                    }

            );

        }
    }

    /**
     * ??????????????????
     * @param formData
     */
    public void resultContent(FormData formData,String registerId,String formDefinitionId,Person person){

            StringBuffer sb = new StringBuffer();
            if (!StringUtils.isEmpty(formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE))) {
                //ofd???????????????
                ofdToImg(registerId, formDefinitionId, formData);
                List<YuanWenJpg> yuanWenJpgs = commonMapper.selectByQuery(SqlQuery.from(YuanWenJpg.class)
                        .equal(YuanWenJpgInfo.FORMDEFINITIONID, formDefinitionId)
                        .equal(YuanWenJpgInfo.ARCHIVECODE, formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE).toString())
                        .orderBy(YuanWenJpgInfo.FILENAME));

                if (yuanWenJpgs.size() > 0) {
                    for (int i = 0; i < yuanWenJpgs.size(); i++) {
                        //????????????
                        sb.append(yuanWenTest(yuanWenJpgs.get(i),  formDefinitionId,
                                registerId, formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE), resultType0));
                    }
                } else {
                    sb.append("???????????????????????????????????????????????????,?????????ofd??????????????????,");
                    resultMessageRecord(formDefinitionId, registerId, formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE),
                            resultType0, "??????",
                            "????????????????????????,?????????ofd??????????????????");
                    rs.setYuanWenFileYsTest(rs.getYuanWenFileYsTest() + 1);
                    rs.setYuanWenRgbTest(rs.getYuanWenRgbTest()+1);
                    rs.setYuanWenFileNameTest(rs.getYuanWenFileNameTest()+1);
                    rs.setYuanWenFilePowerTest(rs.getYuanWenFilePowerTest()+1);
                    rs.setYuanWenDpiTest(rs.getYuanWenDpiTest()+1);
                    rs.setYuanWenFormatTest(rs.getYuanWenFormatTest()+1);
                    rs.setYuanWenArchiveCodeTest(rs.getYuanWenArchiveCodeTest()+1);
                }
                if (sb.length() > 0) {
                    String type = exitFlowPath(formDefinitionId, LinkFlowPath.PROCESSING);
                    formData.put(ArchiveEntity.COLUMN_EXIT_FLOW_PATH, type);
                }
                //?????????????????????xml?????????map  ?????????fondCode 0032
                String xmlPath = String.join(File.separator, filePath, "zipPack", formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + ".xml");
                Map<String, String> map = XmlUtil.xmlParsing(xmlPath);
                if (map.size() > 0) {
                    Metadata metadata = commonMapper.selectOneByQuery(SqlQuery.from(Metadata.class).equal(MetadataInfo.FORMDEFINITIONID, formDefinitionId));
                    //????????????????????????
                    List<MetadataRuleTest> metadataRuleTest = configManagerClient.getMetadataRuleTest(metadata.getCode(), metadata.getClassify(), metadata.getArrange());
                    if (metadataRuleTest.size() > 0) {
                        for (MetadataRuleTest ruleTest : metadataRuleTest) {
                            sb.append(metaMethod(ruleTest, formDefinitionId, map, registerId,
                                    formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE), resultType1));
                        }
                        if (StringUtils.isEmpty(formData.get(ArchiveEntity.COLUMN_EXIT_FLOW_PATH)) && sb.length() > 0) {
                            String type = exitFlowPath(formDefinitionId, LinkFlowPath.VOLUMES);
                            formData.put(ArchiveEntity.COLUMN_EXIT_FLOW_PATH, type);
                        }
                    }
                    //?????????????????????
                    List<MataDataInfo> categoryMetadata = configManagerClient.getCategoryMetadata(metadata.getBatch_id());
                    if (categoryMetadata.size() > 0) {
                        List<MataData> mataDatas = categoryMetadata.get(0).getMetadata();
                        for (MataData mataData : mataDatas) {
                            sb.append(detectionMeta(mataData, formDefinitionId, map, registerId,
                                    formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE), resultType1));
                        }
                        if (StringUtils.isEmpty(formData.get(ArchiveEntity.COLUMN_EXIT_FLOW_PATH)) && sb.length() > 0) {
                            String type = exitFlowPath(formDefinitionId, LinkFlowPath.VOLUMES);
                            formData.put(ArchiveEntity.COLUMN_EXIT_FLOW_PATH, type);
                        }
                    } else {
                        sb.append("????????????????????????????????????,");
                        resultMessageRecord(formDefinitionId, registerId, formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE),
                                resultType1, "?????????",
                                "????????????????????????????????????");
                    }
                    //????????????????????????
                    if (yuanWenJpgs.size() > 0) {
                        sb.append(yuanWenMetadataContrast(yuanWenJpgs.get(0), yuanWenJpgs.size(), map, formDefinitionId, registerId,
                                formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE), resultType2));
                    } else {
                        sb.append("?????????????????????:??????????????????,");
                        resultMessageRecord(formDefinitionId, registerId, formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE),
                                resultType2, "?????????????????????",
                                "??????????????????");
                    }
                    if (StringUtils.isEmpty(formData.get(ArchiveEntity.COLUMN_EXIT_FLOW_PATH)) && sb.length() > 0) {
                        String type = exitFlowPath(formDefinitionId, LinkFlowPath.RECEIVE);
                        formData.put(ArchiveEntity.COLUMN_EXIT_FLOW_PATH, type);
                    }

                } else {
                    sb.append("xml???????????????????????????????????????,");
                    resultMessageRecord(formDefinitionId, registerId, formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE),
                            resultType1, "xml??????",
                            "xml???????????????????????????????????????");
                }
            } else {
                sb.append("????????????????????????");
                resultMessageRecord(formDefinitionId, registerId, "",
                        resultType1, "??????", "????????????");
                //????????????????????? ????????????????????????????????????+1
                rs.setYuanWenArchiveCodeTest(rs.getYuanWenArchiveCodeTest() + 1);
                rs.setYuanWenFormatTest(rs.getYuanWenFormatTest() + 1);
                rs.setYuanWenDpiTest(rs.getYuanWenDpiTest() + 1);
                rs.setYuanWenRgbTest(rs.getYuanWenRgbTest() + 1);
                rs.setYuanWenFilePowerTest(rs.getYuanWenFilePowerTest() + 1);
                rs.setYuanWenFileNameTest(rs.getYuanWenFileNameTest() + 1);
                rs.setYuanWenFileYsTest(rs.getYuanWenFileYsTest() + 1);

                rs.setMetadataRepeatabilityTest(rs.getMetadataRepeatabilityTest() + 1);//???????????????
                rs.setMetadataRequireTest(rs.getMetadataRequireTest() + 1);//????????????
                rs.setMetadataTypeTest(rs.getMetadataTypeTest() + 1);//????????????
                rs.setMetadataLengthTest(rs.getMetadataLengthTest() + 1);//???????????????
                rs.setMetadataValRangeTest(rs.getMetadataValRangeTest() + 1);//???????????????
                rs.setMetadataValContentTest(rs.getMetadataValContentTest() + 1);//????????????
                rs.setMetadataDisByteTest(rs.getMetadataDisByteTest() + 1);//???????????????
                rs.setMetadataComplexTest(rs.getMetadataComplexTest() + 1);//???????????????

                rs.setComparisonArchiveCodeTest(rs.getComparisonArchiveCodeTest() + 1);
                rs.setComparisonFondCodeTest(rs.getComparisonFondCodeTest() + 1);
                rs.setComparisonFileYsTest(rs.getComparisonFileYsTest() + 1);

            }
            //??????????????? ????????????????????????
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
                //?????????????????????,?????????????????????,??????????????????
                formData.put(ArchiveEntity.COLUMN_QUALITY_STATE, "0");
            } else {
                //??????????????????,??????????????????,??????????????????
                formData.put(ArchiveEntity.COLUMN_QUALITY_STATE, "1");
            }
            if (person != null) {
                if (StringUtils.isEmpty(formData.getString("updateDate"))) {
                    formData.put("updatePerson", person.getId());
                    formData.put("updateDate", System.currentTimeMillis());
                }
            }
//            formData.put(ArchiveEntity.COLUMN_QUALITY_CONDUCT_STATE,"2");
            formDataService.updateFormDataById(formData);
    }
    /**
     * ??????????????????
     *
     * @param registerId
     */
    @Override
    public void resetResultTest(String registerId) {
        String formDefinitionId = registerService.selectById(registerId).getFormDefinitionId();
        //?????? ??????????????????
        //??????
        String archiveCodeRule = "????????????,??????????????????,????????????????????????????????????";
        yuanWenResultDefault(formDefinitionId, registerId, archiveCodeRule, "??????", "/", 1);

        //??????
        String formatRule = "????????????,?????????????????????????????????????????????";
        yuanWenResultDefault(formDefinitionId, registerId, formatRule, "??????", "/", 2);

        //dpi
        String dpiRule = "??????dpi,???????????????????????????";
        yuanWenResultDefault(formDefinitionId, registerId, dpiRule, "dpi", "300", 3);

        //??????
        String rgbRule = "????????????,???????????????";
        yuanWenResultDefault(formDefinitionId, registerId, rgbRule, "rgbRule", "/", 4);

        //?????????
        String filePowerRule = "???????????????,???????????????????????????";
        yuanWenResultDefault(formDefinitionId, registerId, filePowerRule, "?????????", "/", 5);

        //????????????
        String fileNameRule = "???????????????,??????????????????????????????";
        yuanWenResultDefault(formDefinitionId, registerId, fileNameRule, "????????????", "/", 6);

        //?????????????????????????????????
        metadataDefault(formDefinitionId, registerId);

        //?????? ?????????????????????????????????
        //????????????
        String comparisonArchiveCodeRule = "?????????????????????,??????????????????";
        yuanWenMetadataDefault(formDefinitionId, registerId, comparisonArchiveCodeRule, "??????", 1);
        //????????????
        String comparisonFondCodeRule = "?????????????????????,??????????????????";
        yuanWenMetadataDefault(formDefinitionId, registerId, comparisonFondCodeRule, "??????", 2);
        //????????????
        String comparisonFileYsRule = "?????????????????????,??????????????????";
        yuanWenMetadataDefault(formDefinitionId, registerId, comparisonFileYsRule, "??????", 3);


    }

    /**
     * ????????????????????????
     *
     * @param registerId
     * @return
     */
    @Override
    public List<Map> getResultMessage(String registerId) {
        List<ResultMessage> resultMessages = commonMapper.selectByQuery(SqlQuery.from(ResultMessage.class)
                .equal(ResultMessageInfo.REGISTERID, registerId)
                .orderBy(ResultMessageInfo.ARCHIVECODE)
                .orderBy(ResultMessageInfo.CREATEDATE));
        List<Map> list = new ArrayList();
        for (ResultMessage resultMessage : resultMessages) {
            Map map = new HashMap();
            map.put("archival_code", resultMessage.getArchiveCode());
            map.put("resultType", resultMessage.getResultType());
            //?????????????????????????????????
            map.put("resultElement", resultMessage.getResultElementName());
            map.put("resultMessage", resultMessage.getResultMessage());
            list.add(map);
        }
        return list;
    }

    @Override
    public List<ResultTestStatistics> resultStatistics(String registerId) {
        String formDefinitionId = registerService.selectById(registerId).getFormDefinitionId();
        SqlQuery<ResultTestStatistics> sqlQuery = SqlQuery.from(ResultTestStatistics.class)
                .equal(ResultTestStatisticsInfo.REGISTERID, registerId)
                .equal(ResultTestStatisticsInfo.FORMDEFINITIONID, formDefinitionId);
        List<ResultTestStatistics> resultTestStatistics = commonMapper.selectByQuery(sqlQuery);
        return resultTestStatistics;
    }

    @Override
    public List<ResultTest> detectionDateInit(String registerId) {
        List<ResultTest> resultTests = commonMapper.selectByQuery(SqlQuery.from(ResultTest.class)
                .equal(ResultTestInfo.REGISTERID, registerId)
                .orderBy(ResultTestInfo.ORDERBY)
                .orderBy(ResultTestInfo.CREATEDATE));

        return resultTests;
    }

    @Override
    public Map percentageInit(String registerId) {
        String formDefinitionId = registerService.selectById(registerId).getFormDefinitionId();
        Long totalFen = formDataService.countId(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
            sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), LinkFlowPath.OVER)
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), "3");
        });
        SqlQuery<ResultMessage> resultMessageSqlQuery = SqlQuery.from(ResultMessage.class)
                .equal(ResultMessageInfo.REGISTERID, registerId)
                .groupBy(ResultMessageInfo.ARCHIVECODE);
        Long completeFen = Long.valueOf(commonMapper.selectByQuery(resultMessageSqlQuery).size());
        float percent;
        if (totalFen != 0L && completeFen != 0L) {
            if (totalFen == completeFen) {
                percent = 100;
            } else {
                double l = completeFen.doubleValue() / totalFen.doubleValue();
                percent = (int) (l * 100);
            }
        } else {
            percent = 0;
        }
        if(totalFen<completeFen){
            completeFen = totalFen;
        }
        Map map = new HashMap();
        map.put("percent", percent);
        map.put("totalFen", totalFen);
        map.put("completeFen", completeFen);
        return map;
    }

    /**
     * ??????????????????????????????
     */
    private void yuanWenResultDefault(String formDefinitionId, String registerId,
                                      String rules, String fieldName, String preset, int order) {
        ResultTest resultTest = selectOne(SqlQuery.from(ResultTest.class)
                .equal(ResultTestInfo.REGISTERID, registerId)
                .equal(ResultTestInfo.RESULTTYPE, "1")
                .equal(ResultTestInfo.ORDERBY, order));
        if (resultTest == null) {
            resultTest = new ResultTest();
        }
        resultTest.setResultType("1");
        resultTest.setOrder(order);
        resultTest.setFieldName(fieldName);
        resultTest.setFormDefinitionId(formDefinitionId);
        resultTest.setRegisterId(registerId);
        resultTest.setRules(rules);
        resultTest.setPreset(preset);

        if (StringUtils.isEmpty(resultTest.getId())) {
            this.insert(resultTest);
        } else {
            this.updateById(resultTest);
        }
    }

    /**
     * ???????????????????????????
     */
    private void metadataDefault(String formDefinitionId, String registerId) {
        //?????????????????????????????????????????????
        SqlQuery<ResultTest> ResultTestSql = SqlQuery.from(ResultTest.class)
                .equal(ResultTestInfo.REGISTERID, registerId)
                .equal(ResultTestInfo.RESULTTYPE, "2").or().equal(ResultTestInfo.RESULTTYPE, "4");
        commonMapper.deleteByQuery(ResultTestSql);

        //???????????????????????? map???key???authors  value:??????
        Map<String, String> map = new HashMap<>();
        //
        List<FormField> defaultField = packetsDataServiceImpl.getDefaultField(formDefinitionId);
        for (FormField formField : defaultField) {
            map.put(formField.getFieldCode(), formField.getLabel());
        }
        //???????????????????????????????????????id,????????????????????????????????????
        Metadata metadata = commonMapper.selectOneByQuery(SqlQuery.from(Metadata.class).equal(MetadataInfo.FORMDEFINITIONID, formDefinitionId));

        //????????????????????????
        List<MetadataRuleTest> metadataRuleTest = configManagerClient.getMetadataRuleTest(metadata.getCode(), metadata.getClassify(), metadata.getArrange());
        if (metadataRuleTest.size() > 0) {
            for (MetadataRuleTest ruleTest : metadataRuleTest) {
                List<TestRule> metadataRules = ruleTest.getMetadataRuleTest();

                if (metadataRules.size() > 0) {
                    for (TestRule metadataRule : metadataRules) {
                        //???????????????authors
                        String metadataField = metadataRule.getMetadata();
                        //??????map???????????????????????????????????????????????????????????????
                        if (map.containsKey(metadataField)) {
                            StringBuffer sb = new StringBuffer();
                            List<TestRule.Rules> rules = metadataRule.getRules();
                            for (TestRule.Rules rule : rules) {
                                //????????????????????????
                                if (rule.getType().equals("MaxMinVal")) {
                                    for (TestRule.Conditions condition : rule.getConditions()) {
                                        String conditionsField = condition.getMetadata();
                                        //????????????????????????????????????????????????????????????????????????
                                        sb.append(map.get(conditionsField) + condition.getCondition() + "??????" + condition.getVal() + "???,");
                                    }
                                    int maxVal = Integer.parseInt(rule.getMaxVal());
                                    int minVal = Integer.parseInt(rule.getMinVal());
                                    sb.append("???????????????????????????" + map.get(metadataField) + "???????????????" + minVal + "???" + maxVal + "??????,");
                                    //rules??????conditions?????????

                                    //??????
                                } else if (rule.getType().equals("codomain")) {
                                    for (TestRule.Conditions condition : rule.getConditions()) {
                                        String conditionsField = condition.getMetadata();
                                        sb.append(map.get(conditionsField) + condition.getCondition() + "??????" + condition.getVal() + "???,");
                                    }
                                    sb.append("???????????????????????????" + map.get(metadataField) + "??????" + rule.getCondition() + rule.getVal() + ",");
                                }
                                //????????????????????????
                                else if (rule.getType().equals("MaxMinLen")) {
                                    for (TestRule.Conditions condition : rule.getConditions()) {
                                        String conditionsField = condition.getMetadata();
                                        sb.append(map.get(conditionsField) + condition.getCondition() + "??????" + condition.getVal() + "???,");
                                    }
                                    int maxLen = Integer.parseInt(rule.getMaxLen());
                                    int minLen = Integer.parseInt(rule.getMinLen());
                                    sb.append("???????????????????????????" + map.get(metadataField) + "????????????????????????" + minLen + "???" + maxLen + "??????,");
                                }
//                                ????????????
                                else if (rule.getType().equals("disByte")) {
                                    for (TestRule.Conditions condition : rule.getConditions()) {
                                        String conditionsField = condition.getMetadata();
                                        sb.append(map.get(conditionsField) + condition.getCondition() + "??????" + condition.getVal() + "???,");
                                    }
                                    sb.append("???????????????????????????" + map.get(metadataField) + "??????" + rule.getCondition() + rule.getVal() + ",");

                                }
                                //????????????
                                else if (rule.getType().equals("type")) {
                                    for (TestRule.Conditions condition : rule.getConditions()) {
                                        String conditionsField = condition.getMetadata();
                                        sb.append(map.get(conditionsField) + condition.getCondition() + "??????" + condition.getVal() + "???,");
                                    }
                                    sb.append(map.get(metadataField) + "???????????????" + rule.getCondition() + rule.getVal() + ",");
                                }
                                //??????
                                else if (rule.getType().equals("constraints")) {
                                    for (TestRule.Conditions condition : rule.getConditions()) {
                                        String conditionsField = condition.getMetadata();
                                        sb.append(map.get(conditionsField) + condition.getCondition() + "??????" + condition.getVal() + "???,");
                                    }
                                    sb.append("???????????????????????????" + map.get(metadataField) + "????????????");
                                }

                            }
                            ResultTest resultTest = new ResultTest();
                            resultTest.setResultType("4");
                            resultTest.setOrder(0);
                            resultTest.setFormDefinitionId(formDefinitionId);
                            resultTest.setRegisterId(registerId);
                            resultTest.setRules(sb.toString());
                            resultTest.setFieldName(map.get(metadataField));
                            resultTest.setCharacterType("");
                            resultTest.setRepeatability(false);
                            resultTest.setRequired(false);
                            this.insert(resultTest);
                        }
                    }
                }

            }

        }

        //????????????????????????????????????????????????????????????????????????????????????????????????????????????
        List<MataDataInfo> categoryMetadata = configManagerClient.getCategoryMetadata(metadata.getBatch_id());
        if (categoryMetadata.size() > 0) {
            List<MataData> mataDates = categoryMetadata.get(0).getMetadata();
            for (MataData mataData : mataDates) {
                StringBuffer rules = new StringBuffer();
                String characterType = "";
                boolean isRepeatability = false;
                boolean isRequired = false;
                //???????????????????????????????????????,??????????????????????????????
                if (map.containsKey(mataData.geteName())) {

                    //????????????????????? ????????????
                    String name = mataData.getName();
                    //???????????????????????????????????????
                    if (!StringUtils.isEmpty(mataData.getConstraints())) {
                        //constraints?????????1????????? 2?????? 3??????
                        if (mataData.getConstraints().equals("1")) {
                            rules.append(name + "???????????????,");
                            isRequired = true;
                        } else if (mataData.getConstraints().equals("2")) {
                            rules.append(name + "???????????????,");
                        } else if (mataData.getConstraints().equals("3")) {
                            rules.append(name + "???????????????,");
                        }
                    }
                    //repeats????????? 1???????????? 2???????????????
                    if (!StringUtils.isEmpty(mataData.getRepeats())) {
                        if (mataData.getRepeats().equals("1")) {
                            rules.append("?????????,");
                            isRepeatability = true;
                        } else if (mataData.getRepeats().equals("2")) {
                            rules.append("????????????,");
                        }
                    }
                    //????????????
                    if (!StringUtils.isEmpty(mataData.getType())) {
                        //type 1???int 2????????? 3?????????
                        if (mataData.getType().equals("1")) {
                            rules.append(name + "????????????????????????,");
                            characterType = "1";
                        } else if (mataData.getType().equals("2")) {
                            rules.append(name + "????????????????????????,");
                            characterType = "2";
                        } else if (mataData.getType().equals("3")) {
                            rules.append(name + "????????????????????????,");
                            characterType = "3";
                        }
                    }
                    //?????????
                    if (!StringUtils.isEmpty(mataData.getMaxVal()) && !StringUtils.isEmpty(mataData.getMinVal())) {
                        rules.append("?????????" + mataData.getMinLen() + mataData.getMaxVal() + "??????,");
                    }
                    //?????????
                    if (mataData.getMinLen() != 0 && mataData.getMinLen() != 0) {
                        rules.append("???????????????" + mataData.getMinLen() + mataData.getMaxLen() + "??????,");
                    }
                    //??????
                    if (!StringUtils.isEmpty(mataData.getCodomain())) {
                        rules.append("???????????????" + mataData.getCodomain() + ",");
                    }
                    //????????????
                    if (!StringUtils.isEmpty(mataData.getDisByte())) {
                        rules.append("??????????????????" + mataData.getCodomain() + ",");
                    }
                    if (rules.length() > 0) {
                        rules.deleteCharAt(rules.length() - 1);
                    }
                    ResultTest resultTest = new ResultTest();
                    resultTest.setResultType("2");
                    int order = -1;
                    if (!StringUtils.isEmpty(mataData.getOrders())) {
                        order = Integer.parseInt(mataData.getOrders());
                    }
                    resultTest.setOrder(order);
                    resultTest.setFormDefinitionId(formDefinitionId);
                    resultTest.setRegisterId(registerId);
                    resultTest.setRules(rules.toString());
                    resultTest.setFieldName(name);
                    resultTest.setCharacterType(characterType);
                    resultTest.setRepeatability(isRepeatability);
                    resultTest.setRequired(isRequired);
                    this.insert(resultTest);

                }
            }

        }


    }

    /**
     * ???????????????????????????????????????
     */
    private void yuanWenMetadataDefault(String formDefinitionId, String registerId, String rules, String fieldName, int order) {
        ResultTest resultTest = selectOne(SqlQuery.from(ResultTest.class)
                .equal(ResultTestInfo.REGISTERID, registerId)
                .equal(ResultTestInfo.RESULTTYPE, "3")
                .equal(ResultTestInfo.ORDERBY, order));
        if (resultTest == null) {
            resultTest = new ResultTest();
        }
        resultTest.setResultType("3");
        resultTest.setOrder(order);
        resultTest.setFormDefinitionId(formDefinitionId);
        resultTest.setRegisterId(registerId);
        resultTest.setFieldName(fieldName);
        resultTest.setRules(rules);
        if (StringUtils.isEmpty(resultTest.getId())) {
            this.insert(resultTest);
        } else {
            this.updateById(resultTest);
        }
    }

    /**
     * @param formDefinitionId
     * @param exit             ??????????????????,??????????????????
     * @return
     */
    private String exitFlowPath(String formDefinitionId, String exit) {
        ResultEntity resultEntity = flowPathService.flowPathAll(formDefinitionId);

        //???????????????????????????,??????????????????????????????
        if (resultEntity.isSuccess()) {
            //????????????????????????????????????,???????????????????????????????????????????????????
            List data = Collections.singletonList(resultEntity.getData());
            //???????????????????????????????????????,????????????,?????????
            String[] arr = (String[]) data.get(0);
            if (Arrays.asList(arr).indexOf(exit) != -1) {
                return exit;
            } else {
                //????????????exit???link???????????????,????????????,??????????????????????????????
                for (int i = 0; i < LinkFlowPath.LinkFlowPath.length; i++) {
                    if (LinkFlowPath.LinkFlowPath[i].equals(exit)) {
                        for (int j = 0; j < i + 1; j++) {
                            if (Arrays.asList(arr).indexOf(LinkFlowPath.LinkFlowPath[i - j]) != -1) {
                                return LinkFlowPath.LinkFlowPath[i - j];
                            }
                        }
                        break;
                    }
                }

            }
        }
        //??????????????????,????????????????????????
        return LinkFlowPath.RECEIVE;

    }

    /**
     * ????????????
     *
     * @param yuanWenJpg
     * @return
     */
    private String yuanWenTest(YuanWenJpg yuanWenJpg, String formDefinitionId, String registerId, String archiveCode, String resultType) {
        StringBuffer sb = new StringBuffer();
        //??????tif?????????dpi??????????????????????????????
        //dpi??????
        if (!StringUtils.isEmpty(yuanWenJpg.getFileDpi())) {
            ResultTest resultTest = commonMapper.selectOneByQuery(SqlQuery.from(ResultTest.class)
                    .equal(ResultTestInfo.FORMDEFINITIONID, yuanWenJpg.getFormDefinitionId())
                    .equal(ResultTestInfo.FIELDNAME, "dpi"));
            //?????????
            if (resultTest == null || StringUtils.isEmpty(resultTest.getPreset())) {
                sb.append(yuanWenJpg.getFileType()+"??????" +yuanWenJpg.getImgYsNo()+ "?????????dpi???????????????,");
                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                        resultType, "dpi",
                        yuanWenJpg.getFileType()+"??????" +yuanWenJpg.getImgYsNo()+ "?????????dpi???????????????,");
                rs.setYuanWenDpiTest(rs.getYuanWenDpiTest() + 1);
            } else {
                String[] split = yuanWenJpg.getFileDpi().split(",");
                if (split.length > 0) {
                    int dpiHeight = Integer.parseInt(split[0]);
                    int dpiWidth = Integer.parseInt(split[1]);
                    if (dpiHeight < Integer.parseInt(resultTest.getPreset()) || dpiWidth < Integer.parseInt(resultTest.getPreset())) {
                        sb.append(yuanWenJpg.getFileType()+"??????" +yuanWenJpg.getImgYsNo()+ "?????????dpi???????????????,");
                        resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                resultType, "dpi",
                                yuanWenJpg.getFileType()+"??????" +yuanWenJpg.getImgYsNo()+ "?????????dpi???????????????,");
                        rs.setYuanWenDpiTest(rs.getYuanWenDpiTest() + 1);
                    }
                }

            }
        } else {
            sb.append(yuanWenJpg.getFileType()+"??????" +yuanWenJpg.getImgYsNo()+ "?????????????????????dpi,");
            resultMessageRecord(formDefinitionId, registerId, archiveCode,
                    resultType, "dpi",
                    yuanWenJpg.getFileType()+"??????" +yuanWenJpg.getImgYsNo()+ "?????????????????????dpi,");
            rs.setYuanWenDpiTest(rs.getYuanWenDpiTest() + 1);
        }

        //???????????????
        if (StringUtils.isEmpty(yuanWenJpg.getFilePower())) {
            sb.append(yuanWenJpg.getFileType()+"??????" +yuanWenJpg.getImgYsNo()+ "??????????????????????????????,");
            resultMessageRecord(formDefinitionId, registerId, archiveCode,
                    resultType, "?????????",
                    yuanWenJpg.getFileType()+"??????" +yuanWenJpg.getImgYsNo()+ "??????????????????????????????,");
            rs.setYuanWenFilePowerTest(rs.getYuanWenFilePowerTest() + 1);
        }
        //????????????
        if (!StringUtils.isEmpty(yuanWenJpg.getFileRGB())) {
            if (yuanWenJpg.getFileRGB().equals("??????")) {
                sb.append(yuanWenJpg.getFileType()+"??????" +yuanWenJpg.getImgYsNo()+ "??????????????????,");
                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                        resultType, "??????",
                        yuanWenJpg.getFileType()+"??????" +yuanWenJpg.getImgYsNo()+ "??????????????????,");
                rs.setYuanWenRgbTest(rs.getYuanWenRgbTest() + 1);
            }
        } else {
            sb.append(yuanWenJpg.getFileType()+"??????" +yuanWenJpg.getImgYsNo()+ "???????????????????????????,");
            resultMessageRecord(formDefinitionId, registerId, archiveCode,
                    resultType, "??????",
                    yuanWenJpg.getFileType()+"??????" +yuanWenJpg.getImgYsNo()+ "???????????????????????????,");
            rs.setYuanWenRgbTest(rs.getYuanWenRgbTest() + 1);
        }
        //??????????????????
        if (StringUtils.isEmpty(yuanWenJpg.getFileName())) {
            sb.append(yuanWenJpg.getFileType()+"??????" +yuanWenJpg.getImgYsNo()+  "??????????????????????????????,");
            resultMessageRecord(formDefinitionId, registerId, archiveCode,
                    resultType, "????????????",
                    yuanWenJpg.getFileType()+"??????" +yuanWenJpg.getImgYsNo()+  "??????????????????????????????,");
            rs.setYuanWenFileNameTest(rs.getYuanWenFileNameTest() + 1);
        }
        return sb.toString();
    }

    /**
     * ???????????????????????????????????????
     *
     * @param map
     * @return
     */
    private String detectionMeta(MataData mataData, String formDefinitionId, Map<String, String> map, String registerId,
                                 String archiveCode, String resultType) {
        StringBuffer sb = new StringBuffer();
        //?????????????????????????????????,??????xml?????????????????????????????????,???????????????
        if (map.containsKey(mataData.geteName())) {
            String name = mataData.getName();
            //constraints?????????1????????? 2?????? 3??????
            if (!StringUtils.isEmpty(mataData.getConstraints())) {
                if (mataData.getConstraints().equals("1") && map.get(mataData.geteName().toLowerCase()) == null) {
                    sb.append(name + "????????????,");
                    resultMessageRecord(formDefinitionId, registerId, archiveCode,
                            resultType, name,
                            name + "????????????");
                    rs.setMetadataRequireTest(rs.getMetadataRequireTest() + 1);
                }
            }
//            repeats????????? 1???????????? 2???????????????
            if (!StringUtils.isEmpty(mataData.getRepeats())) {
                if (mataData.getRepeats().equals("2")) {
                    //?????????id?????????????????????????????????
                    if (map.get(mataData.geteName().toLowerCase()) != null) {
                        Long aLong = formDataService.countId(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
                            sqlQuery.equal(formRelationWrapper.getColumn(mataData.geteName()), map.get(mataData.geteName()));
                        });
                        if (aLong > 1) {
                            sb.append(name + "????????????,");
                            resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                    resultType, name,
                                    name + "????????????");
                            rs.setMetadataRepeatabilityTest(rs.getMetadataRepeatabilityTest() + 1);
                        }
                    }
                }
            }
            if (!StringUtils.isEmpty(mataData.getType())) {
                //type 1???int 2????????? 3?????????
                if (mataData.getType().equals("1")) {
                    if (map.get(mataData.geteName().toLowerCase()) != null) {
                        if (!org.apache.commons.lang3.StringUtils.isNumeric(map.get(mataData.geteName()))) {
                            sb.append(name + "????????????????????????,");
                            resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                    resultType, name,
                                    name + "????????????????????????");
                            rs.setMetadataTypeTest(rs.getMetadataTypeTest() + 1);
                        }
                    }

                } else if (mataData.getType().equals("2")) {
                    if (map.get(mataData.geteName().toLowerCase()) != null) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                        try {
                            dateFormat.parse(map.get(mataData.geteName().toLowerCase()));
                        } catch (Exception e) {
                            sb.append(name + "????????????????????????,");
                            resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                    resultType, name,
                                    name + "????????????????????????");
                            rs.setMetadataTypeTest(rs.getMetadataTypeTest() + 1);

                        }
                    }
                } else if (mataData.getType().equals("3")) {
                    if (map.get(mataData.geteName().toLowerCase()) != null) {
                        if (!(map.get(mataData.geteName().toLowerCase()) instanceof String)) {
                            sb.append(name + "????????????????????????,");
                            resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                    resultType, name,
                                    name + "????????????????????????");
                            rs.setMetadataTypeTest(rs.getMetadataTypeTest() + 1);
                        }
                    }
                }
            }
            //?????????????????????????????????
            if (mataData.getMaxLen() != 0 && mataData.getMinLen() != 0) {
                int valLength = 0;
                if (!StringUtils.isEmpty(map.get(mataData.geteName()))) {
                    valLength = map.get(mataData.geteName()).length();
                }
                if (valLength < mataData.getMinLen() || valLength > mataData.getMaxLen()) {
                    sb.append(name + "??????????????????" + mataData.getMinLen() + "???" + mataData.getMaxLen() + "??????");
                    resultMessageRecord(formDefinitionId, registerId, archiveCode,
                            resultType, name,
                            name + "??????????????????" + mataData.getMinLen() + "???" + mataData.getMaxLen() + "??????");
                    rs.setMetadataLengthTest(rs.getMetadataLengthTest() + 1);
                }
            }
            //???????????????????????????
            if (!StringUtils.isEmpty(mataData.getMaxVal()) && !StringUtils.isEmpty(mataData.getMinLen())) {
                int max = Integer.parseInt(mataData.getMaxVal());
                int min = Integer.parseInt(mataData.getMinVal());
                int val = Integer.parseInt(map.get(mataData.geteName()));
                if (val < min || val > max) {
                    sb.append(name + "???????????????" + min + "???" + max + "??????,");
                    resultMessageRecord(formDefinitionId, registerId, archiveCode,
                            resultType, name,
                            name + "???????????????" + min + "???" + max + "??????");
                    rs.setMetadataValRangeTest(rs.getMetadataValRangeTest() + 1);
                }
            }
            //????????????
            if (!StringUtils.isEmpty(mataData.getCodomain())) {
                if (!StringUtils.isEmpty(map.get(mataData.geteName()))) {
                    //??????????????????????????????
                    if (!map.get(mataData.geteName()).contains(mataData.getCodomain())) {
                        sb.append(name + "??????????????????" + mataData.getCodomain() + ",");
                        resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                resultType, name,
                                name + "??????????????????" + mataData.getCodomain());
                        rs.setMetadataValContentTest(rs.getMetadataValContentTest() + 1);
                    }
                }
            }
            //???????????????
            if (!StringUtils.isEmpty(mataData.getDisByte())) {
                if (!StringUtils.isEmpty(map.get(mataData.geteName()))) {
                    if (map.get(mataData.geteName()).contains(mataData.getDisByte())) {
                        sb.append(name + "?????????????????????" + mataData.getDisByte() + ",");
                        resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                resultType, name,
                                name + "?????????????????????" + mataData.getDisByte());
                        rs.setMetadataDisByteTest(rs.getMetadataDisByteTest() + 1);
                    }
                }

            }
        }

        return sb.toString();
    }

    /**
     * ???????????????????????????????????????
     *
     * @param map
     * @return
     */
    private String metaMethod(MetadataRuleTest ruleTest, String formDefinitionId, Map<String, String> map, String registerId,
                              String archiveCode, String resultType) {
        StringBuffer sb = new StringBuffer();
        //??????????????????????????????????????????
        //???????????????????????? map???key???authors  value:??????
        Map<String, String> chineseMap = new HashMap<>();
        List<FormField> defaultField = packetsDataServiceImpl.getDefaultField(formDefinitionId);
        for (FormField formField : defaultField) {
            chineseMap.put(formField.getFieldCode(), formField.getLabel());
        }
        List<TestRule> metadataRules = ruleTest.getMetadataRuleTest();
        if (metadataRules.size() > 0) {
            for (TestRule metadataRule : metadataRules) {
                //???????????????authors
                String metadataField = metadataRule.getMetadata();
                //??????map???????????????????????????????????????????????????????????????
                if (chineseMap.containsKey(metadataField)) {
                    List<TestRule.Rules> rules = metadataRule.getRules();
                    for (TestRule.Rules rule : rules) {
                        if (containRule(map, rule)) {
                            String xmlVal = map.get(metadataField);
                            //????????????????????????
                            if (rule.getType().equals("MaxMinVal")) {
                                int maxVal = Integer.parseInt(rule.getMaxVal());
                                int minVal = Integer.parseInt(rule.getMinVal());
                                if (Integer.parseInt(xmlVal) > maxVal || Integer.parseInt(xmlVal) < minVal) {
                                    sb.append(chineseMap.get(metadataField) + "???????????????" + minVal + "???" + maxVal + "??????,");
                                    resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                            resultType, chineseMap.get(metadataField),
                                            chineseMap.get(metadataField) + "???????????????" + minVal + "???" + maxVal + "??????");
                                }
                            } else if (rule.getType().equals("codomain")) {
                                if (!StringUtils.isEmpty(xmlVal)) {
                                    switch (rule.getCondition()) {
                                        case "??????":
                                            if (!xmlVal.contains(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "??????" + rule.getCondition() + "??????" + rule.getVal() + "???,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + chineseMap.get(metadataField) + "??????"
                                                                + rule.getCondition() + "??????" + rule.getVal());

                                            }
                                            break;
                                        case "?????????":
                                            if (xmlVal.contains(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "??????" + rule.getCondition() + "??????" + rule.getVal() + "???,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "??????" + rule.getCondition() + "??????" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "??????":
                                            if (Integer.parseInt(xmlVal) == Integer.parseInt(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "??????" + rule.getCondition() + "??????" + rule.getVal() + "???,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "??????" + rule.getCondition() + "??????" + rule.getVal()
                                                );

                                            }
                                            break;
                                        case "??????":
                                            if (Integer.parseInt(xmlVal) > Integer.parseInt(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "??????" + rule.getCondition() + "??????" + rule.getVal() + "???,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "??????" + rule.getCondition() + "??????" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "??????":
                                            if (Integer.parseInt(xmlVal) < Integer.parseInt(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "??????" + rule.getCondition() + "??????" + rule.getVal() + "???,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "??????" + rule.getCondition() + "??????" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "?????????":
                                            if (StringUtils.isEmpty(xmlVal)) {
                                                sb.append(chineseMap.get(metadataField) + "??????" + rule.getCondition());
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "??????" + rule.getCondition() + "??????" + rule.getVal()
                                                );
                                            }
                                            break;
                                    }
                                }
                            }
                            //????????????????????????
                            else if (rule.getType().equals("MaxMinLen")) {
                                int maxLen = Integer.parseInt(rule.getMaxLen());
                                int minLen = Integer.parseInt(rule.getMinLen());
                                if (map.get(metadataField).length() > maxLen || map.get(metadataField).length() < minLen) {
                                    sb.append(chineseMap.get(metadataField) + "????????????????????????" + minLen + "???" + maxLen + "??????,");
                                    resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                            resultType, chineseMap.get(metadataField),
                                            chineseMap.get(metadataField) + "????????????????????????" + minLen + "???" + maxLen + "??????"
                                    );
                                }
                            }
                            //????????????
                            else if (rule.getType().equals("disByte")) {
                                if (!StringUtils.isEmpty(xmlVal)) {
                                    switch (rule.getCondition()) {
                                        case "??????":
                                            if (!xmlVal.contains(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "????????????" + rule.getCondition() + "??????" + rule.getVal() + "???,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "????????????" + rule.getCondition() + "??????" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "?????????":
                                            if (xmlVal.contains(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "????????????" + rule.getCondition() + "??????" + rule.getVal() + "???,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "????????????" + rule.getCondition() + "??????" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "??????":
                                            if (xmlVal.equals(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "????????????" + rule.getCondition() + "??????" + rule.getVal() + "???,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "????????????" + rule.getCondition() + "??????" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "??????":
                                            if (Integer.parseInt(xmlVal) > Integer.parseInt(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "????????????" + rule.getCondition() + "??????" + rule.getVal() + "???,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "????????????" + rule.getCondition() + "??????" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "??????":
                                            if (Integer.parseInt(xmlVal) < Integer.parseInt(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "????????????" + rule.getCondition() + "??????" + rule.getVal() + "???,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "????????????" + rule.getCondition() + "??????" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "?????????":
                                            if (StringUtils.isEmpty(xmlVal)) {
                                                sb.append(chineseMap.get(metadataField) + "????????????" + rule.getCondition() + "??????" + rule.getVal() + "???,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "????????????" + rule.getCondition() + "??????" + rule.getVal()
                                                );
                                            }
                                            break;
                                    }
                                }
                            }
                            //????????????
                            else if (rule.getType().equals("type")) {
                                if (!StringUtils.isEmpty(xmlVal)) {
                                    switch (rule.getCondition()) {
                                        case "??????":
                                            if (!xmlVal.contains(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "??????" + rule.getCondition() + "?????????" + rule.getVal() + "???,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "??????" + rule.getCondition() + "?????????" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "?????????":
                                            if (xmlVal.contains(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "??????" + rule.getCondition() + "?????????" + rule.getVal() + "???,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "??????" + rule.getCondition() + "?????????" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "??????":
                                            if (xmlVal.equals(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "??????" + rule.getCondition() + "?????????" + rule.getVal() + "??????");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "??????" + rule.getCondition() + "?????????" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "??????":
                                            if (Integer.parseInt(xmlVal) > Integer.parseInt(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "??????" + rule.getCondition() + "?????????" + rule.getVal() + "??????");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "??????" + rule.getCondition() + "?????????" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "??????":
                                            if (Integer.parseInt(xmlVal) < Integer.parseInt(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "??????" + rule.getCondition() + "?????????" + rule.getVal() + "??????");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "??????" + rule.getCondition() + "?????????" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "?????????":
                                            if (StringUtils.isEmpty(xmlVal)) {
                                                sb.append(chineseMap.get(metadataField) + "??????" + rule.getCondition() + "?????????" + rule.getVal());
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "??????" + rule.getCondition() + "?????????" + rule.getVal()
                                                );
                                            }
                                            break;
                                    }
                                }
                            }
                            //??????
                            else if (rule.getType().equals("constraints")) {
                                if (StringUtils.isEmpty(xmlVal)) {
                                    sb.append(chineseMap.get(metadataField) + "?????????????????????,");
                                    resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                            resultType, chineseMap.get(metadataField),
                                            chineseMap.get(metadataField) + "?????????????????????"
                                    );
                                }
                            }
                        }
                    }
                }

            }
        }
        if (sb.length() > 0) {
            rs.setMetadataComplexTest(rs.getMetadataComplexTest() + 1);
        }
        return sb.toString();
    }

    private boolean containRule(Map<String, String> map, TestRule.Rules rule) {
        for (TestRule.Conditions condition : rule.getConditions()) {
            //?????????
            String conditionsField = condition.getMetadata();
            //????????????????????????xml???map????????????
            String xmlVal = map.get(conditionsField);
            if (!StringUtils.isEmpty(xmlVal)) {
                switch (condition.getCondition()) {
                    case "??????":
                        if (xmlVal.contains(condition.getVal())) {
                            return true;
                        }
                        break;
                    case "?????????":
                        if (!xmlVal.contains(condition.getVal())) {
                            return true;
                        }
                        break;
                    case "??????":
                        if (xmlVal.equals(condition.getVal())) {
                            return true;
                        }
                        break;
                    case "??????":
                        if (Integer.parseInt(condition.getVal()) > Integer.parseInt(xmlVal)) {
                            return true;
                        }
                        break;
                    case "??????":
                        if (Integer.parseInt(condition.getVal()) < Integer.parseInt(xmlVal)) {
                            return true;
                        }
                        break;
                    case "?????????":
                        if (!StringUtils.isEmpty(xmlVal)) {
                            return true;
                        }
                        break;
                }
            }
        }
        return false;
    }

    /**
     * ???????????????????????????
     *
     * @param yuanWenJpg   ????????????
     * @param jpgTotalPage ???????????????
     * @param map          ???????????????
     * @return
     */
    public String yuanWenMetadataContrast(YuanWenJpg yuanWenJpg, int jpgTotalPage, Map<String, String> map, String formDefinitionId, String registerId, String archiveCode, String resultType) {
        StringBuffer sb = new StringBuffer();
        //???????????????????????????
        if (!StringUtils.isEmpty(yuanWenJpg.getArchiveCode()) && !StringUtils.isEmpty(map.get(ArchiveEntity.COLUMN_ARCHIVE_CODE))) {
            if (!map.get(ArchiveEntity.COLUMN_ARCHIVE_CODE).equals(yuanWenJpg.getArchiveCode())) {
                sb.append("???????????????????????????????????????,");
                resultMessageRecord(formDefinitionId, registerId,
                        archiveCode,
                        resultType, "??????",
                        "???????????????");
                rs.setComparisonArchiveCodeTest(rs.getComparisonArchiveCodeTest() + 1);
            }
        } else {
            sb.append("????????????????????????????????????,");
            resultMessageRecord(formDefinitionId, registerId,
                    archiveCode,
                    resultType, "??????",
                    "????????????");
            resultMessageRecord(formDefinitionId, registerId,
                    archiveCode,
                    resultType, "??????",
                    "????????????");
            rs.setComparisonArchiveCodeTest(rs.getComparisonArchiveCodeTest() + 1);
        }

        //??????????????????????????????
        if (!StringUtils.isEmpty(yuanWenJpg.getFondCode()) && !StringUtils.isEmpty(map.get(ArchiveEntity.COLUMN_FOND_CODE))) {
            if (!(map.get(ArchiveEntity.COLUMN_FOND_CODE).equals(yuanWenJpg.getFondCode()))) {
                sb.append("??????????????????????????????????????????,");
                resultMessageRecord(formDefinitionId, registerId,
                        archiveCode,
                        resultType, "?????????",
                        "??????????????????");
                rs.setComparisonFondCodeTest(rs.getComparisonFondCodeTest() + 1);
            }
        } else {
            sb.append("???????????????????????????????????????,");
            resultMessageRecord(formDefinitionId, registerId,
                    archiveCode,
                    resultType, "?????????",
                    "???????????????");
            rs.setComparisonFondCodeTest(rs.getComparisonFondCodeTest() + 1);
        }

        if (!StringUtils.isEmpty(map.get(ArchiveEntity.COLUMN_YS))) {
            //???????????????????????????
            if (!(map.get(ArchiveEntity.COLUMN_YS).equals(jpgTotalPage + ""))) {
                sb.append("???????????????????????????????????????,");
                resultMessageRecord(formDefinitionId, registerId,
                        archiveCode,
                        resultType, "??????",
                        "???????????????");
                rs.setComparisonFileYsTest(rs.getComparisonFileYsTest() + 1);
            }
        } else {
            sb.append("????????????????????????????????????,");
            rs.setComparisonFileYsTest(rs.getComparisonFileYsTest() + 1);
            resultMessageRecord(formDefinitionId, registerId,
                    archiveCode,
                    resultType, "??????",
                    "????????????");
        }
        return sb.toString();
    }

    /**
     * ?????????????????????
     *
     * @param formDefinitionId
     * @param registerId
     * @param archiveCode
     * @param resultType
     * @param resultElementName
     * @param resultMessage
     */
    private void resultMessageRecord(String formDefinitionId, String registerId, String archiveCode, String resultType,
                                     String resultElementName, String resultMessage) {
        ResultMessage resultMs = commonMapper.selectOneByQuery(SqlQuery.from(ResultMessage.class).equal(ResultMessageInfo.REGISTERID, registerId)
                .equal(ResultMessageInfo.RESULTTYPE, resultType)
                .equal(ResultMessageInfo.ARCHIVECODE, archiveCode)
                .equal(ResultMessageInfo.RESULTELEMENTNAME, resultElementName));
        ResultMessage message;
        if (resultMs != null) {
            message = resultMs;
            message.setResultMessage(message.getResultMessage() + resultMessage);
        } else {
            message = new ResultMessage();
            message.setFormDefinitionId(formDefinitionId);
            message.setRegisterId(registerId);
            message.setArchiveCode(archiveCode);
            message.setResultElementName(resultElementName);
            message.setResultType(resultType);
            message.setResultMessage(resultMessage);
        }
        message.setCreateDate(System.currentTimeMillis());
        if (StringUtils.isEmpty(message.getId())) {
            message.setId(UUID.randomUUID().toString());
            commonMapper.insert(message);
        } else {
            commonMapper.updateById(message);
        }

    }

    @Override
    public ResultEntity resultUpdateType(Person person, Register register) {
        //????????????????????????,
        register.setFormDefinitionId(registerService.selectById(register.getId()).getFormDefinitionId());
        List<FormData> overDate = formDataService.selectFormData(register.getFormDefinitionId(), (sqlQuery, wrapper) -> {
            sqlQuery.equal(wrapper.getColumn(ArchiveEntity.COLUMN_STATUS), LinkFlowPath.OVER)
//                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_QUALITY_CONDUCT_STATE),"2")
                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_PACKET_STATE),"0")
                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_ASYNC_STATE),"0")
                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_QUALITY_STATE), "0");
        });
        BaseQuery query = new BaseQuery();
        query.setFormDefinitionId(register.getFormDefinitionId());
        query.setFormid(register.getId());
        return registerService.resultUpdateType(person, overDate, query);
    }

    @Override
    public ResultEntity expDistribution(String registerId) {
        //????????????
        Map<String, Object> employees = getExcelData(registerId);
        try {
            //????????????
            //??????maven?????????resources??????????????????
            ClassPathResource classPathResource = new ClassPathResource("template" + File.separator + "cs.xlsx");
            InputStream is = classPathResource.getInputStream();
            //????????? --?????????????????????

            //????????? --?????????????????????
            ApplicationHome applicationHome = new ApplicationHome(this.getClass());

            // ???????????????????????????????????????????????????
            String path = applicationHome.getDir().getParentFile().getParentFile().getAbsolutePath() + "\\src\\main\\resources\\template\\";
            OutputStream os = new FileOutputStream(path + "md.xlsx");
            Context context = new Context();
            if (employees != null) {
                for (String key : employees.keySet()) {
                    context.putVar(key, employees.get(key));
                }
            }
            //???????????? ?????????????????????,????????????????????????excel..
            JxlsHelper jxlsHelper = JxlsHelper.getInstance();
            Transformer transformer = jxlsHelper.createTransformer(is, os);
            jxlsHelper.processTemplate(context, transformer);

            return ResultEntity.success(path + "md.xlsx");
        } catch (Exception e) {
            e.printStackTrace();
            return ResultEntity.error("??????????????????,???????????????,??????????????????");
        }
    }

    public Map<String, Object> getExcelData(String registerId) {
        //????????????????????????????????????
        Map<String, Object> result = new HashMap<>();
        result.put("code", "cs");
        result.put("data", "cs??????");
        List<ResultTestStatistics> resultTestStatisticsList = resultStatistics(registerId);
        for (ResultTestStatistics resultTestStatistics : resultTestStatisticsList) {
            //??????|????????????????????????"
            result.put("yuanWenArchiveCodeTest", resultTestStatistics.getYuanWenArchiveCodeTest());
            result.put("yuanWenFormatTest", resultTestStatistics.getYuanWenFormatTest());
            result.put("yuanWenDpiTest", resultTestStatistics.getYuanWenDpiTest());
            result.put("yuanWenFilePowerTest", resultTestStatistics.getYuanWenFilePowerTest());
            result.put("yuanWenFileNameTest", resultTestStatistics.getYuanWenFileNameTest());
            result.put("yuanWenFileYsTest", resultTestStatistics.getYuanWenFileYsTest());
            result.put("yuanWenRgbTest", resultTestStatistics.getYuanWenRgbTest());
            //??????|???????????????????????????"
            result.put("metadataRequireTest", resultTestStatistics.getMetadataRequireTest());
            result.put("metadataRepeatabilityTest", resultTestStatistics.getMetadataRepeatabilityTest());
            result.put("metadataTypeTest", resultTestStatistics.getMetadataTypeTest());
            result.put("metadataLengthTest", resultTestStatistics.getMetadataLengthTest());
            result.put("metadataValRangeTest", resultTestStatistics.getMetadataValRangeTest());
            result.put("metadataValContentTest", resultTestStatistics.getMetadataValContentTest());
            result.put("metadataDisByteTest", resultTestStatistics.getMetadataDisByteTest());
            result.put("metadataComplexTest", resultTestStatistics.getMetadataComplexTest());
            //???????????????????????????????????????
            result.put("comparisonArchiveCodeTest", resultTestStatistics.getComparisonArchiveCodeTest());
            result.put("comparisonFondCodeTest", resultTestStatistics.getComparisonFondCodeTest());
            result.put("comparisonFileYsTest", resultTestStatistics.getComparisonFileYsTest());
            break;
        }
        List<Map> maps = getResultMessage(registerId);
        result.put("list", maps);
        return result;
    }

    /**
     * ?????????????????????xml
     */
    public void xmlFormDataGenerateFile(FormData formData) {
        packetsDataServiceImpl.xmlGenerateFile(formData);
    }

    /**
     * ???over??????????????????????????????xml
     */
    public void xmlGenerateFile(String formDefinitionId) {
        List<FormData> overDate = formDataService.selectFormData(formDefinitionId, (sqlQuery, wrapper) -> {
            sqlQuery.equal(wrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), "3")
//                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_PACKET_STATE),0)
            ;
        });
        if (overDate.size() > 0) {
            for (FormData formData : overDate) {
                packetsDataServiceImpl.xmlGenerateFile(formData);
            }
        }
    }

    /**
     * ofd??????????????????
     *
     * @param registerId
     * @param formDefinitionId
     * @param formData
     */
    public void ofdToImg(String registerId, String formDefinitionId, FormData formData) {
        // 1.????????????ofd????????????,????????????ofd??????????????????
        String originPath = String.join(File.separator, filePath, "ofd", formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        String outPath = String.join(File.separator, filePath, "ofdToJpg", formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        File ofdFile = new File(originPath);
        File outfile = new File(outPath);
        if (!outfile.exists()) {
            outfile.mkdirs();
        }
        if (!ofdFile.exists()) {
            ofdFile.mkdirs();
        }
        File[] files = ofdFile.listFiles();
        if (files != null) {
            for (File listFile : files) {
                for (File file : listFile.listFiles()) {
                    //??????
                    if (file.isFile()) {
                        if (file.getName().contains(".ofd")) {
                            String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
                            Path src = Paths.get(file.getAbsolutePath());
                            BufferedImage input;
                            // 2. ????????????????????????(?????????)
//         FontLoader.getInstance().scanFontDir(new File("src/main/java/com/dr/digital/resultTest/font"));
                            // 3. ????????????????????????,?????? ?????????????????????(Pixels per millimeter)
                            try (OFDReader reader = new OFDReader(src)) {
                                ImageMaker imageMaker = new ImageMaker(reader, 15);
                                for (int i = 0; i < imageMaker.pageSize(); i++) {
                                    // 4. ????????????????????????
                                    BufferedImage image = imageMaker.makePage(i);
                                    //????????????
                                    Path dist = Paths.get(outPath, fileName + "_" + (i + 1) + ".jpg");
                                    // 5. ???????????????????????????
                                    ImageIO.write(image, "JPG", dist.toFile());
                                    // 6. Close OFDReader ???????????????????????????????????? try close ??????
                                    String jpgFilePath = dist.toAbsolutePath().toString();

                                    if (jpgImageUtil.check(jpgFilePath).equals("check")) {
                                        String jpgFileName = fileName + "_" + (i + 1) + ".jpg";
                                        YuanWenJpg yuanWenJpg = commonMapper.selectOneByQuery(SqlQuery.from(YuanWenJpg.class).equal(YuanWenJpgInfo.FORMDEFINITIONID, formDefinitionId)
                                                .equal(YuanWenJpgInfo.FILENAME, jpgFileName));
                                        if (yuanWenJpg == null) {
                                            yuanWenJpg = new YuanWenJpg();
                                        }
                                        String[] split = fileName.split("-");
                                        yuanWenJpg.setFondCode(split[0]);
                                        yuanWenJpg.setArchiveCode(fileName.substring(0, fileName.lastIndexOf("-")));
                                        yuanWenJpg.setFileName(jpgFileName);
                                        String substring = fileName.substring(fileName.lastIndexOf("-")+1);
                                        yuanWenJpg.setFileType(ofdType(substring));
                                        yuanWenJpg.setImgYsNo((i + 1));
                                        //?????????
                                        String filePower = jpgImageUtil.resolution(jpgFilePath);
                                        yuanWenJpg.setFilePower(filePower);
                                        //dpi
                                        yuanWenJpg.setFileDpi(jpgImageUtil.dpiA4Transformation((filePower)));
                                        //??????
                                        String color = jpgImageUtil.ImageRGB(jpgFilePath);
                                        yuanWenJpg.setFileRGB(color);

                                        if (StringUtils.isEmpty(yuanWenJpg.getId())) {
                                            yuanWenJpg.setRegisterId(registerId);
                                            yuanWenJpg.setFormDefinitionId(formDefinitionId);
                                            yuanWenJpg.setId(UUID.randomUUID().toString());
                                            commonMapper.insert(yuanWenJpg);
                                        } else {
                                            commonMapper.updateById(yuanWenJpg);
                                        }
                                        //????????????
//                                    File jpgFile = new File(jpgFilePath);
//                                    if (jpgFile.isFile()) {
//                                        jpgFile.delete();
//                                    }
                                    }

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }


            }

        }
    }

    /**
     * ????????????????????????ofd?????????jpg
     */
    private String ofdType(String key) {
        String fileType = "?????????";
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
        return fileType;
    }
}