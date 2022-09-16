package com.dr.digital.bsp.bo;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * bsp字典
 *
 * @author dr
 */
public class BspDict extends AbstractBspEntity {
    /**
     * 字典编码
     */
    @JsonAlias("CODE")
    private String code;
    /**
     * 字典类型编码,对应pub_config
     */
    @JsonAlias("KIND")
    private String kind;
    /**
     * 字典名称
     */
    @JsonAlias("NAME")
    private String name;
    /**
     * 父级字典编码
     */
    @JsonAlias("PARENT_CODE")
    private String parentCode;
    /**
     * 备注
     */
    @JsonAlias("REMARK")
    private String remark;
    /**
     * 排序字段
     */
    @JsonAlias("SORT_ORDER")
    private int sortOrder;
    /**
     * 字典类型
     */
    @JsonAlias("TYPE")
    private String type;


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
