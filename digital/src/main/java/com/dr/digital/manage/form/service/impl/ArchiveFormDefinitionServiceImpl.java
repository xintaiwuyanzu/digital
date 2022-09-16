package com.dr.digital.manage.form.service.impl;

import com.dr.digital.configManager.bo.MataData;
import com.dr.digital.configManager.bo.MataDataInfo;
import com.dr.digital.enums.CategoryType;
import com.dr.digital.enums.KindType;
import com.dr.digital.event.ArchiveFormCreateEvent;
import com.dr.digital.manage.form.command.FormDefinitionFieldListAddCommand;
import com.dr.digital.manage.form.service.ArchiveFormDefinitionService;
import com.dr.digital.manage.model.entity.AbstractArchiveEntity;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.util.Constants;
import com.dr.digital.util.UUIDUtils;
import com.dr.framework.common.entity.IdEntity;
import com.dr.framework.common.entity.StatusEntity;
import com.dr.framework.common.form.core.entity.FormDefinition;
import com.dr.framework.common.form.core.entity.FormField;
import com.dr.framework.common.form.core.query.FormDefinitionQuery;
import com.dr.framework.common.form.core.service.FormDefinitionService;
import com.dr.framework.common.form.display.entity.FieldDisplayScheme;
import com.dr.framework.common.form.display.entity.FormDisplayScheme;
import com.dr.framework.common.form.display.service.FormDisplayService;
import com.dr.framework.common.form.engine.CommandExecutor;
import com.dr.framework.common.form.engine.model.core.FieldModel;
import com.dr.framework.common.form.engine.model.core.FormModel;
import com.dr.framework.common.form.engine.model.display.FieldDisplay;
import com.dr.framework.common.form.engine.model.display.FormDisplay;
import com.dr.framework.common.form.schema.model.JsonSchema;
import com.dr.framework.common.page.Page;
import com.dr.framework.common.service.DataBaseService;
import com.dr.framework.core.orm.database.Dialect;
import com.dr.framework.core.orm.database.tools.AnnotationTableReaderUtil;
import com.dr.framework.core.orm.module.EntityRelation;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 描述：
 *
 * @author tuzl
 * @date 2020/7/29 13:49
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Order(Ordered.LOWEST_PRECEDENCE - 2)
public class ArchiveFormDefinitionServiceImpl implements ArchiveFormDefinitionService, InitializingBean {
    @Autowired
    FormDefinitionService formDefinitionService;
    @Autowired
    FormDisplayService formDisplayService;
    @Autowired
    ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    protected CommandExecutor commandExecutor;

    //TODO 需要修改
    @Override
    public FormDefinition addForm(FormDefinition formDefinition) {
        //TODO 判断表单类型
        List<FieldModel> fieldModels = getDefaultFields(formDefinition);
        FormDefinition definition = (FormDefinition) formDefinitionService.addFormDefinition(formDefinition, fieldModels, true);
        this.addFormDisplay(this.findFieldList(definition.getId()), definition);
        //创建表单时发布事件
        applicationEventPublisher.publishEvent(new ArchiveFormCreateEvent(definition));
        return formDefinition;
    }

    /**
     * 添加默认表单显示方案
     *
     * @param formFieldList  新添加的字段
     * @param formDefinition
     */
    @Override
    public void addFormDisplay(List<FormField> formFieldList, FormDefinition formDefinition) {
        FormDisplayScheme formDisplayScheme = new FormDisplayScheme();
        formDisplayScheme.setLabelWidth(100);
        //添加编辑的显示方案
        String id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("添加");
        formDisplayScheme.setType("form");
        formDisplayScheme.setCode("form");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItem(formFieldList, formDisplayScheme);
        //列表页面的显示方案
        id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("列表");
        formDisplayScheme.setType("list");
        formDisplayScheme.setCode("list");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItem(formFieldList, formDisplayScheme);
        //查询的显示方案
        id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("查询");
        formDisplayScheme.setType("search");
        formDisplayScheme.setCode("search");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItem(formFieldList, formDisplayScheme);
        //质检的显示方案
        id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("质检");
        formDisplayScheme.setType("quality");
        formDisplayScheme.setCode("quality");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItem(formFieldList, formDisplayScheme);
        //任务列表的显示方案
        id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("任务");
        formDisplayScheme.setType("task");
        formDisplayScheme.setCode("task");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItem(formFieldList, formDisplayScheme);
        //结果列表的显示方案
        id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("结果");
        formDisplayScheme.setType("result");
        formDisplayScheme.setCode("result");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItem(formFieldList, formDisplayScheme);
    }

