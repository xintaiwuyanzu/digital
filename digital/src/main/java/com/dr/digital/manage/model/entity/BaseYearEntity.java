package com.dr.digital.manage.model.entity;

import com.dr.framework.core.orm.annotations.Column;

/**
 * 基础年度范围父类
 *
 * @author dr
 */
public class BaseYearEntity extends BaseBusinessIdEntity {
    public static final String IS_DEFAULT_COLUMN_NAME = "isDefault";

    @Column(name = IS_DEFAULT_COLUMN_NAME, comment = "是否默认配置", length = 50, order = 1)
    private boolean isDefault;
    @Column(comment = "起始年度", length = 100, order = 2)
    private int startYear;
    @Column(comment = "终止年度", length = 100, order = 3)
    private int endYear;

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public int getEndYear() {
        return endYear;
    }

    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }

}
