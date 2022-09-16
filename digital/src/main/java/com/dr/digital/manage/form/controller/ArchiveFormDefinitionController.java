package com.dr.digital.manage.form.controller;

import com.dr.digital.manage.form.service.ArchiveFormDefinitionService;
import com.dr.digital.manage.log.annotation.SysLog;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.entity.ResultListEntity;
import com.dr.framework.common.form.core.entity.FormDefinition;
import com.dr.framework.common.form.core.entity.FormField;
import com.dr.framework.common.form.core.query.FormDefinitionQuery;
import com.dr.framework.common.form.core.service.FormDefinitionService;
import com.dr.framework.common.form.display.entity.FieldDisplayScheme;
import com.dr.framework.common.form.display.entity.FormDisplayScheme;
import com.dr.framework.common.form.engine.model.core.FieldType;
import com.dr.framework.common.form.engine.model.core.FormModel;
import com.dr.framework.common.form.engine.model.display.FieldDisplay;
import com.dr.framework.common.form.engine.model.display.FormDisplay;
import com.dr.framework.common.form.schema.model.JsonSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author lc
 * @date
 */
@RestController
@RequestMapping(value = "/api/manage/form")
public class ArchiveFormDefinitionController {
    @Autowired
    ArchiveFormDefinitionService archiveFormService;
    @Autowired
    CommonMapper commonMapper;
    @Autowired
    FormDefinitionService formDefinitionService;

    /*
     * =========================================
     * 表单定义开始
     * =========================================
     */
    @RequestMapping(value = "/addForm")
    @SysLog("添加表单")
    public ResultEntity addForm(FormDefinition formDefinition) {
        return ResultEntity.success(archiveFormService.addForm(formDefinition));
    }

    @RequestMapping(value = "/updateForm")
    @SysLog("更新表单")
    public ResultEntity updateForm(FormDefinition formDefinition) {
        return ResultEntity.success(archiveFormService.updateForm(formDefinition));
    }

    @RequestMapping(value = "/deleteForm")
    @SysLog("删除表单")
    public ResultEntity deleteForm(String id) {
        return ResultEntity.success(archiveFormService.deleteForm(id));
    }

