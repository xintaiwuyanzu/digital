package com.dr.digital.manage.task.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseStatusEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.ColumnType;
import com.dr.framework.core.orm.annotations.Table;

/**
 * 档案任务表
 *
 * @author dr
 */
@Table(comment = "档案任务表", name = Constants.TABLE_PREFIX + "TASK", module = Constants.MODULE_NAME)
public class ArchiveTask extends BaseStatusEntity<String> {
    /**
     * 任务名称
     */
    @Column(comment = "任务名称", length = 1000)
    private String taskName;
    @Column(comment = "任务类型", length = 200)
    private String taskType;

    /**
     * 任务原始发起人信息在
     * @see com.dr.framework.common.entity.BaseCreateInfoEntity
     */

    /**
     * 发起人信息
     */
    @Column(comment = "发起人Id", length = 100)
    private String sourcePersonId;
    @Column(comment = "发起人名称", length = 200)
    private String sourcePersonName;
    @Column(comment = "发起时间", type = ColumnType.DATE)
    private long sourceDate;
    /**
     * 接收人相关信息
     */
    @Column(comment = "接收人ID", length = 100)
    private String targetPersonId;
    @Column(comment = "接收人名称", length = 200)
    private String targetPersonName;
    @Column(comment = "接收时间", type = ColumnType.DATE)
    private long targetRecieveDate;
    @Column(comment = "办结时间", type = ColumnType.DATE)
    private long targetEndDate;
    /**
     * 业务关联信息
     */
    @Column(comment = "批量任务Id", length = 100)
    private String batchId;
    @Column(comment = "上一个任务Id", length = 100)
    private String preTaskId;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getSourcePersonId() {
        return sourcePersonId;
    }

    public void setSourcePersonId(String sourcePersonId) {
        this.sourcePersonId = sourcePersonId;
    }

    public String getSourcePersonName() {
        return sourcePersonName;
    }

    public void setSourcePersonName(String sourcePersonName) {
        this.sourcePersonName = sourcePersonName;
    }

    public long getSourceDate() {
        return sourceDate;
    }

    public void setSourceDate(long sourceDate) {
        this.sourceDate = sourceDate;
    }

    public String getTargetPersonId() {
        return targetPersonId;
    }

    public void setTargetPersonId(String targetPersonId) {
        this.targetPersonId = targetPersonId;
    }

    public String getTargetPersonName() {
        return targetPersonName;
    }

    public void setTargetPersonName(String targetPersonName) {
        this.targetPersonName = targetPersonName;
    }

    public long getTargetRecieveDate() {
        return targetRecieveDate;
    }

    public void setTargetRecieveDate(long targetRecieveDate) {
        this.targetRecieveDate = targetRecieveDate;
    }

    public long getTargetEndDate() {
        return targetEndDate;
    }

    public void setTargetEndDate(long targetEndDate) {
        this.targetEndDate = targetEndDate;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getPreTaskId() {
        return preTaskId;
    }

    public void setPreTaskId(String preTaskId) {
        this.preTaskId = preTaskId;
    }
}
