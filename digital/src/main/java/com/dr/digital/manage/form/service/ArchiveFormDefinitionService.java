package com.dr.digital.manage.form.service;

import com.dr.digital.configManager.bo.MataDataInfo;
import com.dr.framework.common.form.core.entity.FormDefinition;
import com.dr.framework.common.form.core.entity.FormField;
import com.dr.framework.common.form.core.query.FormDefinitionQuery;
import com.dr.framework.common.form.engine.model.core.FormModel;
import com.dr.framework.common.form.engine.model.display.FieldDisplay;
import com.dr.framework.common.form.engine.model.display.FormDisplay;
import com.dr.framework.common.form.schema.model.JsonSchema;
import com.dr.framework.common.page.Page;

import java.util.List;
import java.util.Map;

/**
 * 描述：
 *
 * @author tuzl
 * @date 2020/7/29 13:49
 */
public interface ArchiveFormDefinitionService {
    /**
     * 显示方案类型
     * 列表
     */
    String FORM_DISPLAY_TYPE = "list";

    /**
     * 表单
     */
    String FORM_DISPLAY_FORM = "form";

    /**
     * 搜索
     */
    String FORM_DISPLAY_SEARCH = "search";

    /**
     * 添加表单定义
     *
     * @param formDefinition
     * @return
     */
    FormDefinition addForm(FormDefinition formDefinition);

    /**
     * 更新表单定义
     *
     * @param formDefinition
     * @return
     */
    FormDefinition updateForm(FormDefinition formDefinition);

    /**
     * 删除表单定义
     *
     * @param formId
     * @return
     */
    Long deleteForm(String formId);

    /**
     * 查询表单定义分页
     *
     * @param formDefinitionQuery
     * @param index
     * @param size
     * @return
     */
    Page<? extends FormModel> findFormPage(FormDefinitionQuery formDefinitionQuery, int index, int size);

    /**
     * 查询表单定义list
     *
     * @param formDefinitionQuery
     * @return
     */
    List<? extends FormModel> findFormList(FormDefinitionQuery formDefinitionQuery);

    /**
     * 查询单一表单定义数据
     *
     * @param formDefinitionId
     * @return
     */
    FormModel selectFormDefinitionById(String formDefinitionId);

    /**
     * 添加表单字段
     *
     * @param formField
     * @return
     */
    FormField addField(FormField formField);

    /**
     * 批量添加表单字段
     *
     * @param formFieldList
     * @return
     */
    List<FormField> addFieldList(List<FormField> formFieldList);

    /**
     * 更新表单字段定义
     *
     * @param formField
     * @return
     */
    FormField updateField(FormField formField);

    /**
     * 删除表单字段定义
     *
     * @param formDefinitionId
     * @param fieldCode
     * @return
     */
    FormField deleteField(String formDefinitionId, String fieldCode);

    /**
     * 查询字段list
     *
     * @param formDefinitionId
     * @return
     */
    List<FormField> findFieldList(String formDefinitionId);

    /**
     * 查询显示方案list
     *
     * @param formDefinitionId
     * @return
     */
    List<FormDisplay> findDisplayList(String formDefinitionId);

    /**
     * 添加显示方案
     *
     * @param formDisplay
     * @return
     */
    FormDisplay addDisplay(FormDisplay formDisplay);

    /**
     * 删除表单显示方案
     *
     * @param formDisplayId
     * @return
     */
    long deleteFormDisplay(String formDisplayId);

    /**
     * 添加字段显示方案
     *
     * @param fieldDisplay
     * @param metaMap      额外参数
     * @return
     */
    FieldDisplay addFieldDisplay(FieldDisplay fieldDisplay, Map<String, String> metaMap);

    /**
     * 更新字段显示方案
     *
     * @param fieldDisplay
     * @param metaMap      额外参数
     * @return
     */
    FieldDisplay updateFieldDisplay(FieldDisplay fieldDisplay, Map<String, String> metaMap);

    /**
     * 删除字段显示方案
     *
     * @param fieldId
     * @return
     */
    long deleteFieldDisplay(String fieldId);

    /**
     * 更新显示方案
     *
     * @param formDisplay
     * @return
     */
    FormDisplay updateDisplay(FormDisplay formDisplay);

    /**
     * 查询显示方案
     *
     * @param formDisplayId
     * @return
     */
    FormDisplay selectFormDisplay(String formDisplayId);

    /**
     * 根据表单定义Id，表单类型，表单编码查询表单显示方案
     *
     * @param formDefinitionId
     * @param formType
     * @param formCode
     * @return
     */
    FormDisplay selectFormDisplay(String formDefinitionId, String formType, String formCode);

    /**
     * 显示方案
     *
     * @param formDisplayId
     * @return
     */
    JsonSchema jsonSchema(String formDisplayId);

    /**
     * 添加默认表单显示方案
     *
     * @param formFieldList  新添加的字段
     * @param formDefinition
     */
    void addFormDisplay(List<FormField> formFieldList, FormDefinition formDefinition);

    void addFormDisplay(MataDataInfo collect, FormDefinition formDefinition);
}
