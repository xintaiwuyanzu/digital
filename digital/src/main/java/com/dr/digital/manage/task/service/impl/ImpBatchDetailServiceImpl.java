package com.dr.digital.manage.task.service.impl;

import com.dr.digital.configManager.bo.LinkFlowPath;
import com.dr.digital.manage.form.service.ArchiveDataManager;
import com.dr.digital.manage.fournaturescheck.service.TestRecord2IMPService;
import com.dr.digital.manage.impexpscheme.bo.FormKeyMap;
import com.dr.digital.manage.impexpscheme.entity.ImpExpSchemeItem;
import com.dr.digital.manage.model.entity.AbstractArchiveEntity;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.manage.task.entity.ArchiveBatch;
import com.dr.digital.manage.task.entity.ImpBatchDetail;
import com.dr.framework.common.entity.IdEntity;
import com.dr.framework.common.entity.StatusEntity;
import com.dr.framework.common.form.core.model.FormData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导入
 *
 * @author: dr
 * @date: 2020/11/18 1:31
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ImpBatchDetailServiceImpl extends BaseDataParserBatchDetailService<ImpBatchDetail> {
    @Autowired
    ArchiveDataManager archiveDataManager;

    @Autowired
    TestRecord2IMPService testRecord2IMPService;

    @Override
    @Async
    protected void doCreateDetail(ArchiveBatch batch, BaseQuery query) {
        //更新状态
        batch.setStartDate(System.currentTimeMillis());
        //导入中
        batch.setStatus("2");
        commonMapper.updateById(batch);
        List<ImpExpSchemeItem> impExpSchemeItems = impExpSchemeItemService.selectBySchemaId(query.getImpSchemaId());
        List<FormKeyMap> formKeyMaps = impExpSchemeItems.stream().map(
                i -> {
                    FormKeyMap formKeyMap = new FormKeyMap();
                    formKeyMap.setTargetCode(i.getHashKey());
                    formKeyMap.setFieldName(i.getName());
                    formKeyMap.setFieldCode(i.getCode());
                    return formKeyMap;
                }).collect(Collectors.toList());
        try (FileInputStream inputStream = new FileInputStream(batch.getFileLocation())) {
            Iterator<Map<String, Object>> iterator = dataParser.readData(inputStream, batch.getMineType());
            while (iterator.hasNext()) {
                Map<String, Object> map = iterator.next();
                FormData data = new FormData(query.getFormDefinitionId());
                if (map.containsKey(IdEntity.ID_COLUMN_NAME)) {
                    data.setId((String) map.get(IdEntity.ID_COLUMN_NAME));
                }
                for (FormKeyMap formKeyMap : formKeyMaps) {
                    Object value = map.get(formKeyMap.getTargetCode());
                    if (value != null) {
                        data.put(formKeyMap.getFieldCode(), (Serializable) value);
                    }
                }
                //设置数据来源
                data.put(AbstractArchiveEntity.COLUMN_SOURCE_TYPE, query.getSourceCode());
                String status = query.getName();
                if (StringUtils.isEmpty(status)) {
                    status = ArchiveDataManager.STATUS_RECEIVE;
                }
                data.put(AbstractArchiveEntity.COLUMN_CATEGORY_CODE, query.getCategoryCode());
                //导入数据设置为预归档状态
                data.put(AbstractArchiveEntity.COLUMN_STATUS, status);
                data.put(AbstractArchiveEntity.COLUMN_TRANSITION_STATE, '0');
                data.put(AbstractArchiveEntity.COLUMN_SPLIT_STATE, '0');
                //新增的字段在这边初始化一下
                data.put(ArchiveEntity.DISASSEMBLY_TAGGING, "0");
                data.put(ArchiveEntity.COLUMN_QUALITY_STATE, "0");
                data.put(ArchiveEntity.COLUMN_QUALITY_CONDUCT_STATE, "0");
                data.put(ArchiveEntity.COLUMN_ASYNC_STATE, "0");
                data.put(ArchiveEntity.COLUMN_FOLDER_STATE,"0");
                data.put(ArchiveEntity.PEOPEL_CODE, "默认");
                data.put(ArchiveEntity.PEOPLE_NAME,"默认");
                data.put(ArchiveEntity.COLUMN_EXIT_FLOW_PATH, LinkFlowPath.RECEIVE);
                //数据清洗标识
                data.put(ArchiveEntity.DATA_CLEANING,"0");

                data.put(AbstractArchiveEntity.COLUMN_DISTINGUISH_STATE, '0');
                data.put(AbstractArchiveEntity.COLUMN_DISASSEMBLY_STATE, '0');
                data.put(AbstractArchiveEntity.COLUMN_PACKET_STATE, '0');
                //执行保存数据
                //TODO 需要实现四性检测未通过的不能插入
                archiveDataManager.insertFormData(data, null, query.getCategoryId());
                //创建批次记录
                //TODO 四性检测
                data = testRecord2IMPService.startTest(data, query);
                query.setFourDetection(data.getString("fourDetection"));
                newBatchDetail(data, batch, query);
            }
            //保存结果
            batch.setStatus(StatusEntity.STATUS_ENABLE_STR);
            batch.setEndDate(System.currentTimeMillis());
            commonMapper.updateById(batch);
        } catch (IOException e) {
            e.printStackTrace();
            batch.setStatus(StatusEntity.STATUS_UNKNOW_STR);
            batch.setEndDate(System.currentTimeMillis());
            commonMapper.updateById(batch);
        }
    }

    @Override
    public String getType() {
        return BATCH_TYPE_IMP;
    }

    @Override
    public String getName() {
        return "导入";
    }

}
