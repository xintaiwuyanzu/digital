package com.dr.digital.event;

import com.dr.framework.common.form.core.model.FormData;

/**
 * 档案表单数据添加事件
 *
 * @author dr
 */
public class ArchiveDataEditEvent extends BaseArchiveDataEvent {
    /**
     * 表单数据
     */
    private final FormData data;

    private Boolean isUpdateContent;

    public ArchiveDataEditEvent(String categoryId, FormData data) {
        this(categoryId, data, false);
    }

    public ArchiveDataEditEvent(String categoryId, FormData data, Boolean isUpdateContent) {
        super(categoryId, data.getFormDefinitionId(), data.getId());
        this.data = data;
        this.isUpdateContent = isUpdateContent;
    }

    public FormData getData() {
        return data;
    }

    public Boolean getUpdateContent() {
        return isUpdateContent;
    }

}
