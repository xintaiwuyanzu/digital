package com.dr.digital.manage.service.impl;

import com.dr.digital.manage.service.CodeNameUtil;
import com.dr.framework.common.entity.BaseDescriptionEntity;
import com.dr.framework.common.service.PermissionResourceService;
import com.dr.framework.core.security.bo.PermissionResource;
import org.springframework.transaction.annotation.Transactional;

/**
 * 档案里面全宗和门类分类属于权限资源
 *
 * @author dr
 * @see PermissionResource
 */
public abstract class BasePermissionResourceService<T extends PermissionResource> extends PermissionResourceService<T> {
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
        if (entity instanceof BaseDescriptionEntity) {
            CodeNameUtil.validateCode(commonMapper, getEntityRelation(), (BaseDescriptionEntity) entity, excludeId);
        }
    }

}
