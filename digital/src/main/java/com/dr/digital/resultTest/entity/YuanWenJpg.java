package com.dr.digital.resultTest.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseStatusEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.ColumnType;
import com.dr.framework.core.orm.annotations.Table;

/**
 * @author Mr.Zhu
 * @date 2022/8/23 - 18:08
 */
@Table(name = Constants.TABLE_PREFIX + "yuan_wen_jpg", module = Constants.MODULE_NAME, comment = "ofd原文拆分数据")
public class YuanWenJpg extends BaseStatusEntity<String> {
    @Column(comment = "表单ID", length = 50, order = 1)
    private String formDefinitionId;
    @Column(comment = "批次ID", length = 50, order = 2)
    private String registerId;

    @Column(comment = "全宗号", length = 50, order = 3)
    private String fondCode;
    @Column(comment = "档号", length = 50, order = 4)
    private String archiveCode;
    @Column(comment = "图片名称", length = 200, order = 5)
    private String fileName;
    @Column(comment = "图片类型", length = 200, order = 5)
    private String fileType;
    @Column(comment = "图片页号", length = 200, order = 5)
    private int imgYsNo;
    @Column(comment = "dpi", type = ColumnType.CLOB,  order = 6)
    private String fileDpi;
    @Column(comment = "分辨率", length = 100, order = 7)
    private String filePower;
    @Column(comment = "色彩",type = ColumnType.VARCHAR, order = 8)
    private String fileRGB;

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public int getImgYsNo() {
        return imgYsNo;
    }

    public void setImgYsNo(int imgYsNo) {
        this.imgYsNo = imgYsNo;
    }

    public String getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(String formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }

    public String getRegisterId() {
        return registerId;
    }

    public void setRegisterId(String registerId) {
        this.registerId = registerId;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileDpi() {
        return fileDpi;
    }

    public void setFileDpi(String fileDpi) {
        this.fileDpi = fileDpi;
    }

    public String getFilePower() {
        return filePower;
    }

    public void setFilePower(String filePower) {
        this.filePower = filePower;
    }

    public String getFileRGB() {
        return fileRGB;
    }

    public void setFileRGB(String fileRGB) {
        this.fileRGB = fileRGB;
    }
}
