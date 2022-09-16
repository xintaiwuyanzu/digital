package com.dr.digital.manage.fournaturescheck.controller;

import com.dr.digital.manage.fournaturescheck.entity.FourNatureSchemeItem;
import com.dr.digital.manage.fournaturescheck.entity.FourNatureSchemeItemInfo;
import com.dr.digital.manage.fournaturescheck.service.FourNatureSchemeItemService;
import com.dr.framework.common.controller.BaseServiceController;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author caor
 * @date 2021-07-28 17:15
 */
@RestController
@RequestMapping({"${common.api-path:/api}/fournatureschemeitem"})
public class FourNatureSchemeItemController extends BaseServiceController<FourNatureSchemeItemService, FourNatureSchemeItem> {
    @Override
    protected SqlQuery<FourNatureSchemeItem> buildPageQuery(HttpServletRequest httpServletRequest, FourNatureSchemeItem fourNatureSchemeItem) {
        return SqlQuery.from(FourNatureSchemeItem.class)
                .equal(FourNatureSchemeItemInfo.FOURNATURESCHEMEID, fourNatureSchemeItem.getFourNatureSchemeId())
                .orderByDesc(FourNatureSchemeItemInfo.CREATEDATE);
    }
}
