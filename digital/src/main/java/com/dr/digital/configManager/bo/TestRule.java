package com.dr.digital.configManager.bo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.List;

/**
 * @author: qiuyf
 * @date: 2022/5/26 14:44
 */
public class  TestRule {
    private String metadata;//字段名
    private String creator;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date crTime;
    private int flag;
    private String schemaId;
    private String modifier;
    private String typeId;
    private List<Rules> rules;
    private String id;
    private int examineState;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date moTime;
    private String typeCode;

    public TestRule() {
    }

    public Date getCrTime() {
        return crTime;
    }

    public void setCrTime(Date crTime) {
        this.crTime = crTime;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getExamineState() {
        return examineState;
    }

    public void setExamineState(int examineState) {
        this.examineState = examineState;
    }

    public Date getMoTime() {
        return moTime;
    }

    public void setMoTime(Date moTime) {
        this.moTime = moTime;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public List<Rules> getRules() {
        return rules;
    }

    public void setRules(List<Rules> rules) {
        this.rules = rules;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public static class Rules  {
        private String val;
        /*包含/不包含*/
        private String condition;
        /*
         * 包含,不包含,等于,大于,小于,不为空*/
        private List<Conditions>conditions;
        /*检测元数据字段*/
        private String metadata;
        /*规则类型
         * MaxMinVal 最大值最小值, codomain 值域, MaxMinLen 最大长度最小长度, disByte 禁用字符, type 数据类型*/
        private String type;
        private String maxVal;
        private String minVal;
        private String minLen;
        private String maxLen ;

        public Rules() {
        }

        public Rules(String val, String condition, List<Conditions> conditions, String metadata, String type, String maxVal, String minVal, String minLen, String maxLen) {
            this.val = val;
            this.condition = condition;
            this.conditions = conditions;
            this.metadata = metadata;
            this.type = type;
            this.maxVal = maxVal;
            this.minVal = minVal;
            this.minLen = minLen;
            this.maxLen = maxLen;
        }

        public String getVal() {
            return val;
        }

        public void setVal(String val) {
            this.val = val;
        }

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public List<Conditions> getConditions() {
            return conditions;
        }

        public void setConditions(List<Conditions> conditions) {
            this.conditions = conditions;
        }

        public String getMetadata() {
            return metadata;
        }

        public void setMetadata(String metadata) {
            this.metadata = metadata;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMaxVal() {
            return maxVal;
        }

        public void setMaxVal(String maxVal) {
            this.maxVal = maxVal;
        }

        public String getMinVal() {
            return minVal;
        }

        public void setMinVal(String minVal) {
            this.minVal = minVal;
        }

        public String getMinLen() {
            return minLen;
        }

        public void setMinLen(String minLen) {
            this.minLen = minLen;
        }

        public String getMaxLen() {
            return maxLen;
        }

        public void setMaxLen(String maxLen) {
            this.maxLen = maxLen;
        }
    }
    public static class Conditions{
       private String val;
       private String metadata;
       private String condition;

        public Conditions() {
        }

        public String getVal() {
            return val;
        }

        public void setVal(String val) {
            this.val = val;
        }

        public String getMetadata() {
            return metadata;
        }

        public void setMetadata(String metadata) {
            this.metadata = metadata;
        }

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }
    }
}
