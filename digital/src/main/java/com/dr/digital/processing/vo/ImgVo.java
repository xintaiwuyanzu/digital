package com.dr.digital.processing.vo;

import java.util.List;

public class ImgVo {
    String fileName, fileSize, filePath;
    int index;
    List<String> listContent;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<String> getListContent() {
        return listContent;
    }

    public void setListContent(List<String> listContent) {
        this.listContent = listContent;
    }
}
