package com.dr.digital.configManager.bo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class AbstractConfigEntity {
    /**
     * 元数据类型id
     */
    private String id;
    /**
     * 创建人
     */
    private String creator;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date crTime;
    /**
     * 更新人
     */
    private String modifier;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date moTime;
    /**
     * 状态
     * 1
     */
    private String flag;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getCrTime() {
        return crTime;
    }

    public void setCrTime(Date crTime) {
        this.crTime = crTime;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public Date getMoTime() {
        return moTime;
    }

    public void setMoTime(Date moTime) {
        this.moTime = moTime;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
}
