package com.dr.digital.manage.fournaturescheck.controller;

import com.dr.digital.manage.fournaturescheck.entity.TestRecord;
import com.dr.digital.manage.fournaturescheck.entity.TestRecordInfo;
import com.dr.digital.manage.fournaturescheck.service.TestRecordService;
import com.dr.digital.manage.log.annotation.SysLog;
import com.dr.digital.manage.task.entity.ArchiveBatch;
import com.dr.framework.common.controller.BaseServiceController;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.annotations.Form;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author caor
 * @date 2020-11-15 11:56
 */
@RestController
@RequestMapping({"${common.api-path:/api}/testrecord"})
public class TestRecordController extends BaseServiceController<TestRecordService, TestRecord> {
    @Autowired
    TestRecordService testRecordService;

    @Override
    protected SqlQuery<TestRecord> buildPageQuery(HttpServletRequest httpServletRequest, TestRecord testRecord) {
        SqlQuery<TestRecord> sqlQuery = SqlQuery.from(TestRecord.class);
        if (!StringUtils.isEmpty(testRecord.getFormDataId())) {
            sqlQuery.equal(TestRecordInfo.FORMDATAID, testRecord.getFormDataId());
        }
        sqlQuery.orderByDesc(TestRecordInfo.CREATEDATE);
        return sqlQuery;
    }

    @RequestMapping(value = "/startTest")
    @SysLog("开始四性检测")
    public ResultEntity startTest(HttpServletRequest request, TestRecord testRecord, @Form FormData formData) {
        Assert.isTrue(!StringUtils.isEmpty(formData.getFormDefinitionId()), "表单id不能为空");
        Assert.isTrue(!StringUtils.isEmpty(formData.getId()), "数据id不能为空");
        service.startTest(testRecord, formData);
        return ResultEntity.success();
    }

    @SysLog("检测结果")
    @RequestMapping(value = "/batchDetail")
    public ResultEntity batchDetail(ArchiveBatch batch) {
        return ResultEntity.success(testRecordService.selectByTestRecordList(batch));
    }
}