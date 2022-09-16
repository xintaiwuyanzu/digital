package com.dr.digital.ofd.controller;

import com.dr.digital.ofd.entity.OfdRecord;
import com.dr.digital.ofd.entity.OfdRecordInfo;
import com.dr.digital.register.entity.Register;
import com.dr.framework.common.controller.BaseController;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/ofdRecord")
public class OfdRecordController extends BaseController<OfdRecord> {
    protected void onBeforePageQuery(HttpServletRequest request, SqlQuery<OfdRecord> sqlQuery, OfdRecord entity) {
        super.onBeforePageQuery(request, sqlQuery, entity);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @RequestMapping("deleteByIds")
    public ResultEntity<Register> delete(String ids) {
        String[] split = ids.split(",");
        for (String s : split) {
            commonService.delete(SqlQuery.from(OfdRecord.class).equal(OfdRecordInfo.ID, s));
        }
        return ResultEntity.success();
    }
}
