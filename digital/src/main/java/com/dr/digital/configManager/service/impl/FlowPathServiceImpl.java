package com.dr.digital.configManager.service.impl;

import com.dr.digital.configManager.bo.LinkFlowPath;
import com.dr.digital.configManager.entity.*;
import com.dr.digital.configManager.service.FlowPathService;
import com.dr.digital.manage.form.service.ArchiveDataManager;
import com.dr.digital.manage.log.entity.ArchivesLog;
import com.dr.digital.manage.log.entity.ArchivesLogInfo;
import com.dr.digital.manage.log.service.ArchivesLogService;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.register.entity.Register;
import com.dr.digital.register.entity.RegisterInfo;
import com.dr.digital.register.service.RegisterService;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.dr.framework.core.security.SecurityHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class FlowPathServiceImpl implements FlowPathService {
    @Autowired
    CommonMapper commonMapper;
    @Autowired
    ArchiveDataManager dataManager;
    @Autowired
    ArchivesLogService archivesLogService;
    @Autowired
    RegisterService registerService;

    @Override
    public List<FlowPath> getFlowPath() {
        SqlQuery<FlowPath> sqlQuery = SqlQuery.from(FlowPath.class).orderBy(FlowPathInfo.ID);
        List<FlowPath> flowPaths = commonMapper.selectByQuery(sqlQuery);
        return flowPaths;
    }

    @Override
    public List<FlowPathDetail> getFlowPathDetail(String batch) {
        SqlQuery<FlowPathDetail> sqlQuery = SqlQuery.from(FlowPathDetail.class).equal(FlowPathDetailInfo.FLOWBATCHNAME, batch).orderBy(FlowPathDetailInfo.ID);
        List<FlowPathDetail> flowPaths = commonMapper.selectByQuery(sqlQuery);
        return flowPaths;
    }

    /**
     * 批次剩余
     *
     * @param formDataAllList
     * @return
     */
    @Override
    public String selectLinkData(List<FormData> formDataAllList) {
        Map<String, Integer> map = new HashMap<>();
        for (FormData formData : formDataAllList) {
            String infos = formData.get(ArchiveEntity.COLUMN_STATUS);
            String info = "";
            if ("".equals(infos) || infos == null) {
                info = "数据未配备环节信息";
            } else {
                SqlQuery<FlowPath> sqlQuery = SqlQuery.from(FlowPath.class, false).column(FlowPathInfo.FLOWNAME).equal(FlowPathInfo.FLOWALIAS, infos);
                FlowPath flowPath = commonMapper.selectOneByQuery(sqlQuery);
                info = flowPath.getFlowName();
            }
            if (map.containsKey(info)) {
                Integer integer = map.get(info);
                integer++;
                map.put(info, integer);
            } else {
                map.put(info, 1);
            }
        }
        StringBuilder builder = new StringBuilder();
        String data = "";
        Iterator<String> car = map.keySet().iterator();
        while (car.hasNext()) {
            String key = car.next();
            Integer value = map.get(key);
            builder.append(key + ":" + value);
            builder.append("\n");
        }
        data = builder.toString();
        return data;
    }

    /**提交和退回
     * @param fid
     * @param type
     * @param state
     * @return
     */
    @Override
    public ResultEntity flowPath(String fid, String type, String state) {
        if (!"".equals(fid) && !"".equals(type) && fid != null && type != null) {
            SqlQuery<FlowPathDetail> equal = SqlQuery.from(FlowPathDetail.class).equal(FlowPathDetailInfo.FORMDEFINITIONID, fid);
            FlowPathDetail flowPathDetail = commonMapper.selectOneByQuery(equal);
            String[] flowPaths = flowPathDetail.getFlowBatchName().split(",");
            //去掉自动化的环节，留下存人工环节的数组
            List<String> list = Arrays.asList(LinkFlowPath.LinkFlowPath);
            //纯人工的环节
            List<String> split = new ArrayList<>();
            for (String flowPath : flowPaths) {
                if (list.contains(flowPath)) {
                    split.add(flowPath);
                }
            }
            int index = 0;
            for (int i = 0; i < split.size(); i++) {
                if (split.get(i).equals(type)) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                //1是提交
                if ("1".equals(state)) {
                    String convert = convert(split.get(index + 1), fid);
                    Map<String,String> map = new HashMap<>();
                    map.put("flowBatchName",split.get(index + 1));
                    map.put("flowStringName",convert);
                    //返回，编号以及流程名称
                    return ResultEntity.success(map);
                } else if ("2".equals(state)) {
                    //2是退回
                    //返回当前流程之前的流程 不包含当前环节
                    List<String> exitList = new ArrayList<>();
                    for (int i = 0; i < split.size(); i++) {
                        if (!split.get(i).equals(type)){
                            exitList.add(split.get(i));
                        }else {
                            break;
                        }
                    }
                    return ResultEntity.success(exitList);
                }
            } else {
                return ResultEntity.error("该批次中不包含该流程");
            }
        }
        return null;
    }

    @Override
    public ResultEntity flowPathAll(String fid) {
        if (fid!=null&&!"".equals(fid)){
            String[] split = flowArray(fid,false);
            return ResultEntity.success(split);
        }
        return ResultEntity.error("数据请求失误");
    }
//    @Override
//    public boolean overJudge(String registerId) {
//        String target = commonMapper.selectOneByQuery(SqlQuery.from(Register.class)
//                .equal(RegisterInfo.ID, registerId)).getTarget_format();
//        if (!StringUtils.isEmpty(target)){
//            //数字化成果是4，判断是不是4
//            return target.equals("4");
//        }
//        return false;
//    }
    @Override
    public ResultEntity flowUpdate(String fid, String type) {
        if (check(fid,type)){
            long l = commonMapper.updateByQuery(SqlQuery.from(FlowPathDetail.class)
                    .equal(FlowPathDetailInfo.FORMDEFINITIONID, fid)
                    .set(FlowPathDetailInfo.FLOWBATCHNAME, type));
            if (l>0){
                return ResultEntity.success("修改成功");
            }
            return ResultEntity.success("修改失败");
        }else {
            return ResultEntity.error("输入有误");
        }

    }

    /**
     * 统计所有的批次流程
     * fid=批次id
     *
     * @return
     */
    @Override
    public ResultEntity flowCensus(String fid) {
        if (fid!=null&&!"".equals(fid)){
            String[] split = flowArray(fid,false);
            List<FlowStatistics> list = new ArrayList<>();
            List<FormData> formDataList = dataManager.selectFormData(fid);
            for (int i = 0;i<split.length;i++){
                FlowStatistics flow = new FlowStatistics();
                List<FormData> dataList = dataManager.selectLinkDataNum(fid, split[i]);
                flow.setValue(split[i]);
                //1.总数
                flow.setTotalNum(formDataList.size()+"");
                //2.剩余数量。
                flow.setSurPlusNum(dataList.size()+"");
                //3.已完成。
                //查询提交个数
                List<ArchivesLog> insNum = insNum(fid, split[i]);
                //查询退回个数
                List<ArchivesLog> upNum = upNum(fid, split[i],"");
                int conpleteNum = insNum.size()-upNum.size();
                flow.setCompleteNum(conpleteNum+"");
                //4.应完成
                flow.setShouldNum(formDataList.size()-conpleteNum+"");
                list.add(flow);
            }
            return ResultEntity.success(list);
        }else {
            return ResultEntity.error("数据请求失败");
        }
    }
    //校验
    public boolean check(String fid,String type){
        if (!"".equals(fid) && !"".equals(type) && fid != null && type != null) {
            return true;
        }else {
            return false;
        }
    }

    /**
     *返回该fid下流程信息，数组形式返回
     * @param fid
     * @param value ture 是汉字name
     * @return
     */
    public String[] flowArray(String fid,boolean value){
        FlowPathDetail flowPathDetail = commonMapper.selectOneByQuery(SqlQuery
                .from(FlowPathDetail.class).equal(FlowPathDetailInfo.FORMDEFINITIONID, fid));
        String[] split = flowPathDetail.getFlowBatchName().split(",");
        String[] names = flowPathDetail.getFlowStringName().split(",");
        if (value){
            return names;
        }else {
            return split;
        }
    }

    /**
     * 封成map
     * @param fid
     * @return
     */
    public Map<String,String> flowMap(String fid){
        String[] split = flowArray(fid,false);
        String[] name = flowArray(fid,true);
        Map<String,String> map = new HashMap<>();
        for (int i=0;i<split.length;i++){
            map.put(split[i],name[i]);
        }
        return map;
    }
    /**
     * 判断是提交还是退回
     */
    @Override
    public String judge(String tpye,String endTpye,String fid){
        int hand=0;
        int end = 0;
        String[] strings = flowArray(fid,false);
        for (int i=0;i<strings.length;i++){
            if (tpye.equals(strings[i])){
                hand = i;
                continue;
            }else if(endTpye.equals(strings[i])){
                end = i;
                continue;
            }else if (hand!=0&&end!=0){
                break;
            }
        }
        if (end-hand>0){
            return "新增";//提交
        }else {
            return "退回";//退回
        }
    }

    /**
     * 返回对应的批次名称
     * @param type
     * @param fid
     * @return
     */
    @Override
    public String convert(String type,String fid) {
        Map<String, String> map = flowMap(fid);
        String value = map.get(type);
        return value;
    }


    /**
     * 查询完成人员的完成信息
     * @param fid
     * @param info //当前的流程位置
     *
     * @return
     */
    @Override
    public ResultEntity flowPeopleData(String fid, String info, Person person, Long startTime, Long endTime) {
        //封装数据
        List<Map<String,String>> list = new ArrayList<>();
        //查询提交个数
        List<ArchivesLog> insNum = insNum(fid, info);
        //查询退回个数
        List<ArchivesLog> upNum = upNum(fid, info,"");
        //筛选真正已完成 查询所有已完成的档号
        if(insNum.size()-upNum.size()!=0){
            //存放操作人完成个数
            Map<String,Integer> mapPeople = new HashMap<>();
            //存放已提交档号
            List<String> listDangHao = new ArrayList<>();
            for(ArchivesLog insAchivesLog:insNum){
                {
                    if(!listDangHao.contains(insAchivesLog.getDangHao())){
                        listDangHao.add(insAchivesLog.getDangHao());
                    }
                }
            }
            //根据提交过的档号 查询所有相同已完成的档号记录
            for(String dangHao:listDangHao){
                List<ArchivesLog> inDangHao = upInsDangHao(fid,info,dangHao,"新增");
                List<ArchivesLog> upDangHao = upNum(fid,info,dangHao);
                //筛选出已完成的档号
                if(inDangHao.size()-upDangHao.size()>0){
                    //判断是否是删除的档号，查询出是否存在
                    List<ArchivesLog> deleteDanghao = upInsDangHao(fid,"RECEIVE",dangHao,"删除");
                    for(ArchivesLog archivesLog:inDangHao) {
                        //筛选是否属于删除的数据
                        if(deleteDanghao.size()>0){
                            for(ArchivesLog delete:deleteDanghao){
                                //分三种：已删除没有新建，已删除新建，没删除  里面的操作都是一样的
                                if(!delete.getDangHao().equals(archivesLog.getDangHao())){
                                    //时间筛选
                                    mapPeople = mapPeople(startTime, endTime, archivesLog,mapPeople);
                                    break;
                                }else if(delete.getDangHao().equals(archivesLog.getDangHao())&&delete.getOperatorDate()<archivesLog.getOperatorDate()){
                                    //时间筛选
                                    mapPeople = mapPeople(startTime, endTime, archivesLog,mapPeople);
                                    break;
                                }
                                break;
                            }
                        }else {
                            //时间筛选
                            mapPeople = mapPeople(startTime, endTime, archivesLog,mapPeople);
                            break;
                        }
                    }
                }
            }
            Person personOne = SecurityHolder.get().currentPerson();
            //封装数据
            Map<String,String> mapData = new HashMap<>();
            Set<String> people = mapPeople.keySet();
            for (String p:people){
                //添加权限控制
                //还差一个获取批次创建者
                Register register = registerService.getRegister(fid);
                if (!personOne.getUserName().equals("超级管理员")&&!personOne.getUserName().equals(register.getReceiver())) {
                    if (p.equals(personOne.getUserName())) {
                        mapData.put("name",p);
                        mapData.put("address",mapPeople.get(p)+"");
                        list.add(mapData);
                    }
                }else {
                    mapData.put("name",p);
                    mapData.put("address",mapPeople.get(p)+"");
                    list.add(mapData);
                }
            }
        }
        return ResultEntity.success(list);
    }

    /**
     *   查询提交个数
     * @param fid
     * @param info  当前流程
     * @return  所有提交的记录
     */
    public List<ArchivesLog> insNum(String fid,String info){
        SqlQuery<ArchivesLog> ins = SqlQuery.from(ArchivesLog.class)
                .equal(ArchivesLogInfo.FORMDEFINITIONID, fid)
                .equal(ArchivesLogInfo.CAOZUOLEIXING, "新增")
                .equal(ArchivesLogInfo.CAOZUOHUANJIE, info);
        List<ArchivesLog> insNum = archivesLogService.selectArchivesLog(ins);
        return insNum;
    }
    /**
     *   查询退回个数
     * @param fid
     * @param info  当前流程
     * @param dangHao
     * @return  所有提交的记录
     */
    public List<ArchivesLog> upNum(String fid,String info,String dangHao){
        SqlQuery<ArchivesLog> up = SqlQuery.from(ArchivesLog.class)
                .equal(ArchivesLogInfo.FORMDEFINITIONID, fid)
                .equal(ArchivesLogInfo.CAOZUOLEIXING, "退回");
        //获取当前流程前面的所有流程。
        String[] split = flowArray(fid, false);
        String[] insArray = flowArrayUpAndins(info, split, true);
        String[] upArray = flowArrayUpAndins(info, split, false);
        //循环添加sql,
        //退回记录的目标记录到退回记录是否包含当前记录。
        if(upArray.length>0){
            up.in(ArchivesLogInfo.CAOZUOHUANJIE,upArray);
            up.and();
            up.in(ArchivesLogInfo.MUBIAOHUANJIE,insArray);
        }else {
            up.in(ArchivesLogInfo.MUBIAOHUANJIE,info);
        }
        if (!"".equals(dangHao)){
            up.equal(ArchivesLogInfo.DANGHAO, dangHao);
        }
        //查询出所有退回记录，
        List<ArchivesLog> upNum = archivesLogService.selectArchivesLog(up);
        return upNum;
    }

    /**
     * 查询相同档号的记录
     * @param fid
     * @param info   操作流程
     * @param dangHao   档号
     * @param type   操作类型
     * @return
     */
    public List<ArchivesLog> upInsDangHao(String fid,String info,String dangHao,String type){
        SqlQuery<ArchivesLog> sqlQuery = SqlQuery.from(ArchivesLog.class)
                .equal(ArchivesLogInfo.FORMDEFINITIONID, fid)
                .equal(ArchivesLogInfo.CAOZUOLEIXING, type);

        if (type.equals("退回")){
            sqlQuery.equal(ArchivesLogInfo.MUBIAOHUANJIE, info);
        }else {
            sqlQuery.equal(ArchivesLogInfo.CAOZUOHUANJIE, info);
        }
        sqlQuery.equal(ArchivesLogInfo.DANGHAO, dangHao)
                .orderByDesc(ArchivesLogInfo.OPERATORDATE);
        List<ArchivesLog> inDangHao = archivesLogService.selectArchivesLog(sqlQuery);
        return inDangHao;
    }
    /**
     * 封装成完成的操作人以及完成个数
     * @param startTime 起始时间
     * @param endTime   结束时间
     * @param archivesLog 记录信息
     * @return
     */
    public Map<String,Integer> mapPeople(Long startTime,Long endTime,ArchivesLog archivesLog,Map<String,Integer> mapPeople){
        //存放操作人完成个数
        if(startTime!=0&&startTime>0&&endTime>0){
            //存放操作人以及操作个数
            if (startTime<=archivesLog.getOperatorDate()&&endTime>=archivesLog.getOperatorDate()){
                if (mapPeople.containsKey(archivesLog.getOperatorName())) {
                    Integer integer = mapPeople.get(archivesLog.getOperatorName());
                    integer++;
                    mapPeople.put(archivesLog.getOperatorName(), integer);
                } else {
                    mapPeople.put(archivesLog.getOperatorName(), 1);
                }
            }
        }else {
            if (mapPeople.containsKey(archivesLog.getOperatorName())) {
                Integer integer = mapPeople.get(archivesLog.getOperatorName());
                integer++;
                mapPeople.put(archivesLog.getOperatorName(), integer);
            } else {
                mapPeople.put(archivesLog.getOperatorName(), 1);
            }
        }
        return mapPeople;
    }
    /**
     * 传入当前流程，返回当前以下（不包含当前），返回当前以上（不包含当前）
     * @param type
     * @param array
     * @param upAndIns
     * @return
     */
    public String[] flowArrayUpAndins(String type,String[] array,boolean upAndIns){
        //获取当前下标
        int index=-1;
        for(int i=0;i<array.length;i++){
            if(array[i].equals(type)){
                index = i;
                break;
            }
        }
        if(index>=0){
            if(upAndIns){
                String[] ins = Arrays.copyOfRange(array,0,index+1);
                return ins;
            }else {
                String[] up = Arrays.copyOfRange(array,index+1,array.length);
                return up;
            }
        }
        return array;
    }
}
