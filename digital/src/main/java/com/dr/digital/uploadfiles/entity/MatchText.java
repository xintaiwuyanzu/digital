package com.dr.digital.uploadfiles.entity;

/**
 * @author caor
 * @date 2022-07-26 13:56
 */
public class MatchText {
    /**
     * 类型
     */
    private String fileLocations;
    /**
     * 客户端数据id
     */
    private String clientBatchId;
    /**
     * 服务器传输地址
     */
    private String filePath;

    private String formDefinitionId;
    /**是否删除原文
     *
     */
    private boolean isDeleteFile;
    /**
     * 批次id
     */
    private String registerId;
    /**
     * 批次名称
     */
    private String batchName;
    /**
     * 批次号
     */
    private String batchNo;

    public String getFileLocations() {
        return fileLocations;
    }

    public void setFileLocations(String fileLocations) {
        this.fileLocations = fileLocations;
    }

    public String getClientBatchId() {
        return clientBatchId;
    }

    public void setClientBatchId(String clientBatchId) {
        this.clientBatchId = clientBatchId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(String formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }

    public boolean isDeleteFile() {
        return isDeleteFile;
    }

    public void setDeleteFile(boolean isDeleteFile) {
        this.isDeleteFile = isDeleteFile;
    }
}
