package com.dr.digital.bsp.bo;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * bsp组织机构类型
 *
 * @author dr
 */
public class BspOrganType extends AbstractBspIdEntity {
    /**
     * 组织类型编码
     */
    @JsonAlias("CODE")
    private String code;
    /**
     * 组织类型名称
     */
    @JsonAlias("NAME")
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "BspOrganType{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
