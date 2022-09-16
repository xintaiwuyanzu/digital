package com.dr.digital.bsp.bo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * Bsp实体类父类
 *
 * @author dr
 */
public class AbstractBspEntity extends AbstractBspIdEntity {

    /**
     * 创建时间
     */
    @JsonAlias("CREATE_TIME")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createTime;

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
