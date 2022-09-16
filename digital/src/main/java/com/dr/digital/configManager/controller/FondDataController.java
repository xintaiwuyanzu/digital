package com.dr.digital.configManager.controller;

import com.dr.digital.configManager.bo.CategoryInfo;
import com.dr.digital.configManager.bo.Metadata;
import com.dr.digital.configManager.entity.FlowPath;
import com.dr.digital.configManager.entity.FlowPathDetail;
import com.dr.digital.configManager.entity.ParamEntity;
import com.dr.digital.configManager.service.FlowPathService;
import com.dr.digital.configManager.service.FondDataService;
import com.dr.digital.register.entity.Register;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.entity.TreeNode;
import com.dr.framework.core.organise.entity.Organise;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.web.annotations.Current;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 档案全宗类，这里对接的智能归档配置系统
 */
@RestController
@RequestMapping("api/fonddata")
public class FondDataController {
    @Autowired
    FondDataService fondDataService;
    @Autowired
    private FlowPathService flowPathService;

    /**
     * 查询所有流程信息
     *
     * @return list
     */
    @RequestMapping("/getFlowPath")
    public ResultEntity getFlowPath() {
        List<FlowPath> flowPathList = flowPathService.getFlowPath();
        return ResultEntity.success(flowPathList);
    }

    @RequestMapping("/getFlowPathDetail")
    public ResultEntity getFlowPathDetail(String batch) {
        List<FlowPathDetail> flowPathDetailList = flowPathService.getFlowPathDetail(batch);
        return ResultEntity.success(flowPathDetailList);
    }

    /**
     * 根据组织机构 code 获取全宗
     *
     * @param person
     * @return
     */
    @RequestMapping("/getFondByOrgCode")
    public ResultEntity getFondByOrgCode(@Current Person person) {
        return ResultEntity.success(fondDataService.getFondByOrgCode(person));
    }


    /**
     * 获取门类树
     *
     * @param arcTypes
     * @return
     */
    @RequestMapping("/getCategory")
    public ResultEntity getCategory(String arcTypes) {
        List<CategoryInfo> list = fondDataService.getCategory(arcTypes);
        List<TreeNode> treeList = new ArrayList<>();
        for (CategoryInfo categoryInfo : list) {
            if ("0".equals(categoryInfo.getParentID())) {
                TreeNode treeNode = new TreeNode(categoryInfo.getCode(), categoryInfo.getName());
                treeNode.setParentId(categoryInfo.getParentID());
                treeNode.setDescription(categoryInfo.getId());
                List<TreeNode> treeNodeList = new ArrayList<>();
                for (CategoryInfo category : list) {
                    if (categoryInfo.getId().equals(category.getParentID())) {
                        TreeNode treeNode1 = new TreeNode(category.getCode(), category.getName());
                        treeNode1.setParentId(category.getParentID());
                        treeNode1.setDescription(category.getId());
                        treeNodeList.add(treeNode1);
                    }
                    treeNode.setChildren(treeNodeList);
                }
                treeList.add(treeNode);
            }
        }
        return ResultEntity.success(treeList);
    }

    /**
     * 获取配置的元数据(执行标准)
     *
     * @param code
     * @return
     */
    @RequestMapping("/getArchiveBspDict")
    public ResultEntity getArchiveBspDict(String code) {
        return ResultEntity.success(fondDataService.getArchiveBspDict(code));
    }

    /**
     * 获取文件结构
     *
     * @param batchId
     * @param code
     * @param classify
     * @param arrange
     * @return
     */
    @RequestMapping("/getArchivedTypeFile")
    public ResultEntity getArchivedTypeFile(String batchId,String code, String classify,  String arrange) {
        return ResultEntity.success(fondDataService.getArchivedTypeFile(batchId, code, classify, arrange));
    }

    /**
     * 获取元数据字段
     *
     * @param cod
     * @param classify
     * @param standard
     * @param arrange  :流程控制，占时不做
     * @return
     */
    @RequestMapping("/getCategoryMetadata")
    public ResultEntity getCategoryMetadata(String cod, String classify, String standard, String arrange) {
        return ResultEntity.success(fondDataService.getCategoryMetadata(cod, classify, standard, arrange));
    }

