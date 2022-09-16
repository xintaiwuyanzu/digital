package com.dr.digital.manage.form.entity;

import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseDescriptionEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

@Table(name = Constants.TABLE_PREFIX + "FILE_STRUCTURE", module = Constants.MODULE_NAME, comment = "文件结构")
public class FileStructure extends BaseDescriptionEntity<String> implements ArchiveEntity {

    @Column(comment = "批次ID", length = 100, order = 1)
    private String registerId;

    @Column(comment = "档案id", length = 100, order = 1)
    private String archivesID;

    @Column(comment = "批次号", length = 50, order = 2)
    private String batch_name;

    @Column(name = COLUMN_CATEGORY_CODE, comment = "档案门类代码", order = 3)
    private String archivers_category_code;

    @Column(name = COLUMN_FOND_CODE, comment = "全宗号", order = 4)
    private String fonds_identifier;

    @Column(name = COLUMN_ARCHIVE_CODE, comment = "档号", order = 5)
    private String archival_code;

    @Column(name = COLUMN_AJDH, comment = "父类档号", order = 5)
    private String aj_archival_code;

    @Column(name = COLUMN_FILE_TYPE, comment = "文件类型", length = 200, order = 6)
    private String file_type;

    @Column(name = COLUMN_JH, comment = "文件编号", order = 7)
    private String archives_item_number;

    @Column(name = COLUMN_YH, comment = "页号", order = 8)
    private String page_number;

    @Column(name = COLUMN_YS, comment = "页数", order = 9)
    private String total_number_of_pages;

    @Column( comment = "默认状态", length = 100, order = 1)
    /**
     * 0：ocr自动生成，1:自动拆件生成。
     */
    private String default_state;

    public String getArchivesID() {
        return archivesID;
    }

    public void setArchivesID(String archivesID) {
        this.archivesID = archivesID;
    }

    public String getDefault_state() {
        return default_state;
    }

    public void setDefault_state(String default_state) {
        this.default_state = default_state;
    }

    public String getRegisterId() {
        return registerId;
    }

    public void setRegisterId(String registerId) {
        this.registerId = registerId;
    }

    public String getBatch_name() {
        return batch_name;
    }

    public void setBatch_name(String batch_name) {
        this.batch_name = batch_name;
    }

    public String getArchivers_category_code() {
        return archivers_category_code;
    }

    public void setArchivers_category_code(String archivers_category_code) {
        this.archivers_category_code = archivers_category_code;
    }

    public String getFonds_identifier() {
        return fonds_identifier;
    }

    public void setFonds_identifier(String fonds_identifier) {
        this.fonds_identifier = fonds_identifier;
    }

    public String getArchival_code() {
        return archival_code;
    }

    public void setArchival_code(String archival_code) {
        this.archival_code = archival_code;
    }

    public String getAj_archival_code() {
        return aj_archival_code;
    }

    public void setAj_archival_code(String aj_archival_code) {
        this.aj_archival_code = aj_archival_code;
    }

    public String getFile_type() {
        return file_type;
    }

    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }

    public String getArchives_item_number() {
        return archives_item_number;
    }

    public void setArchives_item_number(String archives_item_number) {
        this.archives_item_number = archives_item_number;
    }

    public String getPage_number() {
        return page_number;
    }

    public void setPage_number(String page_number) {
        this.page_number = page_number;
    }

    public String getTotal_number_of_pages() {
        return total_number_of_pages;
    }

    public void setTotal_number_of_pages(String total_number_of_pages) {
        this.total_number_of_pages = total_number_of_pages;
    }

}
