package com.dr.digital.statistics.controller;

import com.dr.digital.register.entity.Register;
import com.dr.digital.register.entity.RegisterInfo;
import com.dr.digital.register.service.RegisterService;
import com.dr.digital.statistics.entity.StatisticsDetail;
import com.dr.digital.statistics.entity.StatisticsDetailInfo;
import com.dr.digital.statistics.service.StatisticsDetailService;
import com.dr.framework.common.controller.BaseServiceController;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.core.organise.entity.Organise;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.dr.framework.core.web.annotations.Current;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static com.dr.framework.common.controller.BaseController.getOrganise;

/**
 * @author Mr.Zhu
 * @date 2022/8/11 - 16:13
 */
@RestController
@RequestMapping("/api/statisticsDetail")
public class StatisticsDetailController extends BaseServiceController<StatisticsDetailService, StatisticsDetail> {

    @Autowired
    RegisterService registerService;

    @RequestMapping("statisticsRegister")
    public ResultEntity statisticsRegister(String formDefinitionId, @Current Person person){
        if (StringUtils.isEmpty(formDefinitionId)){
            return ResultEntity.error("表单id为空");
        }
        String registerId = registerService.selectOne(SqlQuery.from(Register.class).equal(RegisterInfo.FORMDEFINITIONID,formDefinitionId)).getId();
        this.service.statisticsRegister(formDefinitionId, registerId,person);
        return ResultEntity.success();
    }
    @Override
    protected SqlQuery<StatisticsDetail> buildPageQuery(HttpServletRequest httpServletRequest, StatisticsDetail statisticsDetail) {
        //查看批次详情的人，如果不是超级管理员或是批次创建者，只能看到办理人自己操作过的数据
        //当前登陆人信息
        Person person = getUserLogin(httpServletRequest);
        //批次创建者
        String receiver = registerService.selectById(statisticsDetail.getRegisterId()).getReceiver();
        SqlQuery<StatisticsDetail> sqlQuery = SqlQuery.from(StatisticsDetail.class)
                .equal(StatisticsDetailInfo.REGISTERID, statisticsDetail.getRegisterId())
                .orderBy(StatisticsDetailInfo.ORDERBY);
        if (!person.getUserName().equals("超级管理员")||!person.getUserName().equals(receiver)){
            sqlQuery.equal(StatisticsDetailInfo.HANDLEDBY,person.getUserName());
        }
        return sqlQuery;
    }
}
