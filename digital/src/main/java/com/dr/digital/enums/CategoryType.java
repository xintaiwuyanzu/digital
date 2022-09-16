package com.dr.digital.enums;

import org.springframework.util.StringUtils;

/**
 * 档案类型
 *
 * @author dr
 */
public enum CategoryType {
    //文件
    FILE(0, "文件"),
    //案卷
    ARC(1, "案卷"),
    //项目
    PRO(2, "项目"),
    //件盒
    BOX(3, "件盒");

    CategoryType(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public static CategoryType from(String codeOrValue) {
        if (!StringUtils.isEmpty(codeOrValue)) {
            for (CategoryType type : values()) {
                if (type.value.equalsIgnoreCase(codeOrValue)
                        || String.valueOf(type.code).equalsIgnoreCase(codeOrValue)) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException("未找到指定的档案类型：" + codeOrValue);
    }

    /**
     * 编码
     */
    private Integer code;

    /**
     * 描述
     */
    private String value;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
