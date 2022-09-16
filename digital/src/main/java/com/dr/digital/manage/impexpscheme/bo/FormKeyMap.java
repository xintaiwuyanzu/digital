package com.dr.digital.manage.impexpscheme.bo;

/**
 * 用来映射字段
 *
 * @author dr
 */
public class FormKeyMap {
    /**
     * 字段名称
     */
    private String fieldName;
    /**
     * 字段编码
     */
    private String fieldCode;
    /**
     * 目标编码
     */
    private String targetCode;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldCode() {
        return fieldCode;
    }

    public void setFieldCode(String fieldCode) {
        this.fieldCode = fieldCode;
    }

    public String getTargetCode() {
        return targetCode;
    }

    public void setTargetCode(String targetCode) {
        this.targetCode = targetCode;
    }
}
