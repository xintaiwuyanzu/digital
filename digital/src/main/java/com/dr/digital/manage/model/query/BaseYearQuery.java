package com.dr.digital.manage.model.query;

/**
 * 基础的按照年度查询的类
 *
 * @author dr
 */
public class BaseYearQuery {
    /**
     * 年度查询条件，如果年度为空，则查询默认的配置
     */
    private Long year;

    public Long getYear() {
        return year;
    }

    public void setYear(Long year) {
        this.year = year;
    }
}
