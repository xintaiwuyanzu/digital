package com.dr.digital.manage.task.service.impl;

import com.dr.digital.manage.impexpscheme.service.ImpExpSchemeItemService;
import com.dr.digital.manage.impexpscheme.service.DataParser;
import com.dr.digital.manage.service.impl.DataParserComposite;
import com.dr.digital.manage.task.entity.AbstractBatchDetailEntity;
import com.dr.framework.common.file.autoconfig.CommonFileConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * 带有数据转换的service
 *
 * @author dr
 */
public abstract class BaseDataParserBatchDetailService<D extends AbstractBatchDetailEntity> extends BaseBatchDetailServiceImpl<D> {
    protected DataParser dataParser;
    @Autowired
    protected CommonFileConfig commonFileConfig;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected ImpExpSchemeItemService impExpSchemeItemService;

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        dataParser = new DataParserComposite(applicationContext.getBeansOfType(DataParser.class).values());
    }
}

