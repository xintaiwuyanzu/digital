package com.dr.digital.packet.entity;

import com.dr.digital.common.entity.abstractRecord;
import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseStatusEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

@Table(name = Constants.TABLE_PREFIX + "HANDOVERRECORD", module = Constants.MODULE_NAME, comment = "移交记录")
public class HandoverRecord extends abstractRecord {

    @Column(comment = "移交批次号", length = 50, order = 2)
    private String batch_no;
    @Column(comment = "移交批次名", length = 50, order = 2)
    private String batch_name;

    @Column(comment = "归档信息包移交数量", length = 50, order = 4)
    private String send_number;

    @Column(comment = "归档信息包总字节数", length = 100, order = 5)
    private String send_size;

    @Column(comment = "移交人", length = 100, order = 6)
    private String transactor;

    @Column(comment = "数据移交时间", length = 100, order = 3)
    private String send_time;

    public String getBatch_no() {
        return batch_no;
    }

    public void setBatch_no(String batch_no) {
        this.batch_no = batch_no;
    }

    public String getBatch_name() {
        return batch_name;
    }

    public void setBatch_name(String batch_name) {
        this.batch_name = batch_name;
    }

    public String getSend_number() {
        return send_number;
    }

    public void setSend_number(String send_number) {
        this.send_number = send_number;
    }

    public String getSend_size() {
        return send_size;
    }

    public void setSend_size(String send_size) {
        this.send_size = send_size;
    }

    public String getTransactor() {
        return transactor;
    }

    public void setTransactor(String transactor) {
        this.transactor = transactor;
    }

    public String getSend_time() {
        return send_time;
    }

    public void setSend_time(String send_time) {
        this.send_time = send_time;
    }


}
