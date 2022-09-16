package com.dr.digital.manage.codingscheme.service;


import com.dr.digital.manage.codingscheme.entity.CodingScheme;
import com.dr.digital.manage.codingscheme.entity.CodingSchemeItem;
import com.dr.framework.common.page.Page;
import com.dr.framework.common.service.BaseService;

import java.util.List;

/**
 * describe
 *
 * @author tzl
 * @date 2020/5/29 16:29
 */
public interface CodingSchemeService extends BaseService<CodingScheme>, com.dr.digital.manage.service.internal.CodingSchemeService {

    Page<CodingScheme> findSchemeByFondId(String fondId, Integer page, Integer size);

    long deleteByIds(String ids);

    List<CodingSchemeItem> findSchemeByBusinessId(String businessId);
}
