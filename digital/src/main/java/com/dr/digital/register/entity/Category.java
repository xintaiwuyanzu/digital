package com.dr.digital.register.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseDescriptionEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;
import com.dr.framework.core.security.bo.PermissionResource;

@Table(name = Constants.TABLE_PREFIX + "CATEGORY", module = Constants.MODULE_NAME, comment = "分类表")
public class Category extends BaseDescriptionEntity<String> implements PermissionResource {
    @Column(comment = "批次Id", length = 500, order = 1)
    private String registerId;
    @Column(comment = "业务id", length = 100, order = 1)
    private String businessId;
    @Column(comment = "批次号", length = 50, order = 1)
    private String batch_name;
    /**
     * 分类具体类型，文书，图片，人事等
     */
    @Column(comment = "分类类型", length = 500, order = 2)
    private String categoryType;
    /**
     * 案卷 件盒 文件
     *
     * @see
     */
    @Column(comment = "档案类型", length = 500, order = 3)
    private String archiveType;
    @Column(name = "parent_id", comment = "父Id", length = 100)
    private String parentId;

    public String getRegisterId() {
        return registerId;
    }

    public Category setRegisterId(String registerId) {
        this.registerId = registerId;
        return this;
    }

    public String getBatch_name() {
        return batch_name;
    }

    public void setBatch_name(String batch_name) {
        this.batch_name = batch_name;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public Category setCategoryType(String categoryType) {
        this.categoryType = categoryType;
        return this;
    }

    public String getArchiveType() {
        return archiveType;
    }

    public Category setArchiveType(String archiveType) {
        this.archiveType = archiveType;
        return this;
    }

    public String getBusinessId() {
        return businessId;
    }

    public Category setBusinessId(String businessId) {
        this.businessId = businessId;
        return this;
    }

    @Override
    public String getParentId() {
        return parentId;
    }

    public Category setParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }
}
