package com.dr.digital.manage.fournaturescheck.service;

import com.dr.digital.manage.fournaturescheck.entity.TestRecord;
import com.dr.digital.manage.task.entity.ArchiveBatch;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.service.BaseService;
import com.dr.framework.core.orm.sql.support.SqlQuery;

import java.util.List;

/**
 * @author caor
 * @date 2020-11-15 11:51
 */
public interface TestRecordService extends BaseService<TestRecord> {
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

    void startTest(TestRecord testRecord, FormData formData);

    List<TestRecord> selectByTestRecordList(ArchiveBatch batch);
}