    @Override
    public void addFormDisplay(MataDataInfo collect, FormDefinition formDefinition) {
        FormDisplayScheme formDisplayScheme = new FormDisplayScheme();
        formDisplayScheme.setLabelWidth(100);
        //添加编辑的显示方案
        String id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("添加");
        formDisplayScheme.setType("form");
        formDisplayScheme.setCode("form");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItemForm(collect, formDisplayScheme);
        //质检的显示方案
        id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("质检");
        formDisplayScheme.setType("quality");
        formDisplayScheme.setCode("quality");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItemForm(collect, formDisplayScheme);
        //列表页面的显示方案
        id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("列表");
        formDisplayScheme.setType("list");
        formDisplayScheme.setCode("list");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItemLieBao(formDefinition.getFields(), formDisplayScheme);
        //查询的显示方案
        id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("查询");
        formDisplayScheme.setType("search");
        formDisplayScheme.setCode("search");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItemLieBao(formDefinition.getFields(), formDisplayScheme);
        //任务列表的显示方案
        id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("任务");
        formDisplayScheme.setType("task");
        formDisplayScheme.setCode("task");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItemLieBao(formDefinition.getFields(), formDisplayScheme);
        //结果列表的显示方案
        id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("结果");
        formDisplayScheme.setType("result");
        formDisplayScheme.setCode("result");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItemLieBao(formDefinition.getFields(), formDisplayScheme);
    }

    /**
     * 对接状态下的显示方案
     *
     * @param collect
     * @param formDisplayScheme
     */
    public void addFormDisplayItemForm(MataDataInfo collect, FormDisplayScheme formDisplayScheme) {
        List<FormField> newFormFieldList = new ArrayList<>();
        for (MataData mataData : collect.getMetadata()) {
            FormField formField = new FormField();
            formField.setLabel(mataData.getName());
            formField.setFieldCode(mataData.geteName());
            formField.setFieldLength(255);
            formField.setDescription(StatusEntity.STATUS_DISABLE_STR);
            formField.setFieldTypeStr("STRING");
            if (!StringUtils.isEmpty(mataData.getOrders())) {
                formField.setOrder(Integer.valueOf(mataData.getOrders()));
            }
            newFormFieldList.add(formField);
        }
        List<String> collectList = newFormFieldList.stream().map(formField -> formField.getFieldCode()).collect(Collectors.toList());
        String[] addFields = collectList.toArray(new String[newFormFieldList.size()]);
        String[] finalAddFields = addFields;
        newFormFieldList.forEach(newFormField -> {
            if (!ArrayUtils.contains(finalAddFields, newFormField.getFieldCode())) {
            } else {
                FieldDisplayScheme fieldDisplayScheme = new FieldDisplayScheme();
                fieldDisplayScheme.setFormDisplayId(formDisplayScheme.getId());
                fieldDisplayScheme.setOrder(newFormField.getOrder());
                fieldDisplayScheme.setCode(newFormField.getFieldCode());
                fieldDisplayScheme.setName(newFormField.getLabel());
                fieldDisplayScheme.setType("input");
                fieldDisplayScheme.setLabelWidth(100);
                if (ArchiveEntity.COLUMN_TITLE.equalsIgnoreCase(newFormField.getFieldCode())) {
                    fieldDisplayScheme.setRemarks("200");
                } else if (ArchiveEntity.COLUMN_ARCHIVE_CODE.equalsIgnoreCase(newFormField.getFieldCode())) {
                    fieldDisplayScheme.setRemarks("150");
                }
                formDisplayService.insertField(fieldDisplayScheme);
            }
        });
    }

