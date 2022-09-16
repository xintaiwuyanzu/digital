package com.dr.digital.manage.impexpscheme.controller;

import com.dr.digital.manage.impexpscheme.entity.ImpExpScheme;
import com.dr.digital.manage.impexpscheme.entity.ImpExpSchemeInfo;
import com.dr.digital.manage.impexpscheme.service.ImpExpSchemeService;
import com.dr.framework.common.controller.BaseServiceController;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 导入导出方案
 *
 * @author dr
 * @date 2020/7/31 18:59
 */
@RestController
@RequestMapping("api/impexpscheme")
public class ImpExpSchemeController extends BaseServiceController<ImpExpSchemeService, ImpExpScheme> {

    @Override
    protected SqlQuery<ImpExpScheme> buildPageQuery(HttpServletRequest httpServletRequest, ImpExpScheme impExpScheme) {
        SqlQuery<ImpExpScheme> sqlQuery = SqlQuery.from(ImpExpScheme.class);
        String name = httpServletRequest.getParameter("name");
        if (!StringUtils.isEmpty(name)) {
            sqlQuery.like(ImpExpSchemeInfo.NAME, name);
        }
        return sqlQuery;
    }

    @Override
    protected SqlQuery<ImpExpScheme> buildDeleteQuery(HttpServletRequest request, ImpExpScheme entity) {
        String aIds = request.getParameter("aIds");
        Assert.isTrue(!StringUtils.isEmpty(aIds), "删除参数不能为空!");
        return SqlQuery.from(ImpExpScheme.class, false)
                .column(ImpExpSchemeInfo.ID)
                .in(ImpExpSchemeInfo.ID, aIds.split(","));
    }

    @Override
    public ResultEntity<Boolean> delete(HttpServletRequest request, ImpExpScheme entity) {
        String aIds = request.getParameter("aIds");
        Assert.isTrue(!StringUtils.isEmpty(aIds), "删除参数不能为空!");
        SqlQuery<ImpExpScheme> sqlQuery = SqlQuery.from(ImpExpScheme.class, false)
                .column(ImpExpSchemeInfo.ID)
                .in(ImpExpSchemeInfo.ID, aIds.split(","));
        return ResultEntity.success(service.delete(sqlQuery) > 0L);
    }
}
