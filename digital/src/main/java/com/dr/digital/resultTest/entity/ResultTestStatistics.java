package com.dr.digital.resultTest.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseStatusEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

/**检测统计表
 * @author Mr.Zhu
 * @date 2022/8/20 - 11:15
 */
@Table(name = Constants.TABLE_PREFIX + "RESULT_TEST_STATISTICS", module = Constants.MODULE_NAME, comment = "成果检测统计")
public class ResultTestStatistics extends BaseStatusEntity<String> {


    @Column(comment = "表单ID", length = 50, order = 1)
    private String formDefinitionId;
    @Column(comment = "批次ID", length = 50, order = 2)
    private String registerId;

    @Column(comment = "原文档号检测")
    private int yuanWenArchiveCodeTest;
    @Column(comment = "原文格式检测")
    private int yuanWenFormatTest;
    @Column(comment = "原文dpi检测")
    private int yuanWenDpiTest;
    @Column(comment = "原文色彩检测")
    private int yuanWenRgbTest;
    @Column(comment = "原文分辨率检测")
    private int yuanWenFilePowerTest;
    @Column(comment = "原文命名规范检测")
    private int yuanWenFileNameTest;
    @Column(comment = "原文文件页码检测")
    private int yuanWenFileYsTest;
    @Column(comment = "元数据必填检测")
    private int metadataRequireTest;
    @Column(comment = "元数据唯一性检测")
    private int metadataRepeatabilityTest;
    @Column(comment = "元数据类型检测")
    private int metadataTypeTest;
    @Column(comment = "元数据长度检测")
    private int metadataLengthTest;
    @Column(comment = "元数据值范围检测")
    private int metadataValRangeTest;
    @Column(comment = "元数据值域检测")
    private int metadataValContentTest;
    @Column(comment = "元数据禁用词检测")
    private int metadataDisByteTest;
    @Column(comment = "元数据复杂检测")
    private int metadataComplexTest;
    @Column(comment = "档号对比检测")
    private int comparisonArchiveCodeTest;
    @Column(comment = "全宗对比检测")
    private int comparisonFondCodeTest;
    @Column(comment = "页数对比检测")
    private int comparisonFileYsTest;

    public int getMetadataDisByteTest() {
        return metadataDisByteTest;
    }

    public void setMetadataDisByteTest(int metadataDisByteTest) {
        this.metadataDisByteTest = metadataDisByteTest;
    }

    public int getMetadataLengthTest() {
        return metadataLengthTest;
    }

    public void setMetadataLengthTest(int metadataLengthTest) {
        this.metadataLengthTest = metadataLengthTest;
    }

    public int getMetadataValRangeTest() {
        return metadataValRangeTest;
    }

    public void setMetadataValRangeTest(int metadataValRangeTest) {
        this.metadataValRangeTest = metadataValRangeTest;
    }

    public int getMetadataValContentTest() {
        return metadataValContentTest;
    }

    public void setMetadataValContentTest(int metadataValContentTest) {
        this.metadataValContentTest = metadataValContentTest;
    }

    public int getMetadataComplexTest() {
        return metadataComplexTest;
    }

    public void setMetadataComplexTest(int metadataComplexTest) {
        this.metadataComplexTest = metadataComplexTest;
    }

    public int getYuanWenRgbTest() {
        return yuanWenRgbTest;
    }

    public void setYuanWenRgbTest(int yuanWenRgbTest) {
        this.yuanWenRgbTest = yuanWenRgbTest;
    }

    public String getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(String formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }

    public String getRegisterId() {
        return registerId;
    }

    public void setRegisterId(String registerId) {
        this.registerId = registerId;
    }

    public int getYuanWenArchiveCodeTest() {
        return yuanWenArchiveCodeTest;
    }

    public void setYuanWenArchiveCodeTest(int yuanWenArchiveCodeTest) {
        this.yuanWenArchiveCodeTest = yuanWenArchiveCodeTest;
    }

    public int getYuanWenFormatTest() {
        return yuanWenFormatTest;
    }

    public void setYuanWenFormatTest(int yuanWenFormatTest) {
        this.yuanWenFormatTest = yuanWenFormatTest;
    }

    public int getYuanWenDpiTest() {
        return yuanWenDpiTest;
    }

    public void setYuanWenDpiTest(int yuanWenDpiTest) {
        this.yuanWenDpiTest = yuanWenDpiTest;
    }

    public int getYuanWenFilePowerTest() {
        return yuanWenFilePowerTest;
    }

    public void setYuanWenFilePowerTest(int yuanWenFilePowerTest) {
        this.yuanWenFilePowerTest = yuanWenFilePowerTest;
    }

    public int getYuanWenFileNameTest() {
        return yuanWenFileNameTest;
    }

    public void setYuanWenFileNameTest(int yuanWenFileNameTest) {
        this.yuanWenFileNameTest = yuanWenFileNameTest;
    }

    public int getYuanWenFileYsTest() {
        return yuanWenFileYsTest;
    }

    public void setYuanWenFileYsTest(int yuanWenFileYsTest) {
        this.yuanWenFileYsTest = yuanWenFileYsTest;
    }

    public int getMetadataRequireTest() {
        return metadataRequireTest;
    }

    public void setMetadataRequireTest(int metadataRequireTest) {
        this.metadataRequireTest = metadataRequireTest;
    }

    public int getMetadataRepeatabilityTest() {
        return metadataRepeatabilityTest;
    }

    public void setMetadataRepeatabilityTest(int metadataRepeatabilityTest) {
        this.metadataRepeatabilityTest = metadataRepeatabilityTest;
    }

    public int getMetadataTypeTest() {
        return metadataTypeTest;
    }

    public void setMetadataTypeTest(int metadataTypeTest) {
        this.metadataTypeTest = metadataTypeTest;
    }

    public int getComparisonArchiveCodeTest() {
        return comparisonArchiveCodeTest;
    }

    public void setComparisonArchiveCodeTest(int comparisonArchiveCodeTest) {
        this.comparisonArchiveCodeTest = comparisonArchiveCodeTest;
    }

    public int getComparisonFondCodeTest() {
        return comparisonFondCodeTest;
    }

    public void setComparisonFondCodeTest(int comparisonFondCodeTest) {
        this.comparisonFondCodeTest = comparisonFondCodeTest;
    }

    public int getComparisonFileYsTest() {
        return comparisonFileYsTest;
    }

    public void setComparisonFileYsTest(int comparisonFileYsTest) {
        this.comparisonFileYsTest = comparisonFileYsTest;
    }
}