    // 对接状态下的显示方案
    public void addFormDisplayItemLieBao(List<FormField> newFormFieldList, FormDisplayScheme formDisplayScheme) {
        if (newFormFieldList.size() > 0) {
            //默认的显示方案
            String[] addFields = {
                    ArchiveEntity.COLUMN_ARCHIVE_CODE,
                    ArchiveEntity.COLUMN_TITLE,
                    ArchiveEntity.COLUMN_DUTY_PERSON,
                    ArchiveEntity.COLUMN_FILECODE,
                    ArchiveEntity.COLUMN_YEAR,
                    ArchiveEntity.COLUMN_FILETIME,
                    //需求中要求添加的
                    ArchiveEntity.COLUMN_SPLIT_STATE,//拆分状态
                    ArchiveEntity.COLUMN_DISTINGUISH_STATE,//识别状态
                    ArchiveEntity.COLUMN_DISASSEMBLY_STATE,//拆件状态
                    ArchiveEntity.COLUMN_TRANSITION_STATE,//转换状态
//                    ArchiveEntity.COLUMN_QUALITY_STATE,//质检状态
//                    ArchiveEntity.COLUMN_QUALITY_CONDUCT_STATE,//质检进行状态
//                    ArchiveEntity.COLUMN_ASYNC_STATE,//异步状态
                    ArchiveEntity.DISASSEMBLY_TAGGING//标注状态
            };
            if (formDisplayScheme.getType().equals("task")) {
                String[] addFieldsTask = {
                        ArchiveEntity.COLUMN_ARCHIVE_CODE,
                        ArchiveEntity.COLUMN_TITLE,
                        ArchiveEntity.COLUMN_DUTY_PERSON,
                        ArchiveEntity.COLUMN_FILECODE,
                        ArchiveEntity.COLUMN_YEAR,
                        ArchiveEntity.COLUMN_FILETIME,
                        ArchiveEntity.COLUMN_STATUS
                };
                addFields = addFieldsTask;
            } else if (formDisplayScheme.getType().equals("result")) {
                String[] addFieldsTask = {
                        ArchiveEntity.COLUMN_ARCHIVE_CODE,
                        ArchiveEntity.COLUMN_TITLE,
                        ArchiveEntity.COLUMN_DUTY_PERSON,
                        ArchiveEntity.COLUMN_FILECODE,
                        ArchiveEntity.COLUMN_YEAR,
                        ArchiveEntity.COLUMN_FILETIME,
                        ArchiveEntity.COLUMN_TRANSITION_STATE,
                        ArchiveEntity.COLUMN_PACKET_STATE
                };
                addFields = addFieldsTask;
            }
            String[] finalAddFields = addFields;
            newFormFieldList.forEach(newFormField -> {
                if (!ArrayUtils.contains(finalAddFields, newFormField.getFieldCode())) {
                } else {
                    FieldDisplayScheme fieldDisplayScheme = new FieldDisplayScheme();
                    fieldDisplayScheme.setId(UUIDUtils.getUUID());
                    fieldDisplayScheme.setFormDisplayId(formDisplayScheme.getId());
                    fieldDisplayScheme.setOrder(newFormField.getOrder());
                    fieldDisplayScheme.setCode(newFormField.getFieldCode());
                    fieldDisplayScheme.setName(newFormField.getLabel());
                    fieldDisplayScheme.setLabelWidth(100);
                    fieldDisplayScheme.setType("input");//当前环节
                    //设置前端table列宽度
                    if (ArchiveEntity.COLUMN_TITLE.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setRemarks("200");
                    } else if (ArchiveEntity.COLUMN_ARCHIVE_CODE.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setRemarks("150");
                    }
                    formDisplayService.insertField(fieldDisplayScheme, true);
                    //先添加后再更新
                    if (ArchiveEntity.COLUMN_STATUS.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setType("dict");//当前环节
                        fieldDisplayScheme.setCode(newFormField.getFieldCode());
                        Map<String, String> meta = new HashMap(1);
                        meta.put("dict", "hjcode");
                        updateFieldDisplay(fieldDisplayScheme, meta);
                    } else if (ArchiveEntity.COLUMN_TRANSITION_STATE.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setType("dict");//转换状态
                        fieldDisplayScheme.setCode(newFormField.getFieldCode());
                        Map<String, String> meta = new HashMap(1);
                        meta.put("dict", "zh");
                        updateFieldDisplay(fieldDisplayScheme, meta);
                    } else if (ArchiveEntity.COLUMN_SPLIT_STATE.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setType("dict");//拆分jpg
                        fieldDisplayScheme.setCode(newFormField.getFieldCode());
                        Map<String, String> meta = new HashMap(1);
                        meta.put("dict", "cf");
                        updateFieldDisplay(fieldDisplayScheme, meta);
                    } else if (ArchiveEntity.COLUMN_DISTINGUISH_STATE.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setType("dict");//识别状态
                        fieldDisplayScheme.setCode(newFormField.getFieldCode());
                        Map<String, String> meta = new HashMap(1);
                        meta.put("dict", "sb");
                        updateFieldDisplay(fieldDisplayScheme, meta);
                    } else if (ArchiveEntity.COLUMN_DISASSEMBLY_STATE.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setType("dict");//拆件状态
                        fieldDisplayScheme.setCode(newFormField.getFieldCode());
                        Map<String, String> meta = new HashMap(1);
                        meta.put("dict", "cj");
                        updateFieldDisplay(fieldDisplayScheme, meta);
                    } else if (ArchiveEntity.COLUMN_PACKET_STATE.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setType("dict");//封包状态
                        fieldDisplayScheme.setCode(newFormField.getFieldCode());
                        Map<String, String> meta = new HashMap(1);
                        meta.put("dict", "fb");
                        updateFieldDisplay(fieldDisplayScheme, meta);
                    }else if (ArchiveEntity.DISASSEMBLY_TAGGING.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setType("dict");//标注状态
                        fieldDisplayScheme.setCode(newFormField.getFieldCode());
                        Map<String, String> meta = new HashMap(1);
                        meta.put("dict", "tagging");
                        updateFieldDisplay(fieldDisplayScheme, meta);
                    }else if (ArchiveEntity.COLUMN_QUALITY_STATE.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setType("dict");//质检状态
                        fieldDisplayScheme.setCode(newFormField.getFieldCode());
                        Map<String, String> meta = new HashMap(1);
                        meta.put("dict", "zj");
                        updateFieldDisplay(fieldDisplayScheme, meta);
                    }else if (ArchiveEntity.COLUMN_QUALITY_CONDUCT_STATE.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setType("dict");//质检进行状态
                        fieldDisplayScheme.setCode(newFormField.getFieldCode());
                        Map<String, String> meta = new HashMap(1);
                        meta.put("dict", "zjjx");
                        updateFieldDisplay(fieldDisplayScheme, meta);
                    }else if (ArchiveEntity.COLUMN_ASYNC_STATE.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setType("dict");//质检进行状态
                        fieldDisplayScheme.setCode(newFormField.getFieldCode());
                        Map<String, String> meta = new HashMap(1);
                        meta.put("dict", "async");
                        updateFieldDisplay(fieldDisplayScheme, meta);
                    }

                }
            });
        }
    }

