package com.dr.digital.bsp.bo;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * bap行政区划
 *
 * @author dr
 */
public class BspRegion {
    //下面是行政区划类型
    /**
     * 机关（区划直属机关）
     */
    public static final String REGION_TYPE_0 = "0";
    /**
     * 区划
     */
    public static final String REGION_TYPE_1 = "1";
    /**
     * 政府
     */
    public static final String REGION_TYPE_2 = "2";
    /**
     * 党委
     */
    public static final String REGION_TYPE_3 = "3";
    /**
     * 人大
     */
    public static final String REGION_TYPE_4 = "4";
    /**
     * 机关（区划派驻机关）
     */
    public static final String REGION_TYPE_9 = "9";
    /**
     * 区划类型
     */
    @JsonAlias("REGION_TYPE")
    private String type;
    /**
     * 区划编码
     */
    @JsonAlias("CODE")
    private String code;
    /**
     * 区划名称
     */
    @JsonAlias("NAME")
    private String name;
    /**
     * 区划排序
     */
    @JsonAlias("SORT_ORDER")
    private Integer order;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
