package com.dr.digital.manage.form.command;

import com.dr.framework.common.form.core.command.AbstractFormDefinitionChangeCommand;
import com.dr.framework.common.form.core.entity.FormDefinition;
import com.dr.framework.common.form.core.entity.FormField;
import com.dr.framework.common.form.engine.Command;
import com.dr.framework.common.form.engine.CommandContext;
import com.dr.framework.common.form.engine.model.core.FieldModel;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author caor
 * @date 2021-08-24 12:03
 */
public class FormDefinitionFieldListAddCommand extends AbstractFormDefinitionChangeCommand implements Command<List<FormField>> {
    private final List<FieldModel> fieldModelList;

    public FormDefinitionFieldListAddCommand(String formDefinitionId, boolean updateTable, boolean copyData, List<FieldModel> fieldModelList) {
        super(formDefinitionId, updateTable, copyData);
        this.fieldModelList = fieldModelList;
    }

    public FormDefinitionFieldListAddCommand(String formCode, Integer version, boolean updateTable, boolean copyData, List<FieldModel> fieldModelList) {
        super(formCode, version, updateTable, copyData);
        this.fieldModelList = fieldModelList;
    }

    @Override
    public List<FormField> execute(CommandContext context) {
        //获取原来的表单定义
        FormDefinition old = getFormDefinition(context);
        Assert.isTrue(old != null, FORM_NOT_DEFINITION_ERROR);
        //复制新的表单对象
        FormDefinition newFormDefinition = copyFormDefinition(context, old);
        newFormDefinition.setDefault(true);
        List<FormField> formFieldList = new ArrayList<>();
        fieldModelList.forEach(fieldModel -> {
            //转换添加的字段
            FormField formField = newField(fieldModel);
            formField.setFormDefinitionId(newFormDefinition.getId());
            formField.setVersion(newFormDefinition.getVersion());
            //校验字段定义格式正确
            validateFieldBaseInfo(newFormDefinition, fieldModel);
            newFormDefinition.getFields().add(formField);
            formFieldList.add(formField);
        });
        //保存字段定义到数据库
        saveFormDefinition(context, newFormDefinition);
        //更新表结构
        if (isUpdateTable()) {
            //创建表结构
            createTable(context, newFormDefinition);
            if (isCopyData()) {
                copyTable(context, old, newFormDefinition);
            }
        }
        return formFieldList;
    }

    public List<FieldModel> getFieldList() {
        return fieldModelList;
    }

}
