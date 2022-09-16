package com.dr.digital.manage.log.service.impl;

import com.dr.digital.configManager.entity.FlowPathDetail;
import com.dr.digital.configManager.entity.FlowPathDetailInfo;
import com.dr.digital.configManager.service.impl.FlowPathServiceImpl;
import com.dr.digital.manage.log.entity.ArchivesLog;
import com.dr.digital.manage.log.entity.ArchivesLogInfo;
import com.dr.digital.manage.log.service.ArchivesLogService;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.ocr.entity.OcrQueue;
import com.dr.digital.ocr.entity.OcrQueueInfo;
import com.dr.digital.ofd.entity.OfdRecord;
import com.dr.digital.ofd.entity.OfdRecordInfo;
import com.dr.digital.packet.entity.PacketRecord;
import com.dr.digital.packet.entity.PacketRecordInfo;
import com.dr.digital.register.entity.Register;
import com.dr.digital.register.service.RegisterService;
import com.dr.digital.uploadfiles.entity.JpgQueue;
import com.dr.digital.uploadfiles.entity.JpgQueueInfo;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.core.service.FormDataService;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class ArchivesLogServiceImpl implements ArchivesLogService {
    @Autowired
    FlowPathServiceImpl flowPathServiceImpl;

    @Resource
    CommonMapper commonMapper;

    @Autowired
    FormDataService formDataService;

    @Autowired
    RegisterService registerService;

    /**
     * 添加信息结果
     *
     * @param userName
     * @param dangHao
     * @param link
     * @param title
     * @param describe
     * @return
     */
    @Override
    @Transactional
    public void addArchiveLog(String registerId, String userName, String dangHao, String link, String title, String describe, String box) {
        ArchivesLog archivesLog = new ArchivesLog();
        archivesLog.setId(UUID.randomUUID().toString());
        archivesLog.setRegisterId(registerId);
        archivesLog.setAnJuanTiMing(title);
        archivesLog.setDangHao(dangHao);
        archivesLog.setOperatorName(userName);
        archivesLog.setOperatorDate(System.currentTimeMillis());
        archivesLog.setCaoZuoHuanJie(link);
        archivesLog.setLogDescription(describe);
        archivesLog.setBoxNumber(box);
        commonMapper.insert(archivesLog);
    }

    /**
     * 添加信息结果
     *
     * @param person
     * @param archivesLog
     * @return
     */
    @Override
    public void addArchiveLog(Person person, ArchivesLog archivesLog) {
        /*Archives archives = archiveService.findArchiveById(archivesLog.getArchiveId());
        archivesLog.setBoxNumber(archives.getBoxNumber());
        archivesLog.setDangHao(archives.getDangHao());
        archivesLog.setRegisterId(archives.getRegisterId());
        archivesLog.setAnJuanTiMing(archives.getDangAnTiMing());
        archivesLog.setId(UUID.randomUUID().toString());
        archivesLog.setOperatorName(person.getUserName());
        archivesLog.setOperatorDate(System.currentTimeMillis());
        commonMapper.insert(archivesLog);*/
    }

    /**
     * 批量删除和全部删除
     *
     * @param id
     */
    @Override
    public void removeAll(String id, boolean isAll, String registerId) {
        if (!isAll) {
            commonMapper.deleteByQuery(SqlQuery.from(ArchivesLog.class)
                    .in(ArchivesLogInfo.ID, id.split(",")));
        } else {
            commonMapper.deleteByQuery(SqlQuery.from(ArchivesLog.class).equal(ArchivesLogInfo.REGISTERID, registerId));
        }
    }


    @Override
    public void addArchiveFlowLog(String registerId, String userName,
                                  String status, String type,
                                  String judge, String s, String logDescription,
                                  String dangHao, String title, String box, String fid) {
        ArchivesLog archivesLog = new ArchivesLog();
        archivesLog.setId(UUID.randomUUID().toString());//id
        archivesLog.setCaoZuoHuanJie(status);//操作数据
        archivesLog.setMuBiaoHuanJie(type);//目标数据
        archivesLog.setCaoZuoLeiXing(judge);//状态
        archivesLog.setLogDescription(logDescription);//描述
        archivesLog.setOperatorName(userName);//操作人
        archivesLog.setOperatorDate(System.currentTimeMillis());//创建时间
        archivesLog.setRegisterId(registerId);//registerId
        archivesLog.setFormDefinitionId(fid);//fid
        if (!StringUtils.isEmpty(title) && !"".equals(title) && !"null".equals(title)) {
            archivesLog.setAnJuanTiMing(title);//题名
        } else {
            archivesLog.setAnJuanTiMing("暂无");//题名
        }
        archivesLog.setDangHao(dangHao);//档号
        archivesLog.setBoxNumber(box);//馆编案卷号
        commonMapper.insert(archivesLog);
    }

    @Override
    public List<ArchivesLog> selectArchivesLog(SqlQuery<ArchivesLog> query) {
        List<ArchivesLog> archivesLogs = commonMapper.selectByQuery(query);
        return archivesLogs;
    }

    @Override
    public void deleteArchivesLog(SqlQuery<ArchivesLog> sqlQuery) {
        commonMapper.deleteByQuery(sqlQuery);
    }



}

