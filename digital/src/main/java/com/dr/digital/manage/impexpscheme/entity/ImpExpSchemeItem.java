package com.dr.digital.manage.impexpscheme.entity;

import com.dr.digital.manage.model.entity.BaseBusinessIdEntity;
import com.dr.digital.util.Constants;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

/**
 * 导入导出方案项
 *
 * @author caor
 * @date 2020/7/31 18:56
 */
@Table(name = Constants.MODULE_NAME + "IMP_EXP_SCHEME_ITEM", module = Constants.MODULE_NAME, comment = "导入导出方案项")
public class ImpExpSchemeItem extends BaseBusinessIdEntity {
    @Column(comment = "对应key", length = 50, order = 1)
    private String hashKey;
    @Column(comment = "字段长度", length = 50, order = 4)
    private String codeLength;
    @Column(comment = "字段类型", length = 50, order = 5)
    private String codeType;
    @Column(comment = "备注", length = 100, order = 6)
    private String remarks;

    public String getHashKey() {
        return hashKey;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    public String getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(String codeLength) {
        this.codeLength = codeLength;
    }

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
