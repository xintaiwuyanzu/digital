package com.dr.digital.manage.model.entity;

import com.dr.framework.common.entity.BaseCreateInfoEntity;
import com.dr.framework.core.orm.annotations.Column;

import static com.dr.digital.manage.model.entity.ArchiveEntity.*;

/**
 * 档案关联表
 * 从这张表能够关联查询到具体的一条档案信息
 *
 * @author dr
 */
public class AbstractArchiveRelateEntity extends BaseCreateInfoEntity {
    /**
     * 关联字段
     */
    @Column(comment = "全宗名称", length = 500)
    private String fonds_name;
    @Column(name = COLUMN_FOND_CODE, comment = "全宗号")
    private String fonds_identifier;
    @Column(name = COLUMN_CATEGORY_CODE, comment = "档案门类代码")
    private String archivers_category_code;
    @Column(comment = "分类Id", length = 100)
    private String categoryId;
    @Column(comment = "表单定义Id", length = 100)
    private String formDefinitionId;
    @Column(comment = "表单数据Id", length = 100)
    private String formDataId;
    @Column(name = COLUMN_ORG_CODE, comment = "机构或问题")
    private String organizational_structure_or_function;
    @Column(name = COLUMN_ARCHIVE_CODE, comment = "档号")
    private String archival_code;
    @Column(name = COLUMN_TITLE, comment = "题名", length = 800)
    private String title;
    @Column(name = COLUMN_KEY_WORDS, comment = "关键词", length = 800)
    private String keyword;
    @Column(name = COLUMN_NOTE, comment = "备注", length = 2000)
    private String note;
    @Column(name = COLUMN_YEAR, comment = "年度", length = 50)
    private String year;

    public String getFonds_name() {
        return fonds_name;
    }

    public void setFonds_name(String fonds_name) {
        this.fonds_name = fonds_name;
    }

    public String getFonds_identifier() {
        return fonds_identifier;
    }

    public void setFonds_identifier(String fonds_identifier) {
        this.fonds_identifier = fonds_identifier;
    }

    public String getArchivers_category_code() {
        return archivers_category_code;
    }

    public void setArchivers_category_code(String archivers_category_code) {
        this.archivers_category_code = archivers_category_code;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(String formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }

    public String getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(String formDataId) {
        this.formDataId = formDataId;
    }

    public String getOrganizational_structure_or_function() {
        return organizational_structure_or_function;
    }

    public void setOrganizational_structure_or_function(String organizational_structure_or_function) {
        this.organizational_structure_or_function = organizational_structure_or_function;
    }

    public String getArchival_code() {
        return archival_code;
    }

    public void setArchival_code(String archival_code) {
        this.archival_code = archival_code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
