package com.dr.digital.configManager.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseStatusEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

@Table(name = Constants.TABLE_PREFIX + "TYPEFILE", module = Constants.MODULE_NAME, comment = "档案包结构")
public class TypeFile extends BaseStatusEntity<String> {
    @Column(comment = "表单ID", length = 50, order = 1)
    private String formDefinitionId;
    @Column(comment = "批次ID", length = 50, order = 2)
    private String registerId;
    @Column(comment = "编号", length = 50, order = 3)
    private String yhCode;
    @Column(comment = "顺序号", length = 10, order = 4)
    private String orders;
    @Column(comment = "文件夹名称", length = 200, order = 5)
    private String fileName;
    @Column(comment = "文件夹地址", length = 200, order = 6)
    private String filePath;
    @Column(comment = "文件夹类型", length = 10, order = 7)
    private String fileType;
    @Column(comment = "creator", length = 50, order = 8)
    private String creator;
    @Column(comment = "创建时间", length = 100, order = 9)
    private String crTime;
    @Column(comment = "是否可用", length = 10, order = 10)
    private String flag;
    @Column(comment = "modifier", length = 50, order = 11)
    private String modifier;
    @Column(comment = "isEmpty", length = 10, order = 12)
    private String isEmpty;
    @Column(comment = "moTime", length = 100, order = 13)
    private String moTime;
    @Column(comment = "父id", length = 50, order = 14)
    private String parentID;
    @Column(comment = "schemaId", length = 50, order = 15)
    private String schemaId;
    @Column(comment = "isExist", length = 10, order = 16)
    private String isExist;
    @Column(comment = "typeId", length = 50, order = 17)
    private String typeId;
    @Column(comment = "startTime", length = 100, order = 18)
    private String startTime;

    public String getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(String formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }

    public String getRegisterId() {
        return registerId;
    }

    public void setRegisterId(String registerId) {
        this.registerId = registerId;
    }

    public String getYhCode() {
        return yhCode;
    }

    public void setYhCode(String yhCode) {
        this.yhCode = yhCode;
    }

    public String getOrders() {
        return orders;
    }

    public void setOrders(String orders) {
        this.orders = orders;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCrTime() {
        return crTime;
    }

    public void setCrTime(String crTime) {
        this.crTime = crTime;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getIsEmpty() {
        return isEmpty;
    }

    public void setIsEmpty(String isEmpty) {
        this.isEmpty = isEmpty;
    }

    public String getMoTime() {
        return moTime;
    }

    public void setMoTime(String moTime) {
        this.moTime = moTime;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    public String getIsExist() {
        return isExist;
    }

    public void setIsExist(String isExist) {
        this.isExist = isExist;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}
