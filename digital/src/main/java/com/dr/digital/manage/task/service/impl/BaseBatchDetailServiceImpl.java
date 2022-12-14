package com.dr.digital.manage.task.service.impl;

import com.dr.digital.configManager.bo.LinkFlowPath;
import com.dr.digital.manage.form.service.ArchiveDataManager;
import com.dr.digital.manage.model.entity.AbstractArchiveEntity;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.manage.task.entity.AbstractBatchDetailEntity;
import com.dr.digital.manage.task.entity.ArchiveBatch;
import com.dr.digital.manage.task.service.BaseBatchDetailService;
import com.dr.digital.manage.task.vo.BatchCount;
import com.dr.digital.util.SecurityHolderUtil;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.StatusEntity;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.page.Page;
import com.dr.framework.common.service.CommonService;
import com.dr.framework.common.service.DefaultDataBaseService;
import com.dr.framework.core.organise.service.OrganisePersonService;
import com.dr.framework.core.orm.module.EntityRelation;
import com.dr.framework.core.orm.sql.Column;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.dr.framework.core.security.SecurityHolder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.Executor;

public abstract class BaseBatchDetailServiceImpl<D extends AbstractBatchDetailEntity> implements BaseBatchDetailService<D>, InitializingBean {
    @Autowired
    CommonMapper commonMapper;
    @Autowired
    ArchiveDataManager dataManager;
    @Autowired
    DefaultDataBaseService dataBaseService;
    @Autowired
    Executor executor;
    @Autowired
    OrganisePersonService organisePersonService;
    /**
     * ?????????
     */
    protected EntityRelation entityRelation;
    /**
     * ????????????
     */
    private Class<D> entityClass;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDetail(BaseQuery query, ArchiveBatch entity) {
        String personId = SecurityHolder.get() == null ? "admin" : SecurityHolder.get().currentPerson().getId();
        SecurityHolder securityHolder = SecurityHolderUtil.checkSecurityHolder(organisePersonService, personId);
        executor.execute(() -> {
            SecurityHolder.set(securityHolder);
            //??????????????????????????????
            doCreateDetail(entity, query);
        });
    }

