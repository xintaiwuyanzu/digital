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
 * ?????????
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

    //TODO ????????????
    @Override
    public FormDefinition addForm(FormDefinition formDefinition) {
        //TODO ??????????????????
        List<FieldModel> fieldModels = getDefaultFields(formDefinition);
        FormDefinition definition = (FormDefinition) formDefinitionService.addFormDefinition(formDefinition, fieldModels, true);
        this.addFormDisplay(this.findFieldList(definition.getId()), definition);
        //???????????????????????????
        applicationEventPublisher.publishEvent(new ArchiveFormCreateEvent(definition));
        return formDefinition;
    }

    /**
     * ??????????????????????????????
     *
     * @param formFieldList  ??????????????????
     * @param formDefinition
     */
    @Override
    public void addFormDisplay(List<FormField> formFieldList, FormDefinition formDefinition) {
        FormDisplayScheme formDisplayScheme = new FormDisplayScheme();
        formDisplayScheme.setLabelWidth(100);
        //???????????????????????????
        String id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("??????");
        formDisplayScheme.setType("form");
        formDisplayScheme.setCode("form");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItem(formFieldList, formDisplayScheme);
        //???????????????????????????
        id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("??????");
        formDisplayScheme.setType("list");
        formDisplayScheme.setCode("list");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItem(formFieldList, formDisplayScheme);
        //?????????????????????
        id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("??????");
        formDisplayScheme.setType("search");
        formDisplayScheme.setCode("search");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItem(formFieldList, formDisplayScheme);
        //?????????????????????
        id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("??????");
        formDisplayScheme.setType("quality");
        formDisplayScheme.setCode("quality");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItem(formFieldList, formDisplayScheme);
        //???????????????????????????
        id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("??????");
        formDisplayScheme.setType("task");
        formDisplayScheme.setCode("task");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItem(formFieldList, formDisplayScheme);
        //???????????????????????????
        id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("??????");
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
        //???????????????????????????
        String id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("??????");
        formDisplayScheme.setType("form");
        formDisplayScheme.setCode("form");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItemForm(collect, formDisplayScheme);
        //?????????????????????
        id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("??????");
        formDisplayScheme.setType("quality");
        formDisplayScheme.setCode("quality");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItemForm(collect, formDisplayScheme);
        //???????????????????????????
        id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("??????");
        formDisplayScheme.setType("list");
        formDisplayScheme.setCode("list");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItemLieBao(formDefinition.getFields(), formDisplayScheme);
        //?????????????????????
        id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("??????");
        formDisplayScheme.setType("search");
        formDisplayScheme.setCode("search");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItemLieBao(formDefinition.getFields(), formDisplayScheme);
        //???????????????????????????
        id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("??????");
        formDisplayScheme.setType("task");
        formDisplayScheme.setCode("task");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItemLieBao(formDefinition.getFields(), formDisplayScheme);
        //???????????????????????????
        id = UUIDUtils.getUUID();
        formDisplayScheme.setId(id);
        formDisplayScheme.setName("??????");
        formDisplayScheme.setType("result");
        formDisplayScheme.setCode("result");
        formDisplayScheme.setFormDefinitionId(formDefinition.getId());
        formDisplayService.insert(formDisplayScheme);
        addFormDisplayItemLieBao(formDefinition.getFields(), formDisplayScheme);
    }

    /**
     * ??????????????????????????????
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

    // ??????????????????????????????
    public void addFormDisplayItemLieBao(List<FormField> newFormFieldList, FormDisplayScheme formDisplayScheme) {
        if (newFormFieldList.size() > 0) {
            //?????????????????????
            String[] addFields = {
                    ArchiveEntity.COLUMN_ARCHIVE_CODE,
                    ArchiveEntity.COLUMN_TITLE,
                    ArchiveEntity.COLUMN_DUTY_PERSON,
                    ArchiveEntity.COLUMN_FILECODE,
                    ArchiveEntity.COLUMN_YEAR,
                    ArchiveEntity.COLUMN_FILETIME,
                    //????????????????????????
                    ArchiveEntity.COLUMN_SPLIT_STATE,//????????????
                    ArchiveEntity.COLUMN_DISTINGUISH_STATE,//????????????
                    ArchiveEntity.COLUMN_DISASSEMBLY_STATE,//????????????
                    ArchiveEntity.COLUMN_TRANSITION_STATE,//????????????
//                    ArchiveEntity.COLUMN_QUALITY_STATE,//????????????
//                    ArchiveEntity.COLUMN_QUALITY_CONDUCT_STATE,//??????????????????
//                    ArchiveEntity.COLUMN_ASYNC_STATE,//????????????
                    ArchiveEntity.DISASSEMBLY_TAGGING//????????????
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
                    fieldDisplayScheme.setType("input");//????????????
                    //????????????table?????????
                    if (ArchiveEntity.COLUMN_TITLE.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setRemarks("200");
                    } else if (ArchiveEntity.COLUMN_ARCHIVE_CODE.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setRemarks("150");
                    }
                    formDisplayService.insertField(fieldDisplayScheme, true);
                    //?????????????????????
                    if (ArchiveEntity.COLUMN_STATUS.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setType("dict");//????????????
                        fieldDisplayScheme.setCode(newFormField.getFieldCode());
                        Map<String, String> meta = new HashMap(1);
                        meta.put("dict", "hjcode");
                        updateFieldDisplay(fieldDisplayScheme, meta);
                    } else if (ArchiveEntity.COLUMN_TRANSITION_STATE.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setType("dict");//????????????
                        fieldDisplayScheme.setCode(newFormField.getFieldCode());
                        Map<String, String> meta = new HashMap(1);
                        meta.put("dict", "zh");
                        updateFieldDisplay(fieldDisplayScheme, meta);
                    } else if (ArchiveEntity.COLUMN_SPLIT_STATE.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setType("dict");//??????jpg
                        fieldDisplayScheme.setCode(newFormField.getFieldCode());
                        Map<String, String> meta = new HashMap(1);
                        meta.put("dict", "cf");
                        updateFieldDisplay(fieldDisplayScheme, meta);
                    } else if (ArchiveEntity.COLUMN_DISTINGUISH_STATE.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setType("dict");//????????????
                        fieldDisplayScheme.setCode(newFormField.getFieldCode());
                        Map<String, String> meta = new HashMap(1);
                        meta.put("dict", "sb");
                        updateFieldDisplay(fieldDisplayScheme, meta);
                    } else if (ArchiveEntity.COLUMN_DISASSEMBLY_STATE.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setType("dict");//????????????
                        fieldDisplayScheme.setCode(newFormField.getFieldCode());
                        Map<String, String> meta = new HashMap(1);
                        meta.put("dict", "cj");
                        updateFieldDisplay(fieldDisplayScheme, meta);
                    } else if (ArchiveEntity.COLUMN_PACKET_STATE.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setType("dict");//????????????
                        fieldDisplayScheme.setCode(newFormField.getFieldCode());
                        Map<String, String> meta = new HashMap(1);
                        meta.put("dict", "fb");
                        updateFieldDisplay(fieldDisplayScheme, meta);
                    }else if (ArchiveEntity.DISASSEMBLY_TAGGING.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setType("dict");//????????????
                        fieldDisplayScheme.setCode(newFormField.getFieldCode());
                        Map<String, String> meta = new HashMap(1);
                        meta.put("dict", "tagging");
                        updateFieldDisplay(fieldDisplayScheme, meta);
                    }else if (ArchiveEntity.COLUMN_QUALITY_STATE.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setType("dict");//????????????
                        fieldDisplayScheme.setCode(newFormField.getFieldCode());
                        Map<String, String> meta = new HashMap(1);
                        meta.put("dict", "zj");
                        updateFieldDisplay(fieldDisplayScheme, meta);
                    }else if (ArchiveEntity.COLUMN_QUALITY_CONDUCT_STATE.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setType("dict");//??????????????????
                        fieldDisplayScheme.setCode(newFormField.getFieldCode());
                        Map<String, String> meta = new HashMap(1);
                        meta.put("dict", "zjjx");
                        updateFieldDisplay(fieldDisplayScheme, meta);
                    }else if (ArchiveEntity.COLUMN_ASYNC_STATE.equalsIgnoreCase(newFormField.getFieldCode())) {
                        fieldDisplayScheme.setType("dict");//??????????????????
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
     * ??????????????????????????????(??????)
     *
     * @param formDisplayScheme
     */
    public void addFormDisplayItem(List<FormField> newFormFieldList, FormDisplayScheme formDisplayScheme) {
        Assert.isTrue(newFormFieldList.size() > 0, "???????????????????????????");
        //?????????????????????
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
        //????????????????????????,newFormFieldList????????????????????????????????????id?????????
        FormDisplay oldFormDisplay = formDisplayService.getFormDisplay(newFormFieldList.get(0).getFormDefinitionId(), formDisplayScheme.getType(), formDisplayScheme.getCode());
        List<FieldDisplayScheme> oldFieldDisplaySchemeList = (List<FieldDisplayScheme>) oldFormDisplay.getFields();
        //?????????????????????????????????
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
        //?????????????????????????????????
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
     * ????????????????????????????????????
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
     * ??????id???????????????????????????????????????????????????????????????????????????
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
     * ??????id???????????????????????????????????????????????????????????????????????????
     *
     * @param formFieldList
     * @return
     */
    @Override
    public List<FormField> addFieldList(List<FormField> formFieldList) {
        Assert.isTrue(formFieldList.size() > 0, "??????????????????");
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
     * 1?????????????????????
     *
     * @param formId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long deleteForm(String formId) {
        FormModel formModel = formDefinitionService.selectFormDefinitionById(formId);
        //TODO ????????????????????????
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
     * ????????????????????????????????????
     */
    @Override
    public void afterPropertiesSet() {
        //???????????????????????????????????????
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
