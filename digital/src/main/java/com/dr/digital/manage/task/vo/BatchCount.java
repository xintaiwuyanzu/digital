package com.dr.digital.manage.task.vo;

/**
 * 前端显示用的对象
 *
 * @author dr
 */
public class BatchCount {
    /**
     * 总数  成功数量 失败数量 未办理数量
     */
    private long total, success, fail, undo;

    public BatchCount(long total, long success, long fail, long undo) {
        this.total = total;
        this.success = success;
        this.fail = fail;
        this.undo = undo;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getSuccess() {
        return success;
    }

    public void setSuccess(long success) {
        this.success = success;
    }

    public long getFail() {
        return fail;
    }

    public void setFail(long fail) {
        this.fail = fail;
    }

    public long getUndo() {
        return undo;
    }

    public void setUndo(long undo) {
        this.undo = undo;
    }
}
