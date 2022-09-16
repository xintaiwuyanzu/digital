package com.dr.digital.ocr.entity;

import java.util.List;

/**
 * @author caor
 * @date 2021-09-26 10:14
 */
public class Raw {
    private String image_angle;
    private String rotated_image_width;
    private String rotated_image_height;
    private List<Items> items;
    private List<Tables> tables;
    private StructContent struct_content;

    public String getImage_angle() {
        return image_angle;
    }

    public void setImage_angle(String image_angle) {
        this.image_angle = image_angle;
    }

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

    public List<Items> getItems() {
        return items;
    }

    public void setItems(List<Items> items) {
        this.items = items;
    }

    public List<Tables> getTables() {
        return tables;
    }

    public void setTables(List<Tables> tables) {
        this.tables = tables;
    }

    public StructContent getStruct_content() {
        return struct_content;
    }

    public void setStruct_content(StructContent struct_content) {
        this.struct_content = struct_content;
    }
}
