package com.dr.digital.manage.service.internal;

import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.framework.common.form.core.model.FormData;

/**
 * 编码方案
 * <p>
 * 用来生成或者校验档号
 *
 * @author dr
 */
public interface CodingSchemeService {
    /**
     * 功能描述: 根据业务外键id删除配置
     *
     * @param businessId
     * @author : tzl
     * @date : 2020/6/9 10:48
     */
    void deleteByBusinessId(String businessId);

    /**
     * 根据分类Id，年度，和表单数据生成档号
     *
     * @param cateGoryId
     * @param year       年度为空选择默认编码方案
     * @param archive
     * @return
     */
    String genArchiveCode(String cateGoryId, Integer year, ArchiveEntity archive);

    /**
     * 根据分类Id，年度
     * 校验指定的编码格式是否正确
     * <p>
     * 校验通过则直接通过，通不过则抛异常，具体原因在异常获取。
     * <p>
     * 抛异常的方式不是特别好，会有性能问题，没想好别的解决办法
     *
     * @param cateGoryId
     * @param year       年度为空选择默认编码方案
     * @param code
     */
    void validateCode(String cateGoryId, Integer year, String code);

    /**
     * 根据分类查询默认档号生成方案
     * <p>
     * 根据档号生成方案生成基础档号
     * <p>
     * 根据基础档号查询基础档号存在的档号
     * <p>
     * 根据已有档号排序生成档号
     *
     * @param formData
     * @return
     */
    FormData builderArchiveCode(FormData formData,String cateGoryId);
}
