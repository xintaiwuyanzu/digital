package com.dr.digital.ofd.bo;

public class TokenInfo {
    private Long timestamp;

    private String clientId;

    private String clientSecret;

    private String apiServerName;

    public TokenInfo(Long timestamp,String clientId,String clientSecret,String apiServerName) {
        this.timestamp = timestamp;
        this.clientId = clientId;
        this.clientSecret = clientSecret ;
        this.apiServerName = apiServerName;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
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

}
