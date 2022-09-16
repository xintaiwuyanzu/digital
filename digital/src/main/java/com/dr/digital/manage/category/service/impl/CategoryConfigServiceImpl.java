package com.dr.digital.manage.category.service.impl;

import com.dr.digital.enums.CategoryType;
import com.dr.digital.manage.category.entity.CategoryConfig;
import com.dr.digital.manage.category.entity.CategoryConfigInfo;
import com.dr.digital.manage.category.service.CategoryConfigService;
import com.dr.digital.manage.model.query.CategoryFormQuery;
import com.dr.digital.manage.service.impl.BaseYearServiceImpl;
import com.dr.digital.util.Constants;
import com.dr.framework.common.form.core.service.FormDefinitionService;
import com.dr.framework.common.form.engine.model.core.FormModel;
import com.dr.framework.core.orm.sql.Column;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：
 * TODO 逻辑待验证
 *
 * @author dr
 * @date 2020/6/9 8:47
 */
@Service
public class CategoryConfigServiceImpl extends BaseYearServiceImpl<CategoryConfig> implements CategoryConfigService {
    @Autowired
    FormDefinitionService formDefinitionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long delete(String ids) {
        Assert.isTrue(!StringUtils.isEmpty(ids), "主键不能为空！");
        SqlQuery<CategoryConfig> sqlQuery = SqlQuery.from(CategoryConfig.class, false).column(CategoryConfigInfo.ID).in(CategoryConfigInfo.ID, ids.split(","));
        return super.delete(sqlQuery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByCategoryId(String categoryId) {
        delete(SqlQuery.from(CategoryConfig.class).equal(CategoryConfigInfo.BUSINESSID, categoryId));
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<CategoryConfig> selectByCategoryId(String categoryId) {
        Assert.isTrue(!StringUtils.isEmpty(categoryId), "分类Id不能为空！");
        return selectList(SqlQuery.from(CategoryConfig.class).equal(CategoryConfigInfo.BUSINESSID, categoryId));
    }

    /**
     * 根据查询条件获取分类表单配置
     * TODO 继承
     *
     * @param categoryFormQuery
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryConfig getCategoryForms(CategoryFormQuery categoryFormQuery) {
        CategoryConfig categoryConfig = null;
        if (categoryFormQuery.getYear() != null) {
            categoryConfig = commonMapper.selectOneByQuery(SqlQuery.from(CategoryConfig.class)
                    .equal(CategoryConfigInfo.BUSINESSID, categoryFormQuery.getCategoryId())
                    .lessThanEqual(CategoryConfigInfo.STARTYEAR, categoryFormQuery.getYear())
                    .greaterThanEqual(CategoryConfigInfo.ENDYEAR, categoryFormQuery.getYear()));
        }
        if (categoryConfig == null) {
            categoryConfig = commonMapper.selectOneByQuery(SqlQuery.from(CategoryConfig.class)
                    .equal(CategoryConfigInfo.BUSINESSID, categoryFormQuery.getCategoryId())
                    .equal(CategoryConfigInfo.ISDEFAULT, Constants.YES));
        }
        return categoryConfig;
    }

    @Override
    public List<FormModel> getCategoryForm(CategoryFormQuery categoryFormQuery) {
        List<FormModel> formModelList = new ArrayList<>();
        CategoryConfig categoryConfig = getCategoryForms(categoryFormQuery);
        if (categoryConfig != null) {
            String proFormDefinitionId = null;
            String arcFormDefinitionId = null;
            String fileFormDefinitionId = null;
            CategoryType type = CategoryType.from(categoryFormQuery.getCategoryCode());
            Assert.notNull(type, "错误的档案类型：" + categoryFormQuery.getCategoryType());
            switch (type) {
                case ARC:
                    arcFormDefinitionId = categoryConfig.getArcFormId();
                    fileFormDefinitionId = categoryConfig.getFileFormId();
                    break;
                case PRO:
                    proFormDefinitionId = categoryConfig.getProFormId();
                    arcFormDefinitionId = categoryConfig.getArcFormId();
                    fileFormDefinitionId = categoryConfig.getFileFormId();
                    break;
                case FILE:
                case BOX:
                    fileFormDefinitionId = categoryConfig.getFileFormId();
                    break;
                default:
                    break;
            }
            if (!StringUtils.isEmpty(proFormDefinitionId)) {
                formModelList.add(formDefinitionService.selectFormDefinitionById(proFormDefinitionId));
            }
            if (!StringUtils.isEmpty(arcFormDefinitionId)) {
                formModelList.add(formDefinitionService.selectFormDefinitionById(arcFormDefinitionId));
            }
            if (!StringUtils.isEmpty(fileFormDefinitionId)) {
                formModelList.add(formDefinitionService.selectFormDefinitionById(fileFormDefinitionId));
            }
        }
        return formModelList;
    }

    @Override
    protected Class getSubTableClass() {
        return null;
    }

    @Override
    protected Column getRelateColumn() {
        return null;
    }

    @Override
    public long insert(CategoryConfig entity) {
        bindFormName(entity);
        return super.insert(entity);
    }

    @Override
    public long updateById(CategoryConfig entity) {
        bindFormName(entity);
        return super.updateById(entity);
    }

    @Override
    public List<CategoryConfig> selectByBusinessId(String businessId) {
        Assert.isTrue(!StringUtils.isEmpty(businessId), "业务Id不能为空！");
        return selectList(SqlQuery.from(CategoryConfig.class).equal(CategoryConfigInfo.BUSINESSID, businessId));
    }

    @Override
    public CategoryConfig selectCategoryConfigOne(String formDefinitionId) {
        CategoryConfig categoryConfig = commonMapper.selectOneByQuery(SqlQuery.from(CategoryConfig.class).equal(CategoryConfigInfo.FILEFORMID, formDefinitionId));
        return categoryConfig;
    }

    /**
     * 绑定表单名称
     *
     * @param config
     */
    private void bindFormName(CategoryConfig config) {
        if (!StringUtils.isEmpty(config.getProFormId())) {
            FormModel form = formDefinitionService.selectFormDefinitionById(config.getProFormId());
            config.setProFormName(form.getFormName());
        }
        if (!StringUtils.isEmpty(config.getArcFormId())) {
            FormModel form = formDefinitionService.selectFormDefinitionById(config.getArcFormId());
            config.setArcFormName(form.getFormName());
        }
        if (!StringUtils.isEmpty(config.getFileFormId())) {
            FormModel form = formDefinitionService.selectFormDefinitionById(config.getFileFormId());
            config.setFileFormName(form.getFormName());
        }
    }

}
