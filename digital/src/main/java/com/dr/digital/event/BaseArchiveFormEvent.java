package com.dr.digital.event;

import java.util.EventObject;

/**
 * @author dr
 */
public class BaseArchiveFormEvent extends EventObject {
    /**
     * 表单编码
     */
    private final String formCode;
    /**
     * 表单定义Id
     */
    private final String formDefinitionId;

    public BaseArchiveFormEvent(String formCode, String formDefinitionId) {
        super(formCode);
        this.formCode = formCode;
        this.formDefinitionId = formDefinitionId;
    }

    public String getFormCode() {
        return formCode;
    }

    public String getFormDefinitionId() {
        return formDefinitionId;
    }
}
