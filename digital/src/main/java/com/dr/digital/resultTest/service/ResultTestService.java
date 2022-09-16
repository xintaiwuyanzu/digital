package com.dr.digital.resultTest.service;

import com.dr.digital.register.entity.Register;
import com.dr.digital.resultTest.entity.ResultTest;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.digital.resultTest.entity.ResultTestStatistics;
import com.dr.digital.statistics.entity.StatisticsDetail;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.service.BaseService;
import com.dr.framework.core.organise.entity.Person;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.web.annotations.Current;

import java.util.List;
import java.util.Map;

/**
 * @author Mr.Zhu
 * @date 2022/8/18 - 16:49
 */
public interface ResultTestService extends BaseService<ResultTest> {


    ResultEntity resultUpdateType(Person person, Register register);

    ResultEntity expDistribution(String registerId);
    void startResult(String registerId,@Current Person person);

    void resetResultTest(String registerId);

    List<Map> getResultMessage(String registerId);

    List<ResultTestStatistics> resultStatistics(String registerId);

    List<ResultTest> detectionDateInit(String registerId);

    Map percentageInit(String registerId);

    void xmlGenerateFile(String formDefinitionId);

    void xmlFormDataGenerateFile(FormData formDate);
}
