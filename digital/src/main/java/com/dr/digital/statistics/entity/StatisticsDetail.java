package com.dr.digital.statistics.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseStatusEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

/**
 * @author Mr.Zhu
 * @date 2022/8/11 - 9:28
 */
@Table(name = Constants.TABLE_PREFIX + "STATISTICSDETAIL", module = Constants.MODULE_NAME, comment = "批次详情表")
public class StatisticsDetail extends BaseStatusEntity<String> {
    @Column(comment = "批次id")
    private String registerId;
    @Column(comment = "表单id")
    private String formDefinitionId;
    @Column(comment = "环节名", length = 50, order = 2)
    private String operationLink;
    @Column(comment = "办理人", length = 50, order = 2)
    private String HandledBy;
    @Column(comment = "开始时间", length = 50, order = 2)
    private long startDate;
    @Column(comment = "结束时间", length = 50, order = 2)
    private long endDate;
    @Column(comment = "总份数", length = 50, order = 2)
    private long totalFen;
    @Column(comment = "完成份数", length = 50, order = 2)
    private long completeFenNum;
    @Column(comment = "剩余份数", length = 50, order = 2)
    private long surPlusFenNum;
    @Column(comment = "总页数", length = 50, order = 2)
    private long totalPage;
    @Column(comment = "完成页数", length = 50, order = 2)
    private long completePageNum;
    @Column(comment = "剩余页数", length = 50, order = 2)
    private long surPlusPageNum;


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

    public String getOperationLink() {
        return operationLink;
    }

    public void setOperationLink(String operationLink) {
        this.operationLink = operationLink;
    }

    public String getHandledBy() {
        return HandledBy;
    }

    public void setHandledBy(String handledBy) {
        HandledBy = handledBy;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public long getTotalFen() {
        return totalFen;
    }

    public void setTotalFen(long totalFen) {
        this.totalFen = totalFen;
    }

    public long getCompleteFenNum() {
        return completeFenNum;
    }

    public void setCompleteFenNum(long completeFenNum) {
        this.completeFenNum = completeFenNum;
    }

    public long getSurPlusFenNum() {
        return surPlusFenNum;
    }

    public void setSurPlusFenNum(long surPlusFenNum) {
        this.surPlusFenNum = surPlusFenNum;
    }

    public long getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(long totalPage) {
        this.totalPage = totalPage;
    }

    public long getCompletePageNum() {
        return completePageNum;
    }

    public void setCompletePageNum(long completePageNum) {
        this.completePageNum = completePageNum;
    }

    public long getSurPlusPageNum() {
        return surPlusPageNum;
    }

    public void setSurPlusPageNum(long surPlusPageNum) {
        this.surPlusPageNum = surPlusPageNum;
    }
}
