package com.dr.digital.event;

import com.dr.framework.common.form.engine.model.core.FormModel;

/**
 * 档案表单删除事件
 *
 * @author dr
 */
public class ArchiveFormDeleteEvent extends BaseArchiveFormEvent {
    private final FormModel formModel;

    public ArchiveFormDeleteEvent(FormModel formModel) {
        super(formModel.getFormCode(), formModel.getId());
        this.formModel = formModel;
    }

    public FormModel getFormModel() {
        return formModel;
    }
}
