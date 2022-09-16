package com.dr.digital.resultTest.controller;

import com.dr.digital.register.entity.Register;
import com.dr.digital.register.service.RegisterService;
import com.dr.digital.resultTest.entity.ResultTest;
import com.dr.digital.resultTest.service.ResultTestService;
import com.dr.digital.statistics.entity.Statistics;
import com.dr.digital.statistics.service.StatisticsService;
import com.dr.digital.util.XmlUtil;
import com.dr.framework.common.controller.BaseServiceController;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.dr.framework.core.web.annotations.Current;
import org.springframework.beans.factory.annotation.Autowired;
import com.dr.framework.core.web.annotations.Current;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mr.Zhu
 * @date 2022/8/18 - 16:49
 */
@RestController
@RequestMapping("/api/resultTest")
public class ResultTestController extends BaseServiceController<ResultTestService, ResultTest> {

    @Autowired
    ResultTestService resultTestService;

    @Autowired
    RegisterService registerService;
    @RequestMapping("/xmlGenerateFile")
    public ResultEntity xmlGenerateFile(String registerId){
        String formDefinitionId = registerService.selectById(registerId).getFormDefinitionId();
        resultTestService.xmlGenerateFile(formDefinitionId);
        return ResultEntity.success();
    }

    @RequestMapping("/startResult")
    public ResultEntity startResult(String registerId,@Current Person person){
        resultTestService.startResult(registerId,person);
        return ResultEntity.success();
    }
    @RequestMapping("/resetResultTest")
    public ResultEntity resetResultTest(String registerId){
        resultTestService.resetResultTest(registerId);
        return ResultEntity.success();
    }
    @RequestMapping("/getResultMessage")
    public ResultEntity resultMessage(String registerId){
        return ResultEntity.success(resultTestService.getResultMessage(registerId));
    }
    @RequestMapping("/resultStatistics")
    public ResultEntity resultStatistics(String registerId){
        return ResultEntity.success(resultTestService.resultStatistics(registerId));
    }
    @RequestMapping("/detectionDateInit")
    public ResultEntity detectionDateInit(String registerId){
        return ResultEntity.success(resultTestService.detectionDateInit(registerId));
    }
    @RequestMapping("/percentageInit")
    public ResultEntity percentageInit(String registerId){
        return ResultEntity.success(resultTestService.percentageInit(registerId));
    }

    @Override
    protected SqlQuery<ResultTest> buildPageQuery(HttpServletRequest httpServletRequest, ResultTest resultTest) {
        return null;
    }

    /**
     *退回
     */
    @RequestMapping("resultUpdateType")
    public ResultEntity resultUpdateType(@Current Person person, Register register){
       return resultTestService.resultUpdateType(person,register);
    }

    /**
     * 导出
     * @return
     */
    @RequestMapping("resultExcel")
    public ResultEntity resultExcel(String  registerId){
        ResultEntity resultEntity = resultTestService.expDistribution(registerId);
        return resultEntity;
    }

}

