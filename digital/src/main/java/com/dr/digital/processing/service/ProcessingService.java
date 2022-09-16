package com.dr.digital.processing.service;

import com.dr.digital.processing.vo.ImgVo;

import java.util.List;

public interface ProcessingService {
    /**
     * 查询案卷下所有的图片信息
     *
     * @param id
     * @param type
     * @return
     */
    List<ImgVo> findImgList(String formDefinitionId, String id, String type);

    /**
     * 查询卷内目录下所有的图片信息
     *
     * @param id
     * @return
     */
    List<ImgVo> findImgPageByVId(String formDefinitionId, String id, String type);

    /**
     * 展示卷内图片
     *
     * @param ajFormId
     * @param ajId
     * @param formId
     * @param Id
     * @param type
     * @return
     */
    Object findFileVolumesImg(String ajFormId, String ajId, String formId, String Id, String type);

    ImgVo findTxtByArchiveId(String path, String archiveCode, String fileName);

}