    /**
     * 添加默认字段显示方案(通用)
     *
     * @param formDisplayScheme
     */
    public void addFormDisplayItem(List<FormField> newFormFieldList, FormDisplayScheme formDisplayScheme) {
        Assert.isTrue(newFormFieldList.size() > 0, "新增字段不能为空！");
        //默认的显示方案
        String[] addFields = {
                ArchiveEntity.COLUMN_ARCHIVE_CODE,
                ArchiveEntity.COLUMN_TITLE,
                ArchiveEntity.COLUMN_DUTY_PERSON,
                ArchiveEntity.COLUMN_FILECODE,
                ArchiveEntity.COLUMN_YEAR,
                ArchiveEntity.COLUMN_FILETIME
        };
        if (formDisplayScheme.getType().equals("quality") | formDisplayScheme.getType().equals("form")) {
            List<String> collect = newFormFieldList.stream().map(formField -> formField.getFieldCode()).collect(Collectors.toList());
            addFields = collect.toArray(new String[newFormFieldList.size()]);
        }
        //获取老的表单方案,newFormFieldList中字段为新添加的但是表单id为老的
        FormDisplay oldFormDisplay = formDisplayService.getFormDisplay(newFormFieldList.get(0).getFormDefinitionId(), formDisplayScheme.getType(), formDisplayScheme.getCode());
        List<FieldDisplayScheme> oldFieldDisplaySchemeList = (List<FieldDisplayScheme>) oldFormDisplay.getFields();
        //复制上一版本的表单方案
        oldFieldDisplaySchemeList.forEach(oldFieldDisplayScheme -> {
            FieldDisplayScheme fieldDisplayScheme = new FieldDisplayScheme();
            fieldDisplayScheme.setFormDisplayId(formDisplayScheme.getId());
            fieldDisplayScheme.setOrder(oldFieldDisplayScheme.getOrder());
            fieldDisplayScheme.setCode(oldFieldDisplayScheme.getCode());
            fieldDisplayScheme.setName(oldFieldDisplayScheme.getName());
            fieldDisplayScheme.setType(oldFieldDisplayScheme.getType());
            fieldDisplayScheme.setRemarks(oldFieldDisplayScheme.getRemarks());
            fieldDisplayScheme.setLabelWidth(oldFieldDisplayScheme.getLabelWidth());
            fieldDisplayScheme.setDescription(oldFieldDisplayScheme.getDescription());
            formDisplayService.insertField(fieldDisplayScheme);
        });
        //新添加的字段初始默认值
        String[] finalAddFields = addFields;
        newFormFieldList.forEach(newFormField -> {
            if (!ArrayUtils.contains(finalAddFields, newFormField.getFieldCode())) {
            } else {
                FieldDisplayScheme fieldDisplayScheme = new FieldDisplayScheme();
                fieldDisplayScheme.setFormDisplayId(formDisplayScheme.getId());
                fieldDisplayScheme.setOrder(newFormField.getOrder());
                fieldDisplayScheme.setCode(newFormField.getFieldCode());
                fieldDisplayScheme.setName(newFormField.getLabel());
                fieldDisplayScheme.setType("input");
                fieldDisplayScheme.setLabelWidth(100);
                if ("list".equals(formDisplayScheme.getType()) && !ArchiveEntity.COLUMN_TITLE.equalsIgnoreCase(newFormField.getFieldCode())) {
                    fieldDisplayScheme.setRemarks("130");
                } else if ("form".equals(formDisplayScheme.getType()) && (ArchiveEntity.COLUMN_TITLE.equalsIgnoreCase(newFormField.getFieldCode()) || ArchiveEntity.COLUMN_NOTE.equalsIgnoreCase(newFormField.getFieldCode()))) {
                    fieldDisplayScheme.setRemarks("200");
                } else {
                    fieldDisplayScheme.setRemarks("100");
                }
                formDisplayService.insertField(fieldDisplayScheme);
            }
        });
    }

