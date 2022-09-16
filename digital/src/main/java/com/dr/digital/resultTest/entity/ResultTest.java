package com.dr.digital.resultTest.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseStatusEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

/**检测规则表
 * @author Mr.Zhu
 * @date 2022/8/18 - 16:49
 */
@Table(name = Constants.TABLE_PREFIX + "RESULT_TEST", module = Constants.MODULE_NAME, comment = "成果检测规则")
public class ResultTest extends BaseStatusEntity<String> {

    @Column(comment = "表单ID", length = 50, order = 1)
    private String formDefinitionId;
    @Column(comment = "批次ID", length = 50, order = 2)
    private String registerId;
    /**
     * 原文校验1  目录校验2 原文目录对比校验3
     */
    @Column(comment = "校验类型")
    private String resultType;
    @Column(comment = "字段名称")
    private String fieldName;
    @Column(comment = "字符类型")
    private String characterType;
    @Column(comment = "校验规则")
    private String rules;
    @Column(comment = "预设值")
    private String preset;
    @Column(comment = "是否必填")
    private boolean isRequired;
    @Column(comment = "是否重复")
    private boolean isRepeatability;

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

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getCharacterType() {
        return characterType;
    }

    public void setCharacterType(String characterType) {
        this.characterType = characterType;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public String getPreset() {
        return preset;
    }

    public void setPreset(String preset) {
        this.preset = preset;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public boolean isRepeatability() {
        return isRepeatability;
    }

    public void setRepeatability(boolean repeatability) {
        isRepeatability = repeatability;
    }
}
