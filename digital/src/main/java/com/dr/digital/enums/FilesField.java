package com.dr.digital.enums;

import com.dr.framework.common.config.model.MetaMap;
import com.dr.framework.common.form.engine.model.core.FieldModel;
import com.dr.framework.common.form.engine.model.core.FieldType;

import java.util.Collection;

public enum FilesField implements FieldModel {
    ARCHIVE_CODE("archival_code", "档号", FieldType.STRING, 255, 0, 1),
    FOND_CODE("fonds_identifier", "全宗", FieldType.STRING, 55, 0, 2),
    CATE_GORY_CODE("archivers_category_code", "档案门类代码", FieldType.STRING, 55, 0, 3),
    CATALOGUE_CODE("catalogue_number", "目录号", FieldType.STRING, 55, 0, 4),
    SAVE_TERM("retention_period", "保管期限", FieldType.STRING, 55, 0, 5),
    AJH("archives_file_number", "案卷号", FieldType.STRING, 55, 0, 6),
    JH("archives_item_number", "件号", FieldType.STRING, 55, 0, 7),
    ORG_CODE("organizational_structure_or_function", "机构或问题编码", FieldType.STRING, 255, 0, 8),
    VINTAGES("archivers_year", "年度", FieldType.STRING, 10, 0, 9),
    YH("page_number", "页号", FieldType.STRING, 10, 0, 10);
    private String code, name;
    private FieldType type;
    private int fieldLength, fieldScale, order;

    FilesField(String code, String name, FieldType type, int fieldLength, int fieldScale, int order) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.fieldLength = fieldLength;
        this.fieldScale = fieldScale;
        this.order = order;
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

    public FieldType getType() {
        return type;
    }

    public void setType(FieldType type) {
        this.type = type;
    }

    public void setFieldLength(int fieldLength) {
        this.fieldLength = fieldLength;
    }

    public void setFieldScale(int fieldScale) {
        this.fieldScale = fieldScale;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getFormDefinitionName() {
        return null;
    }

    @Override
    public String getFormDefinitionId() {
        return null;
    }

    @Override
    public String getFormDefinitionCode() {
        return null;
    }

    @Override
    public String getFieldCode() {
        return this.code;
    }

    @Override
    public Collection<String> getFieldAlias() {
        return null;
    }

    @Override
    public FieldType getFieldType() {
        return this.type;
    }

    @Override
    public int getFieldLength() {
        return this.fieldLength;
    }

    @Override
    public int getFieldScale() {
        return this.fieldScale;
    }

    @Override
    public Integer getFieldOrder() {
        return this.order;
    }

    @Override
    public String getFieldState() {
        return "1";
    }

    @Override
    public Integer getVersion() {
        return null;
    }

    @Override
    public String getLabel() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getRemarks() {
        return null;
    }

    @Override
    public MetaMap getMeta() {
        return null;
    }

}
