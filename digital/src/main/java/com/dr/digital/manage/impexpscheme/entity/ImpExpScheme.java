package com.dr.digital.manage.impexpscheme.entity;

import com.dr.digital.manage.model.entity.BaseYearEntity;
import com.dr.digital.util.Constants;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

/**
 * @author dr
 * @date 2020/7/31 18:48
 */
@Table(name = Constants.MODULE_NAME + "IMP_EXP_SCHEME", module = Constants.MODULE_NAME, comment = "导入导出方案")
public class ImpExpScheme extends BaseYearEntity {
    @Column(comment = "方案类型", length = 50, order = 1)
    private String schemeType;
    @Column(comment = "文件类型", length = 50, order = 2)
    private String fileType;
    @Column(comment = "备注", length = 100, order = 3)
    private String remarks;

    public String getSchemeType() {
        return schemeType;
    }

    public void setSchemeType(String schemeType) {
        this.schemeType = schemeType;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
