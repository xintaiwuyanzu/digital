package com.dr.digital.manage.model.query;

import com.dr.framework.core.orm.annotations.Column;

/**
 * 任务查询条件
 *
 * @author dr
 */
public class BaseQuery extends ArchiveDataQuery {
    /**
     * 导入的时候，用这个字段当作状态参数
     */
    private String name;
    private String fileLocation;
    private String fileName;
    private String mineType;
    private String type;
    private Long startDate;
    private Long endDate;
    private String sendPersonId;
    private String targetPersonId;
    private boolean isTask;
    private String categoryCode;
    //检测方案id
    private String fourNatureSchemeId;
    /**
     * 接收库进行四性检测接收门类数据
     */
    private String fondName;
    private String categoryName;
    private String categoryId;
    /**
     * 导入的时候，用这个做数据来源
     */
    private String sourceCode;
    private String sourceName;
    private String sourceValue;
    private String targetValue;
    private String taskId;
    private String appraisalType;
    private String transferingUnit;
    private String transferingUnitPerson;
    /**
     * 导入导出方案Id
     */
    private String impSchemaId;

    /**
     * 四性检测
     */
    private String fourDetection;

    /**
     * 备注
     */
    private String remarks;

    @Column(comment = "表单id", length = 200)
    private String formid;
    @Column(comment = "挂接全宗门类", length = 200)
    private String hookFondCategory;

    public String getFourDetection() {
        return fourDetection;
    }

    public void setFourDetection(String fourDetection) {
        this.fourDetection = fourDetection;
    }



    /**
     * 纠错记录
     */
    private String errorInfo;
    /**
     * 原文挂接路径
     */
    private String souceFilesPath;
    private String status;
    private String archiveCode;
    private String batchName;
    private String beizhu;

    public String getFondName() {
        return fondName;
    }

    public void setFondName(String fondName) {
        this.fondName = fondName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String getCategoryId() {
        return categoryId;
    }

    @Override
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getFourNatureSchemeId() {
        return fourNatureSchemeId;
    }

    public void setFourNatureSchemeId(String fourNatureSchemeId) {
        this.fourNatureSchemeId = fourNatureSchemeId;
    }

    public String getBeizhu() {
        return beizhu;
    }

    public void setBeizhu(String beizhu) {
        this.beizhu = beizhu;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMineType() {
        return mineType;
    }

    public void setMineType(String mineType) {
        this.mineType = mineType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public String getSendPersonId() {
        return sendPersonId;
    }

    public void setSendPersonId(String sendPersonId) {
        this.sendPersonId = sendPersonId;
    }

    public String getTargetPersonId() {
        return targetPersonId;
    }

    public void setTargetPersonId(String targetPersonId) {
        this.targetPersonId = targetPersonId;
    }

    public boolean isTask() {
        return isTask;
    }

    public void setTask(boolean task) {
        isTask = task;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getImpSchemaId() {
        return impSchemaId;
    }

    public void setImpSchemaId(String impSchemaId) {
        this.impSchemaId = impSchemaId;
    }

    public String getSourceValue() {
        return sourceValue;
    }

    public void setSourceValue(String sourceValue) {
        this.sourceValue = sourceValue;
    }

    public String getAppraisalType() {
        return appraisalType;
    }

    public void setAppraisalType(String appraisalType) {
        this.appraisalType = appraisalType;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getArchiveCode() {
        return archiveCode;
    }

    public void setArchiveCode(String archiveCode) {
        this.archiveCode = archiveCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSouceFilesPath() {
        return souceFilesPath;
    }

    public void setSouceFilesPath(String souceFilesPath) {
        this.souceFilesPath = souceFilesPath;
    }

    public String getTransferingUnit() {
        return transferingUnit;
    }

    public void setTransferingUnit(String transferingUnit) {
        this.transferingUnit = transferingUnit;
    }

    public String getTransferingUnitPerson() {
        return transferingUnitPerson;
    }

    public void setTransferingUnitPerson(String transferingUnitPerson) {
        this.transferingUnitPerson = transferingUnitPerson;
    }


    public String getFormid() {
        return formid;
    }

    public void setFormid(String formid) {
        this.formid = formid;
    }

    public String getHookFondCategory() {
        return hookFondCategory;
    }

    public void setHookFondCategory(String hookFondCategory) {
        this.hookFondCategory = hookFondCategory;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
