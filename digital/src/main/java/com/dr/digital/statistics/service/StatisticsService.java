package com.dr.digital.statistics.service;

import com.dr.digital.statistics.entity.Statistics;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.service.BaseService;
import com.dr.framework.core.organise.entity.Person;

import java.util.List;
import java.util.Map;

/**
 * @author Mr.Zhu
 * @date 2022/8/11 - 9:29
 */
public interface StatisticsService extends BaseService<Statistics> {
    void statisticsAllRegister();
}
