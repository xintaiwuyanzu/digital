package com.dr.digital.packet.service;

import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.packet.entity.ArchiveCallback;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.core.organise.entity.Person;

import java.util.List;

/**
 * @Author: caor
 * @Date: 2021-11-23 15:46
 * @Description:
 */
public interface PacketsDataService {
    String BASIC_INFORMATION_METADATA = "基本信息元数据.xml";

    /**
     * 单条目录信息打包成zip
     *
     * @param formDefinitionId
     * @param formDataId       打包路径，若为空则默认创建路径
     */
    void packet(String formDefinitionId, String formDataId);

    void packetAll(BaseQuery query, String queryContent, String command, String formDataId, String formDefinitionId, String registerId);

    void xmlGenerateFile(FormData formData);
    /**
     * 数据在线移交
     *
     * @param formDataList
     * @param registerId
     */
    ResultEntity onlineHandover(List<FormData> formDataList, String registerId, String formDefinitionId, Person person);

    /**
     * 移交回调函数
     *
     * @param archiveCallback
     */
    ResultEntity archivingResult(ArchiveCallback archiveCallback);

}
