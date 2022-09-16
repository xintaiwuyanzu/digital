package com.dr.digital.ocr.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseDescriptionEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.ColumnType;
import com.dr.framework.core.orm.annotations.Table;

@Table(name = Constants.TABLE_PREFIX + "DisassemblyRecord_detail", module = Constants.MODULE_NAME, comment = "拆件详情信息表")
public class DisassemblyRecordDetail extends BaseDescriptionEntity<String> {

    @Column(comment = "fid", length = 50, order = 1)
    private String formDefinitionId;
    @Column(comment = "拆建信息表id", length = 50, order = 1)
    private String disassemblyRecordDetailID;



    @Column(comment = "拆建文件名称", length = 50, order = 1)
    private String fileName;

    @Column(comment = "拆建文件位置", length = 50, order = 1)
    private String filePosition;

    @Column(comment = "拆建详情", type = ColumnType.CLOB, order = 1)
    private String disassemblyRecord;

    public String getDisassemblyRecordDetailID() {
        return disassemblyRecordDetailID;
    }

    public void setDisassemblyRecordDetailID(String disassemblyRecordDetailID) {
        this.disassemblyRecordDetailID = disassemblyRecordDetailID;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDisassemblyRecord() {
        return disassemblyRecord;
    }

    public void setDisassemblyRecord(String disassemblyRecord) {
        this.disassemblyRecord = disassemblyRecord;
    }

    public String getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(String formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }

    public String getFilePosition() {
        return filePosition;
    }

    public void setFilePosition(String filePosition) {
        this.filePosition = filePosition;
    }
}
