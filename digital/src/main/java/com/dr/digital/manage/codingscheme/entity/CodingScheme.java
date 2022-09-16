package com.dr.digital.manage.codingscheme.entity;

import com.dr.digital.manage.model.entity.BaseYearEntity;
import com.dr.digital.util.Constants;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

/**
 * describe
 * 编码方案主表
 *
 * @author tzl
 * @date 2020/5/29 15:05
 */
@Table(name = Constants.TABLE_PREFIX + "CODING_SCHEME", module = Constants.MODULE_NAME, comment = "编码方案")
public class CodingScheme extends BaseYearEntity {
    //这个字典啥用
    @Column(comment = "编码字典", length = 100, order = 6)
    private String schemeDict;
    @Column(comment = "是否自动生成", length = 10, order = 9)
    private boolean isAutoGeneration;
    @Column(comment = "顺号位数", length = 300, order = 5)
    private Integer digit;
    /**
     * 门类分类编码？？
     */
    @Column(comment = "分类名称", length = 500, order = 10)
    private String categoryName;

    public Integer getDigit() {
        return digit;
    }

    public void setDigit(Integer digit) {
        this.digit = digit;
    }

    public boolean isAutoGeneration() {
        return isAutoGeneration;
    }

    public void setAutoGeneration(boolean autoGeneration) {
        isAutoGeneration = autoGeneration;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public boolean getIsAutoGeneration() {
        return isAutoGeneration;
    }

    public void setIsAutoGeneration(boolean isAutoGeneration) {
        this.isAutoGeneration = isAutoGeneration;
    }

    public String getSchemeDict() {
        return schemeDict;
    }

    public void setSchemeDict(String schemeDict) {
        this.schemeDict = schemeDict;
    }
}
