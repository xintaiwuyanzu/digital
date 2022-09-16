package com.dr.digital.ocr.controller;

import com.dr.digital.configManager.entity.FlowPathDetail;
import com.dr.digital.configManager.entity.FlowPathDetailInfo;
import com.dr.digital.ocr.entity.OcrRecord;
import com.dr.digital.ocr.entity.OcrRecordInfo;
import com.dr.digital.register.entity.Register;
import com.dr.framework.common.controller.BaseController;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/ocrRecord")
public class OcrRecordController extends BaseController<OcrRecord> {
    protected void onBeforePageQuery(HttpServletRequest request, SqlQuery<OcrRecord> sqlQuery, OcrRecord entity) {
        SqlQuery<OcrRecord> ocrRecordSqlQuery = sqlQuery.like(OcrRecordInfo.ARCHIVECODE, entity.getArchiveCode()).orderByDesc(OcrRecordInfo.CREATEDATE);
        if (entity.getStartTime() != 0 && entity.getEndTime() != 0) {
            ocrRecordSqlQuery.lessThan(OcrRecordInfo.STARTTIME, entity.getEndTime());
            ocrRecordSqlQuery.greaterThan(OcrRecordInfo.STARTTIME, entity.getStartTime());
        }
        if (entity.getStatus() != null && entity.getStatus().equals("0")) {
            ocrRecordSqlQuery.equal(OcrRecordInfo.CODE, entity.getStatus());
        } else if (entity.getStatus() != null && entity.getStatus().equals("1")) {
            ocrRecordSqlQuery.notEqual(OcrRecordInfo.CODE, 0);
        }
        super.onBeforePageQuery(request, ocrRecordSqlQuery, entity);
    }

    /**
     * 批量删除方式
     *
     * @param ids
     * @return
     */
    @RequestMapping("deleteByIds")
    public ResultEntity<Register> delete(String ids) {
        String[] split = ids.split(",");
        for (String s : split) {
            commonService.delete(SqlQuery.from(OcrRecord.class).equal(OcrRecordInfo.ID, s));
        }
        return ResultEntity.success();
    }

}
