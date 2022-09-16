package com.dr.digital.manage.task.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

@Table(name = Constants.TABLE_PREFIX + "BATCH_DETAIL_APPRAISAL", comment = "鉴定批次详情信息", module = Constants.MODULE_NAME)
public class AppraisalBatchDetail extends AbstractBatchDetailEntity {
    @Column(comment = "原字段code", length = 50)
    private String sourceCode;
    @Column(comment = "原字段名称", length = 200)
    private String sourceName;
    @Column(comment = "原始值", length = 500)
    private String sourceValue;
    @Column(comment = "目标值", length = 500)
    private String targetValue;
    @Column(comment = "鉴定类型", length = 50)
    private String appraisalType;
    @Column(comment = "鉴定人员", length = 50)
    private String appraisalPerson;
    @Column(comment = "鉴定时间", length = 50)
    private Long appraisalDate;

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

    public String getSourceValue() {
        return sourceValue;
    }

    public void setSourceValue(String sourceValue) {
        this.sourceValue = sourceValue;
    }

    public String getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }

    public String getAppraisalType() {
        return appraisalType;
    }

    public void setAppraisalType(String appraisalType) {
        this.appraisalType = appraisalType;
    }

    public String getAppraisalPerson() {
        return appraisalPerson;
    }

    public void setAppraisalPerson(String appraisalPerson) {
        this.appraisalPerson = appraisalPerson;
    }

    public Long getAppraisalDate() {
        return appraisalDate;
    }

    public void setAppraisalDate(Long appraisalDate) {
        this.appraisalDate = appraisalDate;
    }
}
