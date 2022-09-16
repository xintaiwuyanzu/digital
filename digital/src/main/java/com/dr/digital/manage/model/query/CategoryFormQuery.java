package com.dr.digital.manage.model.query;

/**
 * 用来查询门类分类上面的表单配置
 *
 * @author dr
 */
public class CategoryFormQuery extends BaseConfigQuery {
    /**
     * 全宗Id
     */
    private String fondId;
    /**
     * 门类编码
     */
    private String categoryCode;
    /**
     * 档案类型
     *
     * @see
     */
    private String categoryType;

    public String getFondId() {
        return fondId;
    }

    public void setFondId(String fondId) {
        this.fondId = fondId;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }
}
