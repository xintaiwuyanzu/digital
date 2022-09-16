package com.dr.digital.manage.task.service.impl;

import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.manage.task.entity.ArchiveBatch;
import com.dr.digital.manage.task.service.ArchiveBatchService;
import com.dr.digital.manage.task.service.BaseBatchDetailService;
import com.dr.digital.manage.task.vo.BatchCount;
import com.dr.digital.util.UUIDUtils;
import com.dr.framework.common.entity.StatusEntity;
import com.dr.framework.common.page.Page;
import com.dr.framework.common.service.CommonService;
import com.dr.framework.common.service.DefaultBaseService;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.security.SecurityHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dr
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ArchiveBatchServiceImpl extends DefaultBaseService<ArchiveBatch> implements ArchiveBatchService {
    Map<String, BaseBatchDetailService> baseBatchServiceMap;

    @Override
    public long deleteById(String... ids) {
        long result = super.deleteById(ids);
        for (String id : ids) {
            ArchiveBatch batch = selectById(id);
            BaseBatchDetailService service = baseBatchServiceMap.get(batch.getBatchType());
            service.deleteByBatchId(id);
        }
        return result;
    }


    @Override
    public BatchCount count(String type, String batchId) {
        return baseBatchServiceMap.get(type).count(batchId);
    }

    @Override
    public long updateById(ArchiveBatch entity) {
        return commonMapper.updateIgnoreNullById(entity);
    }

    @Override
    public Page selectPage(ArchiveBatch batch, Integer pageIndex, Integer pageSize) {
        BaseBatchDetailService service = baseBatchServiceMap.get(batch.getBatchType());
        return service.selectPage(batch, pageIndex, pageSize);
    }

    @Override
    public List selectList(ArchiveBatch batch) {
        BaseBatchDetailService service = baseBatchServiceMap.get(batch.getBatchType());
        return service.selectList(batch);
    }

    @Override
    public ArchiveBatch newBatch(String type, BaseQuery query) {
        BaseBatchDetailService baseBatchDetailService = baseBatchServiceMap.get(type);
        Assert.notNull(baseBatchDetailService, "不能处理：" + type + "类型的批量操作");
        ArchiveBatch batch = new ArchiveBatch();
        //创建批次信息
        batch.setStartDate(System.currentTimeMillis());
        batch.setBatchType(type);
        batch.setMineType(query.getMineType());
        batch.setFileLocation(query.getFileLocation());
        batch.setCategoryCode(query.getCategoryCode());
        batch.setFileName(query.getFileName());
        batch.setFourNatureSchemeId(query.getFourNatureSchemeId());
        if (!StringUtils.isEmpty(query.getFondId())) {
            batch.setFondId(query.getFondId());
        }
        Person person = SecurityHolder.get().currentPerson();
        String batchName = String.format("%s提交的%s", person.getUserName(), baseBatchDetailService.getName());
        batch.setHookFondCategory(query.getHookFondCategory());
        if ("IMP".equals(type)) {
            batchName = String.format("%s的数据导入", person.getUserName());
            batch.setDataSource(query.getSourceCode());
            batch.setFormid(query.getFormDefinitionId());
        }
        if ("IMP_YUANWEN".equals(type)) {
            batchName = String.format("%s提交的原文拆分", person.getUserName());
            batch.setDataSource(query.getSourceCode());
            batch.setFormid(query.getFormDefinitionId());
        }
        if ("FILE_HOOK".equals(type)) {
            batch.setHookFondCategory(query.getHookFondCategory());
            batch.setFormid(query.getFormid());
        }
        if (!StringUtils.isEmpty(query.getBatchName())) {
            batch.setBatchName(query.getBatchName());
        } else {
            batch.setBatchName(batchName);
        }
        batch.setBeizhu(query.getBeizhu());
        CommonService.bindCreateInfo(batch);
        commonMapper.insert(batch);
        baseBatchDetailService.createDetail(query, batch);
        return batch;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        Map<String, BaseBatchDetailService> beans = getApplicationContext().getBeansOfType(BaseBatchDetailService.class);
        baseBatchServiceMap = Collections.synchronizedMap(new HashMap<>(beans.size()));
        beans.forEach((k, v) -> baseBatchServiceMap.put(v.getType(), v));
    }

    @Override
    public ArchiveBatch newBatchJpgByPath(String type, BaseQuery query, Person person) {
        BaseBatchDetailService baseBatchDetailService = baseBatchServiceMap.get(type);
        Assert.notNull(baseBatchDetailService, "不能处理：" + type + "类型的批量操作");
        ArchiveBatch batch = new ArchiveBatch();
        //创建批次信息
        batch.setStartDate(System.currentTimeMillis());
        batch.setStatus(StatusEntity.STATUS_DISABLE_STR);
        batch.setBatchType(type);
        if (!StringUtils.isEmpty(query.getFondId())) {
            batch.setFondId(query.getFondId());
        }
        String batchName = String.format("%s提交的%s", person.getUserName(), baseBatchDetailService.getName());
        batch.setHookFondCategory(query.getHookFondCategory());
        if ("IMP_YUANWEN".equals(type)) {
            batchName = String.format("%s提交的原文拆分", person.getUserName());
            batch.setDataSource(query.getSourceCode());
            batch.setFormid(query.getFormDefinitionId());
        }
        if (!StringUtils.isEmpty(query.getBatchName())) {
            batch.setBatchName(query.getBatchName());
        } else {
            batch.setBatchName(batchName);
        }
        batch.setBeizhu(query.getBeizhu());
        batch.setId(UUIDUtils.getUUID());
        commonMapper.insert(batch);
        return batch;
    }
}
