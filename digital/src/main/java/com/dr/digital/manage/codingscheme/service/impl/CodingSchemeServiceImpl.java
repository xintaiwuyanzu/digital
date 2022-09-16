package com.dr.digital.manage.codingscheme.service.impl;

import com.dr.digital.manage.codingscheme.entity.CodingScheme;
import com.dr.digital.manage.codingscheme.entity.CodingSchemeInfo;
import com.dr.digital.manage.codingscheme.entity.CodingSchemeItem;
import com.dr.digital.manage.codingscheme.entity.CodingSchemeItemInfo;
import com.dr.digital.manage.codingscheme.service.CodingSchemeItemService;
import com.dr.digital.manage.codingscheme.service.CodingSchemeService;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.service.impl.BaseYearServiceImpl;
import com.dr.digital.register.entity.Category;
import com.dr.digital.register.entity.CategoryInfo;
import com.dr.digital.register.service.CategoryService;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.form.core.service.FormDataService;
import com.dr.framework.common.page.Page;
import com.dr.framework.core.orm.sql.Column;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO 大段没用的代码
 * describe
 * 编码方案主表
 *
 * @author tzl
 * @date 2020/5/29 16:34
 */
@Service
public class CodingSchemeServiceImpl extends BaseYearServiceImpl<CodingScheme> implements CodingSchemeService {
    @Autowired
    CategoryService categoryService;
    @Autowired
    CodingSchemeItemService codingSchemeItemService;
    @Autowired
    FormDataService formDataService;

    @Override
    public Page<CodingScheme> findSchemeByFondId(String fondId, Integer index, Integer size) {
        List<Category> categories = commonMapper.selectByQuery(SqlQuery.from(Category.class)
                .equal(CategoryInfo.REGISTERID, fondId));
        List<String> businessIdList = categories.stream().map(Category::getId).collect(Collectors.toList());
        if (businessIdList.size() < 1) {
            businessIdList.add("");
        }
        Page<CodingScheme> page = commonMapper.selectPageByQuery(SqlQuery.from(CodingScheme.class)
                .in(CodingSchemeInfo.BUSINESSID, businessIdList), (index - 1) * size, index * size);
        page.getData().stream().peek(category -> {
            String businessId = category.getBusinessId();
            //CategoryTo categoryTo = categoryService.findCategoryById(businessId);
            //category.setCategoryName(categoryTo.getCategoryName());
        }).collect(Collectors.toList());
        return page;
    }

    @Override
    public List<CodingSchemeItem> findSchemeByBusinessId(String businessId) {
        CodingScheme scheme = getScheme(businessId, new Date().getYear());
        List<CodingSchemeItem> codingSchemeItems = null;
        if (scheme != null) {
            codingSchemeItems = codingSchemeItemService.selectList(SqlQuery.from(CodingSchemeItem.class).equal(CodingSchemeItemInfo.BUSINESSID, scheme.getId()));
        }
        return codingSchemeItems;
    }

    @Override
    public void deleteByBusinessId(String businessId) {
        List<CodingScheme> codingSchemes = commonMapper.selectByQuery(SqlQuery.from(CodingScheme.class)
                .equal(CodingSchemeInfo.BUSINESSID, businessId));
        for (CodingScheme codingScheme : codingSchemes) {
            commonMapper.deleteByQuery(SqlQuery.from(CodingSchemeItem.class)
                    .equal(CodingSchemeItemInfo.BUSINESSID, codingScheme.getId()));
            commonMapper.deleteById(CodingScheme.class, codingScheme.getId());
//            deleteByIds(codingScheme.getId());
        }
    }

    //TODO
    @Override
    public String genArchiveCode(String cateGoryId, Integer year, ArchiveEntity archive) {
        return null;
    }

    //TODO
    @Override
    public void validateCode(String cateGoryId, Integer year, String code) {

    }

    private CodingScheme getScheme(String categoryId, Integer year) {
        //根据分类id查询分类下所有编码方案
        List<CodingScheme> codingSchemes = this.selectList(SqlQuery.from(CodingScheme.class)
                .equal(CodingSchemeInfo.BUSINESSID, categoryId));
        if (codingSchemes != null && codingSchemes.size() > 0) {
            CodingScheme scheme = null;
            for (CodingScheme codingScheme : codingSchemes) {
                //选取编码方案在档案年度时间段的编码方案返回
                int startYear = codingScheme.getStartYear();
                int endYear = codingScheme.getEndYear();
                boolean b = (startYear == 0 && year <= endYear) || (endYear == 0 && year >= startYear) || (year >= startYear && year <= endYear);
                if (b) {
                    return codingScheme;
                    //年度条件内不存在方案选取默认方案
                } else if (codingScheme.isDefault()) {
                    scheme = codingScheme;
                }
            }
            if (scheme != null) {
                return scheme;
            }
            //都不存在返回空
            return null;
        } else {
            //分类无编码方案返回空
            return null;
        }
    }

