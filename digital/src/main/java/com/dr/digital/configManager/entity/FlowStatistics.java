package com.dr.digital.configManager.entity;

/**
 * @author caor
 * @date 2022-07-15 13:39
 */

public class FlowStatistics {
    private String index;
    private String label;
    private String value;
    //应处理
    private String shouldNum;
    //剩余
    private String surPlusNum;
    //完成
    private String completeNum;
    //全部
    private String totalNum;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getShouldNum() {
        return shouldNum;
    }

    public void setShouldNum(String shouldNum) {
        this.shouldNum = shouldNum;
    }

    public String getSurPlusNum() {
        return surPlusNum;
    }

    public void setSurPlusNum(String surPlusNum) {
        this.surPlusNum = surPlusNum;
    }

    public String getCompleteNum() {
        return completeNum;
    }

    public void setCompleteNum(String completeNum) {
        this.completeNum = completeNum;
    }

    public String getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(String totalNum) {
        this.totalNum = totalNum;
    }
}
