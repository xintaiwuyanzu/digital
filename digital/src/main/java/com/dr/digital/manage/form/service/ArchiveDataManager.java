package com.dr.digital.manage.form.service;

import com.dr.digital.manage.form.entity.ArchiveRepeat;
import com.dr.digital.manage.model.query.ArchiveDataQuery;
import com.dr.digital.wssplit.entity.WssplitTagging;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.form.core.service.SqlBuilder;
import com.dr.framework.common.page.Page;
import com.dr.framework.core.organise.entity.Person;

import java.util.List;

/**
 * 档案数据管理对象
 * <p>
 * 汇总类，能够处理各种类型的数据
 *
 * @author dr
 */
public interface ArchiveDataManager {
    /**
     * 状态 配置项
     */
    /**
     * 案卷录入
     */
    String STATUS_RECEIVE = "RECEIVE";

    /**
     * 扫描
     */
    String STATUS_SCANNING = "SCANNING";

    /**
     * 图像处理
     */
    String STATUS_PROCESSING = "PROCESSING";

    /**
     * 手动拆件
     */
    String STATUS_WSSPLIT = "WSSPLIT";

    /**
     * 卷内录入
     */
    String STATUS_VOLUMES = "VOLUMES";

    /**
     * 质检
     */
    String STATUS_QUALITY = "QUALITY";

    /**
     * 复检
     */
    String STATUS_RECHECK = "RECHECK";

    /**
     * 办结
     */
    String STATUS_OVER = "OVER";

    /**
     * 添加表单数据
     *
     * @param formData   表单数据
     * @param fondId
     * @param categoryId
     * @return
     */
    FormData insertFormData(FormData formData, String fondId, String categoryId);

    FormData insertFormData(FormData formData, String fondId, String categoryId, String formDefinitionId, Person person);

    /**
     * 返回批次下所有的档号
     *
     * @param formDefinitionId
     * @return
     */
    List<FormData> selectFormData(String formDefinitionId);

    /**
     * 添加表单数据 并 修改顺序号
     *
     * @param formData   表单数据
     * @param fondId
     * @param categoryId
     * @return
     */
    FormData insertFormDataSh(FormData formData, String fondId, String categoryId);

    /**
     * 更新表单数据
     *
     * @param formData
     * @param fondId
     * @param categoryId
     * @return
     */
    FormData updateFormData(FormData formData, String fondId, String categoryId);

    FormData updateFormDataSh(FormData formData, String fondId, String categoryId);

    FormData updateFormDataCheck(FormData formData, String fondId, String categoryId, String formDefinitionId);

    /**
     * 查询单条表单数据
     *
     * @param formDefinitionId
     * @param formDataId
     * @return
     */
    FormData selectOneFormData(String formDefinitionId, String formDataId);

    /**
     * 查询表单数据
     *
     * @param formDefinitionId
     * @param sqlBuilder
     * @param pageIndex
     * @param pageSize
     * @return
     */
    Page<FormData> formDataPage(String formDefinitionId, SqlBuilder sqlBuilder, int pageIndex, int pageSize);

    /**
     * 给前端用的接口
     *
     * @param query
     * @param pageIndex
     * @param pageSize
     * @return
     */
    Page<FormData> formDataPage(ArchiveDataQuery query, int pageIndex, int pageSize);

    /**
     * 查询表单所有数据不分页
     *
     * @param query
     * @return
     */
    List<FormData> formDataPage(ArchiveDataQuery query);

    /**
     * 删除表单数据
     * TODO 这里应该也传全宗和门类Id
     * 犹豫要不要放到每一张表数据中
     *
     * @param formDefinitionId
     * @param aId
     * @return
     */
    Long deleteFormData(String formDefinitionId, String aId);



    /**
     * 根据表单数据Id查询表单数据Id
     *
     * @param formId
     * @return
     */
    default List<FormData> findDataByQuery(String formId) {
        ArchiveDataQuery query = new ArchiveDataQuery();
        query.setFormDefinitionId(formId);
        return findDataByQuery(query);
    }

    /**
     * 根据查询条件查询列表
     *
     * @param query
     * @return
     */
    List<FormData> findDataByQuery(ArchiveDataQuery query);

    /**
     * 更新状态
     *
     * @param ids
     * @param status
     * @param formDefinitionId
     */
    void updateStatus(String ids, String status, String formDefinitionId);

    /**
     * 数据检查（查重方法，若不传archiveCode则查询该表单下所有重复数据）
     *
     * @param fond        全宗号
     * @param category    分类号
     * @param formId      表单id
     * @param status      库状态（移交库、档案库）
     * @param archiveCode 档号
     * @return
     */
    List<ArchiveRepeat> repeat(String fond, String category, String formId, String status, String archiveCode);

    void updateHaveYuanwenByFormData(String categoryId, FormData formData, String haveYuanwen);

    List<FormData> selectLinkDataNum(String fid, String info);

    /**
     * 根据表单id 批次id查询档案
     * @param fid
     * @param id
     * @return
     */
    List<FormData> selectLinkcheckData(String fid, String id);

    /**
     * 添加时的日志
     *
     * @param categoryId
     * @param userName         操作人
     * @param inset            操作流程
     * @param status_info      目标流程
     * @param judge            是提交还是退回
     * @param s
     * @param log              操作说明
     * @param formDefinitionId formID
     */
    void updateLog(String categoryId, String userName, String inset, String status_info, String judge, String s, String log, String dangHao, String title, String box, String formDefinitionId);

    ResultEntity uniquenessJudge(WssplitTagging wssplitTagging);
}
