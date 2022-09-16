package com.dr.digital.manage.fournaturescheck.service;

import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.framework.common.form.core.model.FormData;

public interface TestRecord2IMPService {
    /**
     * 完整性
     */
    String integrity = "INTEGRITY";
    /**
     * 真实性
     */
    String authenticity = "AUTHENTICITY";
    /**
     * 安全性
     */
    String security = "SECURITY";
    /**
     * 可用性
     */
    String usability = "USABILITY";

    FormData startTest(FormData formData, BaseQuery query);
}
