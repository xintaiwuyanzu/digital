package com.dr.digital.manage.task.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

import static com.dr.framework.common.entity.StatusEntity.STATUS_COLUMN_KEY;

/**
 * 批次导入详情
 *
 * @author dr
 */
@Table(name = Constants.TABLE_PREFIX + "BATCH_DETAIL_YUANWEN", comment = "原文拆分", module = Constants.MODULE_NAME)
public class YuanWenBatchDetail extends AbstractBatchDetailEntity {
    @Column(comment = "拆分结果", length = 10)
    private String split;
    @Column(comment = "图片名称", length = 800)
    private String imgName;
    @Column(comment = "图片大小", length = 100)
    private String imgSize;
    @Column(comment = "图片页数", length = 50)
    private String imgYeShu;
    @Column(comment = "目录页数", length = 50)
    private String muLuYeShu;
    @Column(comment = "档号配置", length = 100)
    private String dHMs;

    public String getSplit() {
        return split;
    }

    public void setSplit(String split) {
        this.split = split;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public String getImgSize() {
        return imgSize;
    }

    public void setImgSize(String imgSize) {
        this.imgSize = imgSize;
    }

    public String getImgYeShu() {
        return imgYeShu;
    }

    public void setImgYeShu(String imgYeShu) {
        this.imgYeShu = imgYeShu;
    }

    public String getMuLuYeShu() {
        return muLuYeShu;
    }

    public void setMuLuYeShu(String muLuYeShu) {
        this.muLuYeShu = muLuYeShu;
    }

    public String getdHMs() {
        return dHMs;
    }

    public void setdHMs(String dHMs) {
        this.dHMs = dHMs;
    }
}
