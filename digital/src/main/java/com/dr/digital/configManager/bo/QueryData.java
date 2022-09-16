package com.dr.digital.configManager.bo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * @author Mr.Zhu
 * @date 2022/8/29 - 15:19
 */
public class QueryData {
    private String standardName;
    private String creator;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date crTime;
    private int flag;
    private String modifier;
    private String carrierType;
    private String id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date moTime;
    private String standardCode;

    public String getStandardName() {
        return standardName;
    }

    public void setStandardName(String standardName) {
        this.standardName = standardName;
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

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getCarrierType() {
        return carrierType;
    }

    public void setCarrierType(String carrierType) {
        this.carrierType = carrierType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getMoTime() {
        return moTime;
    }

    public void setMoTime(Date moTime) {
        this.moTime = moTime;
    }

    public String getStandardCode() {
        return standardCode;
    }

    public void setStandardCode(String standardCode) {
        this.standardCode = standardCode;
    }
}
