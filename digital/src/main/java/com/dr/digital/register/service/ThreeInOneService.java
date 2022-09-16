package com.dr.digital.register.service;

import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.core.organise.entity.Person;

public interface ThreeInOneService {
    /**
     * 新的数据清洗接口（拆分，识别，拆件）
     *
     * @param query
     * @param person
     */
    void dataCleaning(BaseQuery query, String registerId, Person person);
    void ocrToChaiJian(FormData formData, String registerId);
}
