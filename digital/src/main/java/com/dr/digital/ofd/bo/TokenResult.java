package com.dr.digital.ofd.bo;

public class TokenResult {
    /**
     * 转换返回标准 0 成功
     */
    private String code;

    /**
     * 返回的信息
     */
    private String msg;

    private Object data;

    public class Object {

        private String authToken;
        private Integer expires_in;

        public String getAuthToken() {
            return authToken;
        }

        public void setAuthToken(String authToken) {
            this.authToken = authToken;
        }

        public Integer getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(Integer expires_in) {
            this.expires_in = expires_in;
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
