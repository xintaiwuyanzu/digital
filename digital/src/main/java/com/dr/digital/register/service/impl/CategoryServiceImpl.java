package com.dr.digital.register.service.impl;

import com.dr.digital.register.entity.Category;
import com.dr.digital.register.entity.CategoryInfo;
import com.dr.digital.register.service.CategoryService;
import com.dr.digital.register.service.RegisterService;
import com.dr.digital.util.Constants;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.TreeNode;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class CategoryServiceImpl implements CategoryService {
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    @Autowired
    CommonMapper commonMapper;
    @Autowired
    RegisterService registerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String inheritCategory(String parentId, String categoryId, Person person) {
        if (StringUtils.isEmpty(parentId)) {
            return "父类Id不能为空";
        }
        if (StringUtils.isEmpty(categoryId)) {
            return "类型Id不能为空";
        }
        Category category = selectById(parentId);
        String[] ids = categoryId.split(",");
        Set<String> idSet = new HashSet<>();
        for (String id : ids) {
            inherit(selectById(id), category, person, idSet);
        }
        return Constants.SUCCESS;
    }

    @Override
    public List<TreeNode> selectCategoryTree(String group) {
        Assert.isTrue(!StringUtils.isEmpty(group), "参数不能为空!");
        return getCategoryTree(group);
    }

    private List<TreeNode> getCategoryTree(String group) {
        List<TreeNode> treeNodes = new ArrayList<>();
        List<Category> categories = commonMapper.selectByQuery(SqlQuery.from(Category.class).equal(CategoryInfo.PARENTID, group).orderBy(CategoryInfo.ORDERBY));
        for (Category category : categories) {
            TreeNode treeNode = new TreeNode(category.getId(), category.getName(), category);
            treeNode.setParentId(category.getParentId());
            treeNode.setChildren(getCategoryTree(category.getId()));
            treeNodes.add(treeNode);
        }
        return treeNodes;
    }

    /**
     * 根据父类主键 查询父类数据
     *
     * @param parentId
     * @return
     */
    public Category selectById(String parentId) {
        Category category = commonMapper.selectById(Category.class, parentId);
        return category;
    }

    private void inherit(Category category, Category parentCategory, Person person, Set<String> idSet) {
        String categoryId = category.getId();
        if (idSet.contains(categoryId)) {
            return;
        }
        idSet.add(categoryId);
        Category newCategory = new Category();
        //门类被缓存管着，所以这里初始化一个新的对象
        BeanUtils.copyProperties(category, newCategory);
        //子类重新赋值判断code是否重复，不重复保存
        String uuid = UUID.randomUUID().toString().replace("-", "");
        newCategory.setId(uuid);
        newCategory.setCreateDate(System.currentTimeMillis());
        newCategory.setUpdateDate(System.currentTimeMillis());
        newCategory.setCreatePerson(person.getId());
        newCategory.setUpdatePerson(person.getId());
        newCategory.setParentId(parentCategory.getId());
        newCategory.setBusinessId(parentCategory.getRegisterId());
        newCategory.setBatch_name(parentCategory.getBatch_name());
        newCategory.setRegisterId(parentCategory.getRegisterId());
        commonMapper.insert(newCategory);
    }

    @Override
    public List<Category> selectCategoryByBusinessId(String businessId) {
        SqlQuery sqlQuery = SqlQuery.from(Category.class)
                .equal(CategoryInfo.BUSINESSID,businessId);
        return commonMapper.selectByQuery(sqlQuery);
    }
}
