package com.dr.digital.event;

import com.dr.framework.common.form.engine.model.core.FormModel;

/**
 * 档案表单创建事件
 *
 * @author dr
 */
public class ArchiveFormCreateEvent extends BaseArchiveFormEvent {
    private final FormModel formModel;

    public ArchiveFormCreateEvent(FormModel formModel) {
        super(formModel.getFormCode(), formModel.getId());
        this.formModel = formModel;
    }

    public FormModel getFormModel() {
        return formModel;
    }
}