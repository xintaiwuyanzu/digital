package com.dr.digital.ocr.bo;

import java.util.List;

/**
 * 通用表格的
 *
 * @author caor
 * @date 2021-09-26 9:46
 */
public class TableBo {
    List<String> img_base64;

    public TableBo(List<String> img_base64) {
        this.img_base64 = img_base64;
    }

    public List<String> getImg_base64() {
        return img_base64;
    }

    public void setImg_base64(List<String> img_base64) {
        this.img_base64 = img_base64;
    }
}
