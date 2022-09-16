package com.dr.digital.ocr.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseDescriptionEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

@Table(name = Constants.TABLE_PREFIX + "DisassemblyRecord", module = Constants.MODULE_NAME, comment = "拆件信息表")
public class DisassemblyRecord extends BaseDescriptionEntity<String> {

    @Column(comment = "formid", length = 50, order = 1)
    private String formDefinitionId;

    @Column(comment = "registerId", length = 50, order = 1)
    private String registerId;

    @Column(comment = "档案id", length = 50, order = 1)
    private String archivesId;

    @Column(comment = "档案号", length = 50, order = 1)
    private String archive_code;

    public String getArchivesId() {
        return archivesId;
    }

    public void setArchivesId(String archivesId) {
        this.archivesId = archivesId;
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

    public String getArchive_code() {
        return archive_code;
    }

    public void setArchive_code(String archive_code) {
        this.archive_code = archive_code;
    }
}
