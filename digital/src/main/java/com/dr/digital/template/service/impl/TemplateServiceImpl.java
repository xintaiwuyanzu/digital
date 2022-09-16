package com.dr.digital.template.service.impl;

import com.dr.digital.manage.form.service.ArchiveDataManager;
import com.dr.digital.manage.log.service.ArchivesLogService;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.register.entity.Register;
import com.dr.digital.register.entity.RegisterInfo;
import com.dr.digital.register.service.RegisterService;
import com.dr.digital.template.service.TemplateService;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.form.core.service.FormDataService;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    ArchiveDataManager dataManager;
    @Autowired
    FormDataService formDataService;
    @Autowired
    ArchivesLogService archivesLogService;
    @Autowired
    CommonMapper commonMapper;

    @Override
    public List<Register> selectList(SqlQuery<Register> sqlQuery) {
        sqlQuery.orderBy(RegisterInfo.ORDERBY);
        return commonMapper.selectByQuery(sqlQuery);
    }

    @Override
    public void updateStatus(Person person, BaseQuery query, String type) {
        List<FormData> dataList = dataManager.findDataByQuery(query);
        if (dataList.size() > 0) {
            dataList.forEach(d -> this.updateDetail(person, d, type, query.getFondId()));
        }
    }

    @Override
    public void lhUpdateType(Person person, BaseQuery query, String type, String childFormId) {
        List<FormData> dataList = dataManager.findDataByQuery(query);
        for (FormData list : dataList) {
            //查询当前状态下的卷内信息
            lhUpdateDetail(person, list, type, childFormId, query.getFondId());
            updateDetail(person, list, type, query.getFondId());
        }
    }

    @Override
    public void lhUpdateStatus(String ids, String status, String formDefinitionId, String childFormId, String registerId, Person person) {
        String[] split = ids.split(",");
        for (String id : split) {
            FormData formData = formDataService.selectOneFormData(formDefinitionId, id);
            lhUpdateDetail(person, formData, status, childFormId, registerId);
            updateDetail(person, formData, status, registerId);
        }
    }

    public void updateDetail(Person person, FormData formDate, String type, String registerId) {
        //增加日志记录
        addLongs(person, formDate, type, registerId);
        formDate.put(ArchiveEntity.COLUMN_STATUS, type);
        formDataService.updateFormDataById(formDate);
    }

    /**
     * 查询当前状态下的卷内信息
     *
     * @param formData
     * @param type
     * @param childFormId
     */
    public void lhUpdateDetail(Person person, FormData formData, String type, String childFormId, String registerId) {
        List<FormData> formList = formDataService.selectFormData(childFormId, (sqlQuery, formRelationWrapper) -> {
            sqlQuery.like(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_AJDH), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE))
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), formData.get(ArchiveEntity.COLUMN_STATUS) + "")
                    .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
        if (formList.size() > 0) {
            formList.forEach(d -> this.updateDetail(person, d, type, registerId));
        }
    }

    /**
     * 新增操作环节记录
     *
     * @param person
     * @param formDate
     * @param type
     * @param registerId
     */
    public void addLongs(Person person, FormData formDate, String type, String registerId) {
        String status = formDate.get(ArchiveEntity.COLUMN_STATUS);
        if (!StringUtils.isEmpty(status)) {
            if ("RECEIVE".equals(status) && "SCANNING".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_SCANNING,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "新增图像扫描",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("SCANNING".equals(status) && "PROCESSING".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_PROCESSING,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "新增图像修改",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("PROCESSING".equals(status) && "VOLUMES".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_VOLUMES,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "新增看图著录",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("VOLUMES".equals(status) && "QUALITY".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_QUALITY,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "新增初级验收",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("QUALITY".equals(status) && "RECHECK".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_RECHECK,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "新增复检验收",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("RECHECK".equals(status) && "OVER".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_OVER,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "新增数字化成果",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("OVER".equals(status) && "RECHECK".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_RECHECK,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "退回复检验收",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("RECHECK".equals(status) && "QUALITY".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_QUALITY,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "退回初检验收",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("QUALITY".equals(status) && "VOLUMES".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_VOLUMES,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "退回看图著录",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("VOLUMES".equals(status) && "PROCESSING".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_PROCESSING,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "退回图像修改",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("PROCESSING".equals(status) && "SCANNING".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_SCANNING,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "退回图像扫描",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            } else if ("SCANNING".equals(status) && "RECEIVE".equals(type)) {
                archivesLogService.addArchiveLog(registerId, person.getUserName(),
                        formDate.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "",
                        ArchiveDataManager.STATUS_RECEIVE,
                        formDate.get(ArchiveEntity.COLUMN_TITLE) + "",
                        "退回登记",
                        formDate.get(ArchiveEntity.COLUMN_AJH) + "");
            }
        }
    }

}
