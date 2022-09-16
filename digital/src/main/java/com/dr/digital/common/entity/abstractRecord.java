package com.dr.digital.common.entity;

import com.dr.framework.common.entity.BaseDescriptionEntity;
import com.dr.framework.core.orm.annotations.Column;

/**
 * @author Mr.Zhu
 * @date 2022/8/15 - 15:59
 */
public class abstractRecord  extends BaseDescriptionEntity<String> {

    @Column(comment = "题名")
    private String title;

    @Column(comment = "批次Id", length = 500, order = 1)
    private String registerId;

    @Column(comment = "档案实例ID")
    private String formDataId;

    @Column(comment = "表单id", length = 100, order = 1)
    private String formDefinitionId;

    @Column(comment = "档号", length = 50, order = 1)
    private String archiveCode;

    @Column(comment = "全宗号")
    private String fondCode;

    @Column(comment = "文件名称", length = 50, order = 1)
    private String fileName;

    @Column(comment = "存放地址", length = 300, order = 1)
    private String filePath;

    @Column(comment = "图像大小", length = 50, order = 1)
    private String fileKb;

    @Column(comment = "返回信息", length = 50, order = 2)
    private String message;

    @Column(comment = "返回值", length = 50, order = 2)
    private String success;



    public String getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(String formDataId) {
        this.formDataId = formDataId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRegisterId() {
        return registerId;
    }

    public void setRegisterId(String registerId) {
        this.registerId = registerId;
    }

    public String getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(String formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }

    public String getArchiveCode() {
        return archiveCode;
    }

    public void setArchiveCode(String archiveCode) {
        this.archiveCode = archiveCode;
    }

    public String getFondCode() {
        return fondCode;
    }

    public void setFondCode(String fondCode) {
        this.fondCode = fondCode;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileKb() {
        return fileKb;
    }

    public void setFileKb(String fileKb) {
        this.fileKb = fileKb;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }


}
