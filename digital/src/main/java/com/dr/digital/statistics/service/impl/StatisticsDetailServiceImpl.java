package com.dr.digital.statistics.service.impl;

import com.dr.digital.configManager.entity.FlowPathDetail;
import com.dr.digital.configManager.entity.FlowPathDetailInfo;
import com.dr.digital.configManager.service.impl.FlowPathServiceImpl;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.ocr.entity.OcrQueue;
import com.dr.digital.ocr.entity.OcrQueueInfo;
import com.dr.digital.ofd.entity.OfdRecord;
import com.dr.digital.ofd.entity.OfdRecordInfo;
import com.dr.digital.packet.entity.PacketRecord;
import com.dr.digital.packet.entity.PacketRecordInfo;
import com.dr.digital.register.entity.Register;
import com.dr.digital.register.service.RegisterService;
import com.dr.digital.statistics.entity.StatisticsDetail;
import com.dr.digital.statistics.entity.StatisticsDetailInfo;
import com.dr.digital.statistics.service.StatisticsDetailService;
import com.dr.digital.uploadfiles.entity.JpgQueue;
import com.dr.digital.uploadfiles.entity.JpgQueueInfo;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.core.service.FormDataService;
import com.dr.framework.common.service.DefaultBaseService;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Mr.Zhu
 * @date 2022/8/11 - 11:55
 */
@Service
public class StatisticsDetailServiceImpl extends DefaultBaseService<StatisticsDetail> implements StatisticsDetailService {
    @Autowired
    FormDataService formDataService;

    @Autowired
    RegisterService registerService;

    @Autowired
    FlowPathServiceImpl flowPathServiceImpl;

    Person person1 = null;

