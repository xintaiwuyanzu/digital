package com.dr.digital.manage.impexpscheme.controller;

import com.dr.digital.manage.impexpscheme.entity.ImpExpSchemeItem;
import com.dr.digital.manage.impexpscheme.entity.ImpExpSchemeItemInfo;
import com.dr.digital.manage.impexpscheme.service.ImpExpSchemeItemService;
import com.dr.framework.common.controller.BaseServiceController;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author caor
 * @date 2020/7/31 18:59
 */
@RestController
@RequestMapping("api/impexpschemeitem")
public class ImpExpSchemeItemController extends BaseServiceController<ImpExpSchemeItemService, ImpExpSchemeItem> {

    @Override
    protected SqlQuery<ImpExpSchemeItem> buildPageQuery(HttpServletRequest httpServletRequest, ImpExpSchemeItem entity) {
        SqlQuery<ImpExpSchemeItem> sqlQuery = SqlQuery.from(ImpExpSchemeItem.class);
        if (!StringUtils.isEmpty(entity.getName())) {
            sqlQuery.like(ImpExpSchemeItemInfo.NAME, entity.getName());
        }
        sqlQuery.equal(ImpExpSchemeItemInfo.BUSINESSID, entity.getBusinessId());
        sqlQuery.orderBy(ImpExpSchemeItemInfo.ORDERBY);
        return sqlQuery;
    }

    @Override
    public ResultEntity<Boolean> delete(HttpServletRequest request, ImpExpSchemeItem entity) {
        return ResultEntity.success(service.delete(request.getParameter("ids")) > 0L);
    }
}
