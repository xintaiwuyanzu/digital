package com.dr.digital.configManager.service.impl;

import com.dr.digital.configManager.bo.CategoryBspDict;
import com.dr.digital.configManager.bo.CategoryInfo;
import com.dr.digital.configManager.bo.FieldMetaData;
import com.dr.digital.enums.CategoryType;
import com.dr.digital.manage.category.entity.CategoryConfig;
import com.dr.digital.manage.category.entity.CategoryConfigInfo;
import com.dr.digital.manage.category.service.CategoryConfigService;
import com.dr.digital.manage.model.entity.AbstractArchiveEntity;
import com.dr.digital.register.entity.Category;
import com.dr.digital.register.entity.Register;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.IdEntity;
import com.dr.framework.common.entity.StatusEntity;
import com.dr.framework.common.form.core.entity.FormDefinition;
import com.dr.framework.common.form.core.entity.FormField;
import com.dr.framework.common.form.engine.model.core.FieldModel;
import com.dr.framework.common.form.engine.model.core.FieldType;
import com.dr.framework.common.form.util.Constants;
import com.dr.framework.common.service.DataBaseService;
import com.dr.framework.core.organise.entity.Organise;
import com.dr.framework.core.organise.query.OrganiseQuery;
import com.dr.framework.core.organise.service.OrganisePersonService;
import com.dr.framework.core.orm.database.Dialect;
import com.dr.framework.core.orm.database.tools.AnnotationTableReaderUtil;
import com.dr.framework.core.orm.module.EntityRelation;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.dr.framework.core.security.service.SecurityManager;
import com.dr.framework.sys.service.PermissionService;
import com.dr.framework.sys.service.RoleService;
import com.dr.framework.sys.service.SysDictService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ??????????????????????????????????????????????????????
 * ????????????????????????
 *
 * @author dr
 */
public abstract class AbstractFondSyncService implements InitializingBean {
    @Autowired
    protected OrganisePersonService organisePersonService;
    @Autowired
    protected CategoryConfigService categoryConfigService;
    @Autowired
    protected SysDictService sysDictService;
    @Autowired
    protected SecurityManager securityManager;
    @Autowired
    protected PermissionService permissionService;
    @Autowired
    protected RoleService roleService;
    @Autowired
    CommonMapper commonMapper;

    /**
     * ??????????????????????????????
     *
     * @param orgCode
     * @return
     */
    protected Organise getOrganiseByCode(String orgCode) {
        if (StringUtils.hasText(orgCode)) {
            return organisePersonService.getOrganise(new OrganiseQuery.Builder().codeEqual(orgCode).statusEqual("1").getQuery());
        } else {
            return null;
        }
    }


    protected FormDefinition newForm(CategoryInfo categoryInfo, Register register) {
        FormDefinition formDefinition = new FormDefinition();
        //??????????????????????????????????????????
        formDefinition.setFormType(String.valueOf(CategoryType.FILE.getCode()));
        //??????????????????
        String formCode = RandomStringUtils.randomAlphabetic(10).toLowerCase();
        formDefinition.setFormCode(categoryInfo.getCode() + "_" +register.getBatch_no());
        formDefinition.setFormTable(formCode);
        formDefinition.setFormName(categoryInfo.getName());
        formDefinition.setDescription(categoryInfo.getRemark() + "??????????????????");
        formDefinition.setRemarks(categoryInfo.getRemark());
        formDefinition.setDefault(true);
        formDefinition.setVersion(StatusEntity.STATUS_ENABLE);
        return formDefinition;
    }

