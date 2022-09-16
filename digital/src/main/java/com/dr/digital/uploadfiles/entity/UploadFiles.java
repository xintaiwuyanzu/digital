package com.dr.digital.uploadfiles.entity;


import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseStatusEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

/**
 * 附件实体类
 */
@Table(name = Constants.TABLE_PREFIX + "UPLOADFILES", module = Constants.MODULE_NAME, comment = "上传附件")
public class UploadFiles extends BaseStatusEntity<String> {

    @Column(comment = "父ID", length = 500, order = 1)
    private String parentId;

    @Column(comment = "下一个ID", length = 500, order = 2)
    private String nextId;

    @Column(comment = "原文件名", length = 500, order = 3)
    private String srcName;

    @Column(comment = "文件相对路径", length = 500, order = 4)
    private String filePath;

    @Column(comment = "文件绝对路径", length = 500, order = 9)
    private String absolutePath;

    @Column(comment = "缩率图路径", length = 500, order = 4)
    private String thumbnailPath;

    @Column(comment = "缩率图绝对路径", length = 500, order = 4)
    private String thumbnailAbsolutePath;

    @Column(comment = "文件大小", length = 200, order = 5)
    private String fileSize;

    @Column(comment = "外键ID", length = 50, order = 6)
    private String bussinessId;

    @Column(comment = "批次", length = 6, order = 7)
    private Integer batch;

    @Column(comment = "状态", length = 2, order = 8)
    private String filesTatus;

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getThumbnailAbsolutePath() {
        return thumbnailAbsolutePath;
    }

    public void setThumbnailAbsolutePath(String thumbnailAbsolutePath) {
        this.thumbnailAbsolutePath = thumbnailAbsolutePath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getBussinessId() {
        return bussinessId;
    }

    public void setBussinessId(String bussinessId) {
        this.bussinessId = bussinessId;
    }

    public Integer getBatch() {
        return batch;
    }

    public void setBatch(Integer batch) {
        this.batch = batch;
    }

    public String getFilesTatus() {
        return filesTatus;
    }

    public void setFilesTatus(String filesTatus) {
        this.filesTatus = filesTatus;
    }

    public String getSrcName() {
        return srcName;
    }

    public void setSrcName(String srcName) {
        this.srcName = srcName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getNextId() {
        return nextId;
    }

    public void setNextId(String nextId) {
        this.nextId = nextId;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }
}
