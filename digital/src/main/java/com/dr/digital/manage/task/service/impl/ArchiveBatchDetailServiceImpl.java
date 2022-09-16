package com.dr.digital.manage.task.service.impl;

import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.manage.task.entity.ArchiveBatch;
import com.dr.digital.manage.task.entity.ArchiveBatchDetail;
import com.dr.digital.manage.task.vo.BatchCount;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.page.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 归档
 *
 * @author: caor
 * @date: 2020/11/18 1:31
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ArchiveBatchDetailServiceImpl extends BaseBatchDetailServiceImpl<ArchiveBatchDetail> {

    @Override
    protected ArchiveBatchDetail newBatchDetail(FormData data, ArchiveBatch batch, BaseQuery query) {
        //修改档案数据信息为管理库状态
        //dataManager.updateStatus(data.getId(), STATUS_RECEIVE, data.getFormDefinitionId());
        return super.newBatchDetail(data, batch, query);
    }


    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public ArchiveBatchDetail detail(String id) {
        return null;
    }

}
