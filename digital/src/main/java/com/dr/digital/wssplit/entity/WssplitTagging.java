package com.dr.digital.wssplit.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseDescriptionEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.ColumnType;
import com.dr.framework.core.orm.annotations.Table;

@Table(name = Constants.TABLE_PREFIX + "Wssplit_tagging", module = Constants.MODULE_NAME, comment = "档案标注类")
public class WssplitTagging extends BaseDescriptionEntity<String>{
    @Column(comment = "批注", type = ColumnType.CLOB, order = 40)
    private String noteName;

    @Column(comment = "批注详情", type = ColumnType.CLOB, order = 40)
    private String note;

    @Column(comment = "formDefinitionId")
    private String formDefinitionId;

    @Column(comment = "批次id")
    private String categoryId;

    @Column(comment = "档案id")
    private String archivesId;

    /**
     * 0：没有批注，1：有批注（没批注，改记录也不存在）
     */
    @Column(comment = "状态")
    private String wssplitTaggingCondition;

    public String getWssplitTaggingCondition() {
        return wssplitTaggingCondition;
    }

    public void setWssplitTaggingCondition(String wssplitTaggingCondition) {
        this.wssplitTaggingCondition = wssplitTaggingCondition;
    }

    public String getNoteName() {
        return noteName;
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    public String getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(String formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getArchivesId() {
        return archivesId;
    }

    public void setArchivesId(String archivesId) {
        this.archivesId = archivesId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
