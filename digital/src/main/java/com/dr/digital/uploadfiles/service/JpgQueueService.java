package com.dr.digital.uploadfiles.service;

import com.dr.digital.uploadfiles.entity.JpgQueue;
import com.dr.digital.uploadfiles.entity.MatchText;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.core.organise.entity.Person;

import java.util.List;
import java.util.Map;

public interface JpgQueueService {
    //ocr线程池数量
    int THREAD_POOL_SIZE = 3;

    /**
     * 添加jpg 队列
     *
     * @param formDefinitionId
     * @param registerId
     * @param person
     */
    void addJpgQueue(String formDefinitionId, String registerId, String batchName, String batchNo, Person person);


    void tiffToJpgByPath(String formDefinitionId, String registerId, String type, Person person);

    /**
     * 执行jpg队列
     *
     * @return
     */
    void implementIJpg();

    List<JpgQueue> getArchiveCodeByJpg(String archiveCode);

    ResultEntity getMatchText(MatchText matchText, Person person);

//    List getFormDataByJagQueue(String formDefinitionId,List list);

//    Map<String, Integer> getRegisterIdByCount(String registerId, String flowPath);

    ResultEntity selectFunction();

    void test();
}
