package com.dr.digital.manage.task.service.impl;

import com.dr.digital.manage.form.service.ArchiveDataManager;
import com.dr.digital.manage.fournaturescheck.service.TestRecord2IMPService;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.manage.task.entity.ArchiveBatch;
import com.dr.digital.manage.task.entity.YuanWenBatchDetail;
import com.dr.digital.processing.vo.ImgVo;
import com.dr.digital.uploadfiles.service.UploadFilesService;
import com.dr.digital.util.FileUtil;
import com.dr.digital.util.ZipUtil;
import com.dr.framework.common.entity.StatusEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * 导入
 *
 * @author: dr
 * @date: 2020/11/18 1:31
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ImpYuanWenBatchDetailServiceImpl extends BaseDataParserBatchDetailService<YuanWenBatchDetail> {
    @Autowired
    ArchiveDataManager archiveDataManager;
    @Autowired
    TestRecord2IMPService testRecord2IMPService;
    @Autowired
    @Lazy
    UploadFilesService uploadFilesService;
    @Value("${filePath}")
    private String filePath;

    @Override
    @Async
    protected void doCreateDetail(ArchiveBatch batch, BaseQuery query) {
        //更新状态
        batch.setStartDate(System.currentTimeMillis());
        //导入中
        batch.setStatus("2");
        commonMapper.updateById(batch);
        try (FileInputStream inputStream = new FileInputStream(batch.getFileLocation())) {
            ZipUtil.unZipFiles(batch.getFileLocation(), filePath + File.separator + "tifold" + File.separator + batch.getFileName().substring(0, batch.getFileName().lastIndexOf(".")));
            //遍历文件夹取出所有的文件
            Map<String, File> fileMap = FileUtil.findtif(filePath + File.separator + "tifold" + File.separator + batch.getFileName().substring(0, batch.getFileName().lastIndexOf(".")));
            for (Map.Entry<String, File> file : fileMap.entrySet()) {
                ImgVo imgVo = new ImgVo();
                imgVo.setFilePath(file.getKey());
                imgVo.setFileName(file.getValue().toString());
                if (file.getValue().toString().substring(file.getValue().toString().indexOf(".")).equals(".tif")) {
                    //tiff拆分转成jpg
                    uploadFilesService.tiffToJpg(imgVo, batch.getId());
                } else if (file.getValue().toString().substring(file.getValue().toString().indexOf(".")).equals(".jpg")) {
                    //jpg不拆分直接放到文件夹下面
                    int i = file.getKey().lastIndexOf(File.separator, (file.getKey().lastIndexOf(File.separator) - 1));
                    int r = file.getKey().lastIndexOf(File.separator);
                    String path2 = file.getKey().substring(i + 1, r);
                    String path1 = path2.substring(0, path2.indexOf("-"));
                    File file1 = new File(filePath + File.separator + "filePath" + File.separator + path1 + File.separator + path2 + File.separator, file.getValue().toString());
                    if (!file1.getParentFile().exists()) {
                        file1.getParentFile().mkdirs();
                    }
                    //如果是图片需要获取这个文件夹下所有的文件
                    new File(imgVo.getFilePath()).renameTo(new File(filePath + File.separator + "filePath" + File.separator + path1 + File.separator + path2 + File.separator + imgVo.getFileName()));
                }
            }
            FileUtil.deleteFolder(new File(filePath + File.separator + "tifold" + File.separator + batch.getFileName().substring(0, batch.getFileName().lastIndexOf("."))));
            //保存结果
            batch.setStatus(StatusEntity.STATUS_ENABLE_STR);
            batch.setEndDate(System.currentTimeMillis());
            commonMapper.updateById(batch);
        } catch (IOException e) {
            e.printStackTrace();
            batch.setStatus(StatusEntity.STATUS_DISABLE_STR);
            batch.setEndDate(System.currentTimeMillis());
            batch.setBeizhu("文件异常");
            commonMapper.updateById(batch);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getType() {
        return BATCH_TYPE_IMP_YUANWEN;
    }

    @Override
    public String getName() {
        return "上传原文";
    }

}
