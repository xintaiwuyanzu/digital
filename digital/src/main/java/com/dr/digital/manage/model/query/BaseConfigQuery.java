package com.dr.digital.manage.model.query;

/**
 * 基础查询条件
 * <p>
 * 根据年度和分类Id查询指定的配置信息
 *
 * @author dr
 */
public class BaseConfigQuery extends BaseYearQuery {

    /**
     * 分类Id
     */
    private String categoryId;

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
