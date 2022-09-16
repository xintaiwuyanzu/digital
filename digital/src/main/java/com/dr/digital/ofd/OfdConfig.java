package com.dr.digital.ofd;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ofd相关配置
 *
 * @author dr
 */
@Configuration
@ConfigurationProperties(prefix = "ofd")
public class OfdConfig {
    /**
     * ofd服务接口访问地址
     */
    private String baseIp;

    private String clientId;

    private String clientSecret;

    private String apiServerName;

    private String srcPath;

    private String targetPath;

    public String getBaseIp() {
        return baseIp;
    }

    public void setBaseIp(String baseIp) {
        this.baseIp = baseIp;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getApiServerName() {
        return apiServerName;
    }

    public void setApiServerName(String apiServerName) {
        this.apiServerName = apiServerName;
    }

    public String getSrcPath() {
        return srcPath;
    }

    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }
}
