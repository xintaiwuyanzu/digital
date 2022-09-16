package com.dr.digital.manage.impexpscheme.service.imp;

import com.dr.digital.manage.impexpscheme.entity.ImpExpScheme;
import com.dr.digital.manage.impexpscheme.entity.ImpExpSchemeItem;
import com.dr.digital.manage.impexpscheme.entity.ImpExpSchemeItemInfo;
import com.dr.digital.manage.impexpscheme.service.ImpExpSchemeService;
import com.dr.digital.manage.service.impl.BaseYearServiceImpl;
import com.dr.framework.core.orm.sql.Column;
import org.springframework.stereotype.Service;

/**
 * @author caor
 * @date 2020/7/31 19:01
 */
@Service
public class ImpExpSchemeServiceImpl extends BaseYearServiceImpl<ImpExpScheme> implements ImpExpSchemeService {
    @Override
    protected Class getSubTableClass() {
        return ImpExpSchemeItem.class;
    }

    @Override
    protected Column getRelateColumn() {
        return ImpExpSchemeItemInfo.BUSINESSID;
    }
}