    @Override
    public Page<D> selectPage(ArchiveBatch batch, Integer start, Integer pageSize) {
        SqlQuery<D> sqlQuery = SqlQuery.from(getEntityClass())
                .equal(getEntityRelation().getColumn("batchId"), batch.getId());
        if (!StringUtils.isEmpty(batch.getStatus())) {
            sqlQuery.equal(getEntityRelation().getColumn(StatusEntity.STATUS_COLUMN_KEY), batch.getStatus());
        }
        //?????????batchnName ??????????????????
        if (!StringUtils.isEmpty(batch.getBatchName())) {
            sqlQuery.like(getEntityRelation().getColumn(ArchiveEntity.COLUMN_TITLE), batch.getBatchName());
        }
        //TODO????????????71???????????????
        sqlQuery.orderBy(getEntityRelation().getColumn("CREATEDATE"));
        return commonMapper.selectPageByQuery(sqlQuery, start * pageSize, (start + 1) * pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public BatchCount count(String batchId) {
        Column batchIdColumn = entityRelation.getColumn("batchId");
        Column statusColumn = entityRelation.getColumn(StatusEntity.STATUS_COLUMN_KEY);
        Class entityClass = getEntityClass();
        long total = commonMapper.countByQuery(SqlQuery.from(entityClass).equal(batchIdColumn, batchId));
        long success = commonMapper.countByQuery(
                SqlQuery.from(entityClass)
                        .equal(batchIdColumn, batchId)
                        .equal(statusColumn, StatusEntity.STATUS_ENABLE)
        );
        long fail = commonMapper.countByQuery(
                SqlQuery.from(entityClass)
                        .equal(batchIdColumn, batchId)
                        .equal(statusColumn, StatusEntity.STATUS_UNKNOW_STR)
        );
        long undo = commonMapper.countByQuery(
                SqlQuery.from(entityClass)
                        .equal(batchIdColumn, batchId)
                        .equal(statusColumn, StatusEntity.STATUS_DISABLE_STR)
        );
        return new BatchCount(total, success, fail, undo);
    }


    @Override
    public long deleteByBatchId(String batchId) {
        return commonMapper.deleteByQuery(SqlQuery.from(getEntityClass()).equal(getEntityRelation().getColumn("batchId"), batchId));
    }

    @Override
    public List<D> selectList(ArchiveBatch batch) {
        SqlQuery<D> sqlQuery = SqlQuery.from(getEntityClass())
                .equal(getEntityRelation().getColumn("batchId"), batch.getId()).orderBy(getEntityRelation().getColumn("id"));
        if (!StringUtils.isEmpty(batch.getStatus())) {
            sqlQuery.equal(getEntityRelation().getColumn(StatusEntity.STATUS_COLUMN_KEY), batch.getStatus());
        }
        return commonMapper.selectByQuery(sqlQuery);
    }

    /**
     * ?????????????????????????????????detail??????
     *
     * @param batch
     * @param query
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    protected void doCreateDetail(ArchiveBatch batch, BaseQuery query) {
        List<FormData> dataList = dataManager.findDataByQuery(query);
        dataList.forEach(d -> this.newBatchDetail(d, batch, query));
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param data
     * @param batch
     * @param query
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    protected D newBatchDetail(FormData data, ArchiveBatch batch, BaseQuery query) {
        //????????????
        D detail = newDetail(query);
        bindBaseInfo(detail, data, query);
        //????????????Id
        detail.setBatchId(batch.getId());
        //????????????
        commonMapper.insert(detail);
        return detail;
    }

    /**
     * ??????????????????
     *
     * @param detail
     * @param data
     */
    protected void bindBaseInfo(D detail, FormData data, BaseQuery query) {
        //????????????
        detail.setFonds_identifier(data.get(ArchiveEntity.COLUMN_FOND_CODE));
        detail.setFonds_name("");
        //??????????????????
        detail.setArchivers_category_code(data.get(ArchiveEntity.COLUMN_CATEGORY_CODE));
        detail.setCategoryId("");
        //????????????
        detail.setFormDefinitionId(data.getFormDefinitionId());
        detail.setFormDataId(data.getId());
        //????????????
//        detail.setId(data.getId());
        detail.setStatus(StatusEntity.STATUS_DISABLE_STR);
        detail.setOrganizational_structure_or_function(data.getString(AbstractArchiveEntity.COLUMN_ORG_CODE));
        detail.setArchival_code(data.getString(AbstractArchiveEntity.COLUMN_ARCHIVE_CODE));
        detail.setTitle(data.getString(AbstractArchiveEntity.COLUMN_TITLE));
        detail.setKeyword(data.getString(AbstractArchiveEntity.COLUMN_KEY_WORDS));
        detail.setNote(data.getString(AbstractArchiveEntity.COLUMN_NOTE));
        detail.setYear(data.getString(AbstractArchiveEntity.COLUMN_YEAR));
        //????????????
        detail.setFourDetection(data.getString("fourDetection"));
        //??????????????????
        CommonService.bindCreateInfo(detail);
    }

    /**
     * ??????detail??????
     *
     * @return
     */
    protected D newDetail(BaseQuery query) {
        try {
            return getEntityClass().newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * ??????id ??????detail????????????
     *
     * @param id
     * @return
     */
    @Override
    public D detail(String id) {
        return commonMapper.selectById(getEntityClass(), id);
    }

    /**
     * ?????????????????????
     *
     * @param ids
     * @param type
     * @param formDefinitionId
     */
    protected void updateStatus(String ids, String type, String formDefinitionId) {
    }

    /**
     * ????????????
     *
     * @param abstractBatchDetailEntityList
     */
    protected void doUpdateFormDate(ArchiveBatch archiveBatch, List<AbstractBatchDetailEntity> abstractBatchDetailEntityList) {
        abstractBatchDetailEntityList.forEach(detail -> {
            FormData formData = dataManager.selectOneFormData(detail.getFormDefinitionId(), detail.getFormDataId());
            dataManager.updateFormData(formData, "", "");
        });
    }

    /**
     * ???????????????????????????????????????
     */
    @Override
    public void afterPropertiesSet() {
        entityRelation = dataBaseService.getTableInfo(this.getEntityClass());
    }

    public synchronized Class<D> getEntityClass() {
        if (this.entityClass == null) {
            Type sc = this.getClass().getGenericSuperclass();
            if (ParameterizedType.class.isAssignableFrom(sc.getClass())) {
                Type[] types = ((ParameterizedType) sc).getActualTypeArguments();
                this.entityClass = (Class) types[0];
            }
        }
        return this.entityClass;
    }

    public EntityRelation getEntityRelation() {
        return entityRelation;
    }
}