    @Override
    public FormData builderArchiveCode(FormData formData, String cateGoryId) {
        //分类号
        String categoryCode = formData.get(ArchiveEntity.COLUMN_CATEGORY_CODE);
        //全宗号
        String fondCode = formData.get(ArchiveEntity.COLUMN_FOND_CODE);
        String vintages = formData.get("VINTAGES");
        if (StringUtils.isEmpty(vintages)) {
            vintages = "0";
        }
        CodingScheme codingScheme = getScheme(cateGoryId, Integer.parseInt(vintages));
        if (codingScheme == null) {
            return formData;
        }
        //顺序号位数
        Integer digit = codingScheme.getDigit();
        String code = "";
        String archiveCode = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
        //存在档号为卷内目录所在案卷的档号，做为卷内目录的基础档号
        if (!StringUtils.isEmpty(archiveCode)) {
            code = archiveCode + "-";
        } else {
            //根据档号生成方案生成基础档号
            List<CodingSchemeItem> codingSchemeItems = codingSchemeItemService.selectList(SqlQuery.from(CodingSchemeItem.class)
                    .equal(CodingSchemeItemInfo.BUSINESSID, codingScheme.getId())
                    .orderBy(CodingSchemeItemInfo.ORDERBY));
            if (codingSchemeItems == null || codingSchemeItems.size() <= 0) {
                formData.put(ArchiveEntity.COLUMN_ARCHIVE_CODE, "");
                return formData;
            }
            for (CodingSchemeItem codingSchemeItem : codingSchemeItems) {
                String itemCode = codingSchemeItem.getCode();
                String connector = codingSchemeItem.getConnector();
                String name = codingSchemeItem.getName();
                if (StringUtils.isEmpty(name)) {
                    code += itemCode + connector;
                } else {
                    String serializable = formData.get(itemCode);
                    if (StringUtils.isEmpty(serializable)) {
                        Assert.isTrue(false, name + "字段不可为空");
                    }
                    code += serializable + connector;
                }
            }
        }
        //根据基础档号查询基础档号存在的档案
        String finalCode = code;
        String status = formData.get(ArchiveEntity.COLUMN_STATUS);
        List<FormData> formDataList = formDataService.selectFormData(formData.getFormDefinitionId(), (sqlQuery, formRelationWrapper) -> {
            sqlQuery.like(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE), finalCode);
            sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), status);
            sqlQuery.orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
        //已有序号根据序号生成档号，无序号根据已有档号排序生成档号
        //顺号位数得到最后一位，完成生成
        String order = formData.get(ArchiveEntity.COLUMN_ORDER_NAME);
        if (!StringUtils.isEmpty(order)) {
            int length = (order + "").length();
            if (digit > length) {
                for (int i = 0; i < digit - length; i++) {
                    code += "0";
                }
            }
            code += order;
        } else if (StringUtils.isEmpty(order) && formDataList.size() > 0) {
            FormData data = formDataList.get(0);
            String danghao = data.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
            danghao = danghao.replaceAll(code, "");
            if (danghao.indexOf("-") > 0) {
                danghao = danghao.substring(0, danghao.indexOf("-"));
            }
            order = (Integer.parseInt(danghao) + 1) + "";
            int length = (order + "").length();
            if (digit > length) {
                for (int i = 0; i < digit - length; i++) {
                    code += "0";
                }
            }
            code += order;
            formData.put(ArchiveEntity.COLUMN_ORDER_NAME, order);
        } else {
            for (int i = 0; i < digit - 1; i++) {
                code += "0";
            }
            code += "1";
            formData.put(ArchiveEntity.COLUMN_ORDER_NAME, 1);
        }
        formData.put(ArchiveEntity.COLUMN_ARCHIVE_CODE, code);
        return formData;
    }

    @Override
    protected Class getSubTableClass() {
        return CodingSchemeItem.class;
    }

    @Override
    protected Column getRelateColumn() {
        return CodingSchemeItemInfo.BUSINESSID;
    }
}
