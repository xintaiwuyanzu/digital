package com.dr.digital.manage.fournaturescheck.controller;

import com.dr.digital.manage.fournaturescheck.entity.FourNatureSchemeItemMethod;
import com.dr.digital.manage.fournaturescheck.entity.FourNatureSchemeItemMethodInfo;
import com.dr.digital.manage.fournaturescheck.service.FourNatureSchemeItemMethodService;
import com.dr.framework.common.controller.BaseServiceController;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author caor
 * @date 2021-07-28 17:17
 */
@RestController
@RequestMapping({"${common.api-path:/api}/fournatureschemeitemmethod"})
public class FourNatureSchemeItemMethodController extends BaseServiceController<FourNatureSchemeItemMethodService, FourNatureSchemeItemMethod> {
    @Override
    protected SqlQuery<FourNatureSchemeItemMethod> buildPageQuery(HttpServletRequest httpServletRequest, FourNatureSchemeItemMethod fourNatureSchemeItemMethod) {
        return SqlQuery.from(FourNatureSchemeItemMethod.class).equal(FourNatureSchemeItemMethodInfo.FOURNATURESCHEMEITEMID, fourNatureSchemeItemMethod.getFourNatureSchemeItemId()).orderByDesc(FourNatureSchemeItemMethodInfo.CREATEDATE);
    }
}
