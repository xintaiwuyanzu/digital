package com.dr.digital.event;

/**
 * 档案表单状态数据更新事件
 *
 * @author dr
 */
public class ArchiveDataStatusChangeEvent extends BaseArchiveDataEvent {
    private final String oldStatus;
    private final String newStatus;

    public ArchiveDataStatusChangeEvent(String formDefinitionId, String dataId, String oldStatus, String newStatus) {
        super(null, formDefinitionId, dataId);
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }
}
