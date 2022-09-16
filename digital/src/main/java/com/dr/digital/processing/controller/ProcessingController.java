package com.dr.digital.processing.controller;

import com.dr.digital.processing.service.ProcessingService;
import com.dr.digital.processing.vo.ImgVo;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.page.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询原文数据
 *
 * @author lc
 */
@RestController
@RequestMapping("api/processing")
public class  ProcessingController {
    @Autowired
    ProcessingService processingService;

    /**
     * 获取案卷下所有的图片信息
     *
     * @param id
     * @param type
     * @param pageIndex
     * @param pageSize
     * @param page
     * @return
     */
    @RequestMapping("/findImgPage")
    public ResultEntity findImgPage(String formDefinitionId, String id, String type, @RequestParam(defaultValue = "0") int pageIndex,
                                    @RequestParam(defaultValue = "20") int pageSize,
                                    @RequestParam(defaultValue = "true") boolean page) {
        List<ImgVo> imgList = processingService.findImgList(formDefinitionId, id, type);
        List<ImgVo> list = new ArrayList<>();
        if (page) {
            Page<ImgVo> imgVoPage = new Page<>(pageIndex * pageSize, pageSize, imgList.size());
            int start = pageIndex + 1;
            int end = start * pageSize;
            if (start * pageSize > imgList.size()) {
                end = imgList.size();
            }
            for (int i = pageIndex * pageSize; i < end; i++) {
                list.add(imgList.get(i));
            }
            imgVoPage.setData(list);
            return ResultEntity.success(imgVoPage);
        } else {
            return ResultEntity.success(imgList);
        }
    }

    /**
     * 查询卷内图片信息
     *
     * @param id
     * @return
     */
    @RequestMapping("/findImgPageByVId")
    public ResultEntity findImgPageByVId(String formDefinitionId, String id, String type, @RequestParam(defaultValue = "0") int pageIndex,
                                         @RequestParam(defaultValue = "20") int pageSize,
                                         @RequestParam(defaultValue = "true") boolean page) {
        List<ImgVo> imgList = processingService.findImgPageByVId(formDefinitionId, id, type);
        List<ImgVo> list = new ArrayList<>();
        if (page) {
            Page<ImgVo> imgVoPage = new Page<>(pageIndex * pageSize, pageSize, imgList.size());
            int start = pageIndex + 1;
            int end = start * pageSize;
            if (start * pageSize > imgList.size()) {
                end = imgList.size();
            }
            for (int i = pageIndex * pageSize; i < end; i++) {
                list.add(imgList.get(i));
            }
            imgVoPage.setData(list);
            return ResultEntity.success(imgVoPage);
        } else {
            return ResultEntity.success(imgList);
        }
    }

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
    @RequestMapping(value = "/findFileVolumesImg")
    public ResultEntity findFileVolumesImg(String ajFormId, String ajId, String formId, String Id, String type) {
        return ResultEntity.success(processingService.findFileVolumesImg(ajFormId, ajId, formId, Id, type));
    }

    /**
     * 查询txt文件
     *
     * @param path
     * @return
     */
    @RequestMapping(value = "/findTxtByArchiveId")
    public ResultEntity findTxtByArchiveId(String path, String archiveCode, String fileName) {
        ImgVo imgVo = processingService.findTxtByArchiveId(path, archiveCode,fileName);
        return ResultEntity.success(imgVo);
    }

}