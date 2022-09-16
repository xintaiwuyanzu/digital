package com.dr.digital.bsp.bo;

import com.fasterxml.jackson.annotation.JsonAlias;

public class AbstractBspIdEntity {
    /**
     * 主键
     */
    @JsonAlias("ID")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
