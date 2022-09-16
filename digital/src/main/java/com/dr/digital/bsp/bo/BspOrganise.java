package com.dr.digital.bsp.bo;

import com.alibaba.fastjson.annotation.JSONField;
import com.dr.digital.bsp.config.BooleanDeserializer;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * bsp组织机构
 *
 * @author dr
 */
public class BspOrganise extends AbstractBspIdEntity {
    /**
     * 机构地址
     */
    @JsonAlias("ADDRESS")
    @JSONField(name="ADDRESS")
    private String address;
    /**
     * 子机构数量
     */
    @JsonAlias("CHILDS")
    @JSONField(name="CHILDS")
    private int childCount;
    /**
     * 机构编码，一般配置为统一社会信用代码
     */
    @JsonAlias("CODE")
    @JSONField(name="CODE")
    private String code;
    /**
     * 机构邮箱
     */
    @JsonAlias("EMAIL")
    @JSONField(name="EMAIL")
    private String email;
    /**
     * 机构图标
     */
    @JsonAlias("ICON")
    @JSONField(name="ICON")
    private String icon;
    /**
     * 是否业务部门
     */
    @JsonAlias("IS_BUSINESS")
    @JSONField(name="IS_BUSINESS")
    @JsonDeserialize(using = BooleanDeserializer.class)
    private boolean business;
    /**
     * 是否进驻中心
     */
    @JsonAlias("IS_CENTER")
    @JSONField(name="IS_CENTER")
    @JsonDeserialize(using = BooleanDeserializer.class)
    private boolean center;
    /**
     * 使得进驻大厅
     */
    @JsonAlias("IS_HALL")
    @JSONField(name="IS_HALL")
    @JsonDeserialize(using = BooleanDeserializer.class)
    private boolean hall;
    /**
     * 是否执法中心
     */
    @JsonAlias("IS_LAW")
    @JSONField(name="IS_LAW")
    @JsonDeserialize(using = BooleanDeserializer.class)
    private boolean law;
    /**
     * 是否监察
     */
    @JsonAlias("IS_MONITOR")
    @JSONField(name="IS_MONITOR")
    @JsonDeserialize(using = BooleanDeserializer.class)
    private boolean monitor;
    /**
     * 是否风险单位
     */
    @JsonAlias("IS_RISK")
    @JSONField(name="IS_RISK")
    @JsonDeserialize(using = BooleanDeserializer.class)
    private boolean risk;
    /**
     * 机构负责人
     */
    @JsonAlias("LEADER")
    @JSONField(name="LEADER")
    private String leader;
    /**
     * 机构名称
     */
    @JsonAlias("NAME")
    @JSONField(name="NAME")
    private String name;
    /**
     * 机构级别
     */
    @JsonAlias("ORGAN_LEVEL")
    @JSONField(name="ORGAN_LEVEL")
    private String organLevel;
    /**
     * 组织机构类型id
     */
    @JsonAlias("ORGAN_TYPE")
    @JSONField(name="ORGAN_TYPE")
    private String organType;
    /**
     * 地图坐标
     */
    @JsonAlias("ORGAN_X")
    @JSONField(name="ORGAN_X")
    private String x;
    @JsonAlias("ORGAN_Y")
    @JSONField(name="ORGAN_Y")
    private String y;

    /**
     * 机构门户
     */
    @JsonAlias("PAGE_HOME")
    @JSONField(name="PAGE_HOME")
    private String pageHome;
    /**
     * 父级编码
     */
    @JsonAlias("PARENT_CODE")
    @JSONField(name="PARENT_CODE")
    private String parentCode;
    /**
     * 机构电话
     */
    @JsonAlias("PHONE")
    @JSONField(name="PHONE")
    private String phone;
    /**
     * 机构拼音
     */
    @JsonAlias("PINYIN")
    @JSONField(name="PINYIN")
    private String pinyin;
    /**
     * 区划编码
     */
    @JsonAlias("REGION_CODE")
    @JSONField(name="REGION_CODE")
    private String regionCode;
    /**
     * 区划名称
     */
    @JsonAlias("REGION_NAME")
    @JSONField(name="REGION_NAME")
    private String regionName;
    /**
     * 备注
     */
    @JsonAlias("REMARK")
    @JSONField(name="REMARK")
    private String remark;
    /**
     * 编码简写
     */
    @JsonAlias("SHORT_CODE")
    @JSONField(name="SHORT_CODE")
    private String shortCode;
    /**
     * 名称简写
     */
    @JsonAlias("SHORT_NAME")
    @JSONField(name="SHORT_NAME")
    private String shortName;
    /**
     * 机构排序
     */
    @JsonAlias("SORT_ORDER")
    @JSONField(name="SORT_ORDER")
    private int order;
    /**
     * 职能级别，0机关、1处室、2科室
     */
    @JsonAlias("TYPE")
    @JSONField(name="TYPE")
    private String type;
    /**
     * 职能级别名称
     */
    @JsonAlias("TYPE_NAME")
    @JSONField(name="TYPE_NAME")
    private String typeName;
    /**
     * 邮政编码
     */
    @JsonAlias("ZIP_CODE")
    @JSONField(name="ZIP_CODE")
    private String zipCode;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isBusiness() {
        return business;
    }

    public void setBusiness(boolean business) {
        this.business = business;
    }

    public boolean isCenter() {
        return center;
    }

    public void setCenter(boolean center) {
        this.center = center;
    }

    public boolean isHall() {
        return hall;
    }

    public void setHall(boolean hall) {
        this.hall = hall;
    }

    public boolean isLaw() {
        return law;
    }

    public void setLaw(boolean law) {
        this.law = law;
    }

    public boolean isMonitor() {
        return monitor;
    }

    public void setMonitor(boolean monitor) {
        this.monitor = monitor;
    }

    public boolean isRisk() {
        return risk;
    }

    public void setRisk(boolean risk) {
        this.risk = risk;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganLevel() {
        return organLevel;
    }

    public void setOrganLevel(String organLevel) {
        this.organLevel = organLevel;
    }

    public String getOrganType() {
        return organType;
    }

    public void setOrganType(String organType) {
        this.organType = organType;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getPageHome() {
        return pageHome;
    }

    public void setPageHome(String pageHome) {
        this.pageHome = pageHome;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
}
