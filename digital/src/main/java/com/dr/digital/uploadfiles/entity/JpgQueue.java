package com.dr.digital.uploadfiles.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseDescriptionEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.ColumnType;
import com.dr.framework.core.orm.annotations.Table;

/**
 * jpg拆分队列
 */
@Table(name = Constants.TABLE_PREFIX + "JPGQUEUE", module = Constants.MODULE_NAME, comment = "jpg拆分队列")
public class JpgQueue extends BaseDescriptionEntity<String> {
    @Column(comment = "系统编号", length = 50, order = 1)
    private String system_code;
    @Column(comment = "批次ID", length = 50, order = 2)
    private String batchID;
    @Column(comment = "批次号", length = 50, order = 2)
    private String batchNo;
    @Column(comment = "批次名称", length = 50, order = 2)
    private String batchName;
    @Column(comment = "表单ID", length = 50, order = 3)
    private String formDefinitionId;
    @Column(comment = "表单实例ID", length = 50, order = 4)
    private String formDateId;
    @Column(comment = "全宗号", length = 50, order = 5)
    private String fondCode;
    @Column(comment = "档号", length = 50, order = 5)
    private String archiveCode;

    @Column(comment = "顺序号", length = 50, order = 5)
    private String batch_number;
    @Column(comment = "文件名称", length = 200, order = 6)
    private String fileName;
    @Column(comment = "文件地址", length = 500, order = 7)
    private String filePath;
    @Column(comment = "文件大小", length = 100, order = 8)
    private String fileSize;
    @Column(comment = "文件内图片dpi", type = ColumnType.CLOB,  order = 8)
    private String fileDpi;
    @Column(comment = "文件分辨率", length = 100, order = 8)
    private String filePower;
    @Column(comment = "文件色彩",type = ColumnType.VARCHAR, order = 8)
    private String fileRGB;

    @Column(comment = "文件类型", length = 100, order = 8)
    private String fileType;
    @Column(comment = "文件页数", length = 100, order = 8)
    private String fileYs;

    @Column(comment = "说明", length = 100, order = 8)
    private String jpgExplain;
    @Column(comment = "创建人姓名", length = 200, order = 7)
    private String personName;


    public String getFileRGB() {
        return fileRGB;
    }

    public void setFileRGB(String fileRGB) {
        this.fileRGB = fileRGB;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileDpi() {
        return fileDpi;
    }

    public void setFileDpi(String fileDpi) {
        this.fileDpi = fileDpi;
    }

    public String getSystem_code() {
        return system_code;
    }

    public void setSystem_code(String system_code) {
        this.system_code = system_code;
    }

    public String getBatchID() {
        return batchID;
    }

    public void setBatchID(String batchID) {
        this.batchID = batchID;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(String formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }

    public String getFormDateId() {
        return formDateId;
    }

    public void setFormDateId(String formDateId) {
        this.formDateId = formDateId;
    }

    public String getFondCode() {
        return fondCode;
    }

    public void setFondCode(String fondCode) {
        this.fondCode = fondCode;
    }

    public String getArchiveCode() {
        return archiveCode;
    }

    public void setArchiveCode(String archiveCode) {
        this.archiveCode = archiveCode;
    }

    public String getBatch_number() {
        return batch_number;
    }

    public void setBatch_number(String batch_number) {
        this.batch_number = batch_number;
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

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getJpgExplain() {
        return jpgExplain;
    }

    public void setJpgExplain(String jpgExplain) {
        this.jpgExplain = jpgExplain;
    }

    public String getFilePower() {
        return filePower;
    }

    public void setFilePower(String filePower) {
        this.filePower = filePower;
    }

    public String getFileYs() {
        return fileYs;
    }

    public void setFileYs(String fileYs) {
        this.fileYs = fileYs;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

}
