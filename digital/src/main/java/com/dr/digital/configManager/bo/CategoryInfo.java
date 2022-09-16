package com.dr.digital.configManager.bo;

import java.util.List;

/**
 * 智能归档配置门类基本信息
 *
 * @author dr
 */
public class CategoryInfo extends AbstractConfigEntity {
    /**
     * 分类编码
     */
    private String code;
    /**
     * 门类档案名称
     */
    private String name;
    private int lastNode;
    private String remark;
    /**
     * 门类档案类型id
     */
    private String type;
    private String parentID;
    /**
     * 门类类型
     */
    private CategoryInfo parent;

    private String classify;

    private List<CategoryInfo> children;

    public List<CategoryInfo> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryInfo> children) {
        this.children = children;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLastNode() {
        return lastNode;
    }

    public void setLastNode(int lastNode) {
        this.lastNode = lastNode;
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

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public CategoryInfo getParent() {
        return parent;
    }

    public void setParent(CategoryInfo parent) {
        this.parent = parent;
    }

    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify;
    }
}
