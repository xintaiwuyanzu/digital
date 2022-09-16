package com.dr.digital.ocr.controller;

import com.dr.digital.ocr.entity.OcrQueue;
import com.dr.digital.ocr.entity.OcrQueueInfo;
import com.dr.digital.ocr.entity.TaskInfoList;
import com.dr.digital.ocr.service.OcrQueueService;
import com.dr.digital.util.UUIDUtils;
import com.dr.framework.common.controller.BaseController;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.dr.framework.core.web.annotations.Current;
import jdk.nashorn.internal.objects.NativeString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ocrQueue")
public class OcrQueueController extends BaseController<OcrQueue> {
    @Autowired
    OcrQueueService ocrQueueService;
    @Autowired
    TaskInfoList taskInfoList;

    /**
     * page 列表页面
     *
     * @param request
     * @param sqlQuery
     * @param entity
     */
    @Override
    protected void onBeforePageQuery(HttpServletRequest request, SqlQuery<OcrQueue> sqlQuery, OcrQueue entity) {
        sqlQuery.equal(OcrQueueInfo.BATCHNAME, entity.getBatchName());
        sqlQuery.equal(OcrQueueInfo.FORMDEFINITIONID, entity.getFormDefinitionId());
        sqlQuery.equal(OcrQueueInfo.FONDCODE, entity.getFondCode());
        sqlQuery.equal(OcrQueueInfo.ARCHIVECODE, entity.getArchiveCode());
        sqlQuery.equal(OcrQueueInfo.STATUS, entity.getStatus());
        sqlQuery.orderByDesc(OcrQueueInfo.CREATEDATE);
        super.onBeforePageQuery(request, sqlQuery, entity);
    }

    /**
     * 添加进入ocr队列
     *
     * @return
     */
    @RequestMapping("/addOcrQueue")
    public ResultEntity addOcrQueue(String formDefinitionId, String registerId, String batchName,String batchNo, @Current Person person) {
        ocrQueueService.ocrQueueLb(formDefinitionId, registerId, batchName, batchNo,person);
        return ResultEntity.success();
    }

    /**
     * 执行ocr队列
     *
     * @return
     */
    @RequestMapping("/implementOcr")
    public ResultEntity implementOcr() {
        ocrQueueService.implementOcr();
        return ResultEntity.success();
    }

    /**
     * 暂停ocr服务
     *
     * @return
     */
    @RequestMapping("/updateStatus")
    public ResultEntity updateStatus() {
        ocrQueueService.updateStatus();
        return ResultEntity.success();
    }

    /**
     * 选择启动
     *
     * @param ocrQueueId
     * @param person
     * @return
     */
    @RequestMapping("/selectOcrStart")
    public ResultEntity selectOcrStart(String ocrQueueId, @Current Person person) {
        ocrQueueService.selectOcrStart(ocrQueueId, person);
        return ResultEntity.success();
    }

    /**
     * 查询转换服务
     *
     * @param entity
     * @param person
     * @return
     */
    @RequestMapping("/searchOcrStart")
    public ResultEntity searchOcrStart(OcrQueue entity, @Current Person person) {
        ocrQueueService.searchOcrStart(entity, person);
        return ResultEntity.success();
    }

    /**
     * 暂停重启服务
     *
     * @return
     */
    @RequestMapping("/allOcrStart")
    public ResultEntity allOcrStart() {
        ocrQueueService.allOcrStart();
        return ResultEntity.success();
    }


    /**
     * 修改批次优先级
     * @param fid
     * @param type
     * @param fromid
     * @return
     */
    @RequestMapping("/updatePriorityData")
    public ResultEntity updatePriorityData(String fid,String type,String fromid){
        String uuid = UUIDUtils.getSimpleUUID();
        ocrQueueService.updatePriorityData(fid,type,fromid,uuid);
        Map<String,String> map = new HashMap<>();
        map.put("id",uuid);
        map.put("data","正在处理中请稍后");
        return ResultEntity.success(map);
    }
    @RequestMapping("/ps")
    public TaskInfoList.TaskInfo updateProcessStatus(String id) {
        return taskInfoList.search(id);
    }
    @RequestMapping("/selectFunction")
    public ResultEntity selectFunction(){
        return ResultEntity.success(ocrQueueService.selectFunction());
    }
}
