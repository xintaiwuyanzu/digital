package com.dr.digital.configManager.bo;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseDescriptionEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;
import com.dr.framework.core.security.bo.PermissionResource;

/**
 * 获取元数据方案
 * 对应批次添加的该方法
 * metadata/getArchiveTypeSchema
 *
 * @lych
 */
@Table(name = Constants.TABLE_PREFIX + "Metadata", module = Constants.MODULE_NAME, comment = "智能归档配置系统查询参数Metadata")
public class Metadata extends BaseDescriptionEntity<String> implements PermissionResource {
    @Column(comment = "存储数据格式的id")
    private String batch_id;
    @Column(comment = "批次编号")
    private String batch_name;
    @Column(comment = "年度")
    private String vintages;
    @Column(comment = "表单ID")
    private String formDefinitionId;
    @Column(comment = "时间")
    private String startTime;
    @Column(comment = "")
    private String crTime;
    @Column(comment = "")
    private String endTime;
    @Column(comment = "")
    private String moTime;
    @Column(comment = "")
    private String orgCode;
    @Column(comment = "")
    private String sysCode;
    @Column(comment = "")
    private String parentID;
    @Column(comment = "")
    private String typeId;
    @Column(comment = "")
    private String creator;
    @Column(comment = "")
    private String modifier;

    /**
     * ——规范标准 字典项---
     */
    @Column(name = "standardId", comment = "")
    private String standardId;
    /**
     * ——载体分类 1电子 2纸质---
     */
    @Column(comment = "载体类别")
    private String classify;
    @Column(comment = "")
    private String flag;
    /**
     * ——整理方式 1:件2卷---
     */
    @Column(comment = "整理方案")
    private String arrange;
    @Column(comment = "排序")
    private String orders;
    @Column(comment = "")
    private String examineState;
    /**
     * 门类名称编码展示 WS.WS
     */
    @Column(comment = "门类编码")
    private String archivers_category_code;
    /*-----------------------------------------批量添加点中元数据信息-------------------------------------------------*/
    /**
     * 元数据显示信息  DA/T 46-2009 文书类电子文件元数据方案|纸质|件|2022-04-01至今
     */
    @Column(name = "metadataName", comment = "元数据名称")
    private String metadataName;
    /**
     * 门类名称  文书档案/公文
     */
    @Column(comment = "门类名称")
    private String archivers_category_code_name;
    /**
     * 方案名称  DA/T 46-2009 文书类电子文件元数据方案
     */
    @Column(comment = "方案名称")
    private String codeFormData;
    /**
     * 案卷 ： 1:案件 2：案卷---
     */
    @Column(comment = "案卷名称")
    private String arrangeFormData;
    /*-----------------------------------------------------截至-----------------------------------------------------------*/
    @Column(comment = "基本信息原数据ID")
    private String mate;

    @Column(comment = "系统编号" ,length = 50, order = 1)
    private String system_code;

    @Column(comment = "行政区划编码", length = 50, order = 10)
    private String region_code;

    /**
     * 1.PDF 2.TIF 3.纸质
     */
    @Column(comment = "原文格式", length = 50, order = 10)
    private String original_format;

    /**
     * 1.PDF 2.OFD
     */
    @Column(comment = "目标格式", length = 50, order = 10)
    private String target_format;


    public String getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(String formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }

    public String getMate() {
        return mate;
    }

    public void setMate(String mate) {
        this.mate = mate;
    }

    public String getBatch_name() {
        return batch_name;
    }

    public void setBatch_name(String batch_name) {
        this.batch_name = batch_name;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getCrTime() {
        return crTime;
    }

    public void setCrTime(String crTime) {
        this.crTime = crTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getMoTime() {
        return moTime;
    }

    public void setMoTime(String moTime) {
        this.moTime = moTime;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getSysCode() {
        return sysCode;
    }

    public void setSysCode(String sysCode) {
        this.sysCode = sysCode;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getStandardId() {
        return standardId;
    }

    public void setStandardId(String standardId) {
        this.standardId = standardId;
    }

    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getArrange() {
        return arrange;
    }

    public void setArrange(String arrange) {
        this.arrange = arrange;
    }

    public String getOrders() {
        return orders;
    }

    public void setOrders(String orders) {
        this.orders = orders;
    }

    public String getExamineState() {
        return examineState;
    }

    public void setExamineState(String examineState) {
        this.examineState = examineState;
    }

    public String getMetadataName() {
        return metadataName;
    }

    public void setMetadataName(String metadataName) {
        this.metadataName = metadataName;
    }

    public String getArchivers_category_code() {
        return archivers_category_code;
    }

    public void setArchivers_category_code(String archivers_category_code) {
        this.archivers_category_code = archivers_category_code;
    }

    public String getCodeFormData() {
        return codeFormData;
    }

    public void setCodeFormData(String codeFormData) {
        this.codeFormData = codeFormData;
    }

    public String getArrangeFormData() {
        return arrangeFormData;
    }

    public void setArrangeFormData(String arrangeFormData) {
        this.arrangeFormData = arrangeFormData;
    }

    public String getArchivers_category_code_name() {
        return archivers_category_code_name;
    }

    public void setArchivers_category_code_name(String archivers_category_code_name) {
        this.archivers_category_code_name = archivers_category_code_name;
    }

    public String getVintages() {
        return vintages;
    }

    public void setVintages(String vintages) {
        this.vintages = vintages;
    }

    public String getBatch_id() {
        return batch_id;
    }

    public void setBatch_id(String batch_id) {
        this.batch_id = batch_id;
    }

    public String getSystem_code() {
        return system_code;
    }

    public void setSystem_code(String system_code) {
        this.system_code = system_code;
    }

    public String getRegion_code() {
        return region_code;
    }

    public void setRegion_code(String region_code) {
        this.region_code = region_code;
    }

    public String getOriginal_format() {
        return original_format;
    }

    public void setOriginal_format(String original_format) {
        this.original_format = original_format;
    }

    public String getTarget_format() {
        return target_format;
    }

    public void setTarget_format(String target_format) {
        this.target_format = target_format;
    }
}