    /**
     * ??????????????????
     *
     * @param fieldMetaData
     * @param fieldNameMap
     * @return
     */
    protected FormField newFormField(FieldMetaData fieldMetaData, Function<String, String> fieldNameMap) {
        //???????????????????????? ??????????????????????????????????????????????????????????????????
        FormField formField = new FormField();
        formField.setId(fieldMetaData.getId());
        formField.setLabel(fieldMetaData.getName());
        formField.setDescription(fieldMetaData.getRemark());
        String eName = fieldMetaData.geteName();
        String convertName = fieldNameMap.apply(eName);
        formField.setFieldCode(convertName);
        if (!convertName.equalsIgnoreCase(eName)) {
            //?????????????????????????????????????????????????????????
            formField.setFieldAliasStr(eName);
        }
        // ????????????????????????????????????string
        formField.setFieldTypeStrEnum(FieldType.STRING);
        formField.setFieldLength(parseLength(fieldMetaData.getTypeLength()));
        //????????????
        if (!StringUtils.isEmpty(fieldMetaData.getOrders())) {
            formField.setOrder(Integer.valueOf(fieldMetaData.getOrders()));
        }
        return formField;
    }

    /**
     * ??????????????????????????????
     *
     * @param categoryInfo
     * @param categoryYear
     * @param formDefinitionId
     */
    protected void bindCategoryConfig(Category categoryInfo, CategoryBspDict categoryYear, String formDefinitionId) {
        //???????????????????????????
        CategoryConfig categoryConfig = new CategoryConfig();
        String id = categoryInfo.getId() + ":" + categoryYear.getID();
        long count = categoryConfigService.count(SqlQuery.from(CategoryConfig.class).equal(CategoryConfigInfo.ID, id));
        categoryConfig.setId(id);
        categoryConfig.setName(categoryInfo.getName() + "????????????");
        categoryConfig.setCode(categoryInfo.getCode());
        categoryConfig.setBusinessId(categoryInfo.getId());
        /*categoryConfig.setStartYear(formatDate(categoryYear.getStartTime()));
        if (StringUtils.hasText(categoryYear.getEndTime())) {
            categoryConfig.setEndYear(formatDate(categoryYear.getEndTime()));
        }*/
        categoryConfig.setFileFormId(formDefinitionId);
        categoryConfig.setFileFormName(categoryInfo.getName());
        categoryConfig.setDefault(true);
        if (count == 0) {
            commonMapper.insert(categoryConfig);
        } else {
            commonMapper.updateIgnoreNullById(categoryConfig);
        }
    }

    private int formatDate(String day) {
        if (StringUtils.hasText(day)) {
            try {
                Date date = DateUtils.parseDate(day, "YYYY-MM-DD");
                return Integer.parseInt(DateFormatUtils.format(date, "YYYYMMDD"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * ??????????????????
     *
     * @param formFieldList
     * @return
     */
    protected void addDefaultMetaData(List<FieldModel> formFieldList) {
        //?????????????????????????????????
        Set<String> fieldNameSet = formFieldList.stream().map(FieldModel::getFieldCode).map(String::toUpperCase).collect(Collectors.toSet());
        System.out.println(fieldNameSet);
        entityRelation.getColumns().stream().filter(f -> !f.getName().equals(IdEntity.ID_COLUMN_NAME)).filter(f -> !fieldNameSet.contains(f.getName().toUpperCase())).forEach(c -> formFieldList.add(new ColumnFieldModel(c)));
    }

    /**
     * ?????????????????????int
     *
     * @param typeLength
     * @return
     */
    private int parseLength(String typeLength) {
        if (StringUtils.hasText(typeLength) && !"0".equalsIgnoreCase(typeLength.trim())) {
            try {
                return Integer.parseInt(typeLength);
            } catch (Exception ignore) {

            }
        }
        return 255;
    }

    @Autowired
    protected DataBaseService dataBaseService;
    protected EntityRelation entityRelation;

    @Override
    public void afterPropertiesSet() throws Exception {
        Dialect dialect = Objects.requireNonNull(dataBaseService.getDataBaseMetaDataByModuleName(Constants.MODULE_NAME)).getDialect();
        entityRelation = new EntityRelation(false);
        //TODO ???????????????????????????????????????
        AnnotationTableReaderUtil.readColumnInfo(entityRelation, AbstractArchiveEntity.class, dialect);
    }

}
