package com.dr.digital.manage.service.impl;

import com.dr.digital.manage.service.CodeNameUtil;
import com.dr.framework.common.entity.BaseDescriptionEntity;
import com.dr.framework.common.service.DefaultBaseService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 基础code name service实现类，
 * <p>
 * 同一个businessId的数据的code代码不能重复
 *
 * @author caor
 * @date 2020/7/28 11:31
 */
public abstract class BaseCodeNameServiceImpl<T extends BaseDescriptionEntity<String>> extends DefaultBaseService<T> {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long insert(T entity) {
        //先验证编码是否重复
        validateCode(entity);
        return super.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long updateById(T entity) {
        //先验证编码是否重复
        validateCode(entity, entity.getId());
        return super.updateById(entity);
    }

    /**
     * 判断编码是否重复
     *
     * @param entity
     * @return
     */
    public void validateCode(T entity, String... excludeId) {
        CodeNameUtil.validateCode(commonMapper, getEntityRelation(), entity, excludeId);
    }
    
}
