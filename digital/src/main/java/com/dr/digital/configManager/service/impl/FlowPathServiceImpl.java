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
     * ????????????
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
                info = "???????????????????????????";
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

    /**???????????????
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
            //?????????????????????????????????????????????????????????
            List<String> list = Arrays.asList(LinkFlowPath.LinkFlowPath);
            //??????????????????
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
                //1?????????
                if ("1".equals(state)) {
                    String convert = convert(split.get(index + 1), fid);
                    Map<String,String> map = new HashMap<>();
                    map.put("flowBatchName",split.get(index + 1));
                    map.put("flowStringName",convert);
                    //?????????????????????????????????
                    return ResultEntity.success(map);
                } else if ("2".equals(state)) {
                    //2?????????
                    //????????????????????????????????? ?????????????????????
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
                return ResultEntity.error("??????????????????????????????");
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
        return ResultEntity.error("??????????????????");
    }
//    @Override
//    public boolean overJudge(String registerId) {
//        String target = commonMapper.selectOneByQuery(SqlQuery.from(Register.class)
//                .equal(RegisterInfo.ID, registerId)).getTarget_format();
//        if (!StringUtils.isEmpty(target)){
//            //??????????????????4??????????????????4
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
                return ResultEntity.success("????????????");
            }
            return ResultEntity.success("????????????");
        }else {
            return ResultEntity.error("????????????");
        }

    }

    /**
     * ???????????????????????????
     * fid=??????id
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
                //1.??????
                flow.setTotalNum(formDataList.size()+"");
                //2.???????????????
                flow.setSurPlusNum(dataList.size()+"");
                //3.????????????
                //??????????????????
                List<ArchivesLog> insNum = insNum(fid, split[i]);
                //??????????????????
                List<ArchivesLog> upNum = upNum(fid, split[i],"");
                int conpleteNum = insNum.size()-upNum.size();
                flow.setCompleteNum(conpleteNum+"");
                //4.?????????
                flow.setShouldNum(formDataList.size()-conpleteNum+"");
                list.add(flow);
            }
            return ResultEntity.success(list);
        }else {
            return ResultEntity.error("??????????????????");
        }
    }
    //??????
    public boolean check(String fid,String type){
        if (!"".equals(fid) && !"".equals(type) && fid != null && type != null) {
            return true;
        }else {
            return false;
        }
    }

    /**
     *?????????fid????????????????????????????????????
     * @param fid
     * @param value ture ?????????name
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
     * ??????map
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
     * ???????????????????????????
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
            return "??????";//??????
        }else {
            return "??????";//??????
        }
    }

    /**
     * ???????????????????????????
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
     * ?????????????????????????????????
     * @param fid
     * @param info //?????????????????????
     *
     * @return
     */
    @Override
    public ResultEntity flowPeopleData(String fid, String info, Person person, Long startTime, Long endTime) {
        //????????????
        List<Map<String,String>> list = new ArrayList<>();
        //??????????????????
        List<ArchivesLog> insNum = insNum(fid, info);
        //??????????????????
        List<ArchivesLog> upNum = upNum(fid, info,"");
        //????????????????????? ??????????????????????????????
        if(insNum.size()-upNum.size()!=0){
            //???????????????????????????
            Map<String,Integer> mapPeople = new HashMap<>();
            //?????????????????????
            List<String> listDangHao = new ArrayList<>();
            for(ArchivesLog insAchivesLog:insNum){
                {
                    if(!listDangHao.contains(insAchivesLog.getDangHao())){
                        listDangHao.add(insAchivesLog.getDangHao());
                    }
                }
            }
            //???????????????????????? ??????????????????????????????????????????
            for(String dangHao:listDangHao){
                List<ArchivesLog> inDangHao = upInsDangHao(fid,info,dangHao,"??????");
                List<ArchivesLog> upDangHao = upNum(fid,info,dangHao);
                //???????????????????????????
                if(inDangHao.size()-upDangHao.size()>0){
                    //??????????????????????????????????????????????????????
                    List<ArchivesLog> deleteDanghao = upInsDangHao(fid,"RECEIVE",dangHao,"??????");
                    for(ArchivesLog archivesLog:inDangHao) {
                        //?????????????????????????????????
                        if(deleteDanghao.size()>0){
                            for(ArchivesLog delete:deleteDanghao){
                                //???????????????????????????????????????????????????????????????  ??????????????????????????????
                                if(!delete.getDangHao().equals(archivesLog.getDangHao())){
                                    //????????????
                                    mapPeople = mapPeople(startTime, endTime, archivesLog,mapPeople);
                                    break;
                                }else if(delete.getDangHao().equals(archivesLog.getDangHao())&&delete.getOperatorDate()<archivesLog.getOperatorDate()){
                                    //????????????
                                    mapPeople = mapPeople(startTime, endTime, archivesLog,mapPeople);
                                    break;
                                }
                                break;
                            }
                        }else {
                            //????????????
                            mapPeople = mapPeople(startTime, endTime, archivesLog,mapPeople);
                            break;
                        }
                    }
                }
            }
            Person personOne = SecurityHolder.get().currentPerson();
            //????????????
            Map<String,String> mapData = new HashMap<>();
            Set<String> people = mapPeople.keySet();
            for (String p:people){
                //??????????????????
                //?????????????????????????????????
                Register register = registerService.getRegister(fid);
                if (!personOne.getUserName().equals("???????????????")&&!personOne.getUserName().equals(register.getReceiver())) {
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
     *   ??????????????????
     * @param fid
     * @param info  ????????????
     * @return  ?????????????????????
     */
    public List<ArchivesLog> insNum(String fid,String info){
        SqlQuery<ArchivesLog> ins = SqlQuery.from(ArchivesLog.class)
                .equal(ArchivesLogInfo.FORMDEFINITIONID, fid)
                .equal(ArchivesLogInfo.CAOZUOLEIXING, "??????")
                .equal(ArchivesLogInfo.CAOZUOHUANJIE, info);
        List<ArchivesLog> insNum = archivesLogService.selectArchivesLog(ins);
        return insNum;
    }
    /**
     *   ??????????????????
     * @param fid
     * @param info  ????????????
     * @param dangHao
     * @return  ?????????????????????
     */
    public List<ArchivesLog> upNum(String fid,String info,String dangHao){
        SqlQuery<ArchivesLog> up = SqlQuery.from(ArchivesLog.class)
                .equal(ArchivesLogInfo.FORMDEFINITIONID, fid)
                .equal(ArchivesLogInfo.CAOZUOLEIXING, "??????");
        //??????????????????????????????????????????
        String[] split = flowArray(fid, false);
        String[] insArray = flowArrayUpAndins(info, split, true);
        String[] upArray = flowArrayUpAndins(info, split, false);
        //????????????sql,
        //?????????????????????????????????????????????????????????????????????
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
        //??????????????????????????????
        List<ArchivesLog> upNum = archivesLogService.selectArchivesLog(up);
        return upNum;
    }

    /**
     * ???????????????????????????
     * @param fid
     * @param info   ????????????
     * @param dangHao   ??????
     * @param type   ????????????
     * @return
     */
    public List<ArchivesLog> upInsDangHao(String fid,String info,String dangHao,String type){
        SqlQuery<ArchivesLog> sqlQuery = SqlQuery.from(ArchivesLog.class)
                .equal(ArchivesLogInfo.FORMDEFINITIONID, fid)
                .equal(ArchivesLogInfo.CAOZUOLEIXING, type);

        if (type.equals("??????")){
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
     * ?????????????????????????????????????????????
     * @param startTime ????????????
     * @param endTime   ????????????
     * @param archivesLog ????????????
     * @return
     */
    public Map<String,Integer> mapPeople(Long startTime,Long endTime,ArchivesLog archivesLog,Map<String,Integer> mapPeople){
        //???????????????????????????
        if(startTime!=0&&startTime>0&&endTime>0){
            //?????????????????????????????????
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
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????
     * @param type
     * @param array
     * @param upAndIns
     * @return
     */
    public String[] flowArrayUpAndins(String type,String[] array,boolean upAndIns){
        //??????????????????
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
