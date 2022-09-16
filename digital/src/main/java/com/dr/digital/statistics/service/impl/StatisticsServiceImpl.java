package com.dr.digital.statistics.service.impl;

import com.dr.digital.configManager.entity.FlowPathDetail;
import com.dr.digital.configManager.entity.FlowPathDetailInfo;
import com.dr.digital.configManager.service.impl.FlowPathServiceImpl;
import com.dr.digital.manage.log.entity.ArchivesLog;
import com.dr.digital.manage.log.entity.ArchivesLogInfo;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.ocr.entity.OcrQueue;
import com.dr.digital.ocr.entity.OcrQueueInfo;
import com.dr.digital.ofd.entity.OfdRecord;
import com.dr.digital.ofd.entity.OfdRecordInfo;
import com.dr.digital.packet.entity.PacketRecord;
import com.dr.digital.packet.entity.PacketRecordInfo;
import com.dr.digital.register.entity.Register;
import com.dr.digital.register.entity.RegisterInfo;
import com.dr.digital.register.service.RegisterService;
import com.dr.digital.statistics.entity.Statistics;
import com.dr.digital.statistics.entity.StatisticsInfo;
import com.dr.digital.statistics.service.StatisticsService;
import com.dr.digital.uploadfiles.entity.JpgQueue;
import com.dr.digital.uploadfiles.entity.JpgQueueInfo;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.core.service.FormDataService;
import com.dr.framework.common.service.DefaultBaseService;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Mr.Zhu
 * @date 2022/8/11 - 9:29
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class StatisticsServiceImpl extends DefaultBaseService<Statistics> implements StatisticsService {
    @Resource
    CommonMapper commonMapper;

    @Autowired
    FormDataService formDataService;

    @Autowired
    RegisterService registerService;


    /**
     * 统计出所有批次的完成情况
     * @return
     */
    @Override
    public void statisticsAllRegister() {
        //查出所有批次
        List<Register> registers = commonMapper.selectByQuery(SqlQuery.from(Register.class));
        for (Register register : registers) {
            Statistics statistics = selectOne(SqlQuery.from(Statistics.class).equal(StatisticsInfo.REGISTERID, register.getId()));
            if (statistics==null){
                statistics = new Statistics();
            }
            statistics.setBatch_no(register.getBatch_no());
            statistics.setBatch_name(register.getBatch_name());

            long totalFen = formDataService.countId(register.getFormDefinitionId(), (sqlQuery, formRelationWrapper) -> {
            });
            //总份数
            statistics.setTotalFen(totalFen);
            //打包总数，即为完成总数
            long packetNum = formDataService.countId(register.getFormDefinitionId(), (sqlQuery, formRelationWrapper) -> {
                sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_PACKET_STATE), '1');
            });
            List<PacketRecord> packetRecord = commonMapper.selectByQuery(SqlQuery.from(PacketRecord.class, false).column(PacketRecordInfo.CREATEDATE)
                    .equal(PacketRecordInfo.FORMDEFINITIONID, register.getFormDefinitionId())
                    .orderByDesc(PacketRecordInfo.CREATEDATE));
            long completeDate = 0L;
            if (judgeListNotNull(packetRecord)){
                completeDate = packetRecord.get(0).getCreateDate();
            }
            //最后一份的完成时间
            statistics.setCompleteDate(completeDate);
            //完成份数
            statistics.setCompleteFenNum(packetNum);
            if (totalFen==packetNum){
                //1表示该批次已经完成
                statistics.setStatus("已完成");
            }else {
                //0表示该批次已经未完成
                statistics.setStatus("未完成");
            }

            List<Map> totalList = commonMapper.selectByQuery(SqlQuery.from(JpgQueue.class, false).column(JpgQueueInfo.FILEYS.sum()
                    .alias("totalPage")).equal(JpgQueueInfo.FORMDEFINITIONID, register.getFormDefinitionId())
                    .setReturnClass(Map.class));
            long totalPage = 0L;
            if (judgeListNotNull(totalList)){
                totalPage = new Double((double) totalList.get(0).get("totalPage")).longValue();
            }
            //总页数
            statistics.setTotalPage(totalPage);

            List<Map> completeList = commonMapper.selectByQuery(SqlQuery.from(JpgQueue.class, false).column(JpgQueueInfo.FILEYS.sum()
                    .alias("completePageNum")).equal(JpgQueueInfo.FORMDEFINITIONID, register.getFormDefinitionId())
                    .equal(JpgQueueInfo.STATUS, 2).setReturnClass(Map.class));
            long completePageNum = 0L;
            if (judgeListNotNull(completeList)){
                completePageNum = new Double((double) completeList.get(0).get("completePageNum")).longValue();
            }
            //完成页数
            statistics.setCompletePageNum(completePageNum);

            SqlQuery<Map> sqlQuery = SqlQuery.from(ArchivesLog.class, false).column(ArchivesLogInfo.OPERATORNAME)
                    .equal(ArchivesLogInfo.FORMDEFINITIONID, register.getFormDefinitionId()).groupBy(ArchivesLogInfo.OPERATORNAME).setReturnClass(Map.class);
            List<Map> list = commonMapper.selectByQuery(sqlQuery);
            //参与人数
            statistics.setTotalPeople(list.size());
            if (StringUtils.isEmpty(statistics.getId())){
                statistics.setRegisterId(register.getId());
                statistics.setFormDefinitionId(register.getFormDefinitionId());
                statistics.setBatch_createDate(register.getCreateDate());
                statistics.setCreateDate(System.currentTimeMillis());
                statistics.setUpdateDate(System.currentTimeMillis());
                this.insert(statistics);
            }else {
                statistics.setUpdateDate(System.currentTimeMillis());
                this.updateById(statistics);
            }
        }
    }
    public boolean judgeListNotNull(List list){
        if (list.size()==0||list==null||list.get(0)==null) return false;
        return true;
    }
}
