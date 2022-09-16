package com.dr.digital.configManager.bo;

import com.dr.framework.common.entity.BaseStatusEntity;

/**
 * 审批档案元数据规则
 *
 * @author dr
 */
public class MetaRule  extends BaseStatusEntity<String> {
    /**
     * 数据类型
     */
    public static final String TYPE_INTEGER = "1"; //int类型
    public static final String TYPE_DATE_TYPE = "2"; //data类型
    public static final String TYPE_CHARACTER = "3";//字符类型

    /**
     * 元数据类型id
     */
    private String ascription;
    /**
     * 约束性
     */
    private String constraints;
    /**
     * 禁用特殊字符（检测编码包含Authenticity005时有值）
     */
    private String disByte;
    /**
     * 英文名称
     */
    private String eName;
    /**
     * 环节（1:归档环节，2:移交与接收环节，3:长期保存环节）
     */
    private String linkCode;
    /**
     * 元素类型
     */
    private String mateType;
    /**
     * 最大长度（检测编码包含Authenticity001时有值）
     */
    private String maxLen;
    /**
     * 最大值（检测编码包含Authenticity004时有值）
     */
    private String maxVal;
    /**
     * 最小长度（检测编码包含Authenticity001时有值）
     */
    private String minLen;
    /**
     * 最小值（检测编码包含Authenticity004时有值）
     */
    private String minVal;
    /**
     * 中文名称
     */
    private String name;
    /**
     * 备注信息
     */
    private String remark;
    /**
     * 可重复性
     */
    private String repeats;
    private String ruleId;
    /**
     * 配置id
     */
    private String configId;

    private String testItems;
    /**
     * 数据类型（integer整形、dataType日期型、character字符型）
     */
    private String type;
    private String typeID;


    /**
     * 是否必填
     *
     * @return
     */
    public boolean isRequired() {
        return "1".equals(constraints);
    }

    public String getAscription() {
        return ascription;
    }

    public void setAscription(String ascription) {
        this.ascription = ascription;
    }

    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    public String getDisByte() {
        return disByte;
    }

    public void setDisByte(String disByte) {
        this.disByte = disByte;
    }

    public String geteName() {
        return eName;
    }

    public void seteName(String eName) {
        this.eName = eName;
    }

    public String getLinkCode() {
        return linkCode;
    }

    public void setLinkCode(String linkCode) {
        this.linkCode = linkCode;
    }

    public String getMateType() {
        return mateType;
    }

    public void setMateType(String mateType) {
        this.mateType = mateType;
    }

    public String getMaxLen() {
        return maxLen;
    }

    public void setMaxLen(String maxLen) {
        this.maxLen = maxLen;
    }

    public String getMaxVal() {
        return maxVal;
    }

    public void setMaxVal(String maxVal) {
        this.maxVal = maxVal;
    }

    public String getMinLen() {
        return minLen;
    }

    public void setMinLen(String minLen) {
        this.minLen = minLen;
    }

    public String getMinVal() {
        return minVal;
    }

    public void setMinVal(String minVal) {
        this.minVal = minVal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRepeats() {
        return repeats;
    }

    public void setRepeats(String repeats) {
        this.repeats = repeats;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getTestItems() {
        return testItems;
    }

    public void setTestItems(String testItems) {
        this.testItems = testItems;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeID() {
        return typeID;
    }

    public void setTypeID(String typeID) {
        this.typeID = typeID;
    }
}
