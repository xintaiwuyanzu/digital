package com.dr.digital.configManager.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseStatusEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

/**
 * 接收参数用
 */
@Table(name = Constants.TABLE_PREFIX + "PARAM", module = Constants.MODULE_NAME, comment = "智能归档配置系统查询参数")
public class ParamEntity extends BaseStatusEntity<String> {
    /**
     * 新增批次
     * 用来存放新建表是查询的档案基本信息原数据，打包时查询用
     */
    @Column(comment = "门类ID")
    private String cateGoryId;
    @Column(comment = "门类code")
    private String archivers_category_code;
    @Column(comment = "code")
    private String code;
    @Column(comment = "年度", name = "vintages")
    private String year;
    @Column(comment = "整理方式", length = 50, order = 15)
    private String arrange;
    @Column(comment = "基本信息原数据ID")
    private String mate;
    @Column(comment = "表单ID")
    private String formDefinitionId;

    public String getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(String formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }

    public String getCateGoryId() {
        return cateGoryId;
    }

    public void setCateGoryId(String cateGoryId) {
        this.cateGoryId = cateGoryId;
    }

    public String getArchivers_category_code() {
        return archivers_category_code;
    }

    public void setArchivers_category_code(String archivers_category_code) {
        this.archivers_category_code = archivers_category_code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getArrange() {
        return arrange;
    }

    public void setArrange(String arrange) {
        this.arrange = arrange;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMate() {
        return mate;
    }

    public void setMate(String mate) {
        this.mate = mate;
    }
}