    /**
     * 获取所有档案表都有的字段
     *
     * @param formDefinition
     * @return
     */
    private List<FieldModel> getDefaultFields(FormDefinition formDefinition) {
        CategoryType categoryType = CategoryType.from(formDefinition.getFormType());
        Class clz;
        switch (categoryType) {
            case PRO:
                clz = KindType.OTHER.getProBaseClass();
                break;
            case ARC:
                clz = KindType.Document.getArcBaseClass();
                break;
            case FILE:
                clz = KindType.OTHER.getFileBaseClass();
                break;
            case BOX:
                clz = KindType.OTHER.getBoxBaseClass();
                break;
            default:
                clz = KindType.OTHER.getArcBaseClass();
                break;
        }
        EntityRelation entityRelation = archiveParentEntityMap.get(clz);
        return entityRelation.getColumns().stream().filter(f -> !f.getName().equalsIgnoreCase(IdEntity.ID_COLUMN_NAME)).map(ColumnFieldModel::new).collect(Collectors.toList());
    }

    /**
     * 表单id必须传值，不传值则无法将上一版本的方案带入下一版本
     *
     * @param formField
     * @return
     */
    @Override
    public FormField addField(FormField formField) {
        FormField newFormField = (FormField) formDefinitionService.addField(formField.getFormDefinitionId(), formField, true);
        List<FormField> fieldList = new ArrayList<>();
        fieldList.add(formField);
        addFormDisplay(fieldList, (FormDefinition) formDefinitionService.selectFormDefinitionById(newFormField.getFormDefinitionId()));
        return newFormField;
    }

