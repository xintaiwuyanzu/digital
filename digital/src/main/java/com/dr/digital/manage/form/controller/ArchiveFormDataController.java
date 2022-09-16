package com.dr.digital.manage.form.controller;

import com.dr.digital.manage.form.service.ArchiveDataManager;
import com.dr.digital.manage.log.annotation.SysLog;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.model.query.ArchiveDataQuery;
import com.dr.digital.wssplit.entity.WssplitTagging;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.annotations.Form;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.security.SecurityHolder;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.Current;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 档案数据控制类
 *
 * @author dr
 */
@RestController
@RequestMapping(value = "/api/manage/formData")
public class ArchiveFormDataController {
    @Autowired
    ArchiveDataManager dataManager;

    /**
     * 查询表单数据，查询条件完全由前台控制
     *
     * @param request
     * @param index
     * @param size
     * @return
     */
    @RequestMapping(value = "/formDataPage")
    @SysLog("查询档案列表页数据")
    public ResultEntity findArchiveData(HttpServletRequest request,
                                        ArchiveDataQuery query,
                                        String archival_code,
                                        @RequestParam(name = ArchiveDataQuery.QUERY_KEY, required = false) String queryContent,
                                        @RequestParam(defaultValue = "0") Integer index,
                                        @RequestParam(defaultValue = "15") Integer size) {
        // TODO,可能需要根据数据权限判断登陆人信息
        Person person = SecurityHolder.get().currentPerson();
        query.parseQuery(queryContent);
        if (index > 0) {
            index = index - 1;
        }
        if (!StringUtils.isEmpty(archival_code)) {
            query.getQueryItems().add(new ArchiveDataQuery.QueryItem("archival_code", archival_code, ArchiveDataQuery.QueryType.EQUAL));
        }
        //添加查询条件
        query.getQueryItems().add(new ArchiveDataQuery.QueryItem("people_code", person.getId()+ "," + "默认", ArchiveDataQuery.QueryType.IN));
        return ResultEntity.success(dataManager.formDataPage(query, index, size));
    }

