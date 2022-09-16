package com.dr.digital.manage.category.entity;

import com.dr.digital.manage.model.entity.BaseYearEntity;
import com.dr.framework.common.form.util.Constants;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

/**
 * 描述：
 * 分类配置表
 *
 * @author tuzl
 * @date 2020/6/8 21:45
 */
@Table(name = Constants.TABLE_PREFIX + "CATEGORY_CONFIG", module = Constants.MODULE_NAME, comment = "分类配置表")
public class CategoryConfig extends BaseYearEntity {
    /**
     * 关联的表单模板信息
     */
    @Column(comment = "项目表单id", length = 500, order = 5)
    private String proFormId;
    @Column(comment = "项目表单名称", length = 500, order = 5)
    private String proFormName;
    @Column(comment = "案卷表单id", length = 500, order = 6)
    private String arcFormId;
    @Column(comment = "案卷表单名称", length = 500, order = 6)
    private String arcFormName;
    @Column(comment = "文件表单id", length = 500, order = 7)
    private String fileFormId;
    @Column(comment = "文件表单名称", length = 500, order = 7)
    private String fileFormName;

    public String getProFormName() {
        return proFormName;
    }

    public void setProFormName(String proFormName) {
        this.proFormName = proFormName;
    }

    public String getArcFormName() {
        return arcFormName;
    }

    public void setArcFormName(String arcFormName) {
        this.arcFormName = arcFormName;
    }

    public String getFileFormName() {
        return fileFormName;
    }

    public void setFileFormName(String fileFormName) {
        this.fileFormName = fileFormName;
    }

    public String getProFormId() {
        return proFormId;
    }

    public void setProFormId(String proFormId) {
        this.proFormId = proFormId;
    }

    public String getArcFormId() {
        return arcFormId;
    }

    public void setArcFormId(String arcFormId) {
        this.arcFormId = arcFormId;
    }

    public String getFileFormId() {
        return fileFormId;
    }

    public void setFileFormId(String fileFormId) {
        this.fileFormId = fileFormId;
    }
}