    @Override
    public void statisticsRegister(String formDefinitionId, String registerId, Person person) {
        //环节顺序： 任务登记 0  tif拆jpg 1 /图像扫描 2  ocr识别 3 图像处理 4 图像质检 5 自动拆件 6
        //手动拆件 7 ofd转换 8 档案著录 9 初检 10 复检 11 数字化成果 12 打包 13
        int taskOder = 0,tiftoJpgOder=1,scanOder=2,ocrOder=3,imageModifyOder=4,imageCheckOder=5,
                autoDisassemblyOrder=6,humanDisassemblyOrder=7,ofdOrder=8,volumesOder=9,qualityOder=10,recheckOder=11,
                overOder=12,packetOrder=13;
        //批次的创建者
        Register register = registerService.selectById(registerId);
        person1 = person;
        //该批档案的总份数
        long totalFen = formDataService.countId(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
        });
        //该批档案的总页数
        long totalPage = 0L;
        StatisticsDetail statisticsDetail = new StatisticsDetail();
        statisticsDetail.setRegisterId(register.getId());
        statisticsDetail.setFormDefinitionId(register.getFormDefinitionId());
        //获得该批次表单拥有的批次流程
        SqlQuery<FlowPathDetail> pathDetailSqlQuery = SqlQuery.from(FlowPathDetail.class).equal(FlowPathDetailInfo.FORMDEFINITIONID, formDefinitionId);
        String flowBatchNames = commonMapper.selectByQuery(pathDetailSqlQuery).get(0).getFlowBatchName();
        String[] batchName = flowBatchNames.split(",");
        //任务登记
        if (containsElement(batchName, "RECEIVE")) {
            long receiveCreateDate = 0L;
            long receiveEndDate = 0L;
            statisticsDetailHumanOperationLink(statisticsDetail,"任务登记", "RECEIVE", formDefinitionId, totalFen, 0L, 0L, receiveCreateDate, receiveEndDate,taskOder);
        }
        //原文拆jpg
        if (containsElement(batchName, "YUANWENTOJPG")) {
            //完成份数
            long yuanToJpgCompleteNum = formDataService.countId(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
                sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '1');
            });
            //总页数
            List<Map> totalList = commonMapper.selectByQuery(SqlQuery.from(JpgQueue.class, false).column(JpgQueueInfo.FILEYS.sum()
                    .alias("totalPage")).equal(JpgQueueInfo.FORMDEFINITIONID, formDefinitionId)
                    .setReturnClass(Map.class));
            if (judgeListNotNull(totalList)) {
                totalPage = new Double((double) totalList.get(0).get("totalPage")).longValue();
            }
            //完成页数
            List<Map> completeList = commonMapper.selectByQuery(SqlQuery.from(JpgQueue.class, false).column(JpgQueueInfo.FILEYS.sum()
                    .alias("completePageNum")).equal(JpgQueueInfo.FORMDEFINITIONID, formDefinitionId)
                    .equal(JpgQueueInfo.STATUS, 2).setReturnClass(Map.class));
            long jpgCompletePageNum = 0L;
            if (judgeListNotNull(completeList)) {
                jpgCompletePageNum = new Double((double) completeList.get(0).get("completePageNum")).longValue();

            }
            //创建时间
            List<JpgQueue> jpgQueues = commonMapper.selectByQuery(SqlQuery.from(JpgQueue.class, false).column(JpgQueueInfo.CREATEDATE, JpgQueueInfo.UPDATEDATE).equal(JpgQueueInfo.FORMDEFINITIONID, formDefinitionId)
                    .equal(JpgQueueInfo.STATUS, 2).orderBy(JpgQueueInfo.CREATEDATE));
            long jpgCreateDate = 0L;
            long jpgEndDate = 0L;
            if (judgeListNotNull(jpgQueues)) {
                jpgCreateDate = jpgQueues.get(0).getCreateDate();
                if (totalFen == yuanToJpgCompleteNum) {
                    jpgEndDate = jpgQueues.get(jpgQueues.size() - 1).getCreateDate();
                    //最后一份的更新时间，现在jpg队列更新时间还没有改，先用创建
                    // long jpgEndDate = jpgQueues.get(jpgQueues.size()-1).getUpdateDate();
                }
            }
            statisticsDetailAutoOperationLink(statisticsDetail,"原文拆jpg",totalFen, yuanToJpgCompleteNum, totalPage, jpgCompletePageNum, jpgCreateDate,
                    jpgEndDate,tiftoJpgOder);

            //图像ocr
            if (containsElement(batchName, "OCR")) {
                //完成份数
                long ocrCompleteFenNum = formDataService.countId(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
                    sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_DISTINGUISH_STATE), '1');
                });
                //完成页数
                long ocrCompletePageNum = commonMapper.countByQuery(SqlQuery.from(OcrQueue.class, false).column(OcrQueueInfo.ID)
                        .equal(OcrQueueInfo.FORMDEFINITIONID, formDefinitionId)
                        .equal(OcrQueueInfo.STATUS, 2));
                //创建时间
                List<OcrQueue> ocrQueues = commonMapper.selectByQuery(SqlQuery.from(OcrQueue.class, false).column(OcrQueueInfo.CREATEDATE, OcrQueueInfo.UPDATEDATE)
                        .equal(OcrQueueInfo.FORMDEFINITIONID, formDefinitionId)
                        .equal(OcrQueueInfo.STATUS, 2).orderBy(OcrQueueInfo.CREATEDATE));
                long ocrCreateDate = 0L;
                long ocrEndDate = 0L;
                if (judgeListNotNull(ocrQueues)) {
                    ocrCreateDate = ocrQueues.get(0).getCreateDate();
                    if (totalFen == ocrCompleteFenNum) {
                        ocrEndDate = ocrQueues.get(ocrQueues.size() - 1).getCreateDate();
                        //最后一份的更新时间，现在ocr队列更新时间还没有改，先用创建
                        //long ocrEndDate = ocrQueues.get(ocrQueues.size()-1).getUpdateDate();
                    }
                }
                statisticsDetailAutoOperationLink(statisticsDetail,"图像ocr", totalFen, ocrCompleteFenNum,
                        totalPage, ocrCompletePageNum, ocrCreateDate, ocrEndDate,ocrOder);
            }
        }
