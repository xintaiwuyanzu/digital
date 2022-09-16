package com.dr.digital.register.controller;

import com.dr.digital.register.entity.Category;
import com.dr.digital.register.entity.CategoryInfo;
import com.dr.digital.register.service.CategoryService;
import com.dr.digital.util.Constants;
import com.dr.framework.common.controller.BaseController;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.dr.framework.core.web.annotations.Current;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 登记分类树数据
 *
 * @author dr
 */
@RestController
@RequestMapping("api/category")
public class CategoryController extends BaseController<Category> {
    @Autowired
    CategoryService categoryService;

    @Resource
    CommonMapper commonMapper;

    /**
     * 分类集成树
     *
     * @param parentId
     * @param categoryId
     * @param person
     * @return
     */
    @RequestMapping("inheritCategory")
    public ResultEntity inheritCategory(String parentId, String categoryId, @Current Person person) {
        String message = categoryService.inheritCategory(parentId, categoryId, person);
        return result(message);
    }

    /**
     * 获取分类树
     *
     * @param group
     * @return
     */
    @RequestMapping("categoryTree")
    public ResultEntity categoryTree(String group) {
        return ResultEntity.success(categoryService.selectCategoryTree(group));
    }

    /**
     * 返回值校验
     *
     * @param message
     * @return
     */
    public ResultEntity result(String message) {
        if (Constants.SUCCESS.equals(message)) {
            return ResultEntity.success();
        } else {
            return ResultEntity.error(message);
        }
    }

    /**
     * 根据批次id查询分类id
     */
    @RequestMapping("byRegisterIdGetCategoryId")
    public ResultEntity byRegisterIdGetCategoryId(String registerId) {
        SqlQuery<Category> sql = SqlQuery.from(Category.class).equal(CategoryInfo.PARENTID, registerId);
        List<Category> categories = commonMapper.selectByQuery(sql);
        return ResultEntity.success(categories);
    }

}
