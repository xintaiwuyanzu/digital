package com.dr.digital.template.entity;

import com.dr.framework.common.entity.BaseCreateInfoEntity;
import com.dr.framework.common.entity.BaseStatusEntity;
import com.dr.framework.common.entity.SourceRefEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.ColumnType;
import com.dr.framework.core.orm.annotations.Table;
import com.dr.framework.core.util.Constants;

/**
 * 系统用户表
 *
 * @author dr
 */
@Table(name = Constants.SYS_TABLE_PREFIX + "template",module = "common", comment = "模板表")
public class Template extends BaseCreateInfoEntity {

    @Column(name = "fieldval", comment = "值", length = 500)
    private String fieldval;
    @Column(name = "field", comment = "字段名", length = 500)
    private String field;
    @Column(name = "remark", comment = "备注", length = 200)
    private String remark;

    public String getFieldval() {
        return fieldval;
    }

    public void setFieldval(String fieldval) {
        this.fieldval = fieldval;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
