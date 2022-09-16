package com.dr.digital.ocr.entity;

import com.dr.digital.common.entity.abstractRecord;
import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseDescriptionEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

@Table(name = Constants.TABLE_PREFIX + "OCRRECORD", module = Constants.MODULE_NAME, comment = "ocr识别记录表")
public class OcrRecord extends abstractRecord {
    @Column(comment = "开始时间", length = 50, order = 1)
    private long startTime;

    @Column(comment = "结束时间", length = 50, order = 1)
    private long endTime;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
