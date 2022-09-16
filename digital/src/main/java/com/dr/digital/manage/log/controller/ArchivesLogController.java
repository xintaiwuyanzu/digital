package com.dr.digital.manage.log.controller;


import com.dr.digital.manage.log.entity.ArchivesLog;
import com.dr.digital.manage.log.entity.ArchivesLogInfo;
import com.dr.digital.manage.log.service.ArchivesLogService;
import com.dr.digital.register.entity.Register;
import com.dr.digital.register.entity.RegisterInfo;
import com.dr.digital.register.service.RegisterService;
import com.dr.framework.common.controller.BaseController;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.dr.framework.core.web.annotations.Current;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/archivesLog")
public class ArchivesLogController extends BaseController<ArchivesLog> {

    @Autowired
    ArchivesLogService archivesLogService;

    @Autowired
    RegisterService registerService;

    @Override
    protected void onBeforePageQuery(HttpServletRequest request, SqlQuery<ArchivesLog> sqlQuery, ArchivesLog entity) {
        Assert.isTrue(!StringUtils.isEmpty(entity.getRegisterId()), "ID不能为空!");
        sqlQuery.equal(ArchivesLogInfo.REGISTERID, entity.getRegisterId());
        sqlQuery.like(ArchivesLogInfo.ANJUANTIMING, entity.getAnJuanTiMing());
        sqlQuery.like(ArchivesLogInfo.DANGHAO, entity.getDangHao());
        sqlQuery.equal(ArchivesLogInfo.BOXNUMBER, entity.getBoxNumber());
        sqlQuery.orderByDesc(ArchivesLogInfo.OPERATORDATE);
        super.onBeforePageQuery(request, sqlQuery, entity);
    }
    /**
     * 添加
     *
     * @param person
     * @param archivesLog
     * @return
     */
    @RequestMapping({"/addArchiveLog"})
    public ResultEntity addArchiveLog(ArchivesLog archivesLog, String registerId, @Current Person person) {
        archivesLogService.addArchiveLog(person, archivesLog);
        return ResultEntity.success();
    }

    /**
     * 删除全部，删除选中
     *
     * @param id
     * @param isAll
     * @return
     */
    @RequestMapping("/removeAll")
    public ResultEntity removeAll(String id, boolean isAll, String registerId) {
        archivesLogService.removeAll(id, isAll, registerId);
        return ResultEntity.success();
    }

}
