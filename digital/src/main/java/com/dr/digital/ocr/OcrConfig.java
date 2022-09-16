package com.dr.digital.ocr;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ocr相关配置
 *
 * @author caor
 */
@Configuration
@ConfigurationProperties(prefix = "ocr")
public class OcrConfig {
    /**
     * 接口访问基础路径
     */
    private String baseUrl;
    /**
     * 客户端名称
     */
    private String name;
    /**
     * 客户端描述
     */
    private String description;

    /**
     * 用户创建应用的通用文字 pubkey
     */
    private String general_pubkey;
    /**
     * 用户创建应用的通用文字 secret_key
     */
    private String general_secret_key;
    /**
     * 用户创建应用的通用表格 pubkey
     */
    private String table_pubkey;
    /**
     * 用户创建应用的通用表格 secret_key
     */
    private String table_secret_key;

    /**
     * 用户创建应用的自定义模板 pubkey
     */
    private String template_pubkey;
    /**
     * 用户创建应用的自定义模板 secret_key
     */
    private String template_secret_key;

    public String getTable_pubkey() {
        return table_pubkey;
    }

    public void setTable_pubkey(String table_pubkey) {
        this.table_pubkey = table_pubkey;
    }

    public String getTable_secret_key() {
        return table_secret_key;
    }

    public void setTable_secret_key(String table_secret_key) {
        this.table_secret_key = table_secret_key;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
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

    public String getGeneral_pubkey() {
        return general_pubkey;
    }

    public void setGeneral_pubkey(String general_pubkey) {
        this.general_pubkey = general_pubkey;
    }

    public String getGeneral_secret_key() {
        return general_secret_key;
    }

    public void setGeneral_secret_key(String general_secret_key) {
        this.general_secret_key = general_secret_key;
    }

    public String getTemplate_pubkey() {
        return template_pubkey;
    }

    public void setTemplate_pubkey(String template_pubkey) {
        this.template_pubkey = template_pubkey;
    }

    public String getTemplate_secret_key() {
        return template_secret_key;
    }

    public void setTemplate_secret_key(String template_secret_key) {
        this.template_secret_key = template_secret_key;
    }
}
