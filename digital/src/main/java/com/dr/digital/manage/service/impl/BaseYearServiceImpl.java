package com.dr.digital.manage.service.impl;

import com.dr.digital.manage.model.entity.BaseYearEntity;
import com.dr.framework.common.entity.DescriptionEntity;
import com.dr.framework.common.entity.IdEntity;
import com.dr.framework.common.entity.StatusEntity;
import com.dr.framework.common.service.CommonService;
import com.dr.framework.common.service.DefaultBaseService;
import com.dr.framework.core.orm.jdbc.Relation;
import com.dr.framework.core.orm.sql.Column;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * TODO 禁用的不大好处理，不用状态字段了
 * 年度范围相关的实体类通用service
 *
 * @author dr
 */
public abstract class BaseYearServiceImpl<T extends BaseYearEntity> extends DefaultBaseService<T> {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long insert(T entity) {
        //校验内容是否正确
        validateAndBind(entity);
        //保存数据
        long count = super.insert(entity);
        //如果是默认的话，更新其他的为非默认
        if (entity.isDefault()) {
            count += changeDefault(entity.getCode(), entity.getId());
        } else {
            //如果不是默认，但是同一code只有一条数据，强制改成默认
            Relation relation = getEntityRelation();
            if (
                    commonMapper.countByQuery(
                            SqlQuery.from(relation)
                                    .equal(entityRelation.getColumn(DescriptionEntity.CODE_COLUMN_NAME), entity.getCode())
                    ) == 1
            ) {
                commonMapper.updateIgnoreNullByQuery(
                        SqlQuery.from(entityRelation)
                                .set(entityRelation.getColumn(BaseYearEntity.IS_DEFAULT_COLUMN_NAME), false)
                                .equal(entityRelation.getColumn(IdEntity.ID_COLUMN_NAME), entity.getId())
                );
                count++;
            }
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long updateById(T entity) {
        //校验内容是否正确
        validateAndBind(entity, entity.getId());
        T old = selectOne(
                SqlQuery.from(getEntityClass())
                        .equal(getEntityRelation().getColumn(IdEntity.ID_COLUMN_NAME), entity.getId())
        );
        Assert.isTrue(old.getCode().equals(entity.getCode()), "编码不能修改！");
        long count = 0;
        if (old.isDefault()) {
            Assert.isTrue(entity.isDefault(), "不能修改默认配置为非默认！");
        } else {
            if (entity.isDefault()) {
                count += changeDefault(entity.getCode(), entity.getId());
            }
        }
        CommonService.bindCreateInfo(entity);
        count += commonMapper.updateIgnoreNullById(entity);
        return count;
    }

    @Transactional(rollbackFor = Exception.class)
    public long deleteByIds(String ids) {
        Assert.isTrue(!StringUtils.isEmpty(ids), "删除主键不能为空！");
        String[] idArr = ids.split(",");
        Relation relation = getEntityRelation();
        SqlQuery sqlQuery = SqlQuery.from(relation, false)
                .column(relation.getColumn(IdEntity.ID_COLUMN_NAME))
                .in(relation.getColumn(IdEntity.ID_COLUMN_NAME), idArr);
        return delete(sqlQuery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long delete(SqlQuery<T> sqlQuery) {
        Assert.isTrue(sqlQuery.hasWhere(), "删除条件不能为空！");
        Assert.isTrue(
                !commonMapper.existsByQuery(
                        SqlQuery.from(getEntityClass())
                                .equal(getEntityRelation().getColumn(BaseYearEntity.IS_DEFAULT_COLUMN_NAME), true)
                                .in(getEntityRelation().getColumn(IdEntity.ID_COLUMN_NAME), sqlQuery)
                )
                , "默认配置项不能删除！"
        );

        //删除从表
        Class subTableClass = getSubTableClass();
        //删除主表
        long count = 0L;
        if (subTableClass != null) {
            count += commonMapper.deleteByQuery(
                    SqlQuery.from(subTableClass).
                            in(getRelateColumn(), sqlQuery)
            );
        }
        count += super.delete(sqlQuery);
        return count;
    }

    /**
     * 获取关联表class
     *
     * @return
     */
    protected abstract Class getSubTableClass();

    /**
     * 获取关联表关联列
     *
     * @return
     */
    protected abstract Column getRelateColumn();

    /**
     * 设置默认
     *
     * @param code
     * @param id
     * @return
     */
    protected long changeDefault(String code, String id) {
        Assert.isTrue(!StringUtils.isEmpty(code), "编码不能为空！");
        Assert.isTrue(!StringUtils.isEmpty(id), "主键不能为空！");
        Assert.isTrue(exists(id), "指定的配置项不存在！");
        Relation entityRelation = getEntityRelation();
        //设置指定的Id为默认
        long count = commonMapper.updateIgnoreNullByQuery(
                SqlQuery.from(getEntityClass())
                        .set(entityRelation.getColumn(BaseYearEntity.IS_DEFAULT_COLUMN_NAME), true)
                        .equal(entityRelation.getColumn(IdEntity.ID_COLUMN_NAME), id)
        );
        //设置相同编码的其他数据为非默认
        count += commonMapper.updateIgnoreNullByQuery(
                SqlQuery.from(getEntityClass())
                        .set(entityRelation.getColumn(BaseYearEntity.IS_DEFAULT_COLUMN_NAME), false)
                        .equal(entityRelation.getColumn(DescriptionEntity.CODE_COLUMN_NAME), code)
                        .notEqual(entityRelation.getColumn(IdEntity.ID_COLUMN_NAME), id)
        );
        return count;
    }

    /**
     * 校验并且绑定默认信息
     *
     * @param yearEntity
     * @param excludeId  排除指定的数据，更新的时候用到
     */
    protected void validateAndBind(T yearEntity, String... excludeId) {
        Assert.isTrue(yearEntity != null, "对象不能为空！");
        Assert.isTrue(!StringUtils.isEmpty(yearEntity.getCode()), "编码不能为空！");
        Assert.isTrue(yearEntity.getStartYear() >= 0, "开始年度格式不正确！");
        Assert.isTrue(yearEntity.getEndYear() >= 0, "结束年度格式不正确！");
        boolean start = yearEntity.getStartYear() >= 0;
        boolean end = yearEntity.getEndYear() >= 0;
        if (start) {
            Assert.isTrue(
                    getByCodeAndYear(yearEntity.getCode(), yearEntity.getStartYear(), excludeId) == null,
                    "已存在开始年度的配置"
            );
            //如果开始时间大于0
            if (end) {
                //开始结束都有明确的年度，说明是中间的配置
                Assert.isTrue(
                        getByCodeAndYear(yearEntity.getCode(), yearEntity.getEndYear(), excludeId) == null,
                        "已存在结束年度的配置"
                );
            } else {
                //开始大于0，结束等于0，说明是最后一个配置项
                Assert.isTrue(
                        getZeroByCode(yearEntity.getCode(), "endYear", excludeId) == null,
                        "已存在结束年度为0的配置"
                );
            }
        } else {
            Assert.isTrue(end, "开始年度和结束年度至少有一个大于0");
            //开始为0，结束为指定年度，说明是最开始的设置
            Assert.isTrue(
                    getZeroByCode(yearEntity.getCode(), "startYear", excludeId) == null,
                    "已存在开始年度为0的配置"
            );
        }
        if (StringUtils.isEmpty(yearEntity.getStatus())) {
            yearEntity.setStatus(StatusEntity.STATUS_ENABLE_STR);
        }
    }

    /**
     * 获取指定编码指定列
     * 是0的配置项
     *
     * @param code
     * @param columnName
     * @param excludeId
     * @return
     */
    protected T getZeroByCode(String code, String columnName, String... excludeId) {
        Assert.isTrue(!StringUtils.isEmpty(code), "字典分组编码不能为空！");
        Relation entityRelation = getEntityRelation();
        SqlQuery<T> sqlQuery = SqlQuery.from(getEntityClass())
                .equal(entityRelation.getColumn(DescriptionEntity.CODE_COLUMN_NAME), code)
                .equal(entityRelation.getColumn(StatusEntity.STATUS_COLUMN_KEY), StatusEntity.STATUS_ENABLE_STR)
                .equal(entityRelation.getColumn(columnName), 0);

        if (excludeId.length > 0) {
            sqlQuery.notIn(entityRelation.getColumn(IdEntity.ID_COLUMN_NAME), excludeId);
        }
        return commonMapper.selectOneByQuery(sqlQuery);
    }

    /**
     * 根据编码获取默认的分组
     *
     * @param code
     * @param excludeId 排除指定的数据，更新的时候用到
     * @return
     */
    protected T getDefaultByCode(String code, String... excludeId) {
        Assert.isTrue(!StringUtils.isEmpty(code), "字典分组编码不能为空！");
        Relation entityRelation = getEntityRelation();

        SqlQuery<T> sqlQuery = SqlQuery.from(getEntityClass())
                .equal(entityRelation.getColumn(DescriptionEntity.CODE_COLUMN_NAME), code)
                .equal(entityRelation.getColumn(StatusEntity.STATUS_COLUMN_KEY), StatusEntity.STATUS_ENABLE_STR)
                .equal(entityRelation.getColumn(BaseYearEntity.IS_DEFAULT_COLUMN_NAME), true);
        if (excludeId.length > 0) {
            sqlQuery.notIn(entityRelation.getColumn(IdEntity.ID_COLUMN_NAME), excludeId);
        }
        return commonMapper.selectOneByQuery(sqlQuery);
    }

    /**
     * 根据编码和年度查询指定的字典分组
     *
     * @param code
     * @param year
     * @param excludeId 排除指定的数据，更新的时候用到
     * @return
     */
    protected T getByCodeAndYear(String code, Integer year, String... excludeId) {
        Assert.isTrue(!StringUtils.isEmpty(code), "字典分组编码不能为空！");
        Assert.isTrue(year != null, "年度不能为空！");
        Relation entityRelation = getEntityRelation();
        SqlQuery<T> sqlQuery = SqlQuery.from(getEntityClass())
                .equal(entityRelation.getColumn(DescriptionEntity.CODE_COLUMN_NAME), code)
                .equal(entityRelation.getColumn(StatusEntity.STATUS_COLUMN_KEY), StatusEntity.STATUS_ENABLE_STR)
                .lessThanEqual(entityRelation.getColumn("startYear"), year)
                .greaterThanEqual(entityRelation.getColumn("endYear"), year);
        if (excludeId.length > 0) {
            sqlQuery.notIn(entityRelation.getColumn(IdEntity.ID_COLUMN_NAME), excludeId);
        }
        return commonMapper.selectOneByQuery(sqlQuery);
    }

    /**
     * 根据编码和时间查询字典分组，
     * 或者默认分组
     *
     * @param code
     * @param year
     * @param excludeId 排除指定的数据，更新的时候用到
     * @return
     */
    protected T getArchiveGroupOrDefault(String code, Integer year, String... excludeId) {
        if (year == null) {
            return getDefaultByCode(code, excludeId);
        } else {
            return getByCodeAndYear(code, year, excludeId);
        }
    }

}