    /**
     * 表单id必须传值，不传值则无法将上一版本的方案带入下一版本
     *
     * @param formFieldList
     * @return
     */
    @Override
    public List<FormField> addFieldList(List<FormField> formFieldList) {
        Assert.isTrue(formFieldList.size() > 0, "字段信息为空");
        List<FieldModel> fieldModelList = new ArrayList<>(formFieldList);
        List<FormField> newFormFieldList = commandExecutor.execute(new FormDefinitionFieldListAddCommand(formFieldList.get(0).getFormDefinitionId(), true, true, fieldModelList));
        addFormDisplay(formFieldList, (FormDefinition) formDefinitionService.selectFormDefinitionById(newFormFieldList.get(0).getFormDefinitionId()));
        return newFormFieldList;
    }

    @Override
    public FormDefinition updateForm(FormDefinition formDefinition) {
        return (FormDefinition) formDefinitionService.updateFormDefinitionBaseInfo(formDefinition);
    }

    @Override
    public FormField updateField(FormField formField) {
        return (FormField) formDefinitionService.changeField(formField.getFormDefinitionId(), formField, true);
    }

    /**
     * 1、删除显示方案
     *
     * @param formId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long deleteForm(String formId) {
        FormModel formModel = formDefinitionService.selectFormDefinitionById(formId);
        //TODO 发布表单删除事件
        //applicationEventPublisher.publishEvent(new ArchiveFormDeleteEvent(formModel));
        List<FormDisplay> formDisplayList = findDisplayList(formId);
        formDisplayList.forEach(formDisplay -> deleteFormDisplay(formDisplay.getId()));
        return formDefinitionService.removeFormDefinitionById(formId, true);
    }

    @Override
    public FormField deleteField(String formDefinitionId, String fieldCode) {
        return (FormField) formDefinitionService.removeField(formDefinitionId, fieldCode);
    }

    @Override
    public Page<? extends FormModel> findFormPage(FormDefinitionQuery formDefinitionQuery, int index, int size) {
        return formDefinitionService.selectPageFormDefinition(formDefinitionQuery, index, size);
    }

    @Override
    public List<? extends FormModel> findFormList(FormDefinitionQuery formDefinitionQuery) {
        return formDefinitionService.selectFormDefinitionByQuery(formDefinitionQuery);
    }

    @Override
    public FormModel selectFormDefinitionById(String formDefinitionId) {
        return formDefinitionService.selectFormDefinitionById(formDefinitionId);
    }

    @Override
    public List<FormField> findFieldList(String formDefinitionId) {
        FormDefinition formDefinition = (FormDefinition) formDefinitionService.selectFormDefinitionById(formDefinitionId);
        return formDefinition.getFields();
    }

    @Override
    public List<FormDisplay> findDisplayList(String formDefinitionId) {
        return formDisplayService.getFormDisplay(formDefinitionId);
    }

    @Override
    public FormDisplay addDisplay(FormDisplay formDisplay) {
        return formDisplayService.insert(formDisplay, true);
    }

    @Override
    public long deleteFormDisplay(String formDisplayId) {
        return formDisplayService.deleteFormDisplay(formDisplayId, true);
    }

    @Override
    public FieldDisplay addFieldDisplay(FieldDisplay fieldDisplay, Map<String, String> metaMap) {
        fieldDisplay = formDisplayService.insertField(fieldDisplay, true);
        if (metaMap != null) {
            formDisplayService.setFieldMeta(fieldDisplay.getFormDisplayId(), fieldDisplay.getCode(), metaMap);
        }
        return fieldDisplay;
    }

    @Override
    public FieldDisplay updateFieldDisplay(FieldDisplay fieldDisplay, Map<String, String> metaMap) {
        fieldDisplay = formDisplayService.updateField(fieldDisplay, true);
        if (metaMap != null) {
            formDisplayService.setFieldMeta(fieldDisplay.getFormDisplayId(), fieldDisplay.getCode(), metaMap);
        }
        return fieldDisplay;
    }

    @Override
    public long deleteFieldDisplay(String fieldId) {
        return formDisplayService.deleteField(fieldId, true);
    }

    @Override
    public FormDisplay updateDisplay(FormDisplay formDisplay) {
        return formDisplayService.update(formDisplay, true);
    }

    @Override
    public FormDisplay selectFormDisplay(String formDisplayId) {
        return formDisplayService.getFormDisplayById(formDisplayId);
    }

    @Override
    public FormDisplay selectFormDisplay(String formDefinitionId, String formType, String formCode) {
        return formDisplayService.getFormDisplay(formDefinitionId, formType, formCode);
    }

    @Override
    public JsonSchema jsonSchema(String formDisplayId) {
        JsonSchema jsonSchema = new JsonSchema();
        FormDisplay formDisplay = formDisplayService.getFormDisplayById(formDisplayId);
        FormModel formModel = formDefinitionService.selectFormDefinitionById(formDisplay.getFormDefinitionId());
        jsonSchema.setFormDisplay(formDisplay);
        jsonSchema.setFormModel(formModel);
        return jsonSchema;
    }

    @Autowired
    DataBaseService dataBaseService;
    Map<Class, EntityRelation> archiveParentEntityMap;

    /**
     * 缓存所有门类的默认表结构
     */
    @Override
    public void afterPropertiesSet() {
        //启动时初始化默认表结构信息
        initFormTemplateRelations();
    }

    private void initFormTemplateRelations() {
        Dialect dialect = dataBaseService.getDataBaseMetaDataByModuleName(Constants.MODULE_NAME).getDialect();
        KindType[] kindTypes = KindType.values();
        archiveParentEntityMap = Collections.synchronizedMap(new HashMap<>(kindTypes.length * 3));
        for (KindType kindType : kindTypes) {
            addIfAbsent(kindType.getArcBaseClass(), dialect);
            addIfAbsent(kindType.getFileBaseClass(), dialect);
            addIfAbsent(kindType.getProBaseClass(), dialect);
            addIfAbsent(kindType.getBoxBaseClass(), dialect);
        }
        addIfAbsent(AbstractArchiveEntity.class, dialect);
    }

    private void addIfAbsent(Class clazz, Dialect dialect) {
        if (clazz != null) {
            archiveParentEntityMap.computeIfAbsent(clazz, c -> {
                EntityRelation relation = new EntityRelation(false);
                AnnotationTableReaderUtil.readColumnInfo(relation, c, dialect);
                return relation;
            });
        }
    }

}