    /**
     * 查询表单数据，查询条件完全由前台控制
     *
     * @param request
     * @param index
     * @param size
     * @return
     */
    @RequestMapping(value = "/formData")
    @SysLog("查询档案列表页数据")
    public ResultEntity findArchive(HttpServletRequest request,
                                    ArchiveDataQuery query,
                                    @RequestParam(name = ArchiveDataQuery.QUERY_KEY, required = false) String queryContent,
                                    @RequestParam(defaultValue = "0") Integer index,
                                    @RequestParam(defaultValue = "15") Integer size) {
        // TODO,可能需要根据数据权限判断登陆人信息
        query.parseQuery(queryContent);
        if (index > 0) {
            index = index - 1;
        }
        return ResultEntity.success(dataManager.formDataPage(query));

    }
    @RequestMapping(value = "/defaultJump")
    public ResultEntity defaultJump(ArchiveDataQuery query, @RequestParam(name = ArchiveDataQuery.QUERY_KEY, required = false) String queryContent,@Current Person person){
        query.parseQuery(queryContent);
//        query.getQueryItems().add(new ArchiveDataQuery.QueryItem("people_code", person.getId()+ "," + "默认", ArchiveDataQuery.QueryType.IN));
        //只找出默认的就可以了
        query.getQueryItems().add(new ArchiveDataQuery.QueryItem("people_code", "默认", ArchiveDataQuery.QueryType.IN));
        return ResultEntity.success(dataManager.formDataPage(query, 0, 1));
    }
    /**
     * 批量添加
     *
     * @param fondId           全宗Id
     * @param categoryId       门类分类Id
     * @param formDefinitionId 表单id
     * @param endArchivalCode  起始的档号
     * @param endArchivalCode  截至的档号
     * @return
     */
    @RequestMapping(value = "/insertFormBatchData")
    public ResultEntity insertFormBatchData(String fondId, String categoryId, String status_info,
                                            String archivers_category_code, String fonds_identifier,
                                            String formDefinitionId, String homeArchivalCode, String endArchivalCode,
                                            @Current Person person) {
        FormData formData = new FormData(formDefinitionId);
        Assert.isTrue(!"".equals(endArchivalCode) && !"".equals(homeArchivalCode), "批量添加时，起始档号和截至档号不能为空");
        int number = 1;
        try {
            String Jh = homeArchivalCode.substring(homeArchivalCode.lastIndexOf("-") + 1);
            int endNumber = Integer.parseInt(endArchivalCode.substring(endArchivalCode.lastIndexOf("-") + 1));
            int homeNumber = Integer.parseInt(Jh);
            String code = homeArchivalCode.substring(0, homeArchivalCode.lastIndexOf("-") + 1);
            if (endNumber - homeNumber > 0) {
                for (int i = homeNumber; i <= endNumber; i++) {
                    String codename = code + getNewName(Jh, i);
                    //重复性判断
                    List<FormData> formDataList = dataManager.selectFormData(formDefinitionId);

                    List<FormData> userList = formDataList.stream().filter(formDatas -> codename.equals(formDatas.get(ArchiveEntity.COLUMN_ARCHIVE_CODE))).collect(Collectors.toList());
                    if (userList.size() > 0) {
                        Assert.isTrue(false, codename + "档号重复");
                    }
                    formData.put(ArchiveEntity.COLUMN_FOND_CODE, fonds_identifier);
                    formData.put(ArchiveEntity.COLUMN_ARCHIVE_CODE, code + getNewName(Jh, i));
                    formData.put(ArchiveEntity.COLUMN_CATEGORY_CODE, archivers_category_code);
                    formData.put(ArchiveEntity.COLUMN_DISASSEMBLY_STATE, "0");
                    formData.put(ArchiveEntity.COLUMN_STATUS, status_info);
                    formData.put(ArchiveEntity.COLUMN_TRANSITION_STATE, "0");
                    formData.put(ArchiveEntity.COLUMN_QUALITY_STATE, "0");//质检状态
                    formData.put(ArchiveEntity.COLUMN_QUALITY_CONDUCT_STATE, "0");//质检进行状态
                    formData.put(ArchiveEntity.COLUMN_ASYNC_STATE,"0");
                    formData.put(ArchiveEntity.COLUMN_FOLDER_STATE,"0");
                    formData.put(ArchiveEntity.COLUMN_SPLIT_STATE, "0");
                    formData.put(ArchiveEntity.COLUMN_DISTINGUISH_STATE, "0");
                    formData.put(ArchiveEntity.COLUMN_PACKET_STATE, "0");
                    formData.put(ArchiveEntity.DISASSEMBLY_TAGGING, "0");
                    //添加唯一性人员初始化
                    formData.put(ArchiveEntity.PEOPEL_CODE, "默认");
                    formData.put(ArchiveEntity.PEOPLE_NAME,"默认");
                    //数据清洗标识
                    formData.put(ArchiveEntity.DATA_CLEANING,"0");
                    formData.put(ArchiveEntity.ID_COLUMN_NAME, UUID.randomUUID().toString());
                    dataManager.insertFormData(formData, fondId, categoryId);
                    Person personOne = SecurityHolder.get().currentPerson();
                    //添加操作日志
                    dataManager.updateLog(categoryId, personOne.getUserName(), "批量添加", status_info, "新增", code + getNewName(Jh, i),
                            "批量新增", code + getNewName(Jh, i),
                            formData.get(ArchiveEntity.COLUMN_TITLE) + "",
                            formData.get(ArchiveEntity.COLUMN_AJH) + "", formDefinitionId);
                }
            } else {
                return ResultEntity.error("起始档号不能大于截至档号");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return ResultEntity.success();
    }

    /**
     * 添加
     *
     * @param formData   表单数据
     * @param fondId     全宗Id
     * @param categoryId 门类分类Id
     * @return
     */
    @RequestMapping(value = "/insertFormData")
    public ResultEntity insertFormData(@Form FormData formData,
                                       String fondId,
                                       String categoryId,
                                       String formDefinitionId,
                                       @Current Person person) {
        return ResultEntity.success(dataManager.insertFormData(formData, fondId, categoryId, formDefinitionId, person));
    }

    @RequestMapping(value = "/insertFormDataSh")
    public ResultEntity insertFormDataSh(@Form FormData formData, String fondId, String categoryId) {
        return ResultEntity.success(dataManager.insertFormDataSh(formData, fondId, categoryId));
    }

    @RequestMapping(value = "/updateFormData")
    public ResultEntity updateFormData(HttpServletRequest request,
                                       @Form FormData formData,
                                       String fondId,
                                       String categoryId,
                                       String formDefinitionId) {
        return ResultEntity.success(dataManager.updateFormDataCheck(formData, fondId, categoryId, formDefinitionId));
    }

    @RequestMapping(value = "/updateFormDataSh")
    public ResultEntity updateFormDataSh(HttpServletRequest request,
                                         @Form FormData formData,
                                         String fondId,
                                         String categoryId) {
        return ResultEntity.success(dataManager.updateFormDataSh(formData, fondId, categoryId));
    }

    @RequestMapping(value = "/updateStatus")
    @SysLog("档案状态信息修改")
    public ResultEntity updateStatus(String ids, String status, String formDefinitionId) {
        dataManager.updateStatus(ids, status, formDefinitionId);
        return ResultEntity.success();
    }

    @RequestMapping(value = "/deleteFormData")
    public ResultEntity deleteFormData(String formId, String id) {
        return ResultEntity.success(dataManager.deleteFormData(formId, id));
    }

    /**
     * 查询详情
     *
     * @param formDefinitionId
     * @param formDataId
     * @return
     */
    @RequestMapping(value = "/detail")
    public ResultEntity<FormData> detail(String formDefinitionId, String formDataId) {
        return ResultEntity.success(dataManager.selectOneFormData(formDefinitionId, formDataId));
    }

    @RequestMapping(value = "/repeat")
    @SysLog("档案信息查重")
    public ResultEntity repeat(String fond, String category, String formId, String status) {
        return ResultEntity.success(dataManager.repeat(fond, category, formId, status, null));
    }
    @RequestMapping(value = "/uniquenessJudge")
    @SysLog("档案信息唯一性")
    public ResultEntity uniquenessJudge(WssplitTagging wssplitTagging) {
        return ResultEntity.success(dataManager.uniquenessJudge(wssplitTagging));
    }

    /**
     * 更换档号位数
     *
     * @param nums
     * @return
     */
    public static String getNewName(String nums, int num) {
        int JhLength = nums.length();
        String photoName = "";
        if (JhLength == 3) {
            if (num < 10) {
                photoName = "00" + num;
            } else if (num >= 10 && num < 100) {
                photoName = "0" + num;
            } else {
                photoName = "" + num;
            }
        } else {
            if (num < 10) {
                photoName = "000" + num;
            } else if (num >= 10 && num < 100) {
                photoName = "00" + num;
            } else if (num >= 100 && num < 1000) {
                photoName = "0" + num;
            } else {
                photoName = "" + num;
            }
        }
        return photoName;
    }

}
