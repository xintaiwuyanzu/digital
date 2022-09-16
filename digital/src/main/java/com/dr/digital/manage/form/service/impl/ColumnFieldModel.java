package com.dr.digital.manage.form.service.impl;

import com.dr.framework.common.config.model.MetaMap;
import com.dr.framework.common.entity.StatusEntity;
import com.dr.framework.common.form.engine.model.core.FieldModel;
import com.dr.framework.common.form.engine.model.core.FieldType;
import com.dr.framework.core.orm.jdbc.Column;

import java.util.Collection;
import java.util.Collections;

/**
 * @author dr
 */
class ColumnFieldModel implements FieldModel {
    private final Column column;

    public ColumnFieldModel(Column column) {
        this.column = column;
    }

    @Override
    public String getFieldCode() {
        return column.getName();
    }

    @Override
    public Collection<String> getFieldAlias() {
        return Collections.emptyList();
    }

    @Override
    public FieldType getFieldType() {
        //TODO
        return FieldType.STRING;
    }

    @Override
    public int getFieldLength() {
        return column.getSize();
    }

    @Override
    public int getFieldScale() {
        return column.getDecimalDigits();
    }

    @Override
    public Integer getFieldOrder() {
        return column.getPosition();
    }

    @Override
    public String getFieldState() {
        return StatusEntity.STATUS_ENABLE_STR;
    }

    @Override
    public String getLabel() {
        return column.getRemark();
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getRemarks() {
        return column.getRemark();
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
    public Integer getVersion() {
        return null;
    }

    @Override
    public MetaMap getMeta() {
        return null;
    }
}
