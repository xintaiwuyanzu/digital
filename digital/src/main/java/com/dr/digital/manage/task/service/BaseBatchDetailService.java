package com.dr.digital.manage.task.service;

import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.manage.task.entity.AbstractBatchDetailEntity;
import com.dr.digital.manage.task.entity.ArchiveBatch;
import com.dr.digital.manage.task.vo.BatchCount;
import com.dr.framework.common.page.Page;

import java.util.List;

/**
 * 基础的批量操作service
 *
 * @author caor
 */
public interface BaseBatchDetailService<D extends AbstractBatchDetailEntity> {
    /**
     * 移交
     */
    String BATCH_TYPE_SEND_CHECK = "SEND_CHECK";
    /**
     * 鉴定
     */
    String BATCH_TYPE_JD = "JD";
    /**
     * 导入
     */
    String BATCH_TYPE_IMP = "IMP";
    /**
     * 导出
     */
    String BATCH_TYPE_EXP = "EXP";
    /**
     * 归档
     */
    String BATCH_TYPE_ARCHIVE = "ARCHIVE";
    /**
     * 纠错
     */
    String BATCH_TYPE_ERROR_RECOVERY = "ERROR_RECOVERY";
    /**
     * 挂接
     */
    String BATCH_TYPE_FILE_HOOK = "FILE_HOOK";
    /**
     * 重建索引
     */
    String BATCH_TYPE_REBUILD_INDEX = "REBUILD_INDEX";

    /**
     * 上传原文
     */
    String BATCH_TYPE_IMP_YUANWEN = "IMP_YUANWEN";

    /**
     * 根据查询条件创建详情信息
     *
     * @param query
     * @param entity
     */
    void createDetail(BaseQuery query, ArchiveBatch entity);

    /**
     * 根据批次Id查询分页数据
     *
     * @param batch
     * @param start
     * @param pageSize
     * @return
     */
    Page<D> selectPage(ArchiveBatch batch, Integer start, Integer pageSize);

    /**
     * 根据批次Id统计总数
     *
     * @param batchId
     * @return
     */
    BatchCount count(String batchId);

    /**
     * 获取批次类型
     *
     * @return
     */
    String getType();

    /**
     * 获取批次名称
     *
     * @return
     */
    String getName();
    List<D> selectList(ArchiveBatch batch);

    /**
     * 删除批次时调用
     * 关联删除批次详情
     *
     * @param batchId
     * @return
     */
    long deleteByBatchId(String batchId);

    /**
     * 根据detailDd查询详细信息
     *
     * @param id
     * @return
     */
    D detail(String id);
}
