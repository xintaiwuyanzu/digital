package com.dr.digital.manage.fournaturescheck.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseStatusEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;


/**
 * @author caor
 * @date 2020-11-15 11:38
 */
@Table(name = Constants.TABLE_PREFIX + "TESTRECORD", module = Constants.MODULE_NAME, comment = "四性检测记录表")
public class TestRecord extends BaseStatusEntity<String> {
    @Column(comment = "表单定义id", length = 50)
    private String formDefinitionId;
    @Column(comment = "formDataId", length = 50)
    private String formDataId;
    @Column(comment = "题名", length = 2000)
    private String title;
    @Column(comment = "档案号", length = 50)
    private String archiveCode;
    @Column(comment = "全宗号", length = 50)
    private String fondCode;
    @Column(comment = "分类号", length = 50)
    private String categoryCode;
    /**
     * INTEGRITY:完整性
     * AUTHENTICITY:真实性
     * SECURITY:安全性
     * USABILITY:可用性
     */
    @Column(comment = "检测类型", length = 50)
    private String testRecordType;
    @Column(comment = "字段编码", length = 50)
    private String testCode;
    @Column(comment = "字段名称", length = 50)
    private String testName;
    @Column(comment = "检测结果", length = 50)
    private String testResult;

    public String getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(String formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }

    public String getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(String formDataId) {
        this.formDataId = formDataId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArchiveCode() {
        return archiveCode;
    }

    public void setArchiveCode(String archiveCode) {
        this.archiveCode = archiveCode;
    }

    public String getFondCode() {
        return fondCode;
    }

    public void setFondCode(String fondCode) {
        this.fondCode = fondCode;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getTestRecordType() {
        return testRecordType;
    }

    public void setTestRecordType(String testRecordType) {
        this.testRecordType = testRecordType;
    }

    public String getTestCode() {
        return testCode;
    }

    public void setTestCode(String testCode) {
        this.testCode = testCode;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setTestResult(String testResult) {
        this.testResult = testResult;
    }
}
