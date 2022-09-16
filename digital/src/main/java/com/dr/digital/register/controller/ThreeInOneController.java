package com.dr.digital.register.controller;

import com.dr.digital.manage.model.query.ArchiveDataQuery;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.register.service.ThreeInOneService;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.web.annotations.Current;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/threeInOne")
public class ThreeInOneController {
    @Autowired
    ThreeInOneService threeInOneService;

    /**
     * 新的数据清洗接口（拆分，识别，拆件）
     *
     * @param request
     * @param query
     * @param registerId
     * @param queryContent
     * @param person
     * @return
     */
    @RequestMapping("dataCleaning")
    public ResultEntity dataCleaning(HttpServletRequest request,
                                     BaseQuery query,
                                     String registerId,
                                     @RequestParam(name = ArchiveDataQuery.QUERY_KEY, required = false) String queryContent,
                                     @Current Person person) {
        query.parseQuery(queryContent);
        threeInOneService.dataCleaning(query, registerId, person);
        return ResultEntity.success();
    }

}
