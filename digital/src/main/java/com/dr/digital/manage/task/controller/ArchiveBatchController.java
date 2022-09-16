package com.dr.digital.manage.task.controller;

import com.dr.digital.manage.impexpscheme.service.imp.DbfFileInfoHandler;
import com.dr.digital.manage.model.query.ArchiveDataQuery;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.manage.task.entity.*;
import com.dr.digital.manage.task.service.ArchiveBatchService;
import com.dr.digital.util.UUIDUtils;
import com.dr.framework.common.controller.BaseServiceController;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.file.autoconfig.CommonFileConfig;
import com.dr.framework.common.file.resource.FileSystemFileResource;
import com.dr.framework.common.page.Page;
import com.dr.framework.common.service.CommonService;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.dr.framework.core.web.annotations.Current;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * 具体项
 *
 * @author dr
 */
@RestController
@RequestMapping({"${common.api-path:/api}/batch"})
public class ArchiveBatchController extends BaseServiceController<ArchiveBatchService, ArchiveBatch> {
    @Autowired
    CommonFileConfig commonFileConfig;
    @Autowired
    DbfFileInfoHandler fileInfoHandler;
    @Autowired
    CommonService commonService;
    @Value("${filePath}")
    private String filePath;

    @Override
    protected SqlQuery<ArchiveBatch> buildPageQuery(HttpServletRequest httpServletRequest, ArchiveBatch batch) {
        SqlQuery<ArchiveBatch> sqlQuery = SqlQuery.from(ArchiveBatch.class);
        if (!StringUtils.isEmpty(batch.getBatchName())) {
            sqlQuery.like(ArchiveBatchInfo.BATCHNAME, batch.getBatchName());
        }
        sqlQuery.equal(ArchiveBatchInfo.BATCHTYPE, batch.getBatchType())
                .orderByDesc(ArchiveBatchInfo.STARTDATE);
        return sqlQuery;
    }

    @PostMapping("/newBatch")
    public ResultEntity<ArchiveTask> newBatch(
            HttpServletRequest request,
            BaseQuery query,
            String type,
            MultipartFile file,
            @RequestParam(name = ArchiveDataQuery.QUERY_KEY, required = false) String queryContent,
            @Current Person person) throws Exception {
        Assert.isTrue(!StringUtils.isEmpty(type), "批次类型不能为空！");
        query.parseQuery(queryContent);
        if (file != null) {
            try (InputStream inputStream = file.getInputStream()) {
                //上传文件
                String filename = file.getOriginalFilename();
                filename = UUIDUtils.getUUID() + filename;
                //保存文件
                String filePath = uploadFile(inputStream, filename);
                String mine = fileInfoHandler.fileMine(new FileSystemFileResource(filePath));
                //设置文件存储位置
                query.setFileLocation(filePath);
                query.setFileName(filename);
                query.setMineType(mine);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        service.newBatch(type, query);
        return ResultEntity.success();
    }

    /**
     * 查询batchdetail列表
     *
     * @param request
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @RequestMapping("/batchDetailPage")
    public ResultEntity batchDetailPage(HttpServletRequest request,
                                        @RequestParam(defaultValue = "0") int pageIndex,
                                        @RequestParam(defaultValue = Page.DEFAULT_PAGE_SIZE_STR) int pageSize,
                                        ArchiveBatch batch) {
        Assert.isTrue(!StringUtils.isEmpty(batch.getBatchType()), "业务类型不能为空！");
        return ResultEntity.success(service.selectPage(batch, pageIndex, pageSize));
    }

    public String uploadFile(InputStream fis, String fileName) throws IOException {
        Date date = new Date();
        String filePaths = String.join(File.separator,filePath,"upload",DateFormatUtils.format(date, "yyyy")
        ,DateFormatUtils.format(date, "MM"),DateFormatUtils.format(date, "dd"),fileName);
//        String filePaths = filePath + File.separator + "upload" + File.separator + DateFormatUtils.format(date, "yyyy")
//                + File.separator + DateFormatUtils.format(date, "MM") + File.separator + DateFormatUtils.format(date, "dd")
//                + File.separator + fileName;
        File file = new File(filePaths);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        FileCopyUtils.copy(fis, new FileOutputStream(filePaths));
        return file.getPath();
    }

    /**
     * 批量删除列表detail数据
     *
     * @param request
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @RequestMapping("/deleteDetail")
    public ResultEntity deleteDetail(HttpServletRequest request,
                                     @RequestParam(defaultValue = "0") int pageIndex,
                                     @RequestParam(defaultValue = Page.DEFAULT_PAGE_SIZE_STR) int pageSize,
                                     YuanWenBatchDetail yuanWenBatchDetail, String ids, String batchId) {
        if ("".equals(ids) || ids == null) {
            SqlQuery<YuanWenBatchDetail> sqlQuery = SqlQuery.from(YuanWenBatchDetail.class).equal(YuanWenBatchDetailInfo.BATCHID, batchId);
            commonService.delete(sqlQuery);
        } else {
            String[] split = ids.split(",");
            for (String s : split) {
                SqlQuery<YuanWenBatchDetail> sqlQuery = SqlQuery.from(YuanWenBatchDetail.class).equal(YuanWenBatchDetailInfo.ID, s);
                commonService.delete(sqlQuery);
            }
        }
        return ResultEntity.success();
    }


    @RequestMapping("/yuanWenDetail")
    public ResultEntity yuanWenDetail(ArchiveBatch batch) {
        List<YuanWenBatchDetail> listDetail = service.selectList(batch);
        for (YuanWenBatchDetail yuanWenBatchDetail : listDetail) {
            if (StringUtils.isEmpty(yuanWenBatchDetail.getImgYeShu()) && StringUtils.isEmpty(yuanWenBatchDetail.getdHMs())) {
                yuanWenBatchDetail.setdHMs("档号匹配成功！");
                yuanWenBatchDetail.setImgYeShu(listDetail.size() + "");
                commonService.update(yuanWenBatchDetail);
            }
        }
        if (listDetail.size()==0) {
            return ResultEntity.success();
        }
        String code = listDetail.get(0).getArchival_code();
        SqlQuery<YuanWenBatchDetail> sqlQuery = SqlQuery.from(YuanWenBatchDetail.class)
                .equal(YuanWenBatchDetailInfo.ARCHIVAL_CODE,code)
                .groupBy(YuanWenBatchDetailInfo.BATCHID)
                .orderByDesc(YuanWenBatchDetailInfo.CREATEDATE);
        if (!StringUtils.isEmpty(batch.getStatus())) {
            sqlQuery.equal(YuanWenBatchDetailInfo.STATUS, batch.getStatus());
        }
        List<YuanWenBatchDetail> yuanWenBatchDetails = commonService.selectList(sqlQuery);

        return ResultEntity.success(yuanWenBatchDetails);
    }

}
