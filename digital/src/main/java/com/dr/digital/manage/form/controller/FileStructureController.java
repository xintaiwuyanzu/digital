package com.dr.digital.manage.form.controller;

import com.dr.digital.manage.form.entity.FileStructure;
import com.dr.digital.manage.form.entity.FileStructureInfo;
import com.dr.digital.manage.form.service.FileStructureService;
import com.dr.digital.manage.log.annotation.SysLog;
import com.dr.framework.common.controller.BaseController;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/api/fileStructure")
public class FileStructureController extends BaseController<FileStructure> {
    @Autowired
    FileStructureService fileStructureService;

    @Override
    protected void onBeforePageQuery(HttpServletRequest request, SqlQuery<FileStructure> sqlQuery, FileStructure entity) {
        sqlQuery.equal(FileStructureInfo.ARCHIVERS_CATEGORY_CODE, entity.getArchivers_category_code())
                .equal(FileStructureInfo.AJ_ARCHIVAL_CODE, entity.getAj_archival_code())
                .equal(FileStructureInfo.REGISTERID, entity.getRegisterId())
                .orderBy(FileStructureInfo.ARCHIVAL_CODE);
        super.onBeforePageQuery(request, sqlQuery, entity);
    }

    @SysLog("查询文件结构列表页数据")
    @RequestMapping(value = "/wjJgDataTree")
    public ResultEntity wjJgDataTree(FileStructure fileStructure) {
        return ResultEntity.success(fileStructureService.wjJgDataTree(fileStructure));
    }

}
