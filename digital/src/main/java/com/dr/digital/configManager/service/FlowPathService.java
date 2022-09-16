package com.dr.digital.configManager.service;

import com.dr.digital.configManager.entity.FlowPath;
import com.dr.digital.configManager.entity.FlowPathDetail;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.core.organise.entity.Person;

import java.util.List;

public interface FlowPathService {

    List<FlowPath> getFlowPath();

    List<FlowPathDetail> getFlowPathDetail(String batch);

    String selectLinkData(List<FormData> formDataAllList);

    ResultEntity flowPath(String fid, String type,String state);

    ResultEntity flowPathAll(String fid);

//    /**
//     * 判断是否是数字化成果
//     * @param registerId
//     * @return
//     */
//    boolean overJudge(String registerId);
    /**
     * 修改批次信息
     * @param fid
     * @param type
     * @return
     */
    ResultEntity flowUpdate(String fid, String type);


    /**
     * 统计所有的批次流程
     * fid=批次id
     *
     * @return
     */
    ResultEntity flowCensus(String fid);

    /**
     * 识别操作状态
     * @param type
     * @param endType
     * @param fid
     * @return
     */
    String judge(String type,String endType,String fid);

    /**
     * 批次编号转换成中文    现在很多都需要fid去查询，因为没有一个写成最终的数据。
     * 完全可以写一个写死的数据，然后进行秀嘎。可以减少复杂性
     * @param type
     * @return
     */
    String convert(String type,String fid);

    /**
     * 查询完成人员的完成信息
     * @param fid
     * @param info //当前的流程位置
     *
     * @return
     */
    ResultEntity flowPeopleData(String fid, String info, Person person, Long startTime, Long endTime);
}
