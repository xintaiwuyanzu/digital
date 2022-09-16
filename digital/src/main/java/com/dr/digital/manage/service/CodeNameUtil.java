package com.dr.digital.manage.service;

import com.dr.digital.manage.model.entity.BaseBusinessIdEntity;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.BaseDescriptionEntity;
import com.dr.framework.common.entity.DescriptionEntity;
import com.dr.framework.common.entity.IdEntity;
import com.dr.framework.core.orm.jdbc.Relation;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 辅助工具类
 *
 * @author dr
 */
public class CodeNameUtil {
    /**
     * 校验编码和业务外键
     *
     * @param commonMapper
     * @param entityRelation
     * @param entity
     * @param excludeId
     */
    public static void validateCode(CommonMapper commonMapper, Relation entityRelation, BaseDescriptionEntity entity, String... excludeId) {
        SqlQuery sqlQuery = SqlQuery.from(entity.getClass())
                .equal(entityRelation.getColumn(DescriptionEntity.CODE_COLUMN_NAME), entity.getCode());
        if (entity instanceof BaseBusinessIdEntity) {
            BaseBusinessIdEntity businessIdEntity = (BaseBusinessIdEntity) entity;
            Assert.isTrue(!StringUtils.isEmpty(businessIdEntity.getBusinessId()), "业务外键不能为空");
            sqlQuery.equal(entityRelation.getColumn(BaseBusinessIdEntity.BUSINESS_ID_COLUMN_NAME), businessIdEntity.getBusinessId());
        }
        if (excludeId.length > 0) {
            sqlQuery.notIn(entityRelation.getColumn(IdEntity.ID_COLUMN_NAME), excludeId);
        }
        Assert.isTrue(commonMapper.countByQuery(sqlQuery) < 1, "编码重复");
    }
}
