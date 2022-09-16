package com.dr.digital.wssplit.controller;

import com.dr.digital.manage.log.annotation.SysLog;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.model.query.ArchiveDataQuery;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.register.entity.Register;
import com.dr.digital.wssplit.entity.SplitRule;
import com.dr.digital.wssplit.entity.SplitRuleInfo;
import com.dr.digital.wssplit.entity.WssplitTagging;
import com.dr.digital.wssplit.service.WsSplitService;
import com.dr.digital.wssplit.vo.WsSplit;
import com.dr.framework.common.controller.BaseController;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/wsSplit")
public class WsSplitController extends BaseController<SplitRule> {
    @Autowired
    WsSplitService wsSplitService;

    @Override
    protected void onBeforePageQuery(HttpServletRequest request, SqlQuery<SplitRule> sqlQuery, SplitRule entity) {
        sqlQuery.orderBy(SplitRuleInfo.IFORDER);
        super.onBeforePageQuery(request, sqlQuery, entity);
    }

    @SysLog("查询档案列表页数据")
    @RequestMapping(value = "/formDataTree")
    public ResultEntity findArchiveData(HttpServletRequest request,
                                        ArchiveDataQuery query,
                                        @RequestParam(name = ArchiveDataQuery.QUERY_KEY, required = false) String queryContent) {
        query.parseQuery(queryContent);
        return ResultEntity.success(wsSplitService.formDataPage(query));
    }

    @RequestMapping(value = "/changeTree")
    public ResultEntity changeTree(WsSplit wsSplit) {
        wsSplitService.changeTree(wsSplit);
        return ResultEntity.success();
    }

    @RequestMapping("deleteByIds")
    public ResultEntity<Register> delete(String ids) {
        String[] split = ids.split(",");
        for (String s : split) {
            commonService.delete(SqlQuery.from(SplitRule.class).equal(SplitRuleInfo.ID, s));
        }
        return ResultEntity.success();
    }

    @RequestMapping(value = "/saveChangeTree")
    public ResultEntity saveChangeTree(String pthohParaM, String volumesDataParaM, String id, BaseQuery query) {
        if (!StringUtils.isEmpty(id)) {
            query.getQueryItems().add(new ArchiveDataQuery.QueryItem("id", id, ArchiveDataQuery.QueryType.EQUAL));
        }
        wsSplitService.saveChangeTree(pthohParaM, volumesDataParaM);
        /*//唯一性
        boolean judge = wsSplitService.uniquenessData(query);
        if (judge){
        }else {
            return ResultEntity.error("当前无法操作，该档案已有操作人");
        }*/
        return ResultEntity.success();
    }
    /**
     *校验是否拆分jpg是否ocr
     */
    @RequestMapping("/checkData")
    public ResultEntity checkData(String formDefinitionId,String id){
        return wsSplitService.checkData(formDefinitionId,id);
    }

    /**
     * 创建批注
     * @param wssplitTagging
     * @return
     */
    @RequestMapping("/splitWsTagging")
    public ResultEntity splitWsTagging(WssplitTagging wssplitTagging){
        wsSplitService.splitWsTagging(wssplitTagging);
        return ResultEntity.success();
    }

    /**
     * 修改批注
     * @param wssplitTagging
     * @return
     */
    @RequestMapping("/splitWsTaggingUpdate")
    public ResultEntity splitWsTaggingUpdate(WssplitTagging wssplitTagging){
        wsSplitService.splitWsTaggingUpdate(wssplitTagging);
        return ResultEntity.success();
    }

    /**
     * 查询批注
     * @param wssplitTagging
     * @return
     */
    @RequestMapping("/splitWsTaggingSelect")
    public ResultEntity splitWsTaggingSelect(WssplitTagging wssplitTagging){
        return ResultEntity.success(wsSplitService.splitWsTaggingSelect(wssplitTagging));
    }
    /**
     * 删除批注
     * @param wssplitTagging
     * @return
     */
    @RequestMapping("/splitWsTaggingDelete")
    public ResultEntity splitWsTaggingDelete(WssplitTagging wssplitTagging){
        wsSplitService.splitWsTaggingDelete(wssplitTagging);
        return ResultEntity.success();
    }

    /**
     * 判断是否已经标注过了
     * @param wssplitTagging
     * @return
     */
    @RequestMapping("/taggingType")
    public boolean taggingType(WssplitTagging wssplitTagging){
      return wsSplitService.taggingType(wssplitTagging);
    }


    /**
     * 更改批注状态
     * @param wssplitTagging  fid,档案id,状态。
     * @return
     */
    @RequestMapping("/splitWsTaggingType")
    public ResultEntity splitWsTaggingType(WssplitTagging wssplitTagging){
        wsSplitService.splitWsTaggingType(wssplitTagging);
        return ResultEntity.success();
    }

    /**
     * 更改拆件状态为手动拆件
     */
    @RequestMapping("/updateSplitStatus")
    public ResultEntity updateSplitStatus(String formDefinitionId,String formDataId){
        wsSplitService.updateSplitStatus(formDefinitionId,formDataId);
        return ResultEntity.success();
    }

}