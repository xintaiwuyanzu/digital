package com.dr.digital.uploadfiles.controller;

import com.dr.digital.register.service.RegisterService;
import com.dr.digital.uploadfiles.entity.JpgQueue;
import com.dr.digital.uploadfiles.entity.JpgQueueInfo;
import com.dr.digital.uploadfiles.entity.MatchText;
import com.dr.digital.uploadfiles.service.JpgQueueService;
import com.dr.framework.common.controller.BaseController;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.core.service.FormDataService;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.dr.framework.core.web.annotations.Current;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/jpgQueue")
public class JpgQueueController extends BaseController<JpgQueue> {
    @Autowired
    JpgQueueService jpgQueueService;
    @Autowired
    RegisterService registerService;
    @Autowired
    FormDataService formDataService;

    /**
     * page 列表页面
     *
     * @param request
     * @param sqlQuery
     * @param entity
     */
    @Override
    protected void onBeforePageQuery(HttpServletRequest request, SqlQuery<JpgQueue> sqlQuery, JpgQueue entity) {
        sqlQuery.equal(JpgQueueInfo.BATCHNAME, entity.getBatchName());
        sqlQuery.equal(JpgQueueInfo.FORMDEFINITIONID, entity.getFormDefinitionId());
        sqlQuery.equal(JpgQueueInfo.FONDCODE, entity.getFondCode());
        sqlQuery.equal(JpgQueueInfo.ARCHIVECODE, entity.getArchiveCode());
        sqlQuery.equal(JpgQueueInfo.STATUS, entity.getStatus());
        sqlQuery.orderByDesc(JpgQueueInfo.CREATEDATE);
        super.onBeforePageQuery(request, sqlQuery, entity);
    }

    /**
     * 添加进入jpg队列
     *
     * @return
     */
    @RequestMapping("/addJpgQueue")
    public ResultEntity addJpgQueue(String formDefinitionId, String registerId, String batchName, String batchNo, @Current Person person) {
        jpgQueueService.addJpgQueue(formDefinitionId, registerId, batchName, batchNo, person);
        return ResultEntity.success();
    }

/************************************************** 常规的拆件服务  *************************************/
    /**
     * 在批次进行批次拆分jpg
     *
     * @param person
     * @return
     */
    @RequestMapping(value = "/batchTiffToJpgByPath")
    public ResultEntity batchTiffToJpgByPath(String formDefinitionId, String registerId, String type, @Current Person person) {
        jpgQueueService.tiffToJpgByPath(formDefinitionId, registerId, type, person);
        return ResultEntity.success();
    }
    /**
     * 根据档号找jpg队列的jpg信息
     * @param archiveCode
     * @return
     */
    @RequestMapping("/getArchiveCodeByJpg")
    public ResultEntity getArchiveCodeByJpg(String archiveCode) {
        return ResultEntity.success(jpgQueueService.getArchiveCodeByJpg(archiveCode));
    }


//    @RequestMapping("/getRegisterIdByCount")
//    public ResultEntity getRegisterIdByCount(String registerId,final String flowPath){
//        return ResultEntity.success(  jpgQueueService.getRegisterIdByCount(registerId,flowPath));
//    }

    @RequestMapping("/getMatchText")
    public ResultEntity getMatchText(MatchText matchText,@Current Person person){
        return jpgQueueService.getMatchText(matchText,person);
    }
    @RequestMapping("/selectFunction")
    public ResultEntity selectFunction(){
        return ResultEntity.success(jpgQueueService.selectFunction());
    }

    @RequestMapping("/test")
    public void test(){
        jpgQueueService.test();
    }
}
