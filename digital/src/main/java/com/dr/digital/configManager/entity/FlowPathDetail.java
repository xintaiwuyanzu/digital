package com.dr.digital.configManager.entity;


import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseCreateInfoEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Id;
import com.dr.framework.core.orm.annotations.Table;

/**
 * lych
 */
@Table(name = Constants.TABLE_PREFIX + "flowPathForm_detail", module = Constants.MODULE_NAME, comment = "批次流程详细信息表")
public class FlowPathDetail extends BaseCreateInfoEntity {

    @Column(comment = "批次id")
    private String formDefinitionId;
    @Column(comment = "流程代码")
    private String flowBatchName;
    @Column(comment = "pid")
    private String pid;
    @Column(comment = "流程名称")
    String flowStringName;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(String formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }

    public String getFlowBatchName() {
        return flowBatchName;
    }

    public void setFlowBatchName(String flowBatchName) {
        this.flowBatchName = flowBatchName;
    }

    public String getFlowStringName() {
        return flowStringName;
    }

    public void setFlowStringName(String flowStringName) {
        this.flowStringName = flowStringName;
    }
}
