package com.dr.digital.configManager.bo;

import java.util.Date;

/**
 * 全宗元数据信息
 *
 * @author dr
 */
public class FondInfo extends AbstractConfigEntity {
    /**
     * 全宗号称
     */
    private String numbers;
    /**
     * 全宗机构类型名称
     */
    private String orgName;
    /**
     * 全宗机构编码
     */
    private String orgCode;
    /**
     * 全宗开始时间
     */
    private Date startTime;
    /**
     * 全宗启用状态(0是历史全宗)
     */
    private String state;
    /**
     * 门类树
     */
    private String arcTypes;
    /**
     * 档案馆编码(属于档案馆才有)
     */
    private String archivesCode;
    /**
     * 归属档案馆(字典id)
     */
    private String belongArchives;

    public String getNumbers() {
        return numbers;
    }

    public void setNumbers(String numbers) {
        this.numbers = numbers;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getArcTypes() {
        return arcTypes;
    }

    public void setArcTypes(String arcTypes) {
        this.arcTypes = arcTypes;
    }

    public String getArchivesCode() {
        return archivesCode;
    }

    public void setArchivesCode(String archivesCode) {
        this.archivesCode = archivesCode;
    }

    public String getBelongArchives() {
        return belongArchives;
    }

    public void setBelongArchives(String belongArchives) {
        this.belongArchives = belongArchives;
    }
}
