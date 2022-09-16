package com.dr.digital.register.service;

import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.register.entity.Register;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.service.BaseService;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;

import java.util.List;
import java.util.Map;

public interface RegisterService extends BaseService<Register> {

    /**
     * 查询批次列表
     *
     * @param var1
     * @return
     */
    List<Register> selectList(SqlQuery<Register> var1);

    /**
     * 提交更新表单数据状态
     *
     * @param query
     * @param type
     */
    void updateStatus(Person person, BaseQuery query, String type);

    /**
     * 更新批次信息  跟新日志
     * @param person
     * @param formDate
     * @param type
     * @param childFormId
     * @param query
     * @param status
     */
    void updateDataDetail(Person person, FormData formDate, String type, String childFormId, BaseQuery query, String status);
    /**
     * 任务下发接口
     *
     * @param person
     * @param query
     * @param type
     * @param childFormId
     * @param status//判断是否是手动拆件
     */
    ResultEntity lhUpdateType(Person person, BaseQuery query, String type, String childFormId,String status);
    ResultEntity resultUpdateType(Person person, List<FormData> formDataList,BaseQuery query);

    void lhUpdateStatus(String ids, String status, String formDefinitionId, String childFormId, String registerId, Person person);

    Map getPercentag(String businessId);

    /**
     * 查询当前批次信息
     * @param fid
     * @return
     */
    Register getRegister(String fid);


    Map getTotalPercentage();

    /**
     * 根据流程环节名判断第一个包含的人工环节
     * @param flowName
     * @return
     */
    String manualLink(String flowName);
}
