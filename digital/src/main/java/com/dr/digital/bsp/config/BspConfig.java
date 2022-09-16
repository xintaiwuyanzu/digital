package com.dr.digital.bsp.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * bsp相关配置
 *
 * @author dr
 */
@ConfigurationProperties("bsp")
public class BspConfig {
    /**
     * 接口访问基础路径
     */
    private String baseUrl;
    /**
     * bsp dubbo访问路径
     */
    @Value("${dubboUrl}")
    private String dubboUrl;
    /**
     * 客户端名称
     */
    private String name;
    /**
     * 客户端描述
     */
    private String description;
    /**
     * 档案馆系统编码
     */
    private String dagCode = "INSPUR-DZZW-DAG";
    /**
     * 福州市电子档案管理中心内门户编码
     */
    private String portalCode = "INSPUR-DZZW-GATEWAY";
    /**
     * 智能归档配置系统编码
     */
    private String configSysCode = "INSPUR-DZZW-ARCHIVES";
    /**
     * sso str编码方式
     */
    private Charset ssoEncoding = StandardCharsets.UTF_8;
    /**
     * 浪潮sso aes对称加密密码
     */
    private String ssoSecretKey = "inspurh2wmABdfM7i3K80mAS";
    /**
     * sm4 国密4加密解密密钥
     * 需要注意这里的密钥不能随便改，密钥长度必须是16位！！
     */
    private String sm4Key = "archive_fuzhoulc";
    /**
     * 与bsp同步的行政区划的根区划编码（福州市）
     */
    private String rootRegionCode = "350100000000";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDubboUrl() {
        return dubboUrl;
    }

    public void setDubboUrl(String dubboUrl) {
        this.dubboUrl = dubboUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDagCode() {
        return dagCode;
    }

    public void setDagCode(String dagCode) {
        this.dagCode = dagCode;
    }

    public String getPortalCode() {
        return portalCode;
    }

    public void setPortalCode(String portalCode) {
        this.portalCode = portalCode;
    }

    public String getConfigSysCode() {
        return configSysCode;
    }

    public void setConfigSysCode(String configSysCode) {
        this.configSysCode = configSysCode;
    }

    public void setSsoEncoding(Charset ssoEncoding) {
        this.ssoEncoding = ssoEncoding;
    }

    public Charset getSsoEncoding() {
        return ssoEncoding;
    }

    public String getSsoSecretKey() {
        return ssoSecretKey;
    }

    public void setSsoSecretKey(String ssoSecretKey) {
        this.ssoSecretKey = ssoSecretKey;
    }

    public String getSm4Key() {
        return sm4Key;
    }

    public void setSm4Key(String sm4Key) {
        this.sm4Key = sm4Key;
    }

    public String getRootRegionCode() {
        return rootRegionCode;
    }

    public void setRootRegionCode(String rootRegionCode) {
        this.rootRegionCode = rootRegionCode;
    }
}
