package com.dr.digital.manage.filehook.service.service.imp;

import com.dr.digital.manage.filehook.service.service.FileHookService;
import com.dr.digital.manage.form.service.ArchiveFormDefinitionService;
import com.dr.digital.manage.model.entity.AbstractArchiveEntity;
import com.dr.framework.common.entity.IdEntity;
import com.dr.framework.common.file.autoconfig.CommonFileConfig;
import com.dr.framework.common.file.resource.FileSystemFileResource;
import com.dr.framework.common.file.service.CommonFileService;
import com.dr.framework.common.form.core.entity.FormDefinition;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.form.core.query.FormDefinitionQuery;
import com.dr.framework.common.form.core.service.FormDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author caor
 * @date 2021-03-11 14:16
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FileHookServiceImpl implements FileHookService {
    @Autowired
    ArchiveFormDefinitionService archiveFormDefinitionService;
    @Autowired
    CommonFileConfig commonFileConfig;
    @Autowired
    FormDataService formDataService;
    @Autowired
    CommonFileService commonFileService;
    List<File> fileList = new ArrayList<>();

    @Override
    @Async
    public void quantityHook(String souceFilesPath) {
        Assert.isTrue(!StringUtils.isEmpty(souceFilesPath), "请选择路径！");
        Assert.isTrue(isExists(souceFilesPath), "路径不存在，请重新选择");
        //创建备份原始文件文件夹
        String backupFilesPath = commonFileConfig.getFullDirPath("backupfiles", null, new Date());
        File file = new File(souceFilesPath);
        fileList = new ArrayList<>();
        List<File> files = this.getFile(file);
        files.forEach(file1 -> {
            //根据获取文件名（不带扩展名）
            String fileName = file1.getName().substring(0, file1.getName().lastIndexOf("."));
            FormDefinitionQuery formDefinitionQuery = new FormDefinitionQuery();
            List<FormDefinition> formDefinitionList = (List<FormDefinition>) archiveFormDefinitionService.findFormList(formDefinitionQuery);
            formDefinitionList.forEach(formDefinition -> {
                List<FormData> formDataList = formDataService.selectSelfColumnFormData(formDefinition.getId(), (sqlQuery, formRelationWrapper) -> {
                    sqlQuery.column(formRelationWrapper.getColumn(AbstractArchiveEntity.COLUMN_ARCHIVE_CODE))
                            .column(formRelationWrapper.getColumn(IdEntity.ID_COLUMN_NAME))
                            .equal(formRelationWrapper.getColumn(AbstractArchiveEntity.COLUMN_ARCHIVE_CODE), fileName);
                });

                if (formDataList.size() > 0) {
                    formDataList.forEach(formData -> {
                        FileSystemFileResource fileSystemFileResource = new FileSystemFileResource(file1);
                        try {
                            //附件表添加数据
                            commonFileService.addFileLast(fileSystemFileResource, formData.get(IdEntity.ID_COLUMN_NAME), "archive", "default");
                            //把挂接成功的从原始目录移动到备份目录
//                            file1.renameTo(new File(backupFilesPath + File.separator + file1.getName()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            });
        });
    }

    /**
     * 判断路径是否存在
     *
     * @param path
     * @return
     */
    @Override
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
