package com.dr.digital.statistics.controller;

import com.dr.digital.manage.codingscheme.entity.CodingScheme;
import com.dr.digital.manage.codingscheme.entity.CodingSchemeInfo;
import com.dr.digital.ofd.controller.BaseController;
import com.dr.digital.register.entity.Register;
import com.dr.digital.register.entity.RegisterInfo;
import com.dr.digital.register.service.RegisterService;
import com.dr.digital.statistics.entity.Statistics;
import com.dr.digital.statistics.entity.StatisticsInfo;
import com.dr.digital.statistics.service.StatisticsDetailService;
import com.dr.digital.statistics.service.StatisticsService;
import com.dr.framework.common.controller.BaseServiceController;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.dr.framework.core.web.annotations.Current;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author Mr.Zhu
 * @date 2022/8/11 - 9:28
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController extends BaseServiceController<StatisticsService, Statistics> {
    @Autowired
    StatisticsService statisticsService;

    @RequestMapping("statisticsAllRegister")
    public ResultEntity statisticsAllRegister(){
        statisticsService.statisticsAllRegister();
        return ResultEntity.success();
    }

    @Override
    protected SqlQuery<Statistics> buildPageQuery(HttpServletRequest httpServletRequest, Statistics statistics) {
        SqlQuery<Statistics> sqlQuery = SqlQuery.from(Statistics.class)
                .equal(StatisticsInfo.REGISTERID, statistics.getId());
        if (statistics.getUpdateDate()!=0&&statistics.getCreateDate()!=0){
            sqlQuery.lessThan(StatisticsInfo.BATCH_CREATEDATE,statistics.getUpdateDate())
                    .greaterThan(StatisticsInfo.BATCH_CREATEDATE,statistics.getCreateDate());
        }
        sqlQuery.orderByDesc(StatisticsInfo.BATCH_CREATEDATE);
        return sqlQuery;
    }
}
