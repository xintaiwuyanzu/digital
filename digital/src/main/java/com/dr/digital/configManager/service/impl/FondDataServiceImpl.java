package com.dr.digital.configManager.service.impl;

import com.dr.digital.configManager.bo.*;
import com.dr.digital.configManager.entity.*;
import com.dr.digital.configManager.service.ConfigManagerClient;
import com.dr.digital.configManager.service.FondDataService;
import com.dr.digital.manage.category.entity.CategoryConfig;
import com.dr.digital.manage.form.service.ArchiveFormDefinitionService;
import com.dr.digital.manage.model.entity.AbstractArchiveEntity;
import com.dr.digital.register.entity.Category;
import com.dr.digital.register.entity.Register;
import com.dr.digital.util.FileUtil;
import com.dr.digital.util.UUIDUtils;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.entity.StatusEntity;
import com.dr.framework.common.entity.TreeNode;
import com.dr.framework.common.form.core.entity.FormDefinition;
import com.dr.framework.common.form.core.entity.FormField;
import com.dr.framework.common.form.core.service.FormDefinitionService;
import com.dr.framework.common.form.engine.model.core.FieldModel;
import com.dr.framework.common.form.engine.model.core.FieldType;
import com.dr.framework.common.form.util.Constants;
import com.dr.framework.common.service.CommonService;
import com.dr.framework.core.organise.entity.Organise;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.database.Dialect;
import com.dr.framework.core.orm.database.tools.AnnotationTableReaderUtil;
import com.dr.framework.core.orm.module.EntityRelation;
import com.dr.framework.core.security.entity.Role;
import com.dr.framework.core.security.query.RoleQuery;
import com.dr.framework.sys.service.SysDictService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class FondDataServiceImpl extends AbstractFondSyncService implements FondDataService {
    @Autowired
    ConfigManagerClient configManagerClient;
    @Autowired
    CommonMapper commonMapper;
    @Autowired
    SysDictService sysDictService;
    @Autowired
    FormDefinitionService formDefinitionService;
    @Autowired
    ArchiveFormDefinitionService archiveFormDefinitionService;

    private Logger logger = LoggerFactory.getLogger(FondDataServiceImpl.class);

    //?????????
    ExecutorService executorService = Executors.newFixedThreadPool(8);

    /**
     * ????????????code????????????
     *
     * @return
     */
    @Override
    public List<FondInfo> getFondByOrgCode(Person person) {
        if (person.getUserCode().equals("admin")) {
            String code = "11350100003604918M";
            return configManagerClient.getMetadataConfig(code);
        } else {
            Organise organise = organisePersonService.getPersonDefaultOrganise(person.getId());
            return configManagerClient.getMetadataConfig(organise.getOrganiseCode());
        }
    }

    /**
     * ??????????????????arcTypes??????????????????
     *
     * @param arcTypes
     * @return
     */
    @Override
    public List<CategoryInfo> getCategory(String arcTypes) {
        return configManagerClient.getCategoryInfo();
    }

    /**
     * ?????????????????????????????? (????????????)
     *
     * @param code
     * @return
     */
    @Override
    public List<CategoryBspDict> getArchiveBspDict(String code) {
        Map map = (Map) configManagerClient.getArchiveBspDict("archivesStandard");
        List<CategoryBspDict> list = (List<CategoryBspDict>) map.get("rows");
        return list;
    }

    /**
     * ???????????????????????????
     *
     * @param code
     * @param classify
     * @param standardId
     * @param arrange
     * @return
     */
    @Override
    public ArrayList<ArchivedTypeFile> getArchivedTypeFile(String standardId, String code, String classify, String arrange) {
        return configManagerClient.getArchivedTypeFile(standardId,code, classify, arrange);
    }

    /**
     * ?????????????????????
     *
     * @param code
     * @param classify
     * @param standard
     * @return
     */
    @Override
    public List<MataDataInfo> getCategoryMetadata(String code, String classify, String standard, String arrange) {
        return configManagerClient.getCategoryMetadata(code, classify, standard, arrange);
    }

    /**
     * ??????
     *  @param register
     * @param metadata
     * @param person
     * @param segment
     */
    @Override
    public ResultEntity insertRegister(Register register, Metadata metadata, Person person, String segment,String segmentName) {
        //????????????????????????  flowPath????????????????????????????????????????????????????????????????????????????????????  22/6/21??????
        //????????????????????????????????????
        register.setCreateDate(UUIDUtils.currentTimeMillis());
        metadata.setCreateDate(UUIDUtils.currentTimeMillis());
        /*
            ??????????????????????????????????????????????????????
            List<MataDataInfo> categoryMetadata = configManagerClient.getCategoryMetadata(metadata.getCode(), metadata.getClassify(), metadata.getArrange(), metadata.getId());
        */
        //?????? ????????????2.1??????id ????????????
        List<MataDataInfo> categoryMetadata = configManagerClient.getCategoryMetadata(metadata.getId());
        insertContainer();
        List<CategoryInfo> categoryInfos = configManagerClient.getCategoryInfo();
        //??????????????????
        CategoryInfo categoryInfo = categoryInfos.stream().filter(categoryInfo1 ->
                metadata.getCode().equals(categoryInfo1.getCode()))
                .collect(Collectors.toList()).get(0);
        register.setId(UUIDUtils.getUUID());
        if (categoryMetadata.size() > 0) {
            //????????????
            MataDataInfo collect = categoryMetadata.get(0);
            FormDefinition formDefinition = newForm(categoryInfo, register);
            List<FieldModel> list = addDefaultFieldModel(collect);
            //????????????
            formDefinition = (FormDefinition) formDefinitionService.addFormDefinition(formDefinition, list, true);
            //??????????????????
            archiveFormDefinitionService.addFormDisplay(collect, formDefinition);
            CommonService.bindCreateInfo(register);
            //??????????????????
            insertCateGory(register.getId(), categoryInfo, formDefinition);
            metadata.setFormDefinitionId(formDefinition.getId());
            metadata.setMate(collect.getId());
            metadata.setCode(register.getCode());
            metadata.setArchivers_category_code(register.getArchivers_category_code());
            metadata.setArrange(register.getArrange());
            //?????????????????????id?????????metadata.
            metadata.setBatch_id(metadata.getId());
            metadata.setId(UUIDUtils.getUUID());
            String personId = register.getPersonId();
            register.setPersonId("admin," + person.getId() + "," + personId);
            register.setName(register.getBatch_name());
            register.setBatch_no(register.getBatch_no());
            register.setForm_scheme(categoryInfo.getName());
            register.setReceiver(person.getUserName());
            register.setHandoverStatus("1");//?????????1????????????
            register.setFormDefinitionId(formDefinition.getId());
            register.setCode(metadata.getClassify());
            commonMapper.insert(metadata);
            commonMapper.insert(register);
            //??????????????????
            insertFlowPath(formDefinition.getId(),segment,register.getId(),segmentName);
            //?????? ???????????????  ????????????????????????
            ArrayList<ArchivedTypeFile> archivedTypeFile = getArchivedTypeFile(metadata.getBatch_id(), metadata.getArchivers_category_code(), metadata.getClassify(), metadata.getArrange());
            if (archivedTypeFile.size() > 0) {
                List<TypeFile> typeFiles = archivedTypeFile.get(0).getTypeFile();
                if (typeFiles.size() > 0) {
                    for (TypeFile typeFile : typeFiles) {
                        executorService.execute(() -> insertTypeFile(typeFile, metadata.getFormDefinitionId(), register.getId()));
                    }
                }
            }
        } else {
            return ResultEntity.error("???????????????????????????????????????!");
        }
        return ResultEntity.success();
    }
    /**
     * ??????????????????
     *
     * @param typeFile
     * @param formDefinitionId
     * @param registerId
     */
    public void insertTypeFile(TypeFile typeFile, String formDefinitionId, String registerId) {
        typeFile.setId(UUIDUtils.getUUID());
        typeFile.setFormDefinitionId(formDefinitionId);
        typeFile.setRegisterId(registerId);
        typeFile.setYhCode(FileUtil.getNewFileName(Integer.valueOf(typeFile.getOrders())));
        commonMapper.insert(typeFile);
    }

    /**
     * ??????
     *
     * @param register
     * @param paramEntity
     * @param person
     */
    @Override
    public void updateRegister(Register register, ParamEntity paramEntity, Person person) {
        CommonService.bindCreateInfo(register);
        commonMapper.updateById(register);
    }

    /**
     * ?????????????????????????????????
     *
     * @param registerId
     * @param categoryInfo
     * @param formDefinition
     */
    public void insertCateGory(String registerId, CategoryInfo categoryInfo, FormDefinition formDefinition) {
        Category category = new Category();
        category.setName(categoryInfo.getName());
        category.setCode(categoryInfo.getCode());
        category.setRegisterId(registerId);
        category.setBusinessId(registerId);
        category.setParentId(registerId);
        category.setCategoryType(formDefinition.getFormCode());
        CommonService.bindCreateInfo(category);
        CategoryConfig categoryConfig = new CategoryConfig();
        /*if (formDefinition.getFormCode().contains("ws")) {
            category.setArchiveType(StatusEntity.STATUS_ENABLE_STR);
            categoryConfig.setArcFormName(formDefinition.getFormName());
            categoryConfig.setArcFormId(formDefinition.getId());
            categoryConfig.setFileFormName("????????????_??????");
            categoryConfig.setFileFormId("d6db6eba-d8cd-45e7-a84d-4ff05fc69270");
        } else {*/
        category.setArchiveType(StatusEntity.STATUS_DISABLE_STR);
        categoryConfig.setFileFormName(formDefinition.getFormName());
        categoryConfig.setFileFormId(formDefinition.getId());
        categoryConfig.setBusinessId(category.getId());
        categoryConfig.setDefault(true);
        categoryConfig.setName(category.getName());
        CommonService.bindCreateInfo(categoryConfig);
        commonMapper.insert(category);
        commonMapper.insert(categoryConfig);
    }

    /**
     * ??????????????????
     *
     * @param fieldModels
     * @return
     */
    public List<FieldModel> addDefaultFieldModel(List<FieldModel> fieldModels) {
        List<FieldModel> list = addDefaultFieldModel();
        List<String> field = list.stream().map(fieldModel -> fieldModel.getFieldCode()).collect(Collectors.toList());
        List<String> label = list.stream().map(fieldModel -> fieldModel.getLabel()).collect(Collectors.toList());
        List<FieldModel> collect1 = fieldModels.stream().filter(fieldModel -> field.equals(fieldModel.getFieldCode())).collect(Collectors.toList());
        List<FieldModel> collect = fieldModels.stream().filter(fieldModel -> label.equals(fieldModel.getLabel())).collect(Collectors.toList());
        collect.removeIf(item -> {
            return collect1.equals(item.getFieldCode());
        });
        collect1.addAll(collect);
        List<String> collect2 = collect1.stream().map(fieldModel -> fieldModel.getLabel()).collect(Collectors.toList());
        List<FieldModel> collect3 = list.stream().filter(fieldModel -> collect2.equals(fieldModel.getLabel())).collect(Collectors.toList());
        return fieldModels;
    }

    /**
     * ??????AbstractArchiveEntity?????????????????????????????????
     *
     * @return
     */
    public List<FieldModel> addDefaultFieldModel() {
        List<FieldModel> list = new ArrayList<>();
        Class<AbstractArchiveEntity> aClass = AbstractArchiveEntity.class;
        Field[] fields = aClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Column.class)) {
                Column presentColumn = field.getDeclaredAnnotation(Column.class);
                if (!StringUtils.isEmpty(presentColumn.name()) && !StringUtils.isEmpty(presentColumn.comment())) {
                    FormField formField = new FormField();
                    formField.setLabel(presentColumn.name());
                    formField.setFieldCode(presentColumn.comment());
                    formField.setFieldLength(presentColumn.length());
                    list.add(formField);
                }
            }
        }
        return list;
    }

    /**
     * ??????ArchiveEntity?????????????????????????????????
     *
     * @return
     */
    public List<FieldModel> addDefaultFieldModel(MataDataInfo mataDataInfo) {
        String GJC = "row," + "all," + "as," + "by," + "column," + "dec," + "exit," + "get," + "long,"
                + "mod," + "set," + "sql," + "use," + "xor," + "select," + "revoke," + "outfile," + "order,";
        List<FieldModel> list = new ArrayList<>();
        List<FormField> formFields = new ArrayList<>();
        for (MataData mataData : mataDataInfo.getMetadata()) {
            FormField formField = new FormField();
            formField.setLabel(mataData.getName());
            //?????????????????????????????????+??????????????????
            if (GJC.contains(mataData.geteName())) {
                formField.setFieldAliasStr(mataData.geteName());
                formField.setFieldCode("s_" + mataData.geteName());
            } else {
                formField.setFieldCode(mataData.geteName());
            }
            formField.setFieldLength(200);

            formField.setDescription(StatusEntity.STATUS_DISABLE_STR);
            if (formFields.size() > 50) {
                formField.setFieldTypeStr("BYTES");
            } else {
                formField.setFieldTypeStr("STRING");
            }
            if (!StringUtils.isEmpty(mataData.getOrders())) {
                formField.setOrder(Integer.valueOf(mataData.getOrders()));
            }
            formFields.add(formField);
        }

        Class<AbstractArchiveEntity> aClass = AbstractArchiveEntity.class;
        Field[] fields = aClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Column.class)) {
                Column presentColumn = field.getDeclaredAnnotation(Column.class);
                if (!StringUtils.isEmpty(presentColumn.name()) && !StringUtils.isEmpty(presentColumn.comment())) {
                    FormField formField = new FormField();
                    formField.setLabel(presentColumn.comment());
                    formField.setFieldCode(presentColumn.name());
                    formField.setFieldLength(presentColumn.length());
                    formField.setDescription(StatusEntity.STATUS_ENABLE_STR);
                    formField.setFieldTypeStr("STRING");
                    formField.setOrder(presentColumn.order());
                    formFields.add(formField);
                }
            }
        }
        // ??????name???key???????????????????????????????????????Pool??????o1???o2?????????????????????????????????o1???
        // ??????name??????????????????value???o1???????????????o2, ?????????o1???????????????o1
        List<FormField> result = formFields.stream().collect(Collectors.toMap(FormField::getFieldCode, a -> a, (o1, o2) -> {
            if (o1.getDescription().equals(StatusEntity.STATUS_ENABLE_STR)) {
                if (!o1.getFieldCode().equals(o2.getFieldCode())) {
                    o1.setFieldAliasStr(o2.getFieldCode().toLowerCase());
                }
                return o1;
            } else {
                if (!o1.getFieldCode().equals(o2.getFieldCode())) {
                    o2.setFieldAliasStr(o1.getFieldCode().toLowerCase());
                }
                return o2;
            }
        })).values().stream().collect(Collectors.toList());
        // ??????name???key???????????????????????????????????????Pool??????o1???o2?????????????????????????????????o1???
        // ??????name??????????????????value???o1???????????????o2, ?????????o1???????????????o1
        List<FormField> collect = result.stream().collect(Collectors.toMap(FormField::getLabel, a -> a, (o1, o2) -> {
            if (o1.getDescription().equals(StatusEntity.STATUS_ENABLE_STR)) {
                if (!o1.getFieldCode().equals(o2.getFieldCode())) {
                    o1.setFieldAliasStr(o2.getFieldCode().toLowerCase());
                }
                return o1;
            } else {
                if (!o1.getFieldCode().equals(o2.getFieldCode())) {
                    o2.setFieldAliasStr(o1.getFieldCode().toLowerCase());
                }
                return o2;
            }
        })).values().stream().collect(Collectors.toList());
        list.addAll(collect);
        return list;
    }

    //????????????
    private List<FieldModel> buildFormFields(List<FieldMetaData> fieldMetaDataList, CategoryInfo categoryInfo) {
        //?????????????????????????????????????????????
        List<TreeNode> sysMetaDataList = sysDictService.dict(DICT_ARCHIVE_METADATA + categoryInfo.getCode());
        Map<String, String> configKeyMap = sysMetaDataList.stream().collect(Collectors.toMap(TreeNode::getId, TreeNode::getLabel));
        Map<String, String> defaultKeyMap = sysDictService.dict(DICT_ARCHIVE_METADATA_DEFAULT).stream().collect(Collectors.toMap(TreeNode::getId, TreeNode::getLabel));
        //?????????????????????????????????
        //??????????????????????????????????????????????????????????????????
        Function<String, String> fieldNameMap = (code) -> Optional.ofNullable(configKeyMap.get(code)).orElseGet(() -> defaultKeyMap.getOrDefault(code, code));
        fieldMetaDataList.forEach(item -> {
            item.setId(UUID.randomUUID().toString());
        });
        return fieldMetaDataList.stream()
                //?????????????????????????????????
                .filter(f -> StringUtils.hasText(f.geteName()))
                //??????????????????
                .map(fieldMetaData -> newFormField(fieldMetaData, fieldNameMap)).collect(Collectors.toList());
    }

    /**
     * @param categoryInfo
     * @param allCategory
     */
    protected void bindParent(CategoryInfo categoryInfo, Map<String, CategoryInfo> allCategory) {
        if (StringUtils.hasText(categoryInfo.getParentID())) {
            CategoryInfo parent = allCategory.get(categoryInfo.getParentID());
            if (parent != null) {
                bindParent(parent, allCategory);
            }
            categoryInfo.setParent(parent);
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param categoryInfos
     * @return
     */
    public List<CategoryInfo> reverseOrderParent(List<CategoryInfo> categoryInfos) {
        List<CategoryInfo> list = new ArrayList<>();
        for (CategoryInfo categoryInfo : categoryInfos) {
            list.add(categoryInfo);
            if (!StringUtils.isEmpty(categoryInfo.getParent())) {
                list.addAll(reverseOrderParent(categoryInfo.getParent()));
                categoryInfo.setParent(null);
            }
        }
        return list.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                new TreeSet<>(Comparator.comparing(CategoryInfo::getId))), ArrayList::new)).stream().map(categoryInfo -> {
            CategoryInfo categoryInfo1 = new CategoryInfo();
            BeanUtils.copyProperties(categoryInfo, categoryInfo1);
            categoryInfo1.setParent(null);
            return categoryInfo1;
        }).collect(Collectors.toList());
    }

    /**
     * ??????????????????????????????
     *
     * @param categoryInfo
     * @return
     */
    public List<CategoryInfo> reverseOrderParent(CategoryInfo categoryInfo) {
        List<CategoryInfo> list = new ArrayList<>();
        list.add(categoryInfo);
        if (!StringUtils.isEmpty(categoryInfo.getParent())) {
            list.addAll(reverseOrderParent(categoryInfo.getParent()));
        }
        return list;
    }

    /**
     * ??????????????????????????????????????????????????????????????????
     *
     * @param list
     * @return
     */
    public List<CategoryInfo> buidTree(List<CategoryInfo> list) {
        List<CategoryInfo> tree = new ArrayList<>();
        for (CategoryInfo node : list) {
            if (node.getParentID().equals("0")) {
                tree.add(findChild(node, list));
            }
        }
        return tree;
    }

    /**
     * ??????????????????????????????????????????????????????????????????
     *
     * @param node
     * @param list
     * @return
     */
    public CategoryInfo findChild(CategoryInfo node, List<CategoryInfo> list) {
        for (CategoryInfo n : list) {
            if (n.getParentID().equals(node.getId())) {
                if (node.getChildren() == null) {
                    node.setChildren(new ArrayList<CategoryInfo>());
                }
                node.getChildren().add(findChild(n, list));
            }
        }
        return node;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Dialect dialect = Objects.requireNonNull(dataBaseService.getDataBaseMetaDataByModuleName(Constants.MODULE_NAME)).getDialect();
        entityRelation = new EntityRelation(false);
        //TODO ???????????????????????????????????????
        AnnotationTableReaderUtil.readColumnInfo(entityRelation, AbstractArchiveEntity.class, dialect);
    }

    public void bindRoleUsers(String roleId, String personIds) {
        Assert.isTrue(!StringUtils.isEmpty(roleId), "??????Id???????????????");
        List<Role> roles = this.roleService.selectList((RoleQuery) ((RoleQuery.Builder) (new RoleQuery.Builder()).idEqual(new String[]{roleId})).build());
        if (roles.size() > 0) {
            if (!StringUtils.isEmpty(personIds)) {
                String[] var5 = personIds.split(",");
                int var6 = var5.length;
                for (int var7 = 0; var7 < var6; ++var7) {
                    securityManager.addRoleToUser(var5[var7], roleId);
                }
            }
        }
    }

    //?????????

    /**
     * ?????????
     *
     * @param code ????????????
     * @return ?????????????????????
     */
    @Override
    public List<Metadata> getArchiveTypeSchema(String code) {
        List<QueryData> queryData = configManagerClient.getQueryData();
        List<Metadata> archiveTypeSchema = configManagerClient.getArchiveTypeSchema(code);
//        List<CategoryBspDict> archiveBspDict = getArchiveBspDict("");
//        String list = JSONObject.toJSONString(archiveBspDict);
//        List<CategoryBspDict> categoryBspDictList = JSONObject.parseArray(list, CategoryBspDict.class);
        //???????????????name
        for (Metadata metadata : archiveTypeSchema) {
            for (QueryData queryDatum : queryData) {
                if (queryDatum.getId().equals(metadata.getStandardId())) {
                    metadata.setCodeFormData(queryDatum.getStandardName());
                    metadata.setMetadataName(queryDatum.getStandardName());
                    break;
                }
//                if (categoryBspDict.getCODE().equals(metadata.getStandard())) {
//                    metadata.setCodeFormData(categoryBspDict.getNAME());
//                    metadata.setMetadataName(categoryBspDict.getNAME());
//                    break;
//                }
            }
            if ("1".equals(metadata.getClassify())) {
                metadata.setMetadataName(metadata.getMetadataName() + "|??????");
            } else if ("2".equals(metadata.getClassify())) {
                metadata.setMetadataName(metadata.getMetadataName() + "|??????");
            }
            if ("1".equals(metadata.getArrange())) {
                metadata.setArrangeFormData("??????");
                metadata.setMetadataName(metadata.getMetadataName() + "|???|" + metadata.getStartTime() + "??????");
            } else if ("2".equals(metadata.getArrange())) {
                metadata.setArrangeFormData("??????");
                metadata.setMetadataName(metadata.getMetadataName() + "|???|" + metadata.getStartTime() + "??????");
            }
            //??????????????????
            String categoryName = getCategoryName(metadata.getCode());
            metadata.setArchivers_category_code_name(categoryName);
        }
        return archiveTypeSchema;
    }

    //??????????????????

    /**
     * ????????????
     *
     * @param code ws.ws
     * @return name:????????????/??????
     */
    @Override
    public String getCategoryName(String code) {
        String name = "";
        List<CategoryInfo> categoryInfoList = configManagerClient.getCategoryInfo();
        for (CategoryInfo categoryInfo : categoryInfoList) {
            if (categoryInfo.getCode().equals(code)) {
                for (CategoryInfo category : categoryInfoList) {
                    if (categoryInfo.getParentID().equals(category.getId())) {
                        name = category.getName();
                        break;
                    }
                }
                name = name + "/" + categoryInfo.getName();
                break;
            }
        }
        return name;
    }

    /**
     * ????????????????????????
     * @param fid
     * @param segment
     * @param pid
     */
    //????????????????????????
    public void insertFlowPath(String fid,String segment,String pid,String segmentName){
        //????????????????????????
        FlowPathDetail detail = new FlowPathDetail();
        detail.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        detail.setFormDefinitionId(fid);
        detail.setFlowBatchName(segment);
        //????????????????????????
        detail.setFlowStringName(segmentName);
        detail.setPid(pid);
        detail.setCreateDate(UUIDUtils.currentTimeMillis());
        commonMapper.insert(detail);
    }

    /**
     * ???????????????
     * @param
     */
    public void insertContainer(){
        List<MataDataInfo> categoryMetadatas = configManagerClient.getCategoryMetadata("1dbc00b87a8d4a32ab9adcb281a78056");
        for (MataDataInfo mataDataInfo : categoryMetadatas){
            //??????mateData
            List<MataData> metadata = mataDataInfo.getMetadata();
            //????????????parentID???????????????
            Map<String,List<MataData>> map = new HashMap<>();
            //????????????id
            for (MataData mataData:metadata){
                //????????????
                if (map.containsKey(mataData.getParentID())){
                    List<MataData> mataDataMapList = map.get(mataData.getParentID());
                    mataDataMapList.add(mataData);
                    map.put(mataData.getParentID(),mataDataMapList);
                }
                else {
                    List<MataData> mataDataMapList = new ArrayList<>();
                    mataDataMapList.add(mataData);
                    map.put(mataData.getParentID(),mataDataMapList);
                }
            }
            List<MataData> mataData = map.get("0");
            List<MataData> metadataList = new ArrayList<>();
            map.put("1",metadataList);
            for (MataData mata :mataData){
                if (map.containsKey(mata.getId())){
                    List<MataData> mataDataList = map.get("1");
                    mataDataList.add(mata);
                    map.put("1",mataDataList);


                }
            }
            Map<String, List<MataData>> map1 = map;
        }
    }
}
