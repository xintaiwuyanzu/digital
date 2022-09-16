package com.dr.digital.manage.form.service.impl;

import com.dr.digital.event.ArchiveDataEditEvent;
import com.dr.digital.event.ArchiveDataStatusChangeEvent;
import com.dr.digital.manage.category.entity.CategoryConfig;
import com.dr.digital.manage.category.service.CategoryConfigService;
import com.dr.digital.manage.codingscheme.service.CodingSchemeService;
import com.dr.digital.manage.form.entity.ArchiveRepeat;
import com.dr.digital.manage.form.service.ArchiveDataManager;
import com.dr.digital.manage.log.service.ArchivesLogService;
import com.dr.digital.manage.model.entity.AbstractArchiveEntity;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.model.query.ArchiveDataQuery;
import com.dr.digital.register.service.CategoryService;
import com.dr.digital.wssplit.entity.WssplitTagging;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.entity.StatusEntity;
import com.dr.framework.common.file.service.CommonFileService;
import com.dr.framework.common.form.core.entity.FormDefinition;
import com.dr.framework.common.form.core.entity.FormField;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.form.core.service.FormDataService;
import com.dr.framework.common.form.core.service.FormDefinitionService;
import com.dr.framework.common.form.core.service.SqlBuilder;
import com.dr.framework.common.page.Page;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.jdbc.Column;
import com.dr.framework.core.security.SecurityHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 默认的档案数据管理实现类
 *
 * @author dr
 */
@Service
public class DefaultArchiveDataManager implements ArchiveDataManager {
    @Autowired
    FormDefinitionService formDefinitionService;
    @Autowired
    FormDataService formDataService;
    @Autowired
    ArchivesLogService archivesLogService;
    @Autowired
    CommonFileService commonFileService;
    @Autowired
    ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    CategoryService categoryService;
    @Autowired
    CodingSchemeService codingSchemeService;
    @Autowired
    CommonMapper commonMapper;
    @Autowired
    CategoryConfigService categoryConfigService;
    @Autowired
    ArchiveDataManager dataManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FormData insertFormData(FormData formData, String fondId, String categoryId) {
        String oldCode = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
        if (StringUtils.isEmpty(oldCode)) {
            //后台根据档号配置规则自动生成档号
            formData = codingSchemeService.builderArchiveCode(formData, categoryId);
        }
        formData = formDataService.addFormData(formData, false);
        //添加操作日志
        //addLog(person, formData, categoryId, "新增案卷数据信息", "新增卷内数据信息");
        return formData;
    }

