package com.dr.digital.template.controller;


import com.dr.digital.register.entity.RegisterInfo;
import com.dr.digital.template.entity.Template;
import com.dr.digital.template.entity.TemplateInfo;
import com.dr.framework.common.controller.BaseController;
import com.dr.framework.common.controller.BaseServiceController;
import com.dr.framework.common.entity.IdEntity;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/api/template")
public class templateController extends BaseController<Template> {

    @Override
    protected void onBeforeDelete(HttpServletRequest request, SqlQuery<Template> sqlQuery, Template entity) {
        sqlQuery.equal(TemplateInfo.ID, entity.getId());
        super.onBeforeDelete(request, sqlQuery, entity);
    }
}