    @RequestMapping(value = "/findFormPage")
    public ResultEntity findFormPage(FormDefinitionQuery formDefinitionQuery, FormDefinition formDefinition, @RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "15") int size) {
        formDefinitionQuery.versionAll();
        formDefinitionQuery.nameLike(formDefinition.getFormName());
        formDefinitionQuery.codeLike(formDefinition.getFormCode());
        return ResultEntity.success(archiveFormService.findFormPage(formDefinitionQuery, pageIndex, size));
    }

    @RequestMapping(value = "/findFormList")
    public ResultListEntity<? extends FormModel> findFormList() {
        FormDefinitionQuery formDefinitionQuery = new FormDefinitionQuery();
        List<? extends FormModel> formList = archiveFormService.findFormList(formDefinitionQuery);
        return ResultListEntity.success(formList);
    }

    /**
     * 查询单一表单定义数据
     *
     * @param formDefinitionId
     * @return
     */
    @RequestMapping(value = "/selectFormDefinitionById")
    public ResultEntity selectFormDefinitionById(String formDefinitionId) {
        FormModel formModel = archiveFormService.selectFormDefinitionById(formDefinitionId);
        return ResultEntity.success(formModel);
    }

    /*
     * =========================================
     * 表单定义结束
     * =========================================
     */
    /*
     * =========================================
     * 表单字段定义开始
     * =========================================
     */
    @RequestMapping(value = "/findFieldTypes")
    public ResultEntity findFieldTypes() {
        return ResultEntity.success(new ArrayList(Arrays.asList(FieldType.values())));
    }

    @RequestMapping(value = "/addField")
    @SysLog("表单添加字段")
    public ResultEntity addField(FormField formField) {
        return ResultEntity.success(archiveFormService.addField(formField));
    }

    @PostMapping(value = "/addFieldList")
    @SysLog("表单批量添加字段")
    public ResultEntity addFieldList(@RequestBody List<FormField> formFieldList) {
        return ResultEntity.success(archiveFormService.addFieldList(formFieldList));
    }

    @RequestMapping(value = "/updateField")
    @SysLog("更新表单字段")
    public ResultEntity updateField(FormField formField) {
        return ResultEntity.success(archiveFormService.updateField(formField));
    }

    @RequestMapping(value = "/deleteField")
    @SysLog("删除表单字段")
    public ResultEntity deleteField(String formDefinitionId, String fieldCode) {
        return ResultEntity.success(archiveFormService.deleteField(formDefinitionId, fieldCode));
    }

    @RequestMapping(value = "/findFieldList")
    public ResultListEntity<FormField> findFieldList(String formDefinitionId) {
        return ResultListEntity.success(archiveFormService.findFieldList(formDefinitionId));
    }

    /*
     * =========================================
     * 表单字段定义结束
     * =========================================
     */
    /*
     * =========================================
     * 表单显示方案开始
     * =========================================
     */

    /**
     * 添加显示方案
     *
     * @param formDisplayScheme
     * @return
     */
    @RequestMapping(value = "/addDisplay")
    public ResultEntity<FormDisplay> addDisplay(FormDisplayScheme formDisplayScheme) {
        return ResultEntity.success(archiveFormService.addDisplay(formDisplayScheme));
    }

    /**
     * 更新显示方案
     *
     * @param formDisplayScheme
     * @return
     */
    @RequestMapping(value = "/updateDisplay")
    public ResultEntity<FormDisplay> updateDisplay(FormDisplayScheme formDisplayScheme) {
        return ResultEntity.success(archiveFormService.updateDisplay(formDisplayScheme));
    }

    @RequestMapping(value = "/selectDisplay")
    public ResultEntity<FormDisplay> selectDisplay(String formDisplayId) {
        return ResultEntity.success(archiveFormService.selectFormDisplay(formDisplayId));
    }

    @RequestMapping(value = "/deleteDisplay")
    public ResultEntity<Long> deleteDisplay(String id) {
        return ResultEntity.success(archiveFormService.deleteFormDisplay(id));
    }

    @RequestMapping(value = "/selectDisplayByDefinition")
    public ResultEntity<FormDisplay> selectDisplay(String formDefinitionId, String formType, String formCode) {
        return ResultEntity.success(archiveFormService.selectFormDisplay(formDefinitionId, formType, formCode));
    }

    @RequestMapping(value = "/findDisplayList")
    public ResultListEntity<FormDisplay> findDisplayList(String formDefinitionId) {
        return ResultListEntity.success(archiveFormService.findDisplayList(formDefinitionId));
    }

    /*
     * =========================================
     * 表单显示方案结束
     * =========================================
     */
    /*
     * =========================================
     * 表单字段显示方案开始
     * =========================================
     */

    /**
     * 添加字段显示方案
     *
     * @param fieldDisplayScheme
     * @return
     */
    @RequestMapping(value = "/addFieldDisplay")
    public ResultEntity<FieldDisplay> addFieldDisplay(FieldDisplayScheme fieldDisplayScheme, String metaDict) {
        if (StringUtils.isEmpty(metaDict)) {
            return ResultEntity.success(archiveFormService.addFieldDisplay(fieldDisplayScheme, null));
        } else {
            Map<String, String> meta = new HashMap<>(1);
            meta.put("dict", metaDict);
            return ResultEntity.success(archiveFormService.addFieldDisplay(fieldDisplayScheme, meta));
        }
    }

    /**
     * 更新字段显示方案
     *
     * @param fieldDisplayScheme
     * @return
     */
    @RequestMapping(value = "/updateFieldDisplay")
    public ResultEntity<FieldDisplay> updateFieldDisplay(FieldDisplayScheme fieldDisplayScheme, String metaDict) {
        if (StringUtils.isEmpty(metaDict)) {
            return ResultEntity.success(archiveFormService.updateFieldDisplay(fieldDisplayScheme, null));
        } else {
            Map<String, String> meta = new HashMap<>(1);
            meta.put("dict", metaDict);
            return ResultEntity.success(archiveFormService.updateFieldDisplay(fieldDisplayScheme, meta));
        }
    }

    /**
     * 删除字段显示方案
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/deleteFieldDisplay")
    public ResultEntity<Long> updateFieldDisplay(String id) {
        return ResultEntity.success(archiveFormService.deleteFieldDisplay(id));
    }

    /*
     * =========================================
     * 表单字段显示方案结束
     * =========================================
     */

    @RequestMapping(value = "/jsonSchema")
    public JsonSchema jsonSchema(String formDisplayId) {
        return archiveFormService.jsonSchema(formDisplayId);
    }
}
