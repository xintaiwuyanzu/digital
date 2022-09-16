package com.dr.digital.wssplit.service;

import com.dr.digital.manage.model.query.ArchiveDataQuery;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.wssplit.entity.WssplitTagging;
import com.dr.digital.wssplit.vo.WsSplit;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.entity.TreeNode;

import java.util.List;

public interface WsSplitService {
    //给前端树用的接口
    List<TreeNode> formDataPage(ArchiveDataQuery query);

    /**
     * 执行拖拽
     *
     * @param wsSplit
     */
    void changeTree(WsSplit wsSplit);

    /**
     * 保存排序后的图像
     *
     * @param pthohParaM
     * @param volumesDataParaM
     */
    void saveChangeTree(String pthohParaM, String volumesDataParaM);

    boolean uniquenessData(BaseQuery query);

    ResultEntity checkData(String formDefinitionId, String id);

    void splitWsTagging(WssplitTagging wssplitTagging);

    void splitWsTaggingUpdate(WssplitTagging wssplitTagging);

    WssplitTagging splitWsTaggingSelect(WssplitTagging wssplitTagging);

    void splitWsTaggingDelete(WssplitTagging wssplitTagging);

    void splitWsTaggingType(WssplitTagging wssplitTagging);

    boolean taggingType(WssplitTagging wssplitTagging);

    void updateSplitStatus(String formDefinitionId,String formDataId);
}
