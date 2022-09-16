package com.dr.digital.ocr.service;

import com.dr.digital.ocr.entity.OcrQueue;
import com.dr.digital.uploadfiles.entity.JpgQueue;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.core.organise.entity.Person;

import java.io.File;

public interface OcrQueueService {
    //ocr线程池数量
    int THREAD_POOL_SIZE = 6;

    /**
     * 添加ocr 队列
     *
     * @param formDefinitionId
     * @param registerId
     * @param person
     */
    void ocrQueueLb(String formDefinitionId, String registerId, String batchName, String batchNo, Person person);

    /**
     * 从 jpg队列到ocr队列
     *
     * @param imgFile
     * @param jpgQueue
     * @return
     */
    OcrQueue addOcrQueue(File imgFile, JpgQueue jpgQueue);



    void addOcrQueueLb(String formDefinitionId, FormData formData, String registerId, String batchName, String batchNo, Person person);

    /**
     * 执行ocr队列
     *
     * @return
     */
    void implementOcr();

    /**
     * 暂停ocr服务
     *
     * @return
     */
    void updateStatus();

    /**
     * 选择启动
     *
     * @param ocrQueueId
     * @param person
     */
    void selectOcrStart(String ocrQueueId, Person person);

    /**
     * 查询转换服务
     *
     * @param entity
     * @param person
     * @return
     */
    void searchOcrStart(OcrQueue entity, Person person);

    /**
     * 暂停重启服务
     *
     * @return
     */
    void allOcrStart();

    void updatePriorityData(String fid, String type, String fromid,String uuid);

    ResultEntity selectFunction();
}
