package com.dr.digital.open.vo;

import java.util.Map;

/**
 * @author caor
 * @date 2021-12-14 21:30
 */
public class ReturnData {

    private String returncode;
    private String returnmsg;
    private Map<String, Object> data; //bytes  convertStatus  type

    public ReturnData(String returncode, String returnmsg, Map<String, Object> data) {
        this.returncode = returncode;
        this.returnmsg = returnmsg;
        this.data = data;
    }

    public static ReturnData success() {
        return new ReturnData("200", "success", null);
    }

    public static <T> ReturnData success(Map data) {
        return new ReturnData("200", "success", data);
    }

    public static <T> ReturnData success(String message, Map data) {
        return new ReturnData("200", message, data);
    }

    public static ReturnData error(Map data) {
        return new ReturnData("false","false",data);
    }

    public static <T> ReturnData error(String message, String code, Map data) {
        return new ReturnData( message, code, data);
    }

    public ReturnData() {
    }

    public String getReturncode() {
        return returncode;
    }

    public void setReturncode(String returncode) {
        this.returncode = returncode;
    }

    public String getReturnmsg() {
        return returnmsg;
    }

    public void setReturnmsg(String returnmsg) {
        this.returnmsg = returnmsg;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
