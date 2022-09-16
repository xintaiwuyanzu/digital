package com.dr.digital.manage.fournaturescheck.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseStatusEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

/**
 * @author caor
 * @date 2021-07-28 15:23
 */
@Table(name = Constants.TABLE_PREFIX + "FOURNATURESCHEME", module = Constants.MODULE_NAME, comment = "四性检测方案")
public class FourNatureScheme extends BaseStatusEntity<String> {
    @Column(comment = "业务id", length = 100)
    private String businessId;
    @Column(comment = "方案名称", length = 300)
    private String schemeName;
    @Column(comment = "检测环节", length = 300)
    private String checkLink;
    @Column(comment = "方案类型", length = 50)
    private String schemeType;
    @Column(comment = "描述", length = 500)
    private String description;

    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public String getSchemeType() {
        return schemeType;
    }

    public void setSchemeType(String schemeType) {
        this.schemeType = schemeType;
    }

    public String getCheckLink() {
        return checkLink;
    }

    public void setCheckLink(String checkLink) {
        this.checkLink = checkLink;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
