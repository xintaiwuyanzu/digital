package com.dr.digital.manage.task.service.impl;

import com.dr.digital.manage.impexpscheme.entity.ImpExpSchemeItem;
import com.dr.digital.manage.model.entity.AbstractArchiveEntity;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.manage.task.entity.ArchiveBatch;
import com.dr.digital.manage.task.entity.ExpBatchDetail;
import com.dr.digital.util.UUIDUtils;
import com.dr.framework.common.entity.StatusEntity;
import com.dr.framework.common.form.core.model.FormData;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 导出
 *
 * @author: dr
 * @date: 2020/11/18 1:31
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ExpBatchDetailServiceImpl extends BaseDataParserBatchDetailService<ExpBatchDetail> {

    @Override
    @Async
    protected void doCreateDetail(ArchiveBatch batch, BaseQuery query) {
        //更新批次状态
        batch.setStartDate(System.currentTimeMillis());
        batch.setStatus("2");
        //创建导出文件目录
        String targetDir = commonFileConfig.getUploadDirWithDate("exp");
        //创建导出文件
        String targetFile = String.join(File.separator, targetDir, UUIDUtils.getUUID() + "." + dataParser.getFileSuffix(batch.getMineType()));
        batch.setFileLocation(targetFile);
        commonMapper.updateById(batch);
        //查询映射方案
        List<ImpExpSchemeItem> impExpSchemeItems = impExpSchemeItemService.selectBySchemaId(query.getImpSchemaId());
        //创建接收文件
        List<FormData> dataList = dataManager.findDataByQuery(query);
        try (OutputStream outputStream = new FileOutputStream(targetFile)) {
            dataParser.writeData(
                    impExpSchemeItems.stream().map(ImpExpSchemeItem::getHashKey).distinct().toArray(String[]::new),
                    new ExpIterator(
                            dataList.listIterator(),
                            batch,
                            query,
                            impExpSchemeItems),
                    batch.getMineType(),
                    outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        batch.setStatus(StatusEntity.STATUS_ENABLE_STR);
        batch.setEndDate(System.currentTimeMillis());
        commonMapper.updateById(batch);
    }

    class ExpIterator implements Iterator<Map<String, Object>> {
        final Iterator<FormData> formDataIterator;
        final ArchiveBatch batch;
        final BaseQuery query;
        final List<ImpExpSchemeItem> impExpSchemeItems;

        ExpIterator(Iterator<FormData> formDataIterator, ArchiveBatch batch, BaseQuery query, List<ImpExpSchemeItem> impExpSchemeItems) {
            this.formDataIterator = formDataIterator;
            this.batch = batch;
            this.query = query;
            this.impExpSchemeItems = impExpSchemeItems;
        }

        @Override
        public boolean hasNext() {
            return formDataIterator.hasNext();
        }

        @Override
        public Map<String, Object> next() {
            FormData data = formDataIterator.next();
            newBatchDetail(data, batch, query);
            return convertData(data, impExpSchemeItems);
        }
    }

    /**
     * 将表单数据转换成目标格式
     *
     * @param formData
     * @param impExpSchemeItems
     * @return
     */
    private Map<String, Object> convertData(FormData formData, List<ImpExpSchemeItem> impExpSchemeItems) {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put(AbstractArchiveEntity.ID_COLUMN_NAME, formData.getId());
        objectMap.put(AbstractArchiveEntity.COLUMN_ARCHIVE_CODE, formData.getString(AbstractArchiveEntity.COLUMN_ARCHIVE_CODE));
        for (ImpExpSchemeItem item : impExpSchemeItems) {
            String key = item.getHashKey();
            Object value = formData.get(item.getCode());
            objectMap.put(key, value);
        }
        return objectMap;
    }

    @Override
    public String getType() {
        return BATCH_TYPE_EXP;
    }

    @Override
    public String getName() {
        return "导出";
    }

}
