package com.dr.digital.statistics.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseStatusEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

/**
 * @author Mr.Zhu
 * @date 2022/8/11 - 9:28
 */
@Table(name = Constants.TABLE_PREFIX + "STATISTICS", module = Constants.MODULE_NAME, comment = "批次统计表")
public class Statistics extends BaseStatusEntity<String> {
    @Column(comment = "批次id")
    private String registerId;
    @Column(comment = "表单id")
    private String formDefinitionId;
    @Column(comment = "批次号", length = 50, order = 2)
    private String batch_no;
    @Column(comment = "批次名称", length = 50, order = 2)
    private String batch_name;
    @Column(comment = "批次创建时间", length = 50, order = 2)
    private long batch_createDate;
    @Column(comment = "完成时间", length = 50, order = 2)
    private long completeDate;
    @Column(comment = "总份数", length = 50, order = 2)
    private long totalFen;
    @Column(comment = "总页数", length = 50, order = 2)
    private long totalPage;
    @Column(comment = "完成份数", length = 50, order = 2)
    private long completeFenNum;
    @Column(comment = "完成页数", length = 50, order = 2)
    private long completePageNum;
    @Column(comment = "参与人数", length = 50, order = 2)
    private long totalPeople;

    public String getRegisterId() {
        return registerId;
    }

    public void setRegisterId(String registerId) {
        this.registerId = registerId;
    }

    public String getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(String formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }

    public String getBatch_no() {
        return batch_no;
    }

    public void setBatch_no(String batch_no) {
        this.batch_no = batch_no;
    }

    public String getBatch_name() {
        return batch_name;
    }

    public void setBatch_name(String batch_name) {
        this.batch_name = batch_name;
    }

    public long getBatch_createDate() {
        return batch_createDate;
    }

    public void setBatch_createDate(long batch_createDate) {
        this.batch_createDate = batch_createDate;
    }

    public long getCompleteDate() {
        return completeDate;
    }

    public void setCompleteDate(long completeDate) {
        this.completeDate = completeDate;
    }

    public long getTotalFen() {
        return totalFen;
    }

    public void setTotalFen(long totalFen) {
        this.totalFen = totalFen;
    }

    public long getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(long totalPage) {
        this.totalPage = totalPage;
    }

    public long getCompleteFenNum() {
        return completeFenNum;
    }

    public void setCompleteFenNum(long completeFenNum) {
        this.completeFenNum = completeFenNum;
    }

    public long getCompletePageNum() {
        return completePageNum;
    }

    public void setCompletePageNum(long completePageNum) {
        this.completePageNum = completePageNum;
    }

    public long getTotalPeople() {
        return totalPeople;
    }

    public void setTotalPeople(long totalPeople) {
        this.totalPeople = totalPeople;
    }
}
