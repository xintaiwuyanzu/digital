package com.dr.digital.ofd.bo;

/**
 * @author caor
 * @date 2022-06-24 13:55
 */
public class PdfOfdDetailed {
    /**
     * n:可选或没有标识 y:必选
     */
    /**
     * 源文件minIO桶名称   n
     */
    private String bucket;
    /**
     * 件文id     y
     */
    private String fileId;
    /**
     * 原文件名称   y
     */
    private String fileName;
    /**
     * 源文件格式    y
     */
    private String format;
    /**
     * 输出格式   y
     */
    private Integer newFormat;
    /**
     * 源文件minIO桶路经   n
     */
    private String path;
    /**
     * 参数（json） n
     */
    private Object paramMap;
    /**
     * 文件大小
     */
    private Integer size;
    /**
     * 目标文件minIO桶位置
     */
    private String targetBucket;
    /**
     * 目标文件minIO桶输出路径
     */
    private String targetPath;

    public  PdfOfdDetailed(){
    }
    public  PdfOfdDetailed(String fileId,String fileName,String format,Integer newFormat,String path){
        this.fileId = fileId;
        this.fileName = fileName;
        this.format = format;
        this.newFormat = newFormat;
        this.path = path;
    }
    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getNewFormat() {
        return newFormat;
    }

    public void setNewFormat(Integer newFormat) {
        this.newFormat = newFormat;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Object getParamMap() {
        return paramMap;
    }

    public void setParamMap(Object paramMap) {
        this.paramMap = paramMap;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getTargetBucket() {
        return targetBucket;
    }

    public void setTargetBucket(String targetBucket) {
        this.targetBucket = targetBucket;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }
}
