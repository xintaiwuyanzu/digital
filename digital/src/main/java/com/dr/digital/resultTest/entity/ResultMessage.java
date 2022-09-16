package com.dr.digital.resultTest.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseStatusEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.ColumnType;
import com.dr.framework.core.orm.annotations.Table;

/**
 * @author Mr.Zhu
 * @date 2022/8/30 - 8:53
 */
@Table(name = Constants.TABLE_PREFIX + "RESULT_MESSAGE", module = Constants.MODULE_NAME, comment = "成果检测质检信息")
public class ResultMessage extends BaseStatusEntity<String> {

    @Column(comment = "表单ID", length = 50, order = 1)
    private String formDefinitionId;
    @Column(comment = "批次ID", length = 50, order = 2)
    private String registerId;
    @Column(comment = "档号", length = 50, order = 3)
    private String archiveCode;
    @Column(comment = "检测类型", length = 50, order = 4)
    private String resultType;
    @Column(comment = "检测元数据名称", length = 50, order = 5)
    private String resultElementName;
    @Column(comment = "检测信息", type =ColumnType.CLOB ,order = 7)
    private String resultMessage;

    public String getResultElementName() {
        return resultElementName;
    }

    public void setResultElementName(String resultElementName) {
        this.resultElementName = resultElementName;
    }


    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public String getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(String formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }

    public String getRegisterId() {
        return registerId;
    }

    public void setRegisterId(String registerId) {
        this.registerId = registerId;
    }

    public String getArchiveCode() {
        return archiveCode;
    }

    public void setArchiveCode(String archiveCode) {
        this.archiveCode = archiveCode;
    }

}
