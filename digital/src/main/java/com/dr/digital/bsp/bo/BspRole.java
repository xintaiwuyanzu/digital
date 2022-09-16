package com.dr.digital.bsp.bo;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * bsp角色
 *
 * @author dr
 */
public class BspRole extends AbstractBspEntity {
    /**
     * 状态
     */
	@JsonAlias("STATUS") 
    private String status;
    /**
     * 角色名称
     */
	@JsonAlias("NAME") 
    private String name;
    /**
     *
     */
	@JsonAlias("PARENT_ID") 
    private String parentId;
    /**
     * 角色备注
     */
	@JsonAlias("REMARK") 
    private String remark;
    /**
     * 角色类型
     */
	@JsonAlias("TYPE") 
    private String type;
    /**
     * 角色值,
     */
	@JsonAlias("VALUE") 
    private String value;
    /**
     * 角色权重
     */
	@JsonAlias("WEIGHT") 
    private String weight;
    /**
     * 排序字段
     */
	@JsonAlias("SORT_ORDER") 
    private Integer order;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
