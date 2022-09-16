package com.dr.digital.manage.model.entity;

import com.dr.framework.core.orm.annotations.Column;


/**
 * 案卷基本表
 *
 * @author jjl
 */
public class AbstractArchiveEntity4AJ extends AbstractArchiveEntity {
    @Column(name = COLUMN_AJH, comment = "馆编案卷号")
    private String archives_file_number;

    @Column(name = COLUMN_JS, comment = "件数")
    private String total_number_of_items;

    @Column(name = COLUMN_CATALOG_NUM, comment = "目录页数")
    private String catalogNum;

    public String getArchives_file_number() {
        return archives_file_number;
    }

    public void setArchives_file_number(String archives_file_number) {
        this.archives_file_number = archives_file_number;
    }

    public String getTotal_number_of_items() {
        return total_number_of_items;
    }

    public void setTotal_number_of_items(String total_number_of_items) {
        this.total_number_of_items = total_number_of_items;
    }

    public String getCatalogNum() {
        return catalogNum;
    }

    public void setCatalogNum(String catalogNum) {
        this.catalogNum = catalogNum;
    }
}
