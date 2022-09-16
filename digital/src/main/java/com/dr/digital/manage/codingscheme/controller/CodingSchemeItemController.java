package com.dr.digital.manage.codingscheme.controller;

import com.dr.digital.manage.codingscheme.entity.CodingSchemeItem;
import com.dr.digital.manage.codingscheme.entity.CodingSchemeItemInfo;
import com.dr.digital.manage.codingscheme.service.CodingSchemeItemService;
import com.dr.framework.common.controller.BaseServiceController;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * describe
 *
 * @author dr
 * @date 2020/5/29 17:13
 */
@ResponseBody
@RestController
@RequestMapping("/api/codingscheme/schemeitem")
public class CodingSchemeItemController extends BaseServiceController<CodingSchemeItemService, CodingSchemeItem> {
    /**
     * @param httpServletRequest
     * @param entity
     * @return
     */
    @Override
    protected SqlQuery<CodingSchemeItem> buildPageQuery(HttpServletRequest httpServletRequest, CodingSchemeItem entity) {
        SqlQuery<CodingSchemeItem> sqlQuery = SqlQuery.from(CodingSchemeItem.class).orderBy(CodingSchemeItemInfo.ORDERBY);
        if (!StringUtils.isEmpty(entity.getBusinessId())) {
            sqlQuery.equal(CodingSchemeItemInfo.BUSINESSID, entity.getBusinessId());
        }
        return sqlQuery;
    }

}
