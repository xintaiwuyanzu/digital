package com.dr.digital.manage.impexpscheme.service.imp;

import com.dr.digital.manage.impexpscheme.entity.ImpExpSchemeItem;
import com.dr.digital.manage.impexpscheme.entity.ImpExpSchemeItemInfo;
import com.dr.digital.manage.impexpscheme.service.ImpExpSchemeItemService;
import com.dr.digital.manage.service.impl.BaseCodeNameServiceImpl;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author caor
 * @date 2020/7/31 19:42
 */
@Service
public class ImpExpSchemeItemServiceImpl extends BaseCodeNameServiceImpl<ImpExpSchemeItem> implements ImpExpSchemeItemService {

    @Override
    public long delete(String ids) {
        SqlQuery<ImpExpSchemeItem> sqlQuery = SqlQuery.from(ImpExpSchemeItem.class);
        if (!StringUtils.isEmpty(ids)) {
            sqlQuery = sqlQuery.in(ImpExpSchemeItemInfo.ID, ids.split(","));
        }
        return delete(sqlQuery);
    }
}
