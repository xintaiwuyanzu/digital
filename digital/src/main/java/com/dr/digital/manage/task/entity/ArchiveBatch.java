package com.dr.digital.manage.task.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseStatusEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.ColumnType;
import com.dr.framework.core.orm.annotations.Table;

/**
 * 档案批次信息表
 *
 * @author dr
 */
@Table(name = Constants.TABLE_PREFIX + "BATCH", comment = "档案批次信息表", module = Constants.MODULE_NAME)
public class ArchiveBatch extends BaseStatusEntity<String> {
    @Column(comment = "批次名称", length = 500)
    private String batchName;

    @Column(comment = "文件存储位置", length = 500)
    private String fileLocation;
    @Column(comment = "文件名称", length = 500)
    private String fileName;
    @Column(comment = "文件类型", length = 100)
    private String mineType;

    @Column(comment = "类型", length = 50)
    private String batchType;
    @Column(comment = "开始时间", type = ColumnType.DATE)
    private Long startDate;
    @Column(comment = "结束时间", type = ColumnType.DATE)
    private Long endDate;

    /**
     * 导入使用
     */
    @Column(comment = "移交单位", length = 500)
    private String transferingUnit;
    @Column(comment = "移交单位负责人", length = 100)
    private String transferingUnitPerson;
    @Column(comment = "数量", length = 100)
    private Long detailNum;
    @Column(comment = "涉密数量", length = 200)
    private Integer secretCount;
    @Column(comment = "全宗id", length = 100)
    private String fondId;
    @Column(comment = "分类id", length = 200)
    private String categoryId;
    @Column(comment = "分类编码", length = 200)
    private String categoryCode;
    @Column(comment = "数据来源", length = 200)
    private String dataSource;
    @Column(comment = "审核状态", length = 200)
    private String shStutas;
    @Column(comment = "备注", length = 500)
    private String beizhu;
    @Column(comment = "导入方案", length = 200)
    private String impSchemaId;
    @Column(comment = "备注", length = 500)
    private String message;
    @Column(comment = "检测方案id", length = 200)
    private String fourNatureSchemeId;

    /**
     * 挂接使用
     */
    @Column(comment = "表单id", length = 200)
    private String formid;
    @Column(comment = "挂接全宗门类", length = 200)
    private String hookFondCategory;

    public String getFourNatureSchemeId() {
        return fourNatureSchemeId;
    }

    public void setFourNatureSchemeId(String fourNatureSchemeId) {
        this.fourNatureSchemeId = fourNatureSchemeId;
    }

    /**
     * 档案指导
     */
    @Column(comment = "是否通过；1通过，2不通过", length = 1)
    private String isPass;

    public String getIsPass() {
        return isPass;
    }

    public void setIsPass(String isPass) {
        this.isPass = isPass;
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

    public String getBatchType() {
        return batchType;
    }

    public void setBatchType(String batchType) {
        this.batchType = batchType;
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

    public Long getDetailNum() {
        return detailNum;
    }

    public void setDetailNum(Long detailNum) {
        this.detailNum = detailNum;
    }

    public String getFondId() {
        return fondId;
    }

    public void setFondId(String fondId) {
        this.fondId = fondId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getShStutas() {
        return shStutas;
    }

    public void setShStutas(String shStutas) {
        this.shStutas = shStutas;
    }

    public Integer getSecretCount() {
        return secretCount;
    }

    public void setSecretCount(Integer secretCount) {
        this.secretCount = secretCount;
    }

    public String getImpSchemaId() {
        return impSchemaId;
    }

    public void setImpSchemaId(String impSchemaId) {
        this.impSchemaId = impSchemaId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
}
