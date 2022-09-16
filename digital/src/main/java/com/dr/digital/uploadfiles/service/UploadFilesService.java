package com.dr.digital.uploadfiles.service;


import com.dr.digital.manage.task.entity.ArchiveBatch;
import com.dr.digital.processing.vo.ImgVo;
import com.dr.digital.uploadfiles.entity.UploadFiles;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.core.organise.entity.Person;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface UploadFilesService {

    /**
     * 根据BussinessId查询附件
     *
     * @param aId
     * @return
     */
    List<UploadFiles> findUploadFilesByBussinessId(String aId);

    /**
     * 根据BussinessId查询数量
     * @param aId
     * @return
     */
    Long findImgCountById(String aId);


    /**
     * 根据BussinessId查询最大顺序号
     *
     * @param aId
     * @return
     */
    Integer findUploadFilesOrderMaxByBussinessId(String aId);

    /**
     * 扫描原文
     *
     * @param formData
     * @param filesOrder
     */
    void uploadFiles(MultipartFile multipartfile, FormData formData, Integer filesOrder, String newFilePath);

    /**
     * 插扫原文
     *
     * @param formData
     * @param newFilePath
     */
    void creatPlug(MultipartFile multipartfile, FormData formData, String newFilePath, String id);

    /**
     * 补扫原文
     *
     * @param multipartfile
     * @param formData
     * @param newFilePath
     * @param id
     */
    void creatSweeping(MultipartFile multipartfile, FormData formData, String newFilePath, String id);

    /**
     * 替扫原文
     *
     * @param formData
     * @param newFilePath
     */
    void creatSaul(MultipartFile multipartfile, FormData formData, String newFilePath, String id, UploadFiles uploadFile);

    /**
     * 根据ID查询
     *
     * @param Id
     * @return
     */
    UploadFiles findUploadFilesOneById(String Id);

    /**
     * 根据下一个id查询子类
     *
     * @param nextId
     * @return
     */
    UploadFiles findUploadFilesByParentId(String nextId);

    /**
     * 根据ID删除附件
     *
     * @param aId aId
     * @return
     */
    String deleteUploadFilesById(String aId);

    /**
     * 删除全部扫描图像
     *
     * @param uploadFiles
     * @param filePath
     * @param flag
     */
    void deleteAllByBussinessId(UploadFiles uploadFiles, String filePath, String flag);

    /**
     * 档案原文拆分
     *
     * @param formData
     * @param registerId
     * @param person
     * @throws Exception
     */
    void fileSplitWrit(FormData formData, String registerId, Person person) throws Exception;

    /**
     * 现在 最新使用的 图片和txt文本合并成（双层ODF）
     *
     * @param formData
     * @param registerId
     * @param person
     * @return
     * @throws Exception
     */
    boolean photoMergeToOfd(FormData formData, String registerId, Person person) throws Exception;

    void updateFileVolumesImg(ImgVo imgVo, Person person);

    void updateFileImg(ImgVo imgVo, String contents, Person person);

    void tiffToJpg(ImgVo imgVo, String batchId);

    boolean photoMergeToPDFToOFD(FormData formData, String childFormId, String registerId, Person person) throws Exception;

    boolean photoMergeToPDF(FormData formData, String childFormId, String registerId, Person person) throws Exception;

    Map<String, String> addXMLFile(String path);

    Map<String, String> mergeBatchPage(String src, String save);

    /**
     * tif 文件拆分成jpg
     */
    void tiffToJpgByPath(Person person);

    /**
     * tif拆成jpg
     *
     * @param file
     * @param batch
     * @param person
     */
    void tifTojPGXc(File file, ArchiveBatch batch, Person person);

    /**
     * pdf 拆成jpg
     *
     * @param file
     * @param batch
     * @param person
     */
    void pdfToImageFile(File file, ArchiveBatch batch, String fileName, Person person);

    /**
     * ocr 识别记录
     *
     * @param dirName
     */
    void insetOcrRecord(String dirName, String code, String message, String fileName, String path, String name, String ysTime, long startTime, long endTime, String fileKb,String registerId);

    /**
     * 双层ofd转换 （目录著录环节）
     */
    void mergeConversion(List<FormData> dataList, String formDefinitionId, String registerId, Person person);

    /**
     * 批次上 拆分成jpg
     *
     * @param formDefinitionId
     * @param person
     */
    void tiffToJpgByPath(String formDefinitionId, Person person);

    /**
     * 批次上的 ocr识别
     *
     * @param formDefinitionId
     * @param person
     */
    void batchJpgToTxt(String formDefinitionId,String registerId, Person person);

    /**
     * 提交表单后，生成对应ofd文件
     * @param formData
     * @param registerId
     * @param person
     */
    void formDataOneToOfd(FormData formData, String registerId, Person person);

    /**
     * 在批次上进行OFD转换
     *
     * @param formDefinitionId
     * @param registerId
     * @param person
     */
    void JpgAndTxtToOfd(String formDefinitionId, String registerId, Person person);
}
