package com.dr.digital.manage.fournaturescheck.service.impl;

import com.dr.digital.manage.form.service.ArchiveDataManager;
import com.dr.digital.manage.form.service.ArchiveFormDefinitionService;
import com.dr.digital.manage.fournaturescheck.entity.TestRecord;
import com.dr.digital.manage.fournaturescheck.entity.TestRecordInfo;
import com.dr.digital.manage.fournaturescheck.service.TestRecordService;
import com.dr.digital.manage.task.entity.ArchiveBatch;
import com.dr.digital.manage.task.entity.ImpBatchDetail;
import com.dr.digital.manage.task.service.ArchiveBatchService;
import com.dr.digital.manage.task.service.BaseBatchDetailService;
import com.dr.digital.util.UUIDUtils;
import com.dr.framework.common.form.core.entity.FormField;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.service.CommonService;
import com.dr.framework.common.service.DefaultBaseService;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author caor
 * @date 2020-11-15 11:52
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class TestRecordServiceImpl extends DefaultBaseService<TestRecord> implements TestRecordService {
    @Autowired
    ArchiveFormDefinitionService archiveFormDefinitionService;
    @Autowired
    ArchiveDataManager archiveDataManager;
    @Autowired
    CommonService commonService;
    @Autowired
    ArchiveBatchService archiveBatchService;

    Map<String, BaseBatchDetailService> baseBatchServiceMap;


    @Override
    public void startTest(TestRecord testRecord, FormData formData) {
        //TODO 需要往批次表中插入数据
        formData = archiveDataManager.selectOneFormData(formData.getFormDefinitionId(), formData.getId());
        List<FormField> findFieldList = archiveFormDefinitionService.findFieldList(formData.getFormDefinitionId());
        FormData finalFormData = formData;
        findFieldList.forEach(formField -> {
            testRecord.setFormDefinitionId(finalFormData.getFormDefinitionId());
            testRecord.setFormDataId(finalFormData.getId());
            String testCode = "";
            String testName = "";
            testRecord.setCreateDate(System.currentTimeMillis());
            switch (formField.getFieldCode()) {
                case "TITLE":
                    //TODO 需要修改档案表中检测状态
                    testRecord.setTestRecordType(authenticity);
                    // 0:未检测 1:通过2:未通过
                    testRecord.setStatus("1");
                    testRecord.setId(UUIDUtils.getUUID());
                    //默认其他检测类型通过
                    insert(testRecord);
                    testRecord.setTestRecordType(usability);
                    testRecord.setId(UUIDUtils.getUUID());
                    insert(testRecord);
                    testRecord.setTestRecordType(security);
                    testRecord.setId(UUIDUtils.getUUID());
                    insert(testRecord);
                    //针对完整性特殊处理
                    testRecord.setTestCode(testCode + formField.getFieldCode() + ",");
                    testRecord.setTestName(testName + formField.getLabel() + ",");
                    if (StringUtils.isEmpty(finalFormData.getFieldValue(formField))) {
                        testRecord.setStatus("2");
                        testRecord.setTestResult("题名不能为空");
                        testRecord.setTestRecordType(integrity);
                        testRecord.setId(UUIDUtils.getUUID());
                        insert(testRecord);
                    } else {
                        testRecord.setTestRecordType(integrity);
                        testRecord.setId(UUIDUtils.getUUID());
                        insert(testRecord);
                    }
                    break;
                case "saveTerm":
                    break;
                case "openScope":
                    break;
                case "worth":
                    break;
                case "destruction":
                    break;
            }
        });
    }

    @Override
    public List<TestRecord> selectByTestRecordList(ArchiveBatch batch) {
        List<ImpBatchDetail> list = archiveBatchService.selectList(batch);
        List<TestRecord> listTestRecord = new ArrayList<>();
        for (ImpBatchDetail formData : list) {
            SqlQuery<TestRecord> sqlQuery = SqlQuery.from(TestRecord.class);
            if (!StringUtils.isEmpty(formData.getId())) {
                sqlQuery.equal(TestRecordInfo.FORMDATAID, formData.getFormDataId());
            }
            List<TestRecord> listTest = commonService.selectList(sqlQuery);
            for (TestRecord testRecord : listTest) {
                listTestRecord.add(testRecord);
            }
        }
        return listTestRecord;
    }
}
