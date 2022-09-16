package com.dr.digital.statistics.service;

import com.dr.digital.statistics.entity.StatisticsDetail;
import com.dr.framework.common.service.BaseService;
import com.dr.framework.core.organise.entity.Person;

import java.util.List;
import java.util.Map;

/**
 * @author Mr.Zhu
 * @date 2022/8/11 - 11:54
 */

public interface StatisticsDetailService extends BaseService<StatisticsDetail> {
    void statisticsRegister(String formDefinitionId, String registerId, Person person);
}
