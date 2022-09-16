package com.dr.digital.manage.model.entity;

import com.dr.framework.common.entity.BaseDescriptionEntity;
import com.dr.framework.core.orm.annotations.Column;

/**
 * 基础业务外键对象
 *
 * @author dr
 */
public class BaseBusinessIdEntity extends BaseDescriptionEntity<String> {
    public static final String BUSINESS_ID_COLUMN_NAME = "businessId";

    @Column(comment = "业务id", length = 100, order = 1)
    private String businessId;

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

}
