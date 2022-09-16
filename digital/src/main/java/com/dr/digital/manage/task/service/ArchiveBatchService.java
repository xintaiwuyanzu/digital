package com.dr.digital.manage.task.service;

import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.manage.task.entity.ArchiveBatch;
import com.dr.digital.manage.task.vo.BatchCount;
import com.dr.framework.common.page.Page;
import com.dr.framework.common.service.BaseService;
import com.dr.framework.core.organise.entity.Person;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 档案批次service
 *
 * @author dr
 */
public interface ArchiveBatchService extends BaseService<ArchiveBatch> {
    /**
     * 根据查询条件创建一个批次记录
     *
     * @param type
     * @param query
     * @return
     */
    ArchiveBatch newBatch(String type, BaseQuery query);

    /**
     * 统计批次信息
     *
     * @param type
     * @param batchId
     * @return
     */
    BatchCount count(String type, String batchId);

    /**
     * 更新批次状态
     *
     * @param entity
     * @return
     */
    @Override
    long updateById(ArchiveBatch entity);

    /**
     * 更新一条详情数据的状态
     *
     * @param type
     * @param detailId
     * @param status
     * @param advice
     */
    //AbstractBatchDetailEntity changeStatus(String type, String detailId, String status, String advice);

    /**
     * 根据type查询不同batchDetailPage
     *
     * @param batch
     * @param pageIndex
     * @param pageSize
     * @return
     */
    Page selectPage(ArchiveBatch batch, Integer pageIndex, Integer pageSize);

    /**
     * 根据detailDd查询详细信息
     *
     * @param id
     * @return
     */
    //AbstractBatchDetailEntity detail(String id, String type);

    /**
     * 审核办结
     *
     * @param entity
     * @param finish
     */
    //void doFinish(AbstractBatchDetailEntity entity, boolean finish);

    /**
     * 创建拆分记录
     *
     * @param type
     * @param query
     * @return
     */
    ArchiveBatch newBatchJpgByPath(String type, BaseQuery query, Person person);

    /**
     * 根据type查询不同batchDetailPage的记录数量
     *
     * @param batch
     * @return
     */
    List selectList(ArchiveBatch batch);
}