    //带校验
    @Override
    public FormData insertFormData(FormData formData, String fondId, String categoryId, String formDefinitionId, Person person) {
        //有一些不是公文的不存在校验的字段
        if ("WS·GW".equals(formData.get(ArchiveEntity.COLUMN_CATEGORY_CODE))) {
            //校验
            checkFormData(formData, formDefinitionId);
        }
        Assert.isTrue(!"".equals(formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE)), "档号不能为空");
        //重复性判断
        List<FormData> formDataList = selectFormData(formDefinitionId);
        List<FormData> userList = formDataList.stream().filter(formDatas -> formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE).equals(formDatas.get(ArchiveEntity.COLUMN_ARCHIVE_CODE))).collect(Collectors.toList());
        if (userList.size() > 0) {
            Assert.isTrue(false, formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "档号重复");
        }
        //质检状态
        formData.put(ArchiveEntity.COLUMN_QUALITY_STATE, "0");
        //质检进行状态
        formData.put(ArchiveEntity.COLUMN_QUALITY_CONDUCT_STATE, "0");
        //手动拆件文件夹状态
        formData.put(ArchiveEntity.COLUMN_FOLDER_STATE,"0");
        //异步进行状态
        formData.put(ArchiveEntity.COLUMN_ASYNC_STATE,"0");
        //添加初始化标注
        formData.put(ArchiveEntity.DISASSEMBLY_TAGGING, "0");
        //添加唯一性人员初始化
        formData.put(ArchiveEntity.PEOPEL_CODE, "默认");
        formData.put(ArchiveEntity.PEOPLE_NAME,"默认");
        //数据清洗标识
        formData.put(ArchiveEntity.DATA_CLEANING,"0");
        FormData formData1 = insertFormData(formData, fondId, categoryId);
        Person personOne = SecurityHolder.get().currentPerson();
        updateLog(categoryId, personOne.getUserName(), "添加", formData.get(ArchiveEntity.COLUMN_STATUS),
                "新增", formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE), "添加", formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE),
                formData.get(ArchiveEntity.COLUMN_TITLE) + "",
                formData.get(ArchiveEntity.COLUMN_AJH) + "",
                formDefinitionId
        );
        return formData1;
    }

    @Override
    public FormData insertFormDataSh(FormData formData, String fondId, String categoryId) {
        List<FormData> formDataList = formDataService.selectFormData(formData.getFormDefinitionId(), (sqlQuery, formRelationWrapper) -> {
            sqlQuery.like(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_AJDH), formData.get(ArchiveEntity.COLUMN_AJDH))
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), formData.get(ArchiveEntity.COLUMN_STATUS) + "")
                    .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
        if (formDataList.size() > 0) {
            for (FormData list : formDataList) {
                String yeHao = StringUtils.isEmpty(list.get(ArchiveEntity.COLUMN_YH)) ? "0" : list.get(ArchiveEntity.COLUMN_YH);
                boolean flag = Integer.parseInt(yeHao) >= Integer.parseInt(formData.get(ArchiveEntity.COLUMN_YH));
                if (flag) {
                    int xYH = Integer.parseInt(list.get(ArchiveEntity.COLUMN_YH)) + Integer.parseInt(list.get(ArchiveEntity.COLUMN_YS));
                    String jHao = getLengthNum(list.get(ArchiveEntity.COLUMN_JH).toString().length(), Integer.parseInt(list.get(ArchiveEntity.COLUMN_JH)) + 1);
                    String dangHao = list.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
                    dangHao = dangHao.substring(0, dangHao.lastIndexOf("-")) + "-" + jHao;
                    list.put(ArchiveEntity.COLUMN_YH, xYH);
                    list.put(ArchiveEntity.COLUMN_JH, jHao);
                    list.put(ArchiveEntity.COLUMN_ARCHIVE_CODE, dangHao);
                    formDataService.updateFormDataById(list);
                }
            }
        }
        return insertFormData(formData, fondId, categoryId);
    }

    @Override
    public FormData updateFormData(FormData formData, String fondId, String categoryId) {

        Person person = SecurityHolder.get().currentPerson();
        if (person != null) {
            if (StringUtils.isEmpty(formData.getString("updateDate"))) {
                formData.put("updatePerson", person.getId());
                formData.put("updateDate", System.currentTimeMillis());
            }
        }
        FormData newFormData = formDataService.updateFormDataById(formData);
        //添加操作日志
        addLog(person, newFormData, categoryId, "更新案卷数据信息", "更新卷内数据信息");
        return newFormData;
    }

    @Override
    public FormData updateFormDataCheck(FormData formData, String fondId, String categoryId, String formDefinitionId) {

        checkFormData(formData, formDefinitionId);
        FormData formData1 = updateFormData(formData, fondId, categoryId);
        return formData1;
    }

    private void checkFormData(FormData formData, String formDefinitionId) {
        FormDefinition formDefinition = (FormDefinition) formDefinitionService.selectFormDefinitionById(formDefinitionId);
        List<FormField> fields = formDefinition.getFields();
        //需要校验的字段添加进来
        List<String> codeList = Arrays.asList(ArchiveEntity.COLUMN_ARCHIVE_CODE, ArchiveEntity.COLUMN_FOND_CODE, ArchiveEntity.COLUMN_TITLE, ArchiveEntity.COLUMN_YEAR);

        for (FormField field : fields) {
            //获取对应字段的数据
            String fieldCode = formData.get(field.getFieldCode());//数据
            String fieldTypeStr = field.getFieldTypeStr();//数据类型
            int fieldLength = field.getFieldLength();//字段长度
            String label = field.getLabel();//名称

            for (String code : codeList) {
                if (code.equals(field.getFieldCode())) {
                    //将对应的字段进行处理
                    checkNull(fieldCode, label, fieldTypeStr, fieldLength, field.getFieldCode());
                    break;
                }
            }
        }
    }

    //接受所有要校验的字段
    private void checkNull(String field, String label, String fieldTypeStr, int length, String alias) {

        List<String> codeList = Arrays.asList(ArchiveEntity.COLUMN_FOND_CODE, ArchiveEntity.COLUMN_YEAR);
        for (String code : codeList) {
            if (code.equals(alias)) {
                boolean numeric = org.apache.commons.lang3.StringUtils.isNumeric(field);
                Assert.isTrue(numeric, label + "只能为数字且不为空");
            }
        }
        //前端不会传null.只有别的门类中不包含这些校验字段时候
        if (field != null) {
            Assert.isTrue(!"".equals(field), label + "不能为空");
            //特殊校验
            if (ArchiveEntity.COLUMN_YEAR.equals(alias)) {
                Assert.isTrue(field.length() == 4, label + "长度必须为4位数字");
            }
            Assert.isTrue(field.length() < length, label + "长度超出" + length + "限制");
        }
    }

    @Override
    public FormData updateFormDataSh(FormData formData, String fondId, String categoryId) {
        FormData lodFormData = formDataService.selectOneFormData(formData.getFormDefinitionId(), formData.getId());
        String JH = StringUtils.isEmpty(lodFormData.get(ArchiveEntity.COLUMN_JH)) ? "0" : lodFormData.get(ArchiveEntity.COLUMN_JH);
        String yeHao = StringUtils.isEmpty(lodFormData.get(ArchiveEntity.COLUMN_YH)) ? "0" : lodFormData.get(ArchiveEntity.COLUMN_YH);
        String yeShu = StringUtils.isEmpty(lodFormData.get(ArchiveEntity.COLUMN_YS)) ? "0" : lodFormData.get(ArchiveEntity.COLUMN_YS);
        int num = 0;
        int oldYeShu = Integer.parseInt(yeShu);
        int newYeShu = Integer.parseInt(formData.get(ArchiveEntity.COLUMN_YS));
        int oldYeHao = Integer.parseInt(yeHao);
        int newYeHao = Integer.parseInt(formData.get(ArchiveEntity.COLUMN_YH));
        int oldJanHao = Integer.parseInt(JH);
        int newJanHao = Integer.parseInt(formData.get(ArchiveEntity.COLUMN_JH));
        if (oldYeShu != newYeShu) {
            num = newYeShu - oldYeShu;
        }
        List<FormData> formDataList = formDataService.selectFormData(formData.getFormDefinitionId(), (sqlQuery, formRelationWrapper) -> {
            sqlQuery.like(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_AJDH), formData.get(ArchiveEntity.COLUMN_AJDH))
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), formData.get(ArchiveEntity.COLUMN_STATUS) + "")
                    .orderBy(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
        countYeHao(formDataList, yeHao, num);
        num = 0;
        if (oldYeHao != newYeHao) {
            num = newYeHao - oldYeHao;
        }
        countYeHao(formDataList, yeHao, num);
        num = 0;
        if (oldJanHao != newJanHao) {
            num = newJanHao - oldJanHao;
        }
        if (formDataList.size() > 0) {
            for (FormData list : formDataList) {
                boolean flag = Integer.parseInt(list.get(ArchiveEntity.COLUMN_JH)) >= Integer.parseInt(JH);
                if (flag) {
                    String jHao = getLengthNum(list.get(ArchiveEntity.COLUMN_JH).toString().length(), Integer.parseInt(list.get(ArchiveEntity.COLUMN_JH)) + num);
                    String dangHao = list.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
                    dangHao = dangHao.substring(0, dangHao.lastIndexOf("-")) + "-" + jHao;
                    list.put(ArchiveEntity.COLUMN_JH, jHao);
                    list.put(ArchiveEntity.COLUMN_ARCHIVE_CODE, dangHao);
                    formDataService.updateFormDataById(list);
                }
            }
        }
        return updateFormData(formData, fondId, categoryId);
    }

    @Override
    public FormData selectOneFormData(String formDefinitionId, String formDataId) {
        return formDataService.selectOneFormData(formDefinitionId, formDataId);
    }

    @Override
    public Page<FormData> formDataPage(String formDefinitionId, SqlBuilder sqlBuilder, int pageIndex, int pageSize) {
        return formDataService.selectPageFormData(formDefinitionId, sqlBuilder, pageIndex, pageSize);
    }

    @Override
    public Page<FormData> formDataPage(ArchiveDataQuery query, int pageIndex, int pageSize) {
        return formDataService.selectPageFormData(query.getFormDefinitionId(), newBuilder(query), pageIndex, pageSize);
    }

    @Override
    public List<FormData> formDataPage(ArchiveDataQuery query) {
        return formDataService.selectFormData(query.getFormDefinitionId(), newBuilder(query));
    }

    /**
     * 1、删除原文附件信息
     * 2、删除目录信息
     * TODO 如果是案卷信息需要删除卷内目录信息
     *
     * @param formDefinitionId
     * @param aId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long deleteFormData(String formDefinitionId, String aId) {
        String[] ids = aId.split(",");
        long count = 0;
        //需要registerid
        CategoryConfig categoryConfig = categoryConfigService.selectCategoryConfigOne(formDefinitionId);
        for (String id : ids) {
            //添加操作日志
            Person person = SecurityHolder.get().currentPerson();
            //根据formid,id查询档号的数据
            FormData formData = formDataService.selectOneFormData(formDefinitionId, id);
            String dh = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
            String title = formData.get(ArchiveEntity.COLUMN_TITLE);
            //添加删除日志
            archivesLogService.addArchiveFlowLog(
                    categoryConfig.getBusinessId(), person.getUserName(), "RECEIVE", "删除", "删除",
                    dh, "删除数据", dh, title, formData.get(ArchiveEntity.COLUMN_AJH) + "", formDefinitionId);
            //sysLogService.insertFormDataLog(this.selectOneFormData(formDefinitionId, id), "删除");
            count += commonFileService.removeFileByRef(id);
            count += formDataService.removeFormData(formDefinitionId, id);
        }
        return count;
    }

    @Override
    public List<FormData> findDataByQuery(ArchiveDataQuery query) {
        return formDataService.selectFormData(query.getFormDefinitionId(), newBuilder(query));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String ids, String status, String formDefinitionId) {
        String[] split = ids.split(",");
        for (String id : split) {
            FormData formData = formDataService.selectOneFormData(formDefinitionId, id);
            ArchiveDataStatusChangeEvent event = new ArchiveDataStatusChangeEvent(
                    formData.getFormDefinitionId(),
                    formData.getId(),
                    formData.getString(StatusEntity.STATUS_COLUMN_KEY),
                    status
            );
            formData.put(ArchiveEntity.COLUMN_STATUS, status);
            formDataService.updateFormDataIgnoreNullById(formData);
            applicationEventPublisher.publishEvent(event);
        }
    }

    @Override
    public List<ArchiveRepeat> repeat(String fond, String category, String formId, String status, String archiveCode) {
        SqlBuilder sqlBuilder = (sqlQuery, formRelationWrapper) -> {
            Column column = formRelationWrapper.getColumn(AbstractArchiveEntity.COLUMN_ARCHIVE_CODE);
            sqlQuery.column(column, formRelationWrapper.idColumn().count("count"))
                    .groupBy(formRelationWrapper.getColumn(AbstractArchiveEntity.COLUMN_ARCHIVE_CODE))
                    .equal(formRelationWrapper.getColumn(AbstractArchiveEntity.COLUMN_FOND_CODE), fond)
                    .equal(formRelationWrapper.getColumn(AbstractArchiveEntity.COLUMN_STATUS), status)
                    .equal(formRelationWrapper.getColumn(AbstractArchiveEntity.COLUMN_CATEGORY_CODE), category)
                    .equal(formRelationWrapper.getColumn(AbstractArchiveEntity.COLUMN_ARCHIVE_CODE), archiveCode)
                    .setReturnClass(ArchiveRepeat.class);
        };
        List<ArchiveRepeat> list = formDataService.countSelf(formId, sqlBuilder);
        for (int i = 0; i < list.size(); i++) {
            if ("1".equals(list.get(i).getCount()) || "1".equals(list.get(i).getCount())) {
                list.remove(i);
                i--;
            }
        }
        return list;
    }

    /**
     * 根据查询条件查询表单数据
     *
     * @param query
     * @return
     */
    private SqlBuilder newBuilder(ArchiveDataQuery query) {
        return ((sqlQuery, wrapper) -> {
            for (ArchiveDataQuery.QueryItem item : query.getQueryItems()) {
                Column column = wrapper.getColumn(item.getKey());
                if (column == null) {
                    continue;
                }
                switch (item.getType()) {
                    case IN:
                        String[] data = item.getValue().split(",");
                        sqlQuery.in(column, data);
                        break;
                    case LIKE:
                        sqlQuery.like(column, item.getValue());
                        break;
                    case EQUAL:
                        sqlQuery.equal(column, item.getValue());
                        break;
                    case END_WITH:
                        sqlQuery.endingWith(column, item.getValue());
                        break;
                    case START_WITH:
                        sqlQuery.startingWith(column, item.getValue());
                        break;
                    default:
                        break;
                }
            }
            sqlQuery.orderBy(wrapper.getColumn(AbstractArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
    }

    /**
     * 修改页号
     *
     * @param formDataList
     * @param oldYh
     * @param num
     */
    private void countYeHao(List<FormData> formDataList, String oldYh, int num) {
        for (FormData list : formDataList) {
            if (Integer.parseInt(list.get(ArchiveEntity.COLUMN_YH)) > Integer.parseInt(oldYh)) {
                String s = String.valueOf(Integer.parseInt(list.get(ArchiveEntity.COLUMN_YH)) + num);
                if (s.length() == 1) {
                    s = "00" + s;
                } else if (s.length() == 2) {
                    s = "0" + s;
                }
                list.put(ArchiveEntity.COLUMN_YH, s);
                formDataService.updateFormDataById(list);
            }
        }
    }

    private String getLengthNum(Integer length, Integer num) {
        String newNum = "";
        String len = num + "";
        for (int i = 0; i < length - len.length(); i++) {
            newNum += "0";
        }
        newNum += num;
        return newNum;
    }

    public void addLog(Person person, FormData formData, String categoryId, String ajMs, String jnMs) {

        if (StringUtils.isEmpty(formData.get(ArchiveEntity.COLUMN_JH))) {
            archivesLogService.addArchiveLog(categoryId, person.getUserName(),
                    formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                    ArchiveDataManager.STATUS_RECEIVE,
                    formData.get(ArchiveEntity.COLUMN_TITLE) + "",
                    ajMs,
                    formData.get(ArchiveEntity.COLUMN_AJH) + "");
        } else {
            archivesLogService.addArchiveLog(categoryId, person.getUserName(),
                    formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                    ArchiveDataManager.STATUS_RECEIVE,
                    formData.get(ArchiveEntity.COLUMN_TITLE) + "",
                    jnMs,
                    formData.get(ArchiveEntity.COLUMN_AJH) + "");
        }
    }

    @Override
    public void updateHaveYuanwenByFormData(String categoryId, FormData formData, String haveYuanwen) {
        formData.put(ArchiveEntity.COLUMN_YW_HAVE, haveYuanwen);
        ArchiveDataEditEvent event = new ArchiveDataEditEvent(categoryId, formData, true);
        formDataService.updateFormDataIgnoreNullById(formData);
        applicationEventPublisher.publishEvent(event);
    }



    @Override
    public List<FormData> selectFormData(String formDefinitionId) {
        //返回批次下所有的数据
        List<FormData> formDataList = formDataService.selectFormData(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
            sqlQuery.orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
        return formDataList;
    }
    @Override
    public List<FormData> selectLinkDataNum(String formDefinitionId, String infos) {
        //返回批次流程数据
        List<FormData> formDataList = formDataService.selectFormData(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
            sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS),infos)
                    .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
        return  formDataList;
    }
    @Override
    public List<FormData> selectLinkcheckData(String formDefinitionId, String id) {
        //返回批次流程数据
        List<FormData> formDataList = formDataService.selectFormData(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
            sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.ID_COLUMN_NAME),id)
                    .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
        return  formDataList;
    }

    @Override
    public void updateLog(String categoryId, String userName, String inset, String status_info, String judge, String dh, String log, String dangHao, String title, String box, String formDefinitionId) {
        archivesLogService.addArchiveFlowLog(categoryId,userName,inset,status_info,judge,dh,log,dangHao,title,box,formDefinitionId);
    }

    @Override
    public ResultEntity uniquenessJudge(WssplitTagging wssplitTagging) {
        FormData formData = dataManager.selectOneFormData(wssplitTagging.getFormDefinitionId(), wssplitTagging.getArchivesId());
        Person person = SecurityHolder.get().currentPerson();
        String id = formData.get(ArchiveEntity.PEOPEL_CODE);
        //判断是否为第一次处理，添加处理人信息，判断是否是处理人处理，
        if ("默认".equals(id)) {
            //添加处理人
            formData.put(ArchiveEntity.PEOPLE_NAME, person.getUserName());
            formData.put(ArchiveEntity.PEOPEL_CODE, person.getId());
            formDataService.updateFormDataById(formData);
            return ResultEntity.success();

        }
        //判断登录人是否为处理人
        else if(person.getUserCode().equals(id)){
            return ResultEntity.success();
        }
        //已有操作人时返回错误
        else {
            return ResultEntity.error("该档案已存在修改人员,给您跳转到无人档案");
        }
    }

}
