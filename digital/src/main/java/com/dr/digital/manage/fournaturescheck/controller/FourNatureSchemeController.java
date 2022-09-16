package com.dr.digital.manage.fournaturescheck.controller;

import com.dr.digital.manage.fournaturescheck.entity.FourNatureScheme;
import com.dr.digital.manage.fournaturescheck.entity.FourNatureSchemeInfo;
import com.dr.digital.manage.fournaturescheck.service.FourNatureSchemeService;
import com.dr.framework.common.controller.BaseServiceController;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author caor
 * @date 2021-07-28 17:13
 */
@RestController
@RequestMapping({"${common.api-path:/api}/fournaturescheme"})
public class FourNatureSchemeController extends BaseServiceController<FourNatureSchemeService, FourNatureScheme> {
    @Override
    protected SqlQuery<FourNatureScheme> buildPageQuery(HttpServletRequest httpServletRequest, FourNatureScheme fourNatureScheme) {
        return SqlQuery.from(FourNatureScheme.class)
                .equal(FourNatureSchemeInfo.BUSINESSID, fourNatureScheme.getBusinessId())
                .orderByDesc(FourNatureSchemeInfo.CREATEDATE);
    }
}