//        //图像扫描  图像扫描的统计逻辑不对，关于卷的先注释掉，以后有卷的情况在考虑
//        if (containsElement(batchName, "SCANNING")) {
//            //纸质走图像扫描的话，总页数从操作记录统计
//            long completePageNum = 0L;
//            //扫描总页数从操作记录查
//            totalPage = completePageNum;
//            long scannerCreteDate = 0L;
//            long scannerEndDate = 0L;
//            statisticsDetailHumanOperationLink(statisticsDetail,"图像扫描", "SCANNING", formDefinitionId, 0L, totalPage, completePageNum, scannerCreteDate, scannerEndDate);
//            //图像处理
//            if (containsElement(batchName, "PROCESSING")) {
//                long proCompletePageNum = 0L;
//                long proCreateDate = 0L;
//                long proEndDate = 0L;
//                statisticsDetailHumanOperationLink(statisticsDetail,"图像处理", "PROCESSING", formDefinitionId, 0L, totalPage, proCompletePageNum, proCreateDate, proEndDate);
//            }
//            //图像质检
//            if (containsElement(batchName, "IMAGES")) {
//                long imageCompletePageNum = 0L;
//                long imageCreateDate = 0L;
//                long imageEndDate = 0L;
//                statisticsDetailHumanOperationLink(statisticsDetail,"图像质检", "SCANNING", formDefinitionId, 0L, totalPage, imageCompletePageNum, imageCreateDate, imageEndDate);
//            }
//            //手动拆件
//            if (containsElement(batchName, "WSSPLIT")) {
//                long chaijianCompletePageNum = 0L;
//                long chaijianCreateDate = 0L;
//                long chaijianEndDate = 0L;
//                statisticsDetailHumanOperationLink(statisticsDetail,"手动拆件", "WSSPLIT", formDefinitionId, 0L, totalPage, chaijianCompletePageNum, chaijianCreateDate, chaijianEndDate);
//            }
//        }
//
            //图像处理
            if (containsElement(batchName, "PROCESSING")) {
                long proCreateDate = 0L;
                long proEndDate = 0L;
                statisticsDetailHumanOperationLink(statisticsDetail,"图像处理", "PROCESSING", formDefinitionId, totalFen, 0L, 0L, proCreateDate, proEndDate,imageModifyOder);
            }
            //图像质检
            if (containsElement(batchName, "IMAGES")) {
                long imageCreateDate = 0L;
                long imageEndDate = 0L;
                statisticsDetailHumanOperationLink(statisticsDetail,"图像质检", "SCANNING", formDefinitionId, totalFen, 0L, 0L, imageCreateDate, imageEndDate,imageCheckOder);
            }
            //手动拆件
            if (containsElement(batchName, "WSSPLIT")) {
                long chaijianCreateDate = 0L;
                long chaijianEndDate = 0L;
                statisticsDetailHumanOperationLink(statisticsDetail,"手动拆件", "WSSPLIT", formDefinitionId,
                        totalFen, 0L, 0L, chaijianCreateDate, chaijianEndDate,humanDisassemblyOrder);
            }

        //自动拆件
        if (containsElement(batchName, "CHAIJIAN")) {
            //完成份数
            long autoChaiNum = formDataService.countId(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
                sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_DISASSEMBLY_STATE), '1');
            });
            //暂时没相关记录表查询
            long autoCreateDate = 0L;
            long autoEndDate = 0L;
            statisticsDetailAutoOperationLink(statisticsDetail,"自动拆件", totalFen,
                    autoChaiNum, 0L, 0L, autoCreateDate, autoEndDate,autoDisassemblyOrder);
        }
        // ofd转换
        if (containsElement(batchName, "OFD")) {
            //完成份数
            long ofdNum = formDataService.countId(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
                sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), '3');
            });
            //创建时间
            List<OfdRecord> ofdRecords = commonMapper.selectByQuery(SqlQuery.from(OfdRecord.class, false).column(OfdRecordInfo.CREATEDATE)
                    .equal(OfdRecordInfo.FORMDEFINITIONID, formDefinitionId)
                    .orderBy(OfdRecordInfo.CREATEDATE));
            long ofdCreateDate = 0L;
            long ofdEndDate = 0L;
            if (judgeListNotNull(ofdRecords)) {
                ofdCreateDate = ofdRecords.get(0).getCreateDate();
                if (totalFen == ofdNum) {
                    ofdRecords.get(ofdRecords.size() - 1).getCreateDate();
                }
            }
            statisticsDetailAutoOperationLink(statisticsDetail,"ofd转换", totalFen, ofdNum, 0L, 0L,
                    ofdCreateDate, ofdEndDate,ofdOrder);
        }
        //档案著录
        if (containsElement(batchName, "VOLUMES")) {
            long volumesCreateDate = 0L;
            long volumesEndDate = 0L;
            statisticsDetailHumanOperationLink(statisticsDetail,"档案著录", "VOLUMES", formDefinitionId, totalFen,
                    0L, 0L, volumesCreateDate, volumesEndDate,volumesOder);
        }
        //初检
        if (containsElement(batchName, "QUALITY")) {
            long qualityCreateDate = 0L;
            long qualityEndDate = 0L;
            statisticsDetailHumanOperationLink(statisticsDetail,"初检", "QUALITY", formDefinitionId, totalFen, 0L, 0L, qualityCreateDate,
                    qualityEndDate,qualityOder);
        }
        //复检
        if (containsElement(batchName, "RECHECK")) {
            long recheckCreateDate = 0L;
            long recheckEndDate = 0L;
            statisticsDetailHumanOperationLink(statisticsDetail,"复检", "RECHECK", formDefinitionId, totalFen, 0L, 0L,
                    recheckCreateDate, recheckEndDate,recheckOder);
        }
        //数字化成果
        if (containsElement(batchName, "OVER")) {
            for (int i = 0; i < batchName.length; i++) {
                if (batchName[i].equals("OVER")) {
                    statisticsDetailHumanOperationLink(statisticsDetail,"数字化成果", batchName[i - 1],
                            formDefinitionId, totalFen, 0L, 0L, 0L, 0L,overOder);
                    break;
                }
            }
        }
        //打包入库
        if (containsElement(batchName, "ZIPPACKET")) {
            //完成份数
            long packetNum = formDataService.countId(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
                sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_PACKET_STATE), '1');
            });
            //创建时间
            List<PacketRecord> packetRecord = commonMapper.selectByQuery(SqlQuery.from(PacketRecord.class, false).column(PacketRecordInfo.CREATEDATE)
                    .equal(PacketRecordInfo.FORMDEFINITIONID, formDefinitionId)
                    .orderBy(PacketRecordInfo.CREATEDATE));
            long packetCreteDate = 0L;
            long packetEndDate = 0L;
            if (judgeListNotNull(packetRecord)) {
                packetCreteDate = packetRecord.get(0).getCreateDate();
                if (totalFen == packetNum) {
                    packetEndDate = packetRecord.get(packetRecord.size() - 1).getCreateDate();
                }
            }
            statisticsDetailAutoOperationLink(statisticsDetail,"打包入库", totalFen, packetNum, 0L,
                    0L, packetCreteDate, packetEndDate,packetOrder);
        }
    }

    /**
     * 判断是否有该环节
     *
     * @param flowBatchName
     * @param BatchName
     * @return
     */
    public static boolean containsElement(String[] flowBatchName, String BatchName) {
        for (int i = 0; i < flowBatchName.length; i++) {
            if (flowBatchName[i].equals(BatchName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 统计自动化环节
     * @param statisticsDetail 当前批次号下的
     * @param operationLink 环节名
     * @param totalFen 总份数
     * @param completeFenNum 完成份数
     * @param totalPage 总页数
     * @param completePageNum 完成页数
     * @param startDate 开始时间
     * @param endDate 结束时间
     */
    public void statisticsDetailAutoOperationLink(StatisticsDetail statisticsDetail,String operationLink, long totalFen,
                                                              long completeFenNum, long totalPage,
                                                              long completePageNum, long startDate, long endDate,int linkOrder){
        statisticsDetail.setOperationLink(operationLink);
        //临时表无需办理人
        statisticsDetail.setHandledBy("/");
        statisticsDetail.setTotalFen(totalFen);
        statisticsDetail.setOrder(linkOrder);
        statisticsSetTotal(statisticsDetail, operationLink, totalFen, totalPage,
                completePageNum, startDate, endDate, completeFenNum,"/");
    }

    /**
     * 人工环节统计
     *
     * @param operationLink    环节名
     * @param batchName        环节常量名
     * @param formDefinitionId 表单id
     * @param totalFen         总份数
     * @param totalPage        总页数
     * @param completePageNum  完成页数
     * @param startDate       创建时间
     * @param endDate          结束时间
     * @return
     */
    public void statisticsDetailHumanOperationLink(StatisticsDetail statisticsDetail,String operationLink, String batchName, String formDefinitionId, long totalFen,
                                            long totalPage, long completePageNum, long startDate, long endDate,int linkOrder) {

        statisticsDetail.setOperationLink(operationLink);
        statisticsDetail.setTotalFen(totalFen);
        statisticsDetail.setOrder(linkOrder);
        long completeFenNum;
        //所有完成数量总和
        ResultEntity resultEntity = flowPathServiceImpl.flowPeopleData(formDefinitionId, batchName, person1, 0L, 0L);
        List<Map<String, String>> data = (List<Map<String, String>>) resultEntity.getData();
        //有权限的看所有人
            for (int i = 0; i < data.size(); i++) {
                completeFenNum = Long.parseLong(data.get(i).get("address"));
                String name = data.get(i).get("name");
                statisticsDetail.setHandledBy(name);
                statisticsSetTotal(statisticsDetail, operationLink, totalFen, totalPage,
                        completePageNum, startDate, endDate, completeFenNum,name);
            }
    }

    private void statisticsSetTotal(StatisticsDetail statisticsDetail, String operationLink, long totalFen, long totalPage, long completePageNum,
                                    long startDate, long endDate, long completeFenNum,String HandledBy) {
        statisticsDetail.setCompleteFenNum(completeFenNum);

        List<Map> totalList = commonMapper.selectByQuery((SqlQuery.from(StatisticsDetail.class, false).column(StatisticsDetailInfo.COMPLETEFENNUM.sum()
                .alias("totalCompleteFenNum"))
                .equal(StatisticsDetailInfo.REGISTERID, statisticsDetail.getRegisterId())
                .equal(StatisticsDetailInfo.OPERATIONLINK, operationLink).setReturnClass(Map.class)));
        //统计同一环节不同人完成的总数，算出总共的剩余数量
        long totalCompleteFenNum = 0;
        if (judgeListNotNull(totalList)){
            totalCompleteFenNum =Long.parseLong(String.valueOf(totalList.get(0).get("totalCompleteFenNum")));
        }else {
            totalCompleteFenNum = completeFenNum;
        }
        statisticsDetail.setSurPlusFenNum(totalFen-totalCompleteFenNum);
        statisticsDetail.setTotalPage(totalPage);
        statisticsDetail.setCompletePageNum(completePageNum);
        statisticsDetail.setSurPlusPageNum(totalPage-completePageNum);
        statisticsDetail.setStartDate(startDate);
        statisticsDetail.setEndDate(endDate);
        if (totalFen-totalCompleteFenNum==0){
            statisticsDetail.setStatus("完成");
        }else {
            statisticsDetail.setStatus("未完成");
        }

        List<StatisticsDetail> detailList = this.selectList(SqlQuery.from(StatisticsDetail.class, false)
                .column(StatisticsDetailInfo.ID)
                .equal(StatisticsDetailInfo.REGISTERID, statisticsDetail.getRegisterId())
                .equal(StatisticsDetailInfo.OPERATIONLINK, operationLink)
                .equal(StatisticsDetailInfo.HANDLEDBY, HandledBy));
        if (detailList.size()==0){
            statisticsDetail.setId(UUID.randomUUID().toString());
            this.insert(statisticsDetail);
        }else {
            statisticsDetail.setId(detailList.get(0).getId());
            this.updateById(statisticsDetail);
        }
    }


    public boolean judgeListNotNull(List list) {
        if (list.size() == 0 || list == null || list.get(0) == null) return false;
        return true;
    }
}
