package com.dr.digital.packet.entity;

import com.dr.framework.common.entity.BaseStatusEntity;
import com.dr.framework.core.orm.annotations.Column;

public class ArchiveCallback extends BaseStatusEntity<String> {
    @Column(comment = "移交批次号", length = 50, order = 2)
    private String batch_name;
    @Column(comment = "归档信息包移交数量", length = 50, order = 4)
    private String send_number;

    @Column(comment = "归档信息包总字节数", length = 100, order = 5)
    private String send_size;

    @Column(comment = "移交人", length = 100, order = 6)
    private String transactor;

    @Column(comment = "数据移交时间", length = 100, order = 3)
    private String send_time;

    @Column(comment = "返回值")
    private String code;

    @Column(comment = "返回信息")
    private String message;

    @Column(comment = "返回状态")
    private boolean success;


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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

}
