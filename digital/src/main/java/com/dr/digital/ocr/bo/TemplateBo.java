package com.dr.digital.ocr.bo;

/**
 * @author caor
 * @date 2021-09-17 13:31
 */
public class TemplateBo {
    String img_base64;
    boolean with_struct_info;
    boolean with_char_info;

    public TemplateBo(boolean with_struct_info, boolean with_char_info, String img_base64) {
        this.with_struct_info = with_struct_info;
        this.with_char_info = with_char_info;
        this.img_base64 = img_base64;
    }

    public boolean isWith_struct_info() {
        return with_struct_info;
    }

    public void setWith_struct_info(boolean with_struct_info) {
        this.with_struct_info = with_struct_info;
    }

    public boolean isWith_char_info() {
        return with_char_info;
    }

    public void setWith_char_info(boolean with_char_info) {
        this.with_char_info = with_char_info;
    }

    public String getImg_base64() {
        return img_base64;
    }

    public void setImg_base64(String img_base64) {
        this.img_base64 = img_base64;
    }
}
