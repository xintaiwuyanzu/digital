package com.dr.digital.manage.fournaturescheck.service.impl;

import com.dr.digital.manage.form.service.ArchiveDataManager;
import com.dr.digital.manage.form.service.ArchiveFormDefinitionService;
import com.dr.digital.manage.fournaturescheck.entity.*;
import com.dr.digital.manage.fournaturescheck.service.FourNatureSchemeItemMethodService;
import com.dr.digital.manage.fournaturescheck.service.FourNatureSchemeItemService;
import com.dr.digital.manage.fournaturescheck.service.FourNatureSchemeService;
import com.dr.digital.manage.fournaturescheck.service.TestRecord2IMPService;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.service.DefaultBaseService;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author caor
 * @date 2020-11-15 11:52
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class TestRecord2IMPServiceImpl extends DefaultBaseService<TestRecord> implements TestRecord2IMPService {
    @Autowired
    ArchiveFormDefinitionService archiveFormDefinitionService;
    @Autowired
    ArchiveDataManager archiveDataManager;
    @Autowired
    FourNatureSchemeService fourNatureSchemeService;
    @Autowired
    FourNatureSchemeItemService fourNatureSchemeItemService;
    @Autowired
    FourNatureSchemeItemMethodService fourNatureSchemeItemMethodService;

    /**
     * 四性检测 20210811 caor
     *
     * @param formData
     * @param query
     * @return
     */
    @Override
    public FormData startTest(FormData formData, BaseQuery query) {
        Assert.isTrue(!StringUtils.isEmpty(formData.getFormDefinitionId()), "表单id未获取到");
        //查询四性检测方案
        List<FourNatureScheme> fourNatureSchemeList = fourNatureSchemeService.selectList(SqlQuery.from(FourNatureScheme.class).equal(FourNatureSchemeInfo.BUSINESSID, formData.getFormDefinitionId()));
        if (fourNatureSchemeList.size() > 0) {
            fourNatureSchemeList.forEach(fourNatureScheme -> {
                //查询检测项
                List<FourNatureSchemeItem> fourNatureSchemeItemList = fourNatureSchemeItemService.selectList(SqlQuery.from(FourNatureSchemeItem.class).equal(FourNatureSchemeItemInfo.FOURNATURESCHEMEID, fourNatureScheme.getId()));
                if (fourNatureSchemeItemList.size() > 0) {
                    fourNatureSchemeItemList.forEach(fourNatureSchemeItem -> {
                        //查询检测元数据项
                        List<FourNatureSchemeItemMethod> fourNatureSchemeItemMethodList = fourNatureSchemeItemMethodService.selectList(SqlQuery.from(FourNatureSchemeItemMethod.class).equal(FourNatureSchemeItemMethodInfo.FOURNATURESCHEMEITEMID, fourNatureSchemeItem.getId()));
                        if (fourNatureSchemeItemMethodList.size() > 0) {
                            //检测结果绑定值
                            TestRecord testRecord = new TestRecord();
                            testRecord.setFormDataId(formData.getId());
                            testRecord.setFormDefinitionId(formData.getFormDefinitionId());
                            testRecord.setTitle(formData.get(ArchiveEntity.COLUMN_TITLE));
                            testRecord.setArchiveCode(formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE));
                            testRecord.setFondCode(formData.get(ArchiveEntity.COLUMN_FOND_CODE));
                            testRecord.setCategoryCode(formData.get(ArchiveEntity.COLUMN_CATEGORY_CODE));
                            testRecord.setTestRecordType(fourNatureSchemeItem.getCheckType());
                            testRecord.setStatus("1");
                            StringBuilder testResult = new StringBuilder();
                            fourNatureSchemeItemMethodList.forEach(fourNatureSchemeItemMethod -> {
                                testRecord.setTestCode(fourNatureSchemeItemMethod.getFormFieldCode());
                                testRecord.setTestName(fourNatureSchemeItemMethod.getFormFieldName());
                                //判断元数据是否为必填项
                                if ("是".equals(fourNatureSchemeItemMethod.getIfRequired())) {
                                    //如果为必填项则校验不能为空
                                    if (StringUtils.isEmpty(formData.get(fourNatureSchemeItemMethod.getFormFieldCode()))) {
                                        formData.put("fourDetection", "不通过");
                                        testRecord.setStatus("2");
                                        testResult.append(fourNatureSchemeItemMethod.getFormFieldName() + "为空;");
                                    }
                                }
                                //判断元数据类型是否为空,不为空时处理
                                if (!StringUtils.isEmpty(fourNatureSchemeItemMethod.getFormFieldType())) {
                                    //检测元数据类型配置为数字
                                    if ("int".equalsIgnoreCase(fourNatureSchemeItemMethod.getFormFieldType())) {
                                        //元数据为非空时检测是否为数字类型
                                        if (!StringUtils.isEmpty(formData.get(fourNatureSchemeItemMethod.getFormFieldCode())) && !isInteger(formData.get(fourNatureSchemeItemMethod.getFormFieldCode()))) {
                                            formData.put("fourDetection", "不通过");
                                            testRecord.setStatus("2");
                                            testResult.append(fourNatureSchemeItemMethod.getFormFieldName() + "不是数字;");
                                        }
                                    }
                                }
                                //判断特殊字符是否为空,不为空时处理
                                if (!StringUtils.isEmpty(fourNatureSchemeItemMethod.getSpecialCharacters())) {
                                    String specialCharacters = formData.get(fourNatureSchemeItemMethod.getFormFieldCode());
                                    //数据不为空并且不含特殊字符
                                    if (!StringUtils.isEmpty(specialCharacters) && specialCharacters.indexOf(fourNatureSchemeItemMethod.getSpecialCharacters().trim()) != -1) {
                                        formData.put("fourDetection", "不通过");
                                        testRecord.setStatus("2");
                                        testResult.append(fourNatureSchemeItemMethod.getFormFieldName() + "包含特殊字符【" + fourNatureSchemeItemMethod.getSpecialCharacters() + "】;");
                                    }
                                }
                                //判断数据最短值是否满足
                                if (!StringUtils.isEmpty(fourNatureSchemeItemMethod.getFormFieldShort())) {
                                    //判断配置的最短值是数字类型
                                    if (isInteger(fourNatureSchemeItemMethod.getFormFieldShort())) {
                                        int formFieldShort = Integer.parseInt(fourNatureSchemeItemMethod.getFormFieldShort());
                                        String value = formData.get(fourNatureSchemeItemMethod.getFormFieldCode());
                                        //数据为空并且配置最短不为空，不通过；数据不为空，数据长度小于配置，不通过
                                        if ((StringUtils.isEmpty(formData.get(fourNatureSchemeItemMethod.getFormFieldCode())) && formFieldShort != 0) || (!(StringUtils.isEmpty(formData.get(fourNatureSchemeItemMethod.getFormFieldCode()))) && (formFieldShort > value.length()))) {
                                            formData.put("fourDetection", "不通过");
                                            testRecord.setStatus("2");
                                            testResult.append(fourNatureSchemeItemMethod.getFormFieldName() + "值域小于配置项的值【" + fourNatureSchemeItemMethod.getFormFieldShort() + "】;");
                                        }
                                    } else {
                                        formData.put("fourDetection", "不通过");
                                        testRecord.setStatus("2");
                                        testResult.append(fourNatureSchemeItemMethod.getFormFieldName() + "，检测配置的值【" + fourNatureSchemeItemMethod.getFormFieldShort() + "】不是数字类型;");
                                    }
                                }
                                //判断数据最长值是否满足
                                if (!StringUtils.isEmpty(fourNatureSchemeItemMethod.getFormFieldLong())) {
                                    //判断配置的最短值是数字类型
                                    if (isInteger(fourNatureSchemeItemMethod.getFormFieldLong())) {
                                        int formfieldLong = Integer.parseInt(fourNatureSchemeItemMethod.getFormFieldLong());
                                        String value = formData.get(fourNatureSchemeItemMethod.getFormFieldCode()) == null ? "" : formData.get(fourNatureSchemeItemMethod.getFormFieldCode());
                                        //数据为空并且配置最短不为空，不通过；数据不为空，数据长度小于配置，不通过
                                        if ((!StringUtils.isEmpty(formData.get(fourNatureSchemeItemMethod.getFormFieldCode())) && formfieldLong == 0) || ((formfieldLong != 0) && (formfieldLong < value.length()))) {
                                            formData.put("fourDetection", "不通过");
                                            testRecord.setStatus("2");
                                            testResult.append(fourNatureSchemeItemMethod.getFormFieldName() + "值域大于配置项的值【" + fourNatureSchemeItemMethod.getFormFieldLong() + "】;");
                                        }
                                    } else {
                                        formData.put("fourDetection", "不通过");
                                        testRecord.setStatus("2");
                                        testResult.append(fourNatureSchemeItemMethod.getFormFieldName() + "，检测配置的值【" + fourNatureSchemeItemMethod.getFormFieldLong() + "】不是数字类型;");
                                    }
                                }
                            });
                            if (!"不通过".equalsIgnoreCase(formData.get("fourDetection"))) {
                                formData.put("fourDetection", "通过");
                            }
                            testRecord.setTestResult(testResult.toString());
                            insert(testRecord);
                        }
                    });
                }
            });
        }
        return formData;
    }

    /**
     * 判断是否为整数
     *
     * @param str 传入的字符串
     * @return 是整数返回true, 否则返回false
     */
    public boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }
}
