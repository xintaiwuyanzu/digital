package com.dr.digital.ofd.bo;

/**
 * 文件转换结果
 *
 * @author dr
 */
public class FileStreamResult {
    /**
     * 转换返回标准 0 成功
     */
    private String code;
    /**
     * 返回的信息
     */
    private String msg;
    /**
     * 返回文件的结果
     */
    private String data;

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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
