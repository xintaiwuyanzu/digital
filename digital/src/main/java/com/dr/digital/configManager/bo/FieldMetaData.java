package com.dr.digital.configManager.bo;

/**
 * 元数据
 *
 * @author dr
 */
public class FieldMetaData extends AbstractConfigEntity {
    /**
     * 可重复性id 读数据字典
     */
    private String repeats;
    /**
     * 英文名称
     */
    private String eName;
    /**
     * 元数据类型id
     * 基本信息元数据
     * 流程信息元数据
     */
    private String ascription;
    /**
     * 立档单位规范全称--备注
     */
    private String remark;
    /**
     * 元数据类型 读字典（integer整形、dataType日期型、character字符型）
     */
    private String type;
    /**
     * 约束性读数据字典
     */
    private String constraints;
    private String parentID;
    private String sysMetaID;
    private String example;
    /**
     * 机构编码
     */
    private String orgCode;
    /**
     * 元数据名称
     */
    private String name;
    /**
     * 门类档案id
     */
    private String typeID;
    /**
     * 元素类型读数据字典
     */
    private String mateType;
    private String orders;
    /**
     * 元素类型长度
     */
    private String typeLength;
    
    public String getRepeats() {
        return repeats;
    }

    public void setRepeats(String repeats) {
        this.repeats = repeats;
    }

    public String geteName() {
        return eName;
    }

    public void seteName(String eName) {
        this.eName = eName;
    }

    public String getAscription() {
        return ascription;
    }

    public void setAscription(String ascription) {
        this.ascription = ascription;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public String getSysMetaID() {
        return sysMetaID;
    }

    public void setSysMetaID(String sysMetaID) {
        this.sysMetaID = sysMetaID;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeID() {
        return typeID;
    }

    public void setTypeID(String typeID) {
        this.typeID = typeID;
    }

    public String getMateType() {
        return mateType;
    }

    public void setMateType(String mateType) {
        this.mateType = mateType;
    }

    public String getOrders() {
        return orders;
    }

    public void setOrders(String orders) {
        this.orders = orders;
    }

    public String getTypeLength() {
        return typeLength;
    }

    public void setTypeLength(String typeLength) {
        this.typeLength = typeLength;
    }
}