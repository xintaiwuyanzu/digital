package com.dr.digital.wssplit.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseDescriptionEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

@Table(name = Constants.TABLE_PREFIX + "SPLITRULE", module = Constants.MODULE_NAME, comment = "拆分规则表")
public class SplitRule extends BaseDescriptionEntity<String> {
    @Column(comment = "文件类型", length = 100, order = 1)
    private String fileType;

    @Column(comment = "条件类型", length = 100, order = 1)
    private String conditionType;

    @Column(comment = "条件", length = 100, order = 1)
    private String pdCondition;

    @Column(comment = "判断内容", length = 100, order = 1)
    private String content;

    @Column(comment = "判断顺序", length = 100, order = 1)
    private int ifOrder;

    @Column(comment = "是否启用", length = 100, order = 1)
    private String isEnable;

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public String getPdCondition() {
        return pdCondition;
    }

    public void setPdCondition(String pdCondition) {
        this.pdCondition = pdCondition;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getIfOrder() {
        return ifOrder;
    }

    public void setIfOrder(int ifOrder) {
        this.ifOrder = ifOrder;
    }

    public String getIsEnable() {
        return isEnable;
    }

    public void setIsEnable(String isEnable) {
        this.isEnable = isEnable;
    }
}
