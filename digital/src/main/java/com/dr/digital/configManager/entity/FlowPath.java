package com.dr.digital.configManager.entity;


import com.dr.digital.util.Constants;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Id;
import com.dr.framework.core.orm.annotations.Table;

import java.util.UUID;

/**
 * 存放所有的批次信息  lych
 */
@Table(name = Constants.TABLE_PREFIX + "flowPathForm", module = Constants.MODULE_NAME, comment = "批次流程信息表")
public class FlowPath {
    @Id(name = "id")
    @Column(comment = "id")
    private String id;

    @Column(comment = "编号")
    private String identifier;

    @Column(comment = "流程名称")
    private String flowName;

    @Column(comment = "代号")
    private String flowAlias;


    public void setId(String id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }


    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getAlias() {
        return flowAlias;
    }

    public void setAlias(String flowAlias) {
        this.flowAlias = flowAlias;
    }
}
