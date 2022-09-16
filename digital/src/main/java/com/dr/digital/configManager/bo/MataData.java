package com.dr.digital.configManager.bo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class MataData {

    private String repeats;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date crTime;
    private int flag;
    private String modifier;
    private String standardId;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date moTime;
    private String type;
    private String constraints;
    private String parentID;
    private String sysMetaID;
    private String maxVal;//最大值
    private String minVal;//最小值
    private String orgCode;
    private int minLen;//最小长度
    private int maxLen;//最大长度
    private String typeID;
    private String mateType;
    private String id;
    private int examineState;
    private String creator;
    private String eName;
    private String ascription;
    private String disByte;//禁用值

    private String typeLength;
    private String codomain;//值域
    private String schemaId;
    private String metadataType;
    private String name;
    private String isSerialNumber;
    private String orders;

    public Date getCrTime() {
        return crTime;
    }

    public void setCrTime(Date crTime) {
        this.crTime = crTime;
    }

    public Date getMoTime() {
        return moTime;
    }

    public void setMoTime(Date moTime) {
        this.moTime = moTime;
    }

    public int getMinLen() {
        return minLen;
    }

    public void setMinLen(int minLen) {
        this.minLen = minLen;
    }

    public int getMaxLen() {
        return maxLen;
    }

    public void setMaxLen(int maxLen) {
        this.maxLen = maxLen;
    }

    public int getExamineState() {
        return examineState;
    }

    public void setExamineState(int examineState) {
        this.examineState = examineState;
    }

    public String getRepeats() {
        return repeats;
    }

    public void setRepeats(String repeats) {
        this.repeats = repeats;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
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

    public String getMaxVal() {
        return maxVal;
    }

    public void setMaxVal(String maxVal) {
        this.maxVal = maxVal;
    }

    public String getMinVal() {
        return minVal;
    }

    public void setMinVal(String minVal) {
        this.minVal = minVal;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
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

    public String getDisByte() {
        return disByte;
    }

    public void setDisByte(String disByte) {
        this.disByte = disByte;
    }

    public String getTypeLength() {
        return typeLength;
    }

    public void setTypeLength(String typeLength) {
        this.typeLength = typeLength;
    }

    public String getCodomain() {
        return codomain;
    }

    public void setCodomain(String codomain) {
        this.codomain = codomain;
    }

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    public String getMetadataType() {
        return metadataType;
    }

    public void setMetadataType(String metadataType) {
        this.metadataType = metadataType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIsSerialNumber() {
        return isSerialNumber;
    }

    public void setIsSerialNumber(String isSerialNumber) {
        this.isSerialNumber = isSerialNumber;
    }

    public String getOrders() {
        return orders;
    }

    public void setOrders(String orders) {
        this.orders = orders;
    }
}
