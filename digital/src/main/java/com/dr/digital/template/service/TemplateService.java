package com.dr.digital.template.service;

import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.register.entity.Register;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;

import java.util.List;

public interface TemplateService {

    List<Register> selectList(SqlQuery<Register> var1);

    /**
     * 提交更新表单数据状态
     *
     * @param query
     * @param type
     */
    void updateStatus(Person person, BaseQuery query, String type);

    void lhUpdateType(Person person, BaseQuery query, String type, String childFormId);

    void lhUpdateStatus(String ids, String status, String formDefinitionId, String childFormId, String registerId, Person person);
}