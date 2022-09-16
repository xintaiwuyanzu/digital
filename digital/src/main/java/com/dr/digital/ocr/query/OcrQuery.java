package com.dr.digital.ocr.query;

import com.dr.digital.manage.model.query.ArchiveDataQuery;

public class OcrQuery extends ArchiveDataQuery {
    
    /**
     * 表单id
     */
    private String formId;

    /**
     * 挂接全宗门类
     */
    private String hookFondCategory;

    /**
     * 案卷表单Id
     */
    private String ajFormDefinitionId;

    /**
     * 文件表单ID
     */
    private String wjFormDefinitionId;

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public String getHookFondCategory() {
        return hookFondCategory;
    }

    public void setHookFondCategory(String hookFondCategory) {
        this.hookFondCategory = hookFondCategory;
    }

    public String getAjFormDefinitionId() {
        return ajFormDefinitionId;
    }

    public void setAjFormDefinitionId(String ajFormDefinitionId) {
        this.ajFormDefinitionId = ajFormDefinitionId;
    }

    public String getWjFormDefinitionId() {
        return wjFormDefinitionId;
    }

    public void setWjFormDefinitionId(String wjFormDefinitionId) {
        this.wjFormDefinitionId = wjFormDefinitionId;
    }

}