    /**
     * 批次保存方法 保存
     *
     * @param register
     * @param metadata
     * @param metadata//流程控制
     * @param person
     * @return
     */
    @RequestMapping("insertRegister")
    public ResultEntity insertRegister(Register register,
                                       Metadata metadata,
                                       String segment,
                                       String segmentName,
                                       @Current Person person,
                                       @Current Organise organise) {
        if (organise.getId().equals("root")){
            register.setOrganiseName(organise.getOrganiseName());
            register.setOrganiseId(organise.getId());
        }else {
            register.setOrganiseName("默认机构"+","+organise.getOrganiseName());
            register.setOrganiseId("root"+","+organise.getId());
        }
        return fondDataService.insertRegister(register, metadata, person, segment, segmentName);
    }

    /**
     * 更新
     *
     * @param register
     * @param paramEntity
     * @param person
     * @return
     */
    @RequestMapping("updateRegister")
    public ResultEntity updateRegister(Register register, ParamEntity paramEntity, @Current Person person) {
        fondDataService.updateRegister(register, paramEntity, person);
        return ResultEntity.success();
    }

    @PostMapping("bindRoleUser")
    public ResultEntity<String> bindRoleUser(String id, String personIds) {
        fondDataService.bindRoleUsers(id, personIds);
        return ResultEntity.success("绑定成功!");
    }

    /**
     * 获取元数据方案 2.1接口
     *
     * @param code：可选
     * @return
     */
    @RequestMapping("/getArchiveTypeSchema")
    public ResultEntity getArchiveTypeSchema(String code) {
        return ResultEntity.success(fondDataService.getArchiveTypeSchema(code));
    }

    /**
     * 获取门类名称
     *
     * @param code
     * @return
     */
    @RequestMapping("/getCategoryName")
    public ResultEntity getCategoryName(String code) {
        return ResultEntity.success(fondDataService.getCategoryName(code));
    }

    /**
     * fid=批次id
     * type = 当前批次里数据的位置
     * state = 1.提交  2.退回
     *
     * @return
     */
    @RequestMapping("/flowPath")
    public ResultEntity flowPath(String fid, String type, String state) {
        return flowPathService.flowPath(fid, type, state);
    }

    /**
     * 查询所有的批次流程
     * fid=批次id
     *
     * @return
     */
    @RequestMapping("/flowPathAll")
    public ResultEntity flowPathAll(String fid) {
        return flowPathService.flowPathAll(fid);
    }

//    /**
//     * 判断是否是数字化成果
//     * @return
//     */
//    @RequestMapping("/overJudge")
//    public boolean overJudge(String registerId) {
//        return flowPathService.overJudge(registerId);
//    }



    /**
     * 统计所有的批次流程
     * fid=批次id
     *
     * @return
     */
    @RequestMapping("/flowCensus")
    public ResultEntity flowCensus(String fid) {
        return flowPathService.flowCensus(fid);
    }

    /**
     * 查询完成人员的完成信息
     * @param fid
     * @param info //当前的流程位置
     *
     * @return
     */
    @RequestMapping("/flowPeopleData")
    public ResultEntity flowPeopleData(String fid,String info,
                                       Long startTime,
                                       Long endTime,
                                       @Current Person person) {
        return flowPathService.flowPeopleData(fid,info,person,startTime,endTime);
    }

    /**
     * fid=批次id
     * type = 修改的值
     *
     * @return
     */
    @RequestMapping("/flowUpdate")
    public ResultEntity flowUpdate(String fid, String type) {
        return flowPathService.flowUpdate(fid, type);
    }

    /**
     * 获取当前用户
     *
     * @param person
     * @return
     */
    @RequestMapping("user")
    public ResultEntity user(@Current Person person) {
        String user = person.getUserName();
        return ResultEntity.success(user);
    }
}
