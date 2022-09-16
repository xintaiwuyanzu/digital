package com.dr.digital.uploadfiles.controller;


import com.dr.digital.manage.form.service.ArchiveDataManager;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.model.query.ArchiveDataQuery;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.processing.vo.ImgVo;
import com.dr.digital.uploadfiles.entity.UploadFiles;
import com.dr.digital.uploadfiles.entity.UploadFilesInfo;
import com.dr.digital.uploadfiles.service.UploadFilesService;
import com.dr.framework.common.controller.BaseController;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.form.core.service.FormDataService;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.dr.framework.core.web.annotations.Current;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/uploadfiles")
public class UploadFilesController extends BaseController<UploadFiles> {
    @Autowired
    UploadFilesService uploadFilesService;
    @Autowired
    FormDataService formDataService;
    @Autowired
    ArchiveDataManager dataManager;

    /**
     * 查询案卷的扫描图片信息
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/findImgByIdAndType")
    public ResultEntity findImgByIdAndType(String formDefinitionId, String id, String type) {
        if (StringUtils.isEmpty(formDefinitionId) && StringUtils.isEmpty(id)) {
            return ResultEntity.error("档案信息不能为空！");
        }
        FormData formData = formDataService.selectOneFormData(formDefinitionId, id);
        if (StringUtils.isEmpty(formData)) {
            return ResultEntity.error("未查到当前档案数据！");
        }
        SqlQuery<UploadFiles> sqlQuery = SqlQuery.from(UploadFiles.class)
                .equal(UploadFilesInfo.BUSSINESSID, formData.getId())
                .equal(UploadFilesInfo.FILESTATUS, "1")
                .orderBy(UploadFilesInfo.ORDERBY);
        List<UploadFiles> uploadFilesList = commonService.selectList(sqlQuery);
        if (uploadFilesList.size() > 0) {
            return ResultEntity.success(uploadFilesList);
        } else {
            return ResultEntity.success(new ArrayList<UploadFiles>());
        }
    }

    /**
     * 根据业务id查询原文数量
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/findImgCountById")
    public ResultEntity findImgCountById(String id) {
        return ResultEntity.success(uploadFilesService.findImgCountById(id));
    }
    /**
     * 根据业务id查询原文数据
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/findImgById")
    public ResultEntity findImgById(String id) {
        List<UploadFiles> uploadFilesByBussinessId = uploadFilesService.findUploadFilesByBussinessId(id);
        return ResultEntity.success(uploadFilesByBussinessId);
    }

    /**
     * 扫描案卷类图像
     *
     * @param multipartfile
     * @param type
     * @param path
     * @param name
     * @param order
     * @param response
     * @return
     */
    @RequestMapping("/addFaceImg")
    public ResultEntity addFaceImg(@RequestParam("file") MultipartFile multipartfile, String type, String path, String name, int order, HttpServletResponse response) {
        response.setCharacterEncoding("utf-8");
        if (multipartfile != null && path != null) {
            String[] archives = path.split(",", -1);
            String archivesId = archives[1].replace(File.separator, "").replace("\\", "");
            FormData formData = formDataService.selectOneFormData(archives[0], archivesId);
            Integer filesOrder = uploadFilesService.findUploadFilesOrderMaxByBussinessId(archivesId);
            String newFilePath = String.join(File.separator, formData.get(ArchiveEntity.COLUMN_FOND_CODE), formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE));
            uploadFilesService.uploadFiles(multipartfile, formData, filesOrder, newFilePath);
        }
        return ResultEntity.success("扫描成功");
    }

    /**
     * 扫描卷内文件，增加件号文件夹级别
     *
     * @param multipartfile
     * @param type
     * @param path
     * @param name
     * @param order
     * @param response
     * @return
     */
    @RequestMapping("/addJnFaceImg")
    public ResultEntity addJnFaceImg(@RequestParam("file") MultipartFile multipartfile, String type, String path, String name, int order, HttpServletResponse response) {
        response.setCharacterEncoding("utf-8");
        if (multipartfile != null && path != null) {
            String[] archives = path.split(",", -1);
            String archivesId = archives[1].replace(File.separator, "").replace("\\", "");
            FormData formData = formDataService.selectOneFormData(archives[0], archivesId);
            Integer filesOrder = uploadFilesService.findUploadFilesOrderMaxByBussinessId(archivesId);
            String newFilePath = formData.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + formData.get(ArchiveEntity.COLUMN_AJDH) + File.separator + formData.get(ArchiveEntity.COLUMN_JH) + File.separator;
            uploadFilesService.uploadFiles(multipartfile, formData, filesOrder, newFilePath);
        }
        return ResultEntity.success("扫描成功");
    }

    /**
     * 插扫案卷类
     *
     * @param multipartfile
     * @param type
     * @param path
     * @param name
     * @param order
     * @param id
     * @param response
     * @return
     */
    @RequestMapping("/forPlug")
    public ResultEntity forPlug(@RequestParam("file") MultipartFile multipartfile, String type, String path, String name, int order, String id, HttpServletResponse response) {
        if (multipartfile != null && path != null) {
            String[] archives = path.split(",", -1);
            String archivesId = archives[1].replace(File.separator, "").replace("\\", "");
            FormData formData = formDataService.selectOneFormData(archives[0], archivesId);
            String newFilePath = formData.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + File.separator;
            uploadFilesService.creatPlug(multipartfile, formData, newFilePath, id);
        }
        return ResultEntity.success("插扫成功");
    }

    /**
     * 插扫文件类
     *
     * @param multipartfile
     * @param type
     * @param path
     * @param name
     * @param order
     * @param id
     * @param response
     * @return
     */
    @RequestMapping("/forJnPlug")
    public ResultEntity forJnPlug(@RequestParam("file") MultipartFile multipartfile, String type, String path, String name, int order, String id, HttpServletResponse response) {
        if (multipartfile != null && path != null) {
            String[] archives = path.split(",", -1);
            String archivesId = archives[1].replace(File.separator, "").replace("\\", "");
            FormData formData = formDataService.selectOneFormData(archives[0], archivesId);
            String newFilePath = formData.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + formData.get(ArchiveEntity.COLUMN_AJDH) + File.separator + formData.get(ArchiveEntity.COLUMN_JH) + File.separator;
            uploadFilesService.creatPlug(multipartfile, formData, newFilePath, id);
        }
        return ResultEntity.success("插扫成功");
    }

    /**
     * 补扫案卷类
     *
     * @param multipartfile
     * @param type
     * @param path
     * @param name
     * @param order
     * @param id
     * @param response
     * @return
     */
    @RequestMapping("/forSweeping")
    public ResultEntity forSweeping(@RequestParam("file") MultipartFile multipartfile, String type, String path, String name, int order, String id, HttpServletResponse response) {
        if (multipartfile != null && path != null) {
            String[] archives = path.split(",", -1);
            String archivesId = archives[1].replace(File.separator, "").replace("\\", "");
            FormData formData = formDataService.selectOneFormData(archives[0], archivesId);
            String newFilePath = formData.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + File.separator;
            uploadFilesService.creatSweeping(multipartfile, formData, newFilePath, id);
        }
        return ResultEntity.success("替扫成功");
    }

    /**
     * 补扫 卷内
     *
     * @param multipartfile
     * @param type
     * @param path
     * @param name
     * @param order
     * @param id
     * @param response
     * @return
     */
    @RequestMapping("/forJnSweeping")
    public ResultEntity forJnSweeping(@RequestParam("file") MultipartFile multipartfile, String type, String path, String name, int order, String id, HttpServletResponse response) {
        if (multipartfile != null && path != null) {
            String[] archives = path.split(",", -1);
            String archivesId = archives[1].replace(File.separator, "").replace("\\", "");
            FormData formData = formDataService.selectOneFormData(archives[0], archivesId);
            String newFilePath = formData.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + formData.get(ArchiveEntity.COLUMN_AJDH) + File.separator + formData.get(ArchiveEntity.COLUMN_JH) + File.separator;
            uploadFilesService.creatSweeping(multipartfile, formData, newFilePath, id);
        }
        return ResultEntity.success("替扫成功");
    }

    /**
     * 替扫 案卷类
     *
     * @param multipartfile
     * @param type
     * @param path
     * @param name
     * @param order
     * @param id
     * @param response
     * @return
     */
    @RequestMapping("/forSaul")
    public ResultEntity forSaul(@RequestParam("file") MultipartFile multipartfile, String type, String path, String name, int order, String id, HttpServletResponse response) {
        if (multipartfile != null && path != null) {
            String[] archives = path.split(",", -1);
            String archivesId = archives[1].replace(File.separator, "").replace("\\", "");
            FormData formData = formDataService.selectOneFormData(archives[0], archivesId);
            String newFilePath = formData.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + File.separator;
            UploadFiles uploadFile = uploadFilesService.findUploadFilesOneById(id);
            uploadFilesService.deleteUploadFilesById(id);
            new File(uploadFile.getAbsolutePath()).delete();
            new File(uploadFile.getThumbnailAbsolutePath()).delete();
            uploadFilesService.creatSaul(multipartfile, formData, newFilePath, id, uploadFile);
        }
        return ResultEntity.success("替扫成功");
    }

    /**
     * 替扫 文件类
     *
     * @param multipartfile
     * @param type
     * @param path
     * @param name
     * @param order
     * @param id
     * @param response
     * @return
     */
    @RequestMapping("/forJnSaul")
    public ResultEntity forJnSaul(@RequestParam("file") MultipartFile multipartfile, String type, String path, String name, int order, String id, HttpServletResponse response) {
        if (multipartfile != null && path != null) {
            String[] archives = path.split(",", -1);
            String archivesId = archives[1].replace(File.separator, "").replace("\\", "");
            FormData formData = formDataService.selectOneFormData(archives[0], archivesId);
            String newFilePath = formData.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + formData.get(ArchiveEntity.COLUMN_AJDH) + File.separator + formData.get(ArchiveEntity.COLUMN_JH) + File.separator;
            UploadFiles uploadFile = uploadFilesService.findUploadFilesOneById(id);
            uploadFilesService.deleteUploadFilesById(id);
            new File(uploadFile.getAbsolutePath()).delete();
            new File(uploadFile.getThumbnailAbsolutePath()).delete();
            uploadFilesService.creatSaul(multipartfile, formData, newFilePath, id, uploadFile);
        }
        return ResultEntity.success("替扫成功");
    }

    /**
     * 删除全部
     *
     * @param id
     */
    @RequestMapping("/deleteAllByBussinessId")
    public ResultEntity deleteAllByBussinessId(String id) throws Exception {
        List<UploadFiles> uploadFilesList = uploadFilesService.findUploadFilesByBussinessId(id);
        for (UploadFiles uploadFiles : uploadFilesList) {
            String filePath = uploadFiles.getThumbnailPath().substring(uploadFiles.getThumbnailPath().indexOf(File.separator, uploadFiles.getThumbnailPath().indexOf("\\")));
            String flag = filePath.substring(1, filePath.indexOf(File.separator, filePath.indexOf(File.separator) + 1));
            uploadFilesService.deleteAllByBussinessId(uploadFiles, filePath, flag);
        }
        return ResultEntity.success();
    }

    /**
     * 删除单个文件
     *
     * @param id
     * @return
     */
    @RequestMapping("/deleteByFile")
    public ResultEntity<Boolean> delete(String id) {
        Assert.isTrue(!StringUtils.isEmpty(id), "附件Id不能为空");
        UploadFiles uploadFiles = uploadFilesService.findUploadFilesOneById(id);
        if (!StringUtils.isEmpty(uploadFiles)) {
            UploadFiles uploadFilesByNextId = uploadFilesService.findUploadFilesByParentId(uploadFiles.getNextId());
            if (uploadFilesByNextId != null) {
                updateSrcName(uploadFilesByNextId.getNextId());
                uploadFilesByNextId.setParentId(uploadFiles.getParentId());
                uploadFilesByNextId.setSrcName(uploadFiles.getSrcName());
                uploadFilesByNextId.setOrder(uploadFiles.getOrder());
                commonService.update(uploadFilesByNextId);
                uploadFilesService.deleteUploadFilesById(id);
                new File(uploadFiles.getAbsolutePath()).delete();
                new File(uploadFiles.getThumbnailAbsolutePath()).delete();
                return ResultEntity.success(true);
            } else {
                uploadFilesService.deleteUploadFilesById(id);
                new File(uploadFiles.getAbsolutePath()).delete();
                new File(uploadFiles.getThumbnailAbsolutePath()).delete();
                return ResultEntity.success(true);
            }
        } else {
            return ResultEntity.error("删除失败");
        }
    }

    private void updateSrcName(String nextId) {
        UploadFiles uploadFiles = uploadFilesService.findUploadFilesByParentId(nextId);
        if (!StringUtils.isEmpty(uploadFiles)) {
            uploadFiles.setSrcName(String.format("%03d", uploadFiles.getOrder() - 1) + ".JPEG");
            uploadFiles.setOrder(uploadFiles.getOrder() - 1);
            commonService.update(uploadFiles);
            updateSrcName(uploadFiles.getNextId());
        }
    }

    /**
     * 档案拆分
     *
     * @param ids
     * @param formDefinitionId
     * @param childFormId
     * @param registerId
     * @param person
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/fileSplitWrit")
    public ResultEntity fileSplitWrit(String ids, String formDefinitionId, String childFormId, String registerId, @Current Person person) throws Exception {
        if (StringUtils.isEmpty(ids)) {
            return ResultEntity.error("参数不能为空！");
        }
        String[] idArray1 = ids.split(",");
        for (String id : idArray1) {
            if (!StringUtils.isEmpty(id)) {
                FormData formData = formDataService.selectOneFormData(formDefinitionId, id);
                if (StringUtils.isEmpty(formData)) {
                    return ResultEntity.error("未查到当前档案数据！");
                }
                uploadFilesService.fileSplitWrit(formData, registerId, person);
            }
        }
        return ResultEntity.success();
    }

    /**
     * 文件改名
     *
     * @param imgVo
     * @param person
     * @return
     */
    @RequestMapping(value = "/updateFileVolumesImg")
    public ResultEntity updateFileVolumesImg(ImgVo imgVo, @Current Person person) {
        uploadFilesService.updateFileVolumesImg(imgVo, person);
        return ResultEntity.success();
    }

    /**
     * 更新图像编辑单张图片
     *
     * @param person
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/updateFileImg")
    public ResultEntity updateFileImg(ImgVo imgVo, String contents, @Current Person person) {
        uploadFilesService.updateFileImg(imgVo, contents, person);
        return ResultEntity.success();
    }

    @RequestMapping(value = "/tiffToJpgByPath")
    public ResultEntity tiffToJpgByPath(@Current Person person) {
        uploadFilesService.tiffToJpgByPath(person);
        return ResultEntity.success("");
    }

    /**
     * 图片合成pdf
     *
     * @param request
     * @param query
     * @param type
     * @param childFormId
     * @param formDefinitionId
     * @param registerId
     * @param queryContent
     * @param person
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/photoMergeToPDF")
    public ResultEntity photoMergeToPDF(HttpServletRequest request,
                                        BaseQuery query,
                                        String type,
                                        String childFormId,
                                        String formDefinitionId,
                                        String registerId,
                                        @RequestParam(name = ArchiveDataQuery.QUERY_KEY, required = false) String queryContent,
                                        @Current Person person) throws Exception {
        query.parseQuery(queryContent);
        long success = 0;
        long error = 0;
        List<FormData> dataList = dataManager.findDataByQuery(query);
        for (FormData formDataOne : dataList) {
            FormData formData = formDataService.selectOneFormData(formDefinitionId, formDataOne.getId());
            if (StringUtils.isEmpty(formData)) {
                return ResultEntity.error("未查到当前档案数据！");
            }
            //档案原文拆分成文件夹
            uploadFilesService.fileSplitWrit(formData, registerId, person);
            //拆分后进行合并转换
            final boolean b = uploadFilesService.photoMergeToPDF(formData, childFormId, registerId, person);
            if (b) {
                success++;
            } else {
                error++;
            }
        }
        return ResultEntity.success(success + "条数据合并成功," + error + "条数据合并失败");
    }

    /**
     * 双层ofd转换 （目录著录环节）
     *
     * @param request
     * @param query
     * @param type
     * @param formDefinitionId
     * @param registerId
     * @param queryContent
     * @param person
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/fileSplitAndMergeAll")
    public ResultEntity fileSplitAndMergeAll(HttpServletRequest request,
                                             BaseQuery query,
                                             String type,
                                             String formDefinitionId,
                                             String registerId,
                                             @RequestParam(name = ArchiveDataQuery.QUERY_KEY, required = false) String queryContent,
                                             @Current Person person) throws Exception {
        query.parseQuery(queryContent);
        query.getQueryItems().add(new ArchiveDataQuery.QueryItem("people_code", person.getId() + "," + "默认", ArchiveDataQuery.QueryType.IN));
        List<FormData> dataList = dataManager.findDataByQuery(query);
        uploadFilesService.mergeConversion(dataList, formDefinitionId, registerId, person);
        return ResultEntity.success();
    }

    /**
     * 在批次进行批次拆分jpg
     *
     * @param person
     * @return
     */
    @RequestMapping(value = "/batchTiffToJpgByPath")
    public ResultEntity batchTiffToJpgByPath(String formDefinitionId, @Current Person person) {
        uploadFilesService.tiffToJpgByPath(formDefinitionId, person);
        return ResultEntity.success();
    }

    /**
     * 在批次上进行ocr识别
     *
     * @param person
     * @return
     */
    @RequestMapping(value = "/batchJpgToTxt")
    public ResultEntity batchJpgToTxt(String formDefinitionId, String registerId, @Current Person person) {
        uploadFilesService.batchJpgToTxt(formDefinitionId, registerId, person);
        return ResultEntity.success();
    }

    /**
     * 在批次上进行OFD转换
     *
     * @param formDefinitionId
     * @param registerId
     * @param person
     * @return
     */
    @RequestMapping(value = "/JpgAndTxtToOfd")
    public ResultEntity JpgAndTxtToOfd(String formDefinitionId, String registerId, @Current Person person) {
        uploadFilesService.JpgAndTxtToOfd(formDefinitionId, registerId, person);
        return ResultEntity.success();
    }

}
