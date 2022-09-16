package com.dr.digital.manage.task.entity;

import com.dr.digital.manage.model.entity.AbstractArchiveRelateEntity;
import com.dr.framework.core.orm.annotations.Column;

import static com.dr.framework.common.entity.StatusEntity.STATUS_COLUMN_KEY;

/**
 * 批次详情
 *
 * @author dr
 */
public class AbstractBatchDetailEntity extends AbstractArchiveRelateEntity {
    /**
     * 状态
     */
    @Column(name = STATUS_COLUMN_KEY, comment = "状态", length = 10)
    private String status;
    @Column(comment = "审核意见", length = 800)
    private String advice;
    @Column(comment = "批次Id", length = 100)
    private String batchId;

    @Column(comment = "检测结果", length = 100)
    private String fourDetection;

    public String getFourDetection() {
        return fourDetection;
    }

    public void setFourDetection(String fourDetection) {
        this.fourDetection = fourDetection;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

}
