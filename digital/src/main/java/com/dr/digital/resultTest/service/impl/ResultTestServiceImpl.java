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
    //统计表的计数器
    ResultTestStatistics rs;

    //xml线程
    ExecutorService executor = Executors.newFixedThreadPool(1);
    //成果检测线程
    ExecutorService executorResult = Executors.newFixedThreadPool(1);

    private String resultType0 = "原文检测";
    private String resultType1 = "元数据检测";
    private String resultType2 = "原文元数据对比检测";

    /**
     * 开始检验
     *
     * @param registerId
     * @param person
     */
    @Override
    public void startResult(String registerId, @Current Person person) {
        String formDefinitionId = registerService.selectById(registerId).getFormDefinitionId();
        //只检验有ofd转换过（转换状态为3）的在数字化成果的文件
        List<FormData> overDate = formDataService.selectFormData(formDefinitionId, (sqlQuery, wrapper) -> {
            sqlQuery.equal(wrapper.getColumn(ArchiveEntity.COLUMN_STATUS), "OVER")
                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), "3")
//                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_PACKET_STATE),0)
                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_QUALITY_STATE), "0")
//                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_QUALITY_CONDUCT_STATE),"0")
                    .orderBy(wrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
        //删除旧的检测
        if (overDate.size() > 0) {
            commonMapper.deleteByQuery(SqlQuery.from(ResultMessage.class)
                    .equal(ResultMessageInfo.REGISTERID, registerId));
        }
        //先将所有等待的质检进行状态改为进行中
//        formDataService.updateFormDataBySqlBuilder(formDefinitionId,(sqlQuery, wrapper) -> {
//            sqlQuery.equal(wrapper.getColumn(ArchiveEntity.COLUMN_STATUS), "OVER")
//                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), "3")
////                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_PACKET_STATE),0)
//                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_QUALITY_STATE), "0")
//                    .equal(wrapper.getColumn(ArchiveEntity.COLUMN_QUALITY_CONDUCT_STATE),"0")
//                    .set(wrapper.getColumn(ArchiveEntity.COLUMN_QUALITY_CONDUCT_STATE),"1");});
        //统计初始化
        rs = new ResultTestStatistics();
        rs.setRegisterId(registerId);
        rs.setFormDefinitionId(formDefinitionId);
        //先判断有没有旧的统计表
        SqlQuery<ResultTestStatistics> sqlQuery = SqlQuery.from(ResultTestStatistics.class)
                .equal(ResultTestStatisticsInfo.REGISTERID, registerId)
                .equal(ResultTestStatisticsInfo.FORMDEFINITIONID, formDefinitionId);
        ResultTestStatistics resultTestStatistics = commonMapper.selectOneByQuery(sqlQuery);
        //在over环节的所有档案
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
     * 质检进行内容
     * @param formData
     */
    public void resultContent(FormData formData,String registerId,String formDefinitionId,Person person){

            StringBuffer sb = new StringBuffer();
            if (!StringUtils.isEmpty(formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE))) {
                //ofd拆图片识别
                ofdToImg(registerId, formDefinitionId, formData);
                List<YuanWenJpg> yuanWenJpgs = commonMapper.selectByQuery(SqlQuery.from(YuanWenJpg.class)
                        .equal(YuanWenJpgInfo.FORMDEFINITIONID, formDefinitionId)
                        .equal(YuanWenJpgInfo.ARCHIVECODE, formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE).toString())
                        .orderBy(YuanWenJpgInfo.FILENAME));

                if (yuanWenJpgs.size() > 0) {
                    for (int i = 0; i < yuanWenJpgs.size(); i++) {
                        //原文检测
                        sb.append(yuanWenTest(yuanWenJpgs.get(i),  formDefinitionId,
                                registerId, formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE), resultType0));
                    }
                } else {
                    sb.append("原文文件页码检测：未检测到文件页码,请确定ofd文件是否生成,");
                    resultMessageRecord(formDefinitionId, registerId, formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE),
                            resultType0, "页码",
                            "未检测到文件页码,请确定ofd文件是否生成");
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
                //读取档号对应的xml文件的map  数值：fondCode 0032
                String xmlPath = String.join(File.separator, filePath, "zipPack", formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + ".xml");
                Map<String, String> map = XmlUtil.xmlParsing(xmlPath);
                if (map.size() > 0) {
                    Metadata metadata = commonMapper.selectOneByQuery(SqlQuery.from(Metadata.class).equal(MetadataInfo.FORMDEFINITIONID, formDefinitionId));
                    //元数据约束性检测
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
                    //元数据常规检测
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
                        sb.append("智能归档校验规则获取失败,");
                        resultMessageRecord(formDefinitionId, registerId, formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE),
                                resultType1, "元数据",
                                "智能归档校验规则获取失败");
                    }
                    //原文目录对比检测
                    if (yuanWenJpgs.size() > 0) {
                        sb.append(yuanWenMetadataContrast(yuanWenJpgs.get(0), yuanWenJpgs.size(), map, formDefinitionId, registerId,
                                formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE), resultType2));
                    } else {
                        sb.append("原文元数据对比:原文数据为空,");
                        resultMessageRecord(formDefinitionId, registerId, formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE),
                                resultType2, "原文元数据对比",
                                "原文数据为空");
                    }
                    if (StringUtils.isEmpty(formData.get(ArchiveEntity.COLUMN_EXIT_FLOW_PATH)) && sb.length() > 0) {
                        String type = exitFlowPath(formDefinitionId, LinkFlowPath.RECEIVE);
                        formData.put(ArchiveEntity.COLUMN_EXIT_FLOW_PATH, type);
                    }

                } else {
                    sb.append("xml文件读取异常请确认是否生成,");
                    resultMessageRecord(formDefinitionId, registerId, formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE),
                            resultType1, "xml文件",
                            "xml文件读取异常请确认是否生成");
                }
            } else {
                sb.append("元数据：档号为空");
                resultMessageRecord(formDefinitionId, registerId, "",
                        resultType1, "档号", "档号为空");
                //档号为空的数据 格式检测和档号检测未通过+1
                rs.setYuanWenArchiveCodeTest(rs.getYuanWenArchiveCodeTest() + 1);
                rs.setYuanWenFormatTest(rs.getYuanWenFormatTest() + 1);
                rs.setYuanWenDpiTest(rs.getYuanWenDpiTest() + 1);
                rs.setYuanWenRgbTest(rs.getYuanWenRgbTest() + 1);
                rs.setYuanWenFilePowerTest(rs.getYuanWenFilePowerTest() + 1);
                rs.setYuanWenFileNameTest(rs.getYuanWenFileNameTest() + 1);
                rs.setYuanWenFileYsTest(rs.getYuanWenFileYsTest() + 1);

                rs.setMetadataRepeatabilityTest(rs.getMetadataRepeatabilityTest() + 1);//重复性检验
                rs.setMetadataRequireTest(rs.getMetadataRequireTest() + 1);//必选检验
                rs.setMetadataTypeTest(rs.getMetadataTypeTest() + 1);//类型检验
                rs.setMetadataLengthTest(rs.getMetadataLengthTest() + 1);//值长度检验
                rs.setMetadataValRangeTest(rs.getMetadataValRangeTest() + 1);//值区间检验
                rs.setMetadataValContentTest(rs.getMetadataValContentTest() + 1);//值域检验
                rs.setMetadataDisByteTest(rs.getMetadataDisByteTest() + 1);//禁用词检验
                rs.setMetadataComplexTest(rs.getMetadataComplexTest() + 1);//复杂性检验

                rs.setComparisonArchiveCodeTest(rs.getComparisonArchiveCodeTest() + 1);
                rs.setComparisonFondCodeTest(rs.getComparisonFondCodeTest() + 1);
                rs.setComparisonFileYsTest(rs.getComparisonFileYsTest() + 1);

            }
            //检测项信息 删除最后一个逗号
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
                //返回信息不为空,表示检验未通过,更新质检状态
                formData.put(ArchiveEntity.COLUMN_QUALITY_STATE, "0");
            } else {
                //返回信息为空,表示检验通过,更新质检状态
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
     * 重置默认规则
     *
     * @param registerId
     */
    @Override
    public void resetResultTest(String registerId) {
        String formDefinitionId = registerService.selectById(registerId).getFormDefinitionId();
        //默认 原文检测规则
        //档号
        String archiveCodeRule = "判断档号,档号不能为空,档号值也应该规范符合规范";
        yuanWenResultDefault(formDefinitionId, registerId, archiveCodeRule, "档号", "/", 1);

        //格式
        String formatRule = "判断格式,是否为本系统支持读取的信息格式";
        yuanWenResultDefault(formDefinitionId, registerId, formatRule, "格式", "/", 2);

        //dpi
        String dpiRule = "判断dpi,是否大于等于预设值";
        yuanWenResultDefault(formDefinitionId, registerId, dpiRule, "dpi", "300", 3);

        //色彩
        String rgbRule = "判断色彩,是否为彩色";
        yuanWenResultDefault(formDefinitionId, registerId, rgbRule, "rgbRule", "/", 4);

        //分辨率
        String filePowerRule = "判断分辨率,是否读取到正常数值";
        yuanWenResultDefault(formDefinitionId, registerId, filePowerRule, "分辨率", "/", 5);

        //命名规范
        String fileNameRule = "判断文件名,是否符合档案命名规范";
        yuanWenResultDefault(formDefinitionId, registerId, fileNameRule, "命名规范", "/", 6);

        //智能归档元数据检测规则
        metadataDefault(formDefinitionId, registerId);

        //默认 原文元数据对比检测规则
        //档号对比
        String comparisonArchiveCodeRule = "判断原文元数据,档号是否相同";
        yuanWenMetadataDefault(formDefinitionId, registerId, comparisonArchiveCodeRule, "档号", 1);
        //全宗对比
        String comparisonFondCodeRule = "判断原文元数据,全宗是否相同";
        yuanWenMetadataDefault(formDefinitionId, registerId, comparisonFondCodeRule, "全宗", 2);
        //页数对比
        String comparisonFileYsRule = "判断原文元数据,页数是否相同";
        yuanWenMetadataDefault(formDefinitionId, registerId, comparisonFileYsRule, "页数", 3);


    }

    /**
     * 返回前端质检信息
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
            //如果已经有了这个字段名
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
     * 原文档号检测默认规则
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
     * 元数据检测默认规则
     */
    private void metadataDefault(String formDefinitionId, String registerId) {
        //先删除掉所有旧的智能归档配置的
        SqlQuery<ResultTest> ResultTestSql = SqlQuery.from(ResultTest.class)
                .equal(ResultTestInfo.REGISTERID, registerId)
                .equal(ResultTestInfo.RESULTTYPE, "2").or().equal(ResultTestInfo.RESULTTYPE, "4");
        commonMapper.deleteByQuery(ResultTestSql);

        //获取系统默认目录 map的key：authors  value:全宗
        Map<String, String> map = new HashMap<>();
        //
        List<FormField> defaultField = packetsDataServiceImpl.getDefaultField(formDefinitionId);
        for (FormField formField : defaultField) {
            map.put(formField.getFieldCode(), formField.getLabel());
        }
        //根据批次从批次表查到元数据id,从智能归档查相关配置信息
        Metadata metadata = commonMapper.selectOneByQuery(SqlQuery.from(Metadata.class).equal(MetadataInfo.FORMDEFINITIONID, formDefinitionId));

        //智能归档约束规则
        List<MetadataRuleTest> metadataRuleTest = configManagerClient.getMetadataRuleTest(metadata.getCode(), metadata.getClassify(), metadata.getArrange());
        if (metadataRuleTest.size() > 0) {
            for (MetadataRuleTest ruleTest : metadataRuleTest) {
                List<TestRule> metadataRules = ruleTest.getMetadataRuleTest();

                if (metadataRules.size() > 0) {
                    for (TestRule metadataRule : metadataRules) {
                        //字段名如：authors
                        String metadataField = metadataRule.getMetadata();
                        //判断map中有这些元数据方案才将约束规则加入数据库中
                        if (map.containsKey(metadataField)) {
                            StringBuffer sb = new StringBuffer();
                            List<TestRule.Rules> rules = metadataRule.getRules();
                            for (TestRule.Rules rule : rules) {
                                //最大长度最小长度
                                if (rule.getType().equals("MaxMinVal")) {
                                    for (TestRule.Conditions condition : rule.getConditions()) {
                                        String conditionsField = condition.getMetadata();
                                        //如果字段大于最大值或者小于最小值，表示不符合要求
                                        sb.append(map.get(conditionsField) + condition.getCondition() + "值为" + condition.getVal() + "的,");
                                    }
                                    int maxVal = Integer.parseInt(rule.getMaxVal());
                                    int minVal = Integer.parseInt(rule.getMinVal());
                                    sb.append("上述条件满足的时候" + map.get(metadataField) + "的值应该在" + minVal + "和" + maxVal + "之间,");
                                    //rules里面conditions的遍历

                                    //值域
                                } else if (rule.getType().equals("codomain")) {
                                    for (TestRule.Conditions condition : rule.getConditions()) {
                                        String conditionsField = condition.getMetadata();
                                        sb.append(map.get(conditionsField) + condition.getCondition() + "值为" + condition.getVal() + "的,");
                                    }
                                    sb.append("上述条件满足的时候" + map.get(metadataField) + "应该" + rule.getCondition() + rule.getVal() + ",");
                                }
                                //最大长度最小长度
                                else if (rule.getType().equals("MaxMinLen")) {
                                    for (TestRule.Conditions condition : rule.getConditions()) {
                                        String conditionsField = condition.getMetadata();
                                        sb.append(map.get(conditionsField) + condition.getCondition() + "值为" + condition.getVal() + "的,");
                                    }
                                    int maxLen = Integer.parseInt(rule.getMaxLen());
                                    int minLen = Integer.parseInt(rule.getMinLen());
                                    sb.append("上述条件满足的时候" + map.get(metadataField) + "字段的长度应该在" + minLen + "和" + maxLen + "之间,");
                                }
//                                禁用字符
                                else if (rule.getType().equals("disByte")) {
                                    for (TestRule.Conditions condition : rule.getConditions()) {
                                        String conditionsField = condition.getMetadata();
                                        sb.append(map.get(conditionsField) + condition.getCondition() + "值为" + condition.getVal() + "的,");
                                    }
                                    sb.append("上述条件满足的时候" + map.get(metadataField) + "禁用" + rule.getCondition() + rule.getVal() + ",");

                                }
                                //数据类型
                                else if (rule.getType().equals("type")) {
                                    for (TestRule.Conditions condition : rule.getConditions()) {
                                        String conditionsField = condition.getMetadata();
                                        sb.append(map.get(conditionsField) + condition.getCondition() + "值为" + condition.getVal() + "的,");
                                    }
                                    sb.append(map.get(metadataField) + "的数据类型" + rule.getCondition() + rule.getVal() + ",");
                                }
                                //必填
                                else if (rule.getType().equals("constraints")) {
                                    for (TestRule.Conditions condition : rule.getConditions()) {
                                        String conditionsField = condition.getMetadata();
                                        sb.append(map.get(conditionsField) + condition.getCondition() + "值为" + condition.getVal() + "的,");
                                    }
                                    sb.append("上述条件满足的时候" + map.get(metadataField) + "应该必填");
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

        //智能归档的必填项、重复性、字符类型、值长度、值域、值大小、禁用字符、必填
        List<MataDataInfo> categoryMetadata = configManagerClient.getCategoryMetadata(metadata.getBatch_id());
        if (categoryMetadata.size() > 0) {
            List<MataData> mataDates = categoryMetadata.get(0).getMetadata();
            for (MataData mataData : mataDates) {
                StringBuffer rules = new StringBuffer();
                String characterType = "";
                boolean isRepeatability = false;
                boolean isRequired = false;
                //判断目录里面有这个元数据后,才将规则加入数据库中
                if (map.containsKey(mataData.geteName())) {

                    //当前字段的名称 比如档号
                    String name = mataData.getName();
                    //首先得保证智能归档那边有值
                    if (!StringUtils.isEmpty(mataData.getConstraints())) {
                        //constraints约束性1为必填 2选填 3可选
                        if (mataData.getConstraints().equals("1")) {
                            rules.append(name + "应该为必填,");
                            isRequired = true;
                        } else if (mataData.getConstraints().equals("2")) {
                            rules.append(name + "应该为选填,");
                        } else if (mataData.getConstraints().equals("3")) {
                            rules.append(name + "应该为可选,");
                        }
                    }
                    //repeats重复性 1为可重复 2为不可重复
                    if (!StringUtils.isEmpty(mataData.getRepeats())) {
                        if (mataData.getRepeats().equals("1")) {
                            rules.append("可重复,");
                            isRepeatability = true;
                        } else if (mataData.getRepeats().equals("2")) {
                            rules.append("不可重复,");
                        }
                    }
                    //字符类型
                    if (!StringUtils.isEmpty(mataData.getType())) {
                        //type 1为int 2为日期 3为字符
                        if (mataData.getType().equals("1")) {
                            rules.append(name + "的类型应该为数字,");
                            characterType = "1";
                        } else if (mataData.getType().equals("2")) {
                            rules.append(name + "的类型应该为日期,");
                            characterType = "2";
                        } else if (mataData.getType().equals("3")) {
                            rules.append(name + "的类型应该为字符,");
                            characterType = "3";
                        }
                    }
                    //值大小
                    if (!StringUtils.isEmpty(mataData.getMaxVal()) && !StringUtils.isEmpty(mataData.getMinVal())) {
                        rules.append("应该在" + mataData.getMinLen() + mataData.getMaxVal() + "之间,");
                    }
                    //值长度
                    if (mataData.getMinLen() != 0 && mataData.getMinLen() != 0) {
                        rules.append("长度应该在" + mataData.getMinLen() + mataData.getMaxLen() + "之间,");
                    }
                    //值域
                    if (!StringUtils.isEmpty(mataData.getCodomain())) {
                        rules.append("应该包含有" + mataData.getCodomain() + ",");
                    }
                    //禁用字符
                    if (!StringUtils.isEmpty(mataData.getDisByte())) {
                        rules.append("应该不包含有" + mataData.getCodomain() + ",");
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
     * 原文元数据对比检测默认规则
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
     * @param exit             默认退回环节,判断是否存在
     * @return
     */
    private String exitFlowPath(String formDefinitionId, String exit) {
        ResultEntity resultEntity = flowPathService.flowPathAll(formDefinitionId);

        //是否能找到对应环节,没有默认退回任务登记
        if (resultEntity.isSuccess()) {
            //判断该批次内有多少个环节,再根据传入的质检信息判断退回到哪里
            List data = Collections.singletonList(resultEntity.getData());
            //判断批次内是否默认退回环节,没有的话,向前找
            String[] arr = (String[]) data.get(0);
            if (Arrays.asList(arr).indexOf(exit) != -1) {
                return exit;
            } else {
                //首先找到exit在link里面的位置,向前一位,判断该批次是否有环节
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
        //没有符合条件,统一退回任务登记
        return LinkFlowPath.RECEIVE;

    }

    /**
     * 原文检测
     *
     * @param yuanWenJpg
     * @return
     */
    private String yuanWenTest(YuanWenJpg yuanWenJpg, String formDefinitionId, String registerId, String archiveCode, String resultType) {
        StringBuffer sb = new StringBuffer();
        //只有tif才进行dpi、分辨率、色彩的检测
        //dpi检测
        if (!StringUtils.isEmpty(yuanWenJpg.getFileDpi())) {
            ResultTest resultTest = commonMapper.selectOneByQuery(SqlQuery.from(ResultTest.class)
                    .equal(ResultTestInfo.FORMDEFINITIONID, yuanWenJpg.getFormDefinitionId())
                    .equal(ResultTestInfo.FIELDNAME, "dpi"));
            //预设值
            if (resultTest == null || StringUtils.isEmpty(resultTest.getPreset())) {
                sb.append(yuanWenJpg.getFileType()+"的第" +yuanWenJpg.getImgYsNo()+ "张图片dpi未设预设值,");
                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                        resultType, "dpi",
                        yuanWenJpg.getFileType()+"的第" +yuanWenJpg.getImgYsNo()+ "张图片dpi未设预设值,");
                rs.setYuanWenDpiTest(rs.getYuanWenDpiTest() + 1);
            } else {
                String[] split = yuanWenJpg.getFileDpi().split(",");
                if (split.length > 0) {
                    int dpiHeight = Integer.parseInt(split[0]);
                    int dpiWidth = Integer.parseInt(split[1]);
                    if (dpiHeight < Integer.parseInt(resultTest.getPreset()) || dpiWidth < Integer.parseInt(resultTest.getPreset())) {
                        sb.append(yuanWenJpg.getFileType()+"的第" +yuanWenJpg.getImgYsNo()+ "张图片dpi低于预设值,");
                        resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                resultType, "dpi",
                                yuanWenJpg.getFileType()+"的第" +yuanWenJpg.getImgYsNo()+ "张图片dpi低于预设值,");
                        rs.setYuanWenDpiTest(rs.getYuanWenDpiTest() + 1);
                    }
                }

            }
        } else {
            sb.append(yuanWenJpg.getFileType()+"的第" +yuanWenJpg.getImgYsNo()+ "张图片未检测到dpi,");
            resultMessageRecord(formDefinitionId, registerId, archiveCode,
                    resultType, "dpi",
                    yuanWenJpg.getFileType()+"的第" +yuanWenJpg.getImgYsNo()+ "张图片未检测到dpi,");
            rs.setYuanWenDpiTest(rs.getYuanWenDpiTest() + 1);
        }

        //分辨率检测
        if (StringUtils.isEmpty(yuanWenJpg.getFilePower())) {
            sb.append(yuanWenJpg.getFileType()+"的第" +yuanWenJpg.getImgYsNo()+ "张图片未检测到分辨率,");
            resultMessageRecord(formDefinitionId, registerId, archiveCode,
                    resultType, "分辨率",
                    yuanWenJpg.getFileType()+"的第" +yuanWenJpg.getImgYsNo()+ "张图片未检测到分辨率,");
            rs.setYuanWenFilePowerTest(rs.getYuanWenFilePowerTest() + 1);
        }
        //色彩检测
        if (!StringUtils.isEmpty(yuanWenJpg.getFileRGB())) {
            if (yuanWenJpg.getFileRGB().equals("黑色")) {
                sb.append(yuanWenJpg.getFileType()+"的第" +yuanWenJpg.getImgYsNo()+ "张图片为黑色,");
                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                        resultType, "色彩",
                        yuanWenJpg.getFileType()+"的第" +yuanWenJpg.getImgYsNo()+ "张图片为黑色,");
                rs.setYuanWenRgbTest(rs.getYuanWenRgbTest() + 1);
            }
        } else {
            sb.append(yuanWenJpg.getFileType()+"的第" +yuanWenJpg.getImgYsNo()+ "张图片未检测到色彩,");
            resultMessageRecord(formDefinitionId, registerId, archiveCode,
                    resultType, "色彩",
                    yuanWenJpg.getFileType()+"的第" +yuanWenJpg.getImgYsNo()+ "张图片未检测到色彩,");
            rs.setYuanWenRgbTest(rs.getYuanWenRgbTest() + 1);
        }
        //命名规范检测
        if (StringUtils.isEmpty(yuanWenJpg.getFileName())) {
            sb.append(yuanWenJpg.getFileType()+"的第" +yuanWenJpg.getImgYsNo()+  "张图片未检测到文件名,");
            resultMessageRecord(formDefinitionId, registerId, archiveCode,
                    resultType, "命名规范",
                    yuanWenJpg.getFileType()+"的第" +yuanWenJpg.getImgYsNo()+  "张图片未检测到文件名,");
            rs.setYuanWenFileNameTest(rs.getYuanWenFileNameTest() + 1);
        }
        return sb.toString();
    }

    /**
     * 智能归档的常规性元数据检测
     *
     * @param map
     * @return
     */
    private String detectionMeta(MataData mataData, String formDefinitionId, Map<String, String> map, String registerId,
                                 String archiveCode, String resultType) {
        StringBuffer sb = new StringBuffer();
        //从智能归档查的判断规则,判断xml目录中是否含有这些字段,含有才判断
        if (map.containsKey(mataData.geteName())) {
            String name = mataData.getName();
            //constraints约束性1为必填 2选填 3可选
            if (!StringUtils.isEmpty(mataData.getConstraints())) {
                if (mataData.getConstraints().equals("1") && map.get(mataData.geteName().toLowerCase()) == null) {
                    sb.append(name + "不能为空,");
                    resultMessageRecord(formDefinitionId, registerId, archiveCode,
                            resultType, name,
                            name + "不能为空");
                    rs.setMetadataRequireTest(rs.getMetadataRequireTest() + 1);
                }
            }
//            repeats重复性 1为可重复 2为不可重复
            if (!StringUtils.isEmpty(mataData.getRepeats())) {
                if (mataData.getRepeats().equals("2")) {
                    //该表单id的表里面值是否唯一一个
                    if (map.get(mataData.geteName().toLowerCase()) != null) {
                        Long aLong = formDataService.countId(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
                            sqlQuery.equal(formRelationWrapper.getColumn(mataData.geteName()), map.get(mataData.geteName()));
                        });
                        if (aLong > 1) {
                            sb.append(name + "不能重复,");
                            resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                    resultType, name,
                                    name + "不能重复");
                            rs.setMetadataRepeatabilityTest(rs.getMetadataRepeatabilityTest() + 1);
                        }
                    }
                }
            }
            if (!StringUtils.isEmpty(mataData.getType())) {
                //type 1为int 2为日期 3为字符
                if (mataData.getType().equals("1")) {
                    if (map.get(mataData.geteName().toLowerCase()) != null) {
                        if (!org.apache.commons.lang3.StringUtils.isNumeric(map.get(mataData.geteName()))) {
                            sb.append(name + "的类型应该为数字,");
                            resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                    resultType, name,
                                    name + "的类型应该为数字");
                            rs.setMetadataTypeTest(rs.getMetadataTypeTest() + 1);
                        }
                    }

                } else if (mataData.getType().equals("2")) {
                    if (map.get(mataData.geteName().toLowerCase()) != null) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                        try {
                            dateFormat.parse(map.get(mataData.geteName().toLowerCase()));
                        } catch (Exception e) {
                            sb.append(name + "的类型应该为日期,");
                            resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                    resultType, name,
                                    name + "的类型应该为日期");
                            rs.setMetadataTypeTest(rs.getMetadataTypeTest() + 1);

                        }
                    }
                } else if (mataData.getType().equals("3")) {
                    if (map.get(mataData.geteName().toLowerCase()) != null) {
                        if (!(map.get(mataData.geteName().toLowerCase()) instanceof String)) {
                            sb.append(name + "的类型应该为字符,");
                            resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                    resultType, name,
                                    name + "的类型应该为字符");
                            rs.setMetadataTypeTest(rs.getMetadataTypeTest() + 1);
                        }
                    }
                }
            }
            //判断最大长度和最小长度
            if (mataData.getMaxLen() != 0 && mataData.getMinLen() != 0) {
                int valLength = 0;
                if (!StringUtils.isEmpty(map.get(mataData.geteName()))) {
                    valLength = map.get(mataData.geteName()).length();
                }
                if (valLength < mataData.getMinLen() || valLength > mataData.getMaxLen()) {
                    sb.append(name + "的长度应该在" + mataData.getMinLen() + "和" + mataData.getMaxLen() + "之间");
                    resultMessageRecord(formDefinitionId, registerId, archiveCode,
                            resultType, name,
                            name + "的长度应该在" + mataData.getMinLen() + "和" + mataData.getMaxLen() + "之间");
                    rs.setMetadataLengthTest(rs.getMetadataLengthTest() + 1);
                }
            }
            //判断最大值和最小值
            if (!StringUtils.isEmpty(mataData.getMaxVal()) && !StringUtils.isEmpty(mataData.getMinLen())) {
                int max = Integer.parseInt(mataData.getMaxVal());
                int min = Integer.parseInt(mataData.getMinVal());
                int val = Integer.parseInt(map.get(mataData.geteName()));
                if (val < min || val > max) {
                    sb.append(name + "的值应该在" + min + "和" + max + "之间,");
                    resultMessageRecord(formDefinitionId, registerId, archiveCode,
                            resultType, name,
                            name + "的值应该在" + min + "和" + max + "之间");
                    rs.setMetadataValRangeTest(rs.getMetadataValRangeTest() + 1);
                }
            }
            //判断值域
            if (!StringUtils.isEmpty(mataData.getCodomain())) {
                if (!StringUtils.isEmpty(map.get(mataData.geteName()))) {
                    //判断是否包含值域的值
                    if (!map.get(mataData.geteName()).contains(mataData.getCodomain())) {
                        sb.append(name + "的值应该含有" + mataData.getCodomain() + ",");
                        resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                resultType, name,
                                name + "的值应该含有" + mataData.getCodomain());
                        rs.setMetadataValContentTest(rs.getMetadataValContentTest() + 1);
                    }
                }
            }
            //判断禁用符
            if (!StringUtils.isEmpty(mataData.getDisByte())) {
                if (!StringUtils.isEmpty(map.get(mataData.geteName()))) {
                    if (map.get(mataData.geteName()).contains(mataData.getDisByte())) {
                        sb.append(name + "的值不应该含有" + mataData.getDisByte() + ",");
                        resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                resultType, name,
                                name + "的值不应该含有" + mataData.getDisByte());
                        rs.setMetadataDisByteTest(rs.getMetadataDisByteTest() + 1);
                    }
                }

            }
        }

        return sb.toString();
    }

    /**
     * 智能归档的复杂性元数据检测
     *
     * @param map
     * @return
     */
    private String metaMethod(MetadataRuleTest ruleTest, String formDefinitionId, Map<String, String> map, String registerId,
                              String archiveCode, String resultType) {
        StringBuffer sb = new StringBuffer();
        //用来判断是否通过复杂性校验的
        //获取系统默认目录 map的key：authors  value:全宗
        Map<String, String> chineseMap = new HashMap<>();
        List<FormField> defaultField = packetsDataServiceImpl.getDefaultField(formDefinitionId);
        for (FormField formField : defaultField) {
            chineseMap.put(formField.getFieldCode(), formField.getLabel());
        }
        List<TestRule> metadataRules = ruleTest.getMetadataRuleTest();
        if (metadataRules.size() > 0) {
            for (TestRule metadataRule : metadataRules) {
                //字段名如：authors
                String metadataField = metadataRule.getMetadata();
                //判断map中有这些元数据方案才将约束规则加入数据库中
                if (chineseMap.containsKey(metadataField)) {
                    List<TestRule.Rules> rules = metadataRule.getRules();
                    for (TestRule.Rules rule : rules) {
                        if (containRule(map, rule)) {
                            String xmlVal = map.get(metadataField);
                            //最大长度最小长度
                            if (rule.getType().equals("MaxMinVal")) {
                                int maxVal = Integer.parseInt(rule.getMaxVal());
                                int minVal = Integer.parseInt(rule.getMinVal());
                                if (Integer.parseInt(xmlVal) > maxVal || Integer.parseInt(xmlVal) < minVal) {
                                    sb.append(chineseMap.get(metadataField) + "的值应该在" + minVal + "和" + maxVal + "之间,");
                                    resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                            resultType, chineseMap.get(metadataField),
                                            chineseMap.get(metadataField) + "的值应该在" + minVal + "和" + maxVal + "之间");
                                }
                            } else if (rule.getType().equals("codomain")) {
                                if (!StringUtils.isEmpty(xmlVal)) {
                                    switch (rule.getCondition()) {
                                        case "包含":
                                            if (!xmlVal.contains(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "应该" + rule.getCondition() + "值为" + rule.getVal() + "的,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + chineseMap.get(metadataField) + "应该"
                                                                + rule.getCondition() + "值为" + rule.getVal());

                                            }
                                            break;
                                        case "不包含":
                                            if (xmlVal.contains(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "应该" + rule.getCondition() + "值为" + rule.getVal() + "的,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "应该" + rule.getCondition() + "值为" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "等于":
                                            if (Integer.parseInt(xmlVal) == Integer.parseInt(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "应该" + rule.getCondition() + "值为" + rule.getVal() + "的,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "应该" + rule.getCondition() + "值为" + rule.getVal()
                                                );

                                            }
                                            break;
                                        case "大于":
                                            if (Integer.parseInt(xmlVal) > Integer.parseInt(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "应该" + rule.getCondition() + "值为" + rule.getVal() + "的,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "应该" + rule.getCondition() + "值为" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "小于":
                                            if (Integer.parseInt(xmlVal) < Integer.parseInt(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "应该" + rule.getCondition() + "值为" + rule.getVal() + "的,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "应该" + rule.getCondition() + "值为" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "不为空":
                                            if (StringUtils.isEmpty(xmlVal)) {
                                                sb.append(chineseMap.get(metadataField) + "应该" + rule.getCondition());
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "应该" + rule.getCondition() + "值为" + rule.getVal()
                                                );
                                            }
                                            break;
                                    }
                                }
                            }
                            //最大长度最小长度
                            else if (rule.getType().equals("MaxMinLen")) {
                                int maxLen = Integer.parseInt(rule.getMaxLen());
                                int minLen = Integer.parseInt(rule.getMinLen());
                                if (map.get(metadataField).length() > maxLen || map.get(metadataField).length() < minLen) {
                                    sb.append(chineseMap.get(metadataField) + "字段的长度应该在" + minLen + "和" + maxLen + "之间,");
                                    resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                            resultType, chineseMap.get(metadataField),
                                            chineseMap.get(metadataField) + "字段的长度应该在" + minLen + "和" + maxLen + "之间"
                                    );
                                }
                            }
                            //禁用字符
                            else if (rule.getType().equals("disByte")) {
                                if (!StringUtils.isEmpty(xmlVal)) {
                                    switch (rule.getCondition()) {
                                        case "包含":
                                            if (!xmlVal.contains(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "应该禁止" + rule.getCondition() + "值为" + rule.getVal() + "的,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "应该禁止" + rule.getCondition() + "值为" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "不包含":
                                            if (xmlVal.contains(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "应该禁止" + rule.getCondition() + "值为" + rule.getVal() + "的,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "应该禁止" + rule.getCondition() + "值为" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "等于":
                                            if (xmlVal.equals(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "应该禁止" + rule.getCondition() + "值为" + rule.getVal() + "的,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "应该禁止" + rule.getCondition() + "值为" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "大于":
                                            if (Integer.parseInt(xmlVal) > Integer.parseInt(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "应该禁止" + rule.getCondition() + "值为" + rule.getVal() + "的,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "应该禁止" + rule.getCondition() + "值为" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "小于":
                                            if (Integer.parseInt(xmlVal) < Integer.parseInt(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "应该禁止" + rule.getCondition() + "值为" + rule.getVal() + "的,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "应该禁止" + rule.getCondition() + "值为" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "不为空":
                                            if (StringUtils.isEmpty(xmlVal)) {
                                                sb.append(chineseMap.get(metadataField) + "应该禁止" + rule.getCondition() + "值为" + rule.getVal() + "的,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "应该禁止" + rule.getCondition() + "值为" + rule.getVal()
                                                );
                                            }
                                            break;
                                    }
                                }
                            }
                            //数据类型
                            else if (rule.getType().equals("type")) {
                                if (!StringUtils.isEmpty(xmlVal)) {
                                    switch (rule.getCondition()) {
                                        case "包含":
                                            if (!xmlVal.contains(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "应该" + rule.getCondition() + "类型为" + rule.getVal() + "的,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "应该" + rule.getCondition() + "类型为" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "不包含":
                                            if (xmlVal.contains(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "应该" + rule.getCondition() + "类型为" + rule.getVal() + "的,");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "应该" + rule.getCondition() + "类型为" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "等于":
                                            if (xmlVal.equals(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "应该" + rule.getCondition() + "类型为" + rule.getVal() + "的，");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "应该" + rule.getCondition() + "类型为" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "大于":
                                            if (Integer.parseInt(xmlVal) > Integer.parseInt(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "应该" + rule.getCondition() + "类型为" + rule.getVal() + "的，");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "应该" + rule.getCondition() + "类型为" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "小于":
                                            if (Integer.parseInt(xmlVal) < Integer.parseInt(rule.getVal())) {
                                                sb.append(chineseMap.get(metadataField) + "应该" + rule.getCondition() + "类型为" + rule.getVal() + "的，");
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "应该" + rule.getCondition() + "类型为" + rule.getVal()
                                                );
                                            }
                                            break;
                                        case "不为空":
                                            if (StringUtils.isEmpty(xmlVal)) {
                                                sb.append(chineseMap.get(metadataField) + "应该" + rule.getCondition() + "类型为" + rule.getVal());
                                                resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                                        resultType, chineseMap.get(metadataField),
                                                        chineseMap.get(metadataField) + "应该" + rule.getCondition() + "类型为" + rule.getVal()
                                                );
                                            }
                                            break;
                                    }
                                }
                            }
                            //必填
                            else if (rule.getType().equals("constraints")) {
                                if (StringUtils.isEmpty(xmlVal)) {
                                    sb.append(chineseMap.get(metadataField) + "的值应该不为空,");
                                    resultMessageRecord(formDefinitionId, registerId, archiveCode,
                                            resultType, chineseMap.get(metadataField),
                                            chineseMap.get(metadataField) + "的值应该不为空"
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
            //字段名
            String conditionsField = condition.getMetadata();
            //字段对应的值，从xml的map里面查的
            String xmlVal = map.get(conditionsField);
            if (!StringUtils.isEmpty(xmlVal)) {
                switch (condition.getCondition()) {
                    case "包含":
                        if (xmlVal.contains(condition.getVal())) {
                            return true;
                        }
                        break;
                    case "不包含":
                        if (!xmlVal.contains(condition.getVal())) {
                            return true;
                        }
                        break;
                    case "等于":
                        if (xmlVal.equals(condition.getVal())) {
                            return true;
                        }
                        break;
                    case "大于":
                        if (Integer.parseInt(condition.getVal()) > Integer.parseInt(xmlVal)) {
                            return true;
                        }
                        break;
                    case "小于":
                        if (Integer.parseInt(condition.getVal()) < Integer.parseInt(xmlVal)) {
                            return true;
                        }
                        break;
                    case "不为空":
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
     * 原文元数据对象对比
     *
     * @param yuanWenJpg   原文对象
     * @param jpgTotalPage 原文总页数
     * @param map          元数据对象
     * @return
     */
    public String yuanWenMetadataContrast(YuanWenJpg yuanWenJpg, int jpgTotalPage, Map<String, String> map, String formDefinitionId, String registerId, String archiveCode, String resultType) {
        StringBuffer sb = new StringBuffer();
        //原文元数据档号对比
        if (!StringUtils.isEmpty(yuanWenJpg.getArchiveCode()) && !StringUtils.isEmpty(map.get(ArchiveEntity.COLUMN_ARCHIVE_CODE))) {
            if (!map.get(ArchiveEntity.COLUMN_ARCHIVE_CODE).equals(yuanWenJpg.getArchiveCode())) {
                sb.append("原文元数据对比：档号不一样,");
                resultMessageRecord(formDefinitionId, registerId,
                        archiveCode,
                        resultType, "档号",
                        "档号不一样");
                rs.setComparisonArchiveCodeTest(rs.getComparisonArchiveCodeTest() + 1);
            }
        } else {
            sb.append("原文元数据对比：档号为空,");
            resultMessageRecord(formDefinitionId, registerId,
                    archiveCode,
                    resultType, "档号",
                    "档号为空");
            resultMessageRecord(formDefinitionId, registerId,
                    archiveCode,
                    resultType, "档号",
                    "档号为空");
            rs.setComparisonArchiveCodeTest(rs.getComparisonArchiveCodeTest() + 1);
        }

        //原文元数据全宗号对比
        if (!StringUtils.isEmpty(yuanWenJpg.getFondCode()) && !StringUtils.isEmpty(map.get(ArchiveEntity.COLUMN_FOND_CODE))) {
            if (!(map.get(ArchiveEntity.COLUMN_FOND_CODE).equals(yuanWenJpg.getFondCode()))) {
                sb.append("原文元数据对比：全宗号不一样,");
                resultMessageRecord(formDefinitionId, registerId,
                        archiveCode,
                        resultType, "全宗号",
                        "全宗号不一样");
                rs.setComparisonFondCodeTest(rs.getComparisonFondCodeTest() + 1);
            }
        } else {
            sb.append("原文元数据对比：全宗号为空,");
            resultMessageRecord(formDefinitionId, registerId,
                    archiveCode,
                    resultType, "全宗号",
                    "全宗号为空");
            rs.setComparisonFondCodeTest(rs.getComparisonFondCodeTest() + 1);
        }

        if (!StringUtils.isEmpty(map.get(ArchiveEntity.COLUMN_YS))) {
            //原文元数据页数对比
            if (!(map.get(ArchiveEntity.COLUMN_YS).equals(jpgTotalPage + ""))) {
                sb.append("原文元数据对比：页数不一样,");
                resultMessageRecord(formDefinitionId, registerId,
                        archiveCode,
                        resultType, "页数",
                        "页数不一样");
                rs.setComparisonFileYsTest(rs.getComparisonFileYsTest() + 1);
            }
        } else {
            sb.append("原文元数据对比：页数为空,");
            rs.setComparisonFileYsTest(rs.getComparisonFileYsTest() + 1);
            resultMessageRecord(formDefinitionId, registerId,
                    archiveCode,
                    resultType, "页数",
                    "页数为空");
        }
        return sb.toString();
    }

    /**
     * 更新质检信息表
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
        //获取到退回的数据,
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
        //获取数据
        Map<String, Object> employees = getExcelData(registerId);
        try {
            //获取模板
            //获取maven项目下resources文件夹下文件
            ClassPathResource classPathResource = new ClassPathResource("template" + File.separator + "cs.xlsx");
            InputStream is = classPathResource.getInputStream();
            //输出流 --导出数据的位置

            //输出流 --导出数据的位置
            ApplicationHome applicationHome = new ApplicationHome(this.getClass());

            // 保存目录位置根据项目需求可随意更改
            String path = applicationHome.getDir().getParentFile().getParentFile().getAbsolutePath() + "\\src\\main\\resources\\template\\";
            OutputStream os = new FileOutputStream(path + "md.xlsx");
            Context context = new Context();
            if (employees != null) {
                for (String key : employees.keySet()) {
                    context.putVar(key, employees.get(key));
                }
            }
            //接下来的 就是插件的功能,进行将数据匹配到excel..
            JxlsHelper jxlsHelper = JxlsHelper.getInstance();
            Transformer transformer = jxlsHelper.createTransformer(is, os);
            jxlsHelper.processTemplate(context, transformer);

            return ResultEntity.success(path + "md.xlsx");
        } catch (Exception e) {
            e.printStackTrace();
            return ResultEntity.error("导出出现故障,请重启查看,或联系管理员");
        }
    }

    public Map<String, Object> getExcelData(String registerId) {
        //获取输入到表格模板的数据
        Map<String, Object> result = new HashMap<>();
        result.put("code", "cs");
        result.put("data", "cs导出");
        List<ResultTestStatistics> resultTestStatisticsList = resultStatistics(registerId);
        for (ResultTestStatistics resultTestStatistics : resultTestStatisticsList) {
            //原文|图像检测未通过项"
            result.put("yuanWenArchiveCodeTest", resultTestStatistics.getYuanWenArchiveCodeTest());
            result.put("yuanWenFormatTest", resultTestStatistics.getYuanWenFormatTest());
            result.put("yuanWenDpiTest", resultTestStatistics.getYuanWenDpiTest());
            result.put("yuanWenFilePowerTest", resultTestStatistics.getYuanWenFilePowerTest());
            result.put("yuanWenFileNameTest", resultTestStatistics.getYuanWenFileNameTest());
            result.put("yuanWenFileYsTest", resultTestStatistics.getYuanWenFileYsTest());
            result.put("yuanWenRgbTest", resultTestStatistics.getYuanWenRgbTest());
            //目录|元数据检测未通过项"
            result.put("metadataRequireTest", resultTestStatistics.getMetadataRequireTest());
            result.put("metadataRepeatabilityTest", resultTestStatistics.getMetadataRepeatabilityTest());
            result.put("metadataTypeTest", resultTestStatistics.getMetadataTypeTest());
            result.put("metadataLengthTest", resultTestStatistics.getMetadataLengthTest());
            result.put("metadataValRangeTest", resultTestStatistics.getMetadataValRangeTest());
            result.put("metadataValContentTest", resultTestStatistics.getMetadataValContentTest());
            result.put("metadataDisByteTest", resultTestStatistics.getMetadataDisByteTest());
            result.put("metadataComplexTest", resultTestStatistics.getMetadataComplexTest());
            //原文元数据对比检测未通过项
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
     * 提交的对象生成xml
     */
    public void xmlFormDataGenerateFile(FormData formData) {
        packetsDataServiceImpl.xmlGenerateFile(formData);
    }

    /**
     * 将over下的文件全部重新生成xml
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
     * ofd拆图片做检测
     *
     * @param registerId
     * @param formDefinitionId
     * @param formData
     */
    public void ofdToImg(String registerId, String formDefinitionId, FormData formData) {
        // 1.确定输入ofd文件位置,确定输出ofd图片文件位置
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
                    //文件
                    if (file.isFile()) {
                        if (file.getName().contains(".ofd")) {
                            String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
                            Path src = Paths.get(file.getAbsolutePath());
                            BufferedImage input;
                            // 2. 加载指定目录字体(非必须)
//         FontLoader.getInstance().scanFontDir(new File("src/main/java/com/dr/digital/resultTest/font"));
                            // 3. 创建转换转换对象,设置 每毫米像素数量(Pixels per millimeter)
                            try (OFDReader reader = new OFDReader(src)) {
                                ImageMaker imageMaker = new ImageMaker(reader, 15);
                                for (int i = 0; i < imageMaker.pageSize(); i++) {
                                    // 4. 指定页码转换图片
                                    BufferedImage image = imageMaker.makePage(i);
                                    //文件输出
                                    Path dist = Paths.get(outPath, fileName + "_" + (i + 1) + ".jpg");
                                    // 5. 存储为指定格式图片
                                    ImageIO.write(image, "JPG", dist.toFile());
                                    // 6. Close OFDReader 删除工作过程中的临时文件 try close 语法
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
                                        //分辨率
                                        String filePower = jpgImageUtil.resolution(jpgFilePath);
                                        yuanWenJpg.setFilePower(filePower);
                                        //dpi
                                        yuanWenJpg.setFileDpi(jpgImageUtil.dpiA4Transformation((filePower)));
                                        //色彩
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
                                        //删除图片
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
     * 判断是什么类型的ofd文件的jpg
     */
    private String ofdType(String key) {
        String fileType = "未分件";
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
        return fileType;
    }
}