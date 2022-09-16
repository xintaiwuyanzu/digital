package com.dr.digital.manage.fournaturescheck.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseStatusEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

/**
 * @author caor
 * @date 2021-07-28 15:23
 */
@Table(name = Constants.TABLE_PREFIX + "FOURNATURESCHEMEITEMMETHOD", module = Constants.MODULE_NAME, comment = "四性检测方案具体方法")
public class FourNatureSchemeItemMethod extends BaseStatusEntity<String> {
    @Column(comment = "检测方案id", length = 50)
    private String fourNatureSchemeItemId;
    @Column(comment = "表单id", length = 50)
    private String formDefineId;
    @Column(comment = "字段code", length = 300)
    private String formFieldCode;
    @Column(comment = "字段名称", length = 300)
    private String formFieldName;
    @Column(comment = "字段最短值", length = 300)
    private String formFieldShort;
    @Column(comment = "字段最长", length = 300)
    private String formFieldLong;
    @Column(comment = "字段类型", length = 300)
    private String formFieldType;
    @Column(comment = "特殊字符", length = 500)
    private String specialCharacters;
    @Column(comment = "是否必填项", length = 100)
    private String ifRequired;
    @Column(comment = "备注", length = 500)
    private String remark;

    public String getFourNatureSchemeItemId() {
        return fourNatureSchemeItemId;
    }

    public void setFourNatureSchemeItemId(String fourNatureSchemeItemId) {
        this.fourNatureSchemeItemId = fourNatureSchemeItemId;
    }

    public String getFormDefineId() {
        return formDefineId;
    }

    public void setFormDefineId(String formDefineId) {
        this.formDefineId = formDefineId;
    }

    public String getFormFieldCode() {
        return formFieldCode;
    }

    public void setFormFieldCode(String formFieldCode) {
        this.formFieldCode = formFieldCode;
    }

    public String getFormFieldName() {
        return formFieldName;
    }

    public void setFormFieldName(String formFieldName) {
        this.formFieldName = formFieldName;
    }

    public String getFormFieldShort() {
        return formFieldShort;
    }

    public void setFormFieldShort(String formFieldShort) {
        this.formFieldShort = formFieldShort;
    }

    public String getFormFieldLong() {
        return formFieldLong;
    }

    public void setFormFieldLong(String formFieldLong) {
        this.formFieldLong = formFieldLong;
    }

    public String getFormFieldType() {
        return formFieldType;
    }

    public void setFormFieldType(String formFieldType) {
        this.formFieldType = formFieldType;
    }

    public String getSpecialCharacters() {
        return specialCharacters;
    }

    public void setSpecialCharacters(String specialCharacters) {
        this.specialCharacters = specialCharacters;
    }

    public String getIfRequired() {
        return ifRequired;
    }

    public void setIfRequired(String ifRequired) {
        this.ifRequired = ifRequired;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
