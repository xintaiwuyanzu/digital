package com.dr.digital.configManager.bo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.List;

/**
 * 方案元数据四性检测规则
 *
 * @author: qiuyf
 */
public class MetadataRuleTest {
    private String standard;
    private String sysCode;
    private String classify;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date crTime;
    private int flag;
    private String code;
    private String modifier;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date moTime;
    private String type;
    private String parentID;
    /*
     * 检测规则*/
    private List<TestRule> metadataRuleTest;
    private String arrange;
    private String orgCode;
    private String name;
    private String typeId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startTime;
    private String fixed;
    private String orders;
    private String id;
    private int examineState;

    public String getStandard() {
        return standard;
    }

    public MetadataRuleTest() {
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public String getSysCode() {
        return sysCode;
    }

    public void setSysCode(String sysCode) {
        this.sysCode = sysCode;
    }

    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public List<TestRule> getMetadataRuleTest() {
        return metadataRuleTest;
    }

    public void setMetadataRuleTest(List<TestRule> metadataRuleTest) {
        this.metadataRuleTest = metadataRuleTest;
    }

    public String getArrange() {
        return arrange;
    }

    public void setArrange(String arrange) {
        this.arrange = arrange;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getFixed() {
        return fixed;
    }

    public void setFixed(String fixed) {
        this.fixed = fixed;
    }

    public String getOrders() {
        return orders;
    }

    public void setOrders(String orders) {
        this.orders = orders;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getExamineState() {
        return examineState;
    }

    public void setExamineState(int examineState) {
        this.examineState = examineState;
    }
}
