package com.dr.digital.packet.controller;


import com.dr.digital.configManager.service.ConfigManagerClient;
import com.dr.digital.configManager.service.FlowPathService;
import com.dr.digital.manage.form.service.ArchiveDataManager;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.model.query.ArchiveDataQuery;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.packet.entity.ArchiveCallback;
import com.dr.digital.packet.service.PacketsDataService;
import com.dr.digital.register.service.RegisterService;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.web.annotations.Current;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.AbstractQueue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:
 * @Date: 2021/11/23 14:22
 * @Description:
 */
@RestController
@RequestMapping(("api/packetsData"))
public class PacketsDataController {
    @Autowired
    PacketsDataService packetsDataService;
    @Autowired
    ArchiveDataManager dataManager;
    @Autowired
    ConfigManagerClient configManagerClient;
    @Autowired
    FlowPathService flowPathService;
    @Autowired
    RegisterService registerService;

    /**
     * 单个数据封包
     *
     * @param request
     * @return
     */
    @RequestMapping("/packet")
    ResultEntity packetData(HttpServletRequest request) {
        String formDefinitionId = request.getParameter("formDefinitionId");
        String formDataId = request.getParameter("formDataId");
        Assert.isTrue(!StringUtils.isEmpty(formDefinitionId), "formdefinitionId不能为空！");
        Assert.isTrue(!StringUtils.isEmpty(formDataId), "formDataId不能为空！");
        packetsDataService.packet(formDefinitionId, formDataId);
        return ResultEntity.success("正在打包");
    }

    /**
     * 封包
     *
     * @param request
     * @param query
     * @param command
     * @param formDataId
     * @param formDefinitionId
     * @param registerId
     * @param queryContent
     * @param person
     * @return
     */
    @RequestMapping(value = "/packetAll")
    public ResultEntity turnChange(HttpServletRequest request,
                                   BaseQuery query,
                                   String command,
                                   String formDataId,
                                   String formDefinitionId,
                                   String registerId,
                                   @RequestParam(name = ArchiveDataQuery.QUERY_KEY, required = false) String queryContent,
                                   @Current Person person) {
        packetsDataService.packetAll(query, queryContent, command, formDataId, formDefinitionId, registerId);
        return ResultEntity.success();
    }

    /**
     * 在线移交
     *
     * @param request
     * @param query
     * @param registerId
     * @param formDefinitionId
     * @param queryContent
     * @param person
     * @return
     */
    @RequestMapping(value = "/onlineHandover")
    public ResultEntity onlineHandover(HttpServletRequest request,
                                       BaseQuery query,
                                       String registerId,
                                       String formDefinitionId,
                                       @RequestParam(name = ArchiveDataQuery.QUERY_KEY, required = false) String queryContent,
                                       @Current Person person) {
        query.parseQuery(queryContent);
        //先查询数字化成果移交的所有数据
        List<FormData> formDataList = dataManager.formDataPage(query);
        //根据fid
        if (formDataList.size() > 0) {
            Map<String,Integer> map = new HashMap<>();
            int num = 0;
            for(FormData formData:formDataList){
                if (formData.get(ArchiveEntity.COLUMN_PACKET_STATE).equals("0")){
                    //查询具体数量
                    num++;
                }
            }
            if (num>0){
                Map percentag = registerService.getPercentag(registerId);
                return ResultEntity.error("当前还有未打包的数据,未打包数量："+num+"剩余时间"+percentag.get("packetNeedTime"));
            }
            return packetsDataService.onlineHandover(formDataList, registerId, formDefinitionId, person);
        } else {
            return ResultEntity.error("没有可移交的数据");
        }
        /*//查询这个批次下的所有表单数据
        List<FormData> formDataAllList = dataManager.findDataByQuery(formDefinitionId);
        int number = formDataAllList.size() - formDataList.size();
        if (formDataList.size() == formDataAllList.size()) {

        } else {
            //查询剩余环节
            String data = flowPathService.selectLinkData(formDataAllList);
            return ResultEntity.error("批次下存在"+number+"未处理完的数据，不能移交！" + "\n"+data);
        }*/
    }

    /**
     * 档案室接收数字化成果后，回调接口
     *
     * @param archiveCallback
     * @return
     */
    @RequestMapping(value = "/archivingResult")
    public ResultEntity archivingResult(ArchiveCallback archiveCallback) {
        //判断这批数据移交归库后的成果，成功将进行中间数据的删除操作 失败则进行退回操作。
        //packetsDataService.archivingResult(archiveCallback);
        return ResultEntity.success();
    }
}
