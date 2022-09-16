package com.dr.digital.ocr.entity;

import java.util.List;

/**
 * @author caor
 * @date 2021-09-22 14:32
 */
public class GeneralResultEntity {
    private String code;
    private String message;
    private Data data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }


    public static class Data {
        private String img_id;
        private List<Items> items;
        private String rotated_image_width;
        private String rotated_image_height;
        private String image_angle;
        private StructContent struct_content;
        private String msg_id;

        public String getRotated_image_width() {
            return rotated_image_width;
        }

        public void setRotated_image_width(String rotated_image_width) {
            this.rotated_image_width = rotated_image_width;
        }

        public String getRotated_image_height() {
            return rotated_image_height;
        }

        public void setRotated_image_height(String rotated_image_height) {
            this.rotated_image_height = rotated_image_height;
        }

        public String getImage_angle() {
            return image_angle;
        }

        public void setImage_angle(String image_angle) {
            this.image_angle = image_angle;
        }

        public StructContent getStruct_content() {
            return struct_content;
        }

        public void setStruct_content(StructContent struct_content) {
            this.struct_content = struct_content;
        }

        public String getMsg_id() {
            return msg_id;
        }

        public void setMsg_id(String msg_id) {
            this.msg_id = msg_id;
        }

        public String getImg_id() {
            return img_id;
        }

        public void setImg_id(String img_id) {
            this.img_id = img_id;
        }

        public List<Items> getItems() {
            return items;
        }

        public void setItems(List<Items> items) {
            this.items = items;
        }

    }
}
