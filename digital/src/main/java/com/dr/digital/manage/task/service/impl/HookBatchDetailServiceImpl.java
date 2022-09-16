package com.dr.digital.manage.task.service.impl;


import com.dr.digital.manage.form.service.ArchiveDataManager;
import com.dr.digital.manage.form.service.ArchiveFormDefinitionService;
import com.dr.digital.manage.model.entity.AbstractArchiveEntity;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.manage.task.entity.AppraisalBatchDetail;
import com.dr.digital.manage.task.entity.ArchiveBatch;
import com.dr.digital.manage.task.entity.FileHookBatchDetail;
import com.dr.digital.manage.task.service.BaseBatchDetailService;
import com.dr.digital.processing.vo.ImgVo;
import com.dr.digital.uploadfiles.service.UploadFilesService;
import com.dr.framework.common.entity.IdEntity;
import com.dr.framework.common.entity.StatusEntity;
import com.dr.framework.common.file.model.FileInfo;
import com.dr.framework.common.file.resource.FileSystemFileResource;
import com.dr.framework.common.file.service.CommonFileService;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.form.core.service.FormDataService;
import com.dr.framework.common.service.CommonService;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.dr.framework.core.security.SecurityHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 原文挂接
 *
 * @author caor
 * @date 2021-03-19 14:16
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class HookBatchDetailServiceImpl extends BaseDataParserBatchDetailService<FileHookBatchDetail> {
    @Autowired
    ArchiveDataManager archiveDataManager;
    @Autowired
    CommonFileService commonFileService;
    @Autowired
    FormDataService formDataService;
    @Autowired
    CommonService commonService;
    @Autowired
    ArchiveFormDefinitionService archiveFormDefinitionService;
    @Autowired
    Map<String, BaseBatchDetailService> baseBatchServiceMap;
    final ExecutorService executorService = Executors.newFixedThreadPool(10);


    @Override
    @Async
    protected void doCreateDetail(ArchiveBatch batch, BaseQuery query) {
        //更新状态
        batch.setStartDate(System.currentTimeMillis());
        //挂接中
        batch.setStatus("2");
        commonMapper.updateById(batch);
        //批量挂接
        try {
            this.quantityHook(batch, query.getSouceFilesPath(), query);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //保存结果
        batch.setStatus(StatusEntity.STATUS_ENABLE_STR);
        batch.setEndDate(System.currentTimeMillis());
        commonMapper.updateById(batch);
    }

    @Override
    public String getType() {
        return BATCH_TYPE_FILE_HOOK;
    }

    @Override
    public String getName() {
        return "原文挂接";
    }

    List<File> fileList = new ArrayList<>();

    public void quantityHook(ArchiveBatch batch, String souceFilesPath, BaseQuery baseQuery) throws InterruptedException {
        Assert.isTrue(!StringUtils.isEmpty(souceFilesPath), "请选择路径！");
        Assert.isTrue(isExists(souceFilesPath), "路径不存在，请重新选择");
        //创建备份原始文件文件夹
        String backupFilesPath = commonFileConfig.getFullDirPath("backupfiles", null, new Date());
        File file = new File(souceFilesPath);
        fileList = new ArrayList<>();
        List<File> files = this.getFile(file);
        CountDownLatch countDownLatch = new CountDownLatch(files.size());
        files.forEach(file1 -> {
            String filePath = file1.getPath();
            // String newFilePath = ArchiveEntity.COLUMN_FOND_CODE) + File.separator + formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + File.separator;

            if (filePath.substring(filePath.lastIndexOf(".")).equals(".txt")) {
                countDownLatch.countDown();
                return;
            }

            SecurityHolder securityHolder = SecurityHolder.get();
            executorService.execute(() -> {
                //根据获取文件名（不带扩展名）
                SecurityHolder.set(securityHolder);
                String fileName = file1.getName().substring(0, file1.getName().lastIndexOf("."));
                baseQuery.setStatus(StatusEntity.STATUS_DISABLE_STR);
                baseQuery.setArchiveCode(fileName);
                FileHookBatchDetail fileHookBatchDetail = newBatchDetail(null, batch, baseQuery);
                ImgVo imgVo = new ImgVo();
                String fileName1 = file1.getName().substring(0, file1.getName().lastIndexOf("."));
                String fileSize1 = Long.toString(file1.length());
                // fileName, fileSize,
                imgVo.setFilePath(filePath);
                imgVo.setFileName(fileName1);
                imgVo.setFileSize(fileSize1);
                List<FormData> formDataList = formDataService.selectSelfColumnFormData(batch.getFormid(), (sqlQuery, formRelationWrapper) -> {
                    sqlQuery.column(formRelationWrapper.getColumn(AbstractArchiveEntity.COLUMN_ARCHIVE_CODE))
                            .column(formRelationWrapper.getColumn(IdEntity.ID_COLUMN_NAME))
                            .column(formRelationWrapper.getColumn(AbstractArchiveEntity.COLUMN_TITLE))
                            .column(formRelationWrapper.getColumn(AbstractArchiveEntity.COLUMN_YEAR))
                            .column(formRelationWrapper.getColumn(AbstractArchiveEntity.COLUMN_FOND_CODE))
                            .equal(formRelationWrapper.getColumn(AbstractArchiveEntity.COLUMN_ARCHIVE_CODE), fileName);
                });
                if (formDataList.size() > 0) {
                    formDataList.forEach(formData -> {
                        FileSystemFileResource fileSystemFileResource = new FileSystemFileResource(file1);
                        try {
                            //附件表添加数据
                            fileSystemFileResource.setFileAttr(file1.getPath());
                            fileSystemFileResource.setFileType("hookArchive");
                            List<FileInfo> fileInfoList = commonFileService.list(formData.get(IdEntity.ID_COLUMN_NAME));
                            if (fileInfoList.size() == 0) {
                                commonFileService.addFileLast(fileSystemFileResource, formData.get(IdEntity.ID_COLUMN_NAME), "archive", "default");
                                archiveDataManager.updateHaveYuanwenByFormData("", formData, "1");
                                //创建批次记录
                                baseQuery.setStatus(StatusEntity.STATUS_ENABLE_STR);
                                updateDetail(fileHookBatchDetail, formData, baseQuery);
                            }
                            //把挂接成功的从原始目录移动到备份目录
                            //file1.renameTo(new File(backupFilesPath + File.separator + file1.getName()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                countDownLatch.countDown();
            });
        });
        countDownLatch.await();
    }

    public void quantityHookTwo(ArchiveBatch batch, String souceFilesPath, BaseQuery baseQuery, String id) throws InterruptedException {
        Assert.isTrue(!StringUtils.isEmpty(souceFilesPath), "请选择路径！");
        Assert.isTrue(isExists(souceFilesPath), "路径不存在，请重新选择");
        //创建备份原始文件文件夹
        BaseBatchDetailService service = baseBatchServiceMap.get("impBatchDetailServiceImpl");
        List<AppraisalBatchDetail> AppraisalBatchDetail = service.selectList(batch);
        String backupFilesPath = commonFileConfig.getFullDirPath("backupfiles", null, new Date());
        File file = new File(souceFilesPath);
        fileList = new ArrayList<>();
        List<File> files = this.getFile(file);
        SqlQuery<FileHookBatchDetail> sql = SqlQuery.from(FileHookBatchDetail.class);
        List<FileHookBatchDetail> list = commonMapper.selectByQuery(sql);
        if (list.stream().filter(w -> String.valueOf(w.getBatchId()).equals(id)).findAny().isPresent()) {

            for (int i = 0; i < list.size(); i++) {
                FileHookBatchDetail data = list.get(i);
                String batchId = data.getBatchId();
                String archiveCode = data.getArchival_code();
                Optional<FileHookBatchDetail> cartOptional = list.stream().filter(item -> item.getBatchId().equals(batchId) && item.getArchival_code().equals(archiveCode)).findFirst();
                if (cartOptional.isPresent()) {
                    // 存在
                    FileHookBatchDetail cart = cartOptional.get();
                }
                for (int j = 0; j < files.size(); j++) {
                    String text = files.get(j).toString();
                    int a = text.lastIndexOf("\\");
                    int b = text.lastIndexOf(".");
                    text = text.substring(a + 1, b);
                    if (text.equals(archiveCode)) {
                        files.remove(files.get(j));
                    }
                }
            }
        }
        CountDownLatch countDownLatch = new CountDownLatch(files.size());
        files.forEach(file1 -> {
            String filePath = file1.getPath();
            if (filePath.substring(filePath.lastIndexOf(".")).equals(".txt")) {
                countDownLatch.countDown();
                return;
            }
            SecurityHolder securityHolder = SecurityHolder.get();
            executorService.execute(() -> {
                //根据获取文件名（不带扩展名）
                SecurityHolder.set(securityHolder);
                String fileName = file1.getName().substring(0, file1.getName().lastIndexOf("."));
                baseQuery.setStatus(StatusEntity.STATUS_DISABLE_STR);
                baseQuery.setArchiveCode(fileName);
                batch.setId(id);
                FileHookBatchDetail fileHookBatchDetail = newBatchDetail(null, batch, baseQuery);
                List<FormData> formDataList = formDataService.selectSelfColumnFormData(batch.getFormid(), (sqlQuery, formRelationWrapper) -> {
                    sqlQuery.column(formRelationWrapper.getColumn(AbstractArchiveEntity.COLUMN_ARCHIVE_CODE))
                            .column(formRelationWrapper.getColumn(IdEntity.ID_COLUMN_NAME))
                            .column(formRelationWrapper.getColumn(AbstractArchiveEntity.COLUMN_TITLE))
                            .column(formRelationWrapper.getColumn(AbstractArchiveEntity.COLUMN_YEAR))
                            .column(formRelationWrapper.getColumn(AbstractArchiveEntity.COLUMN_FOND_CODE))
                            .equal(formRelationWrapper.getColumn(AbstractArchiveEntity.COLUMN_ARCHIVE_CODE), fileName);
                });
                if (formDataList.size() > 0) {
                    formDataList.forEach(formData -> {
                        FileSystemFileResource fileSystemFileResource = new FileSystemFileResource(file1);
                        try {
                            //附件表添加数据
                            fileSystemFileResource.setFileAttr(file1.getPath());
                            fileSystemFileResource.setFileType("hookArchive");
                            List<FileInfo> fileInfoList = commonFileService.list(formData.get(IdEntity.ID_COLUMN_NAME));
                            if (fileInfoList.size() == 0) {
                                baseQuery.setStatus(StatusEntity.STATUS_ENABLE_STR);
                                //  fileHookBatchDetail.setBatchId(id);
                                updateDetail(fileHookBatchDetail, formData, baseQuery);
                                commonFileService.addFileLast(fileSystemFileResource, formData.get(IdEntity.ID_COLUMN_NAME), "archive", "default");
                                archiveDataManager.updateHaveYuanwenByFormData("", formData, "1");
                                //创建批次记录

                            }
                            //把挂接成功的从原始目录移动到备份目录
                            //file1.renameTo(new File(backupFilesPath + File.separator + file1.getName()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                countDownLatch.countDown();
            });
        });
        countDownLatch.await();
    }

    @Override
    protected FileHookBatchDetail newBatchDetail(FormData data, ArchiveBatch batch, BaseQuery query) {
        FileHookBatchDetail fileHookBatchDetail = new FileHookBatchDetail();
        fileHookBatchDetail.setBatchId(batch.getId());
        fileHookBatchDetail.setStatus(query.getStatus());
        fileHookBatchDetail.setArchival_code(query.getArchiveCode());
        //绑定基本信息
        CommonService.bindCreateInfo(fileHookBatchDetail);
        //插入数据
        commonMapper.insert(fileHookBatchDetail);
        return fileHookBatchDetail;
    }

    protected void updateDetail(FileHookBatchDetail fileHookBatchDetail, FormData data, BaseQuery query) {
        fileHookBatchDetail.setFormDefinitionId(data.getFormDefinitionId());
        fileHookBatchDetail.setFormDataId(data.getId());
        fileHookBatchDetail.setFonds_identifier(data.get(ArchiveEntity.COLUMN_FOND_CODE));
        fileHookBatchDetail.setTitle(data.get(ArchiveEntity.COLUMN_TITLE));
        fileHookBatchDetail.setYear(data.getString(AbstractArchiveEntity.COLUMN_YEAR));
        fileHookBatchDetail.setStatus(query.getStatus());
        commonMapper.updateById(fileHookBatchDetail);
    }

    /**
     * 判断路径是否存在
     *
     * @param path
     * @return
     */
    public boolean isExists(String path) {
        boolean isExist = true;
        File file = new File(path);
        if (!file.exists()) {
            isExist = false;
        }
        return isExist;
    }

    /**
     * 获取文件夹及其子文件夹下文件名（包含路径）
     */

    public List<File> getFile(File file) {
        if (file != null) {
            File[] f = file.listFiles();
            if (f != null) {
                for (int i = 0; i < f.length; i++) {
                    getFile(f[i]);
                }
            } else {
                fileList.add(file);
            }
        }
        return fileList;
    }
}
