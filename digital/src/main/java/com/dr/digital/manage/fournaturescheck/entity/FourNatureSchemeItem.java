package com.dr.digital.manage.fournaturescheck.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseStatusEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

/**
 * @author caor
 * @date 2021-07-28 15:23
 */
@Table(name = Constants.TABLE_PREFIX + "FOURNATURESCHEMEITEM", module = Constants.MODULE_NAME, comment = "四性检测方案项")
public class FourNatureSchemeItem extends BaseStatusEntity<String> {
    @Column(comment = "方案id", length = 50)
    private String fourNatureSchemeId;
    @Column(comment = "表单id", length = 50)
    private String formDefinitionId;
    @Column(comment = "检测类型", length = 300)
    private String checkType;
    @Column(comment = "检测项目", length = 300)
    private String checkName;
    @Column(comment = "检测对象", length = 300)
    private String checkTarget;
    @Column(comment = "检测目的", length = 300)
    private String checkAim;
    @Column(comment = "检测方法", length = 500)
    private String checkMethod;

    public String getFourNatureSchemeId() {
        return fourNatureSchemeId;
    }

    public void setFourNatureSchemeId(String fourNatureSchemeId) {
        this.fourNatureSchemeId = fourNatureSchemeId;
    }

    public String getCheckTarget() {
        return checkTarget;
    }

    public void setCheckTarget(String checkTarget) {
        this.checkTarget = checkTarget;
    }

    public String getCheckAim() {
        return checkAim;
    }

    public void setCheckAim(String checkAim) {
        this.checkAim = checkAim;
    }

    public String getCheckMethod() {
        return checkMethod;
    }

    public void setCheckMethod(String checkMethod) {
        this.checkMethod = checkMethod;
    }

    public String getCheckName() {
        return checkName;
    }

    public void setCheckName(String checkName) {
        this.checkName = checkName;
    }

    public String getCheckType() {
        return checkType;
    }

    public void setCheckType(String checkType) {
        this.checkType = checkType;
    }

    public String getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(String formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }
}
