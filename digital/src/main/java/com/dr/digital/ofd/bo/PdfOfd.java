package com.dr.digital.ofd.bo;

import java.util.List;

/**
 * @author caor
 * @date 2022-06-23 8:55
 */
public class PdfOfd {
    /**
     * n:可选或者没有标识也表示可选  y:必选
     */
    /**
     * 校验
     */
    private String token;
    /**
     * 任务id   y
     */
    private String taskId;

    /**
     * 回调地址 y
     */
    private String callbackUrl;

    /**
     * 是否同步
     */
    private Boolean isSync;

    /**
     * 文件集
     */
    private List<PdfOfdDetailed> list;

    public PdfOfd(String taskId,String callbackUrl,Boolean isSync,List<PdfOfdDetailed> list){
        this.taskId = taskId;
        this.callbackUrl =callbackUrl;
        this.isSync = isSync;
        this.list = list;
    }
    public PdfOfd(){};

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public Boolean getSync() {
        return isSync;
    }

    public void setSync(Boolean sync) {
        isSync = sync;
    }

    public List<PdfOfdDetailed> getList() {
        return list;
    }

    public void setList(List<PdfOfdDetailed> list) {
        this.list = list;
    }
}
