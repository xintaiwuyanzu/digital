package com.dr.digital.event;

import java.util.EventObject;

/**
 * 基础的表单数据事件
 *
 * @author dr
 */
public class BaseArchiveDataEvent extends EventObject {
    /**
     * 分类Id,表单定义Id
     */
    private final String categoryId;
    private final String formDefinitionId;

    public BaseArchiveDataEvent(String categoryId, String formDefinitionId, String dataId) {
        super(dataId);
        this.categoryId = categoryId;
        this.formDefinitionId = formDefinitionId;
    }

    public String getDataId() {
        return (String) getSource();
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getFormDefinitionId() {
        return formDefinitionId;
    }
}
