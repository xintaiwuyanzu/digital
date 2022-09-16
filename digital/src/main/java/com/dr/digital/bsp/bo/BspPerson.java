package com.dr.digital.bsp.bo;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.Date;

/**
 * bsp登录用户
 *
 * @author dr
 */
public class BspPerson extends AbstractBspIdEntity {
    /**
     * 用户名称
     */
    @JsonAlias("NAME")
    private String name;
    /**
     * 用户账号
     */
    @JsonAlias("ACCOUNT")
    private String account;
    /**
     * 密码
     */
    @JsonAlias("PASSWORD")
    private String password;
    /**
     * 用户工号
     */
    @JsonAlias("USER_CODE")
    private String userCode;
    /**
     * 用户工号
     */
    @JsonAlias("GRADE")
    private int grade;
    /**
     * 性别，0男1女
     */
    @JsonAlias("GENDER")
    private String gender;
    /**
     * 生日
     */
    @JsonAlias("BIRTHDAY")
    private String birthday;
    /**
     * 身份证
     */
    @JsonAlias("IDENTITY_NUM")
    private String identityNum;

    /**
     * 电话
     */
    @JsonAlias("PHONE")
    private String phone;

    /**
     * 手机
     */
    @JsonAlias("MOBILE")
    private String mobile;

    /**
     * 邮箱
     */
    @JsonAlias("EMAIL")
    private String email;
    /**
     * 职务
     */
    @JsonAlias("POSITION")
    private String position;

    /**
     * 用户类型
     */
    @JsonAlias("TYPE_CODE")
    private String typeCode;
    /**
     * 最后登录时间
     */
    @JsonAlias("LAST_LOGIN_TIME")
    private Date lastLoginTime;
    /**
     * 用户角色编码
     */
    @JsonAlias("ROLE_CODE")
    private String roleCode;
    /**
     * 区划编码
     */
    @JsonAlias("REGION_CODE")
    private String regionCode;
    /**
     * 区划名称
     */
    @JsonAlias("REGION_NAME")
    private String regionName;
    /**
     * 部门编码
     */
    @JsonAlias("ORG_CODE")
    private String orgCode;
    /**
     * 部门名称
     */
    @JsonAlias("ORG_NAME")
    private String orgName;
    /**
     * 部门简码
     */
    @JsonAlias("ORG_SHORT_CODE")
    private String orgShortCode;
    /**
     * 用户角色值
     */
    @JsonAlias("ROLE_VALUE")
    private String roleValue;
    /**
     * 是否管理员
     */
    @JsonAlias("IS_ADMIN")
    private int isAdmin;
    /**
     * 状态
     */
    @JsonAlias("STATUS")
    private String status;
    /**
     * 排序
     */
    @JsonAlias("SORT_ORDER")
    private int order;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getIdentityNum() {
        return identityNum;
    }

    public void setIdentityNum(String identityNum) {
        this.identityNum = identityNum;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
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

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgShortCode() {
        return orgShortCode;
    }

    public void setOrgShortCode(String orgShortCode) {
        this.orgShortCode = orgShortCode;
    }

    public String getRoleValue() {
        return roleValue;
    }

    public void setRoleValue(String roleValue) {
        this.roleValue = roleValue;
    }

    public int getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(int isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
