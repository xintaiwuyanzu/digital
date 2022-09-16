package com.dr.digital.register.service;

import com.dr.digital.register.entity.Category;
import com.dr.framework.common.entity.TreeNode;
import com.dr.framework.core.organise.entity.Person;

import java.util.List;

public interface CategoryService {

    /**
     * 继承分类
     *
     * @param parentId
     * @param categoryId
     * @param person
     * @return
     */
    String inheritCategory(String parentId, String categoryId, Person person);

    /**
     * 获取分类树
     *
     * @param group
     * @return
     */
    List<TreeNode> selectCategoryTree(String group);

    /**
     * 获取分类
     *
     * @param group
     * @return
     */
    List<Category> selectCategoryByBusinessId(String group);

    /**
     * 根据批次id删除表单分类表
     */


}
