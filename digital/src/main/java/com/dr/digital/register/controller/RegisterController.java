package com.dr.digital.register.controller;

import com.dr.digital.configManager.bo.Metadata;
import com.dr.digital.configManager.bo.MetadataInfo;
import com.dr.digital.configManager.entity.FlowPathDetail;
import com.dr.digital.configManager.entity.FlowPathDetailInfo;
import com.dr.digital.configManager.entity.TypeFile;
import com.dr.digital.configManager.entity.TypeFileInfo;
import com.dr.digital.manage.category.entity.CategoryConfig;
import com.dr.digital.manage.category.entity.CategoryConfigInfo;
import com.dr.digital.manage.form.entity.FileStructure;
import com.dr.digital.manage.form.entity.FileStructureInfo;
import com.dr.digital.manage.form.service.ArchiveFormDefinitionService;
import com.dr.digital.manage.log.entity.ArchivesLog;
import com.dr.digital.manage.log.entity.ArchivesLogInfo;
import com.dr.digital.manage.model.query.ArchiveDataQuery;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.ocr.entity.*;
import com.dr.digital.ofd.entity.OfdRecord;
import com.dr.digital.ofd.entity.OfdRecordInfo;
import com.dr.digital.register.entity.Category;
import com.dr.digital.register.entity.CategoryInfo;
import com.dr.digital.register.entity.Register;
import com.dr.digital.register.entity.RegisterInfo;
import com.dr.digital.register.service.RegisterService;
import com.dr.digital.uploadfiles.entity.JpgQueue;
import com.dr.digital.uploadfiles.entity.JpgQueueInfo;
import com.dr.framework.common.controller.BaseController;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.display.entity.FormDisplayScheme;
import com.dr.framework.common.form.display.entity.FormDisplaySchemeInfo;
import com.dr.framework.core.organise.entity.Organise;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.dr.framework.core.security.entity.Role;
import com.dr.framework.core.security.service.SecurityManager;
import com.dr.framework.core.web.annotations.Current;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ??????
 *
 * @author lc
 */
@RestController
@RequestMapping("api/register")
public class RegisterController extends BaseController<Register> {
    @Autowired
    RegisterService registerService;
    @Autowired
    SecurityManager securityManager;
    @Autowired
    ArchiveFormDefinitionService archiveFormService;
    @Resource
    CommonMapper commonMapper;

    @Override
    protected void onBeforePageQuery(HttpServletRequest request, SqlQuery<Register> sqlQuery, Register entity) {
        Organise organise = getOrganise(request);
        sqlQuery.like(RegisterInfo.ORGANISEID, organise.getId());
        sqlQuery.equal(RegisterInfo.ID, entity.getId());
        sqlQuery.like(RegisterInfo.BATCH_NAME, entity.getBatch_name());
        sqlQuery.orderByDesc(RegisterInfo.CREATEDATE);

        super.onBeforePageQuery(request, sqlQuery, entity);
    }

    @Override
    protected void onBeforeInsert(HttpServletRequest request, Register entity) {
        Person userLogin = getUserLogin(request);
        entity.setReceiver(userLogin.getUserName());
        super.onBeforeInsert(request, entity);
    }

    @RequestMapping("deleteByids")
    public ResultEntity<Register> delete(String ids) {
        String[] split = ids.split(",");
        for (String s : split) {
            String formDefinitionId = registerService.selectById(s).getFormDefinitionId();
            //??????????????????
            commonService.delete(SqlQuery.from(DisassemblyRecord.class).equal(DisassemblyRecordInfo.FORMDEFINITIONID,formDefinitionId));
            commonService.delete(SqlQuery.from(DisassemblyRecordDetail.class).equal(DisassemblyRecordDetailInfo.FORMDEFINITIONID,formDefinitionId));
            //????????????
            commonService.delete(SqlQuery.from(Register.class).equal(RegisterInfo.ID, s));
            //????????????
            SqlQuery<FlowPathDetail> equal = SqlQuery.from(FlowPathDetail.class).equal(FlowPathDetailInfo.PID, s);
            commonService.delete(equal);
            //??????????????????
            commonService.delete(SqlQuery.from(FormDisplayScheme.class).equal(FormDisplaySchemeInfo.FORMDEFINITIONID,formDefinitionId));
            //??????ocr??????
            commonService.delete(SqlQuery.from(OcrQueue.class).equal(OcrQueueInfo.BATCHID,s));
            //??????ocr????????????
            commonService.delete(SqlQuery.from(OcrRecord.class).equal(OcrRecordInfo.REGISTERID,s));
            //????????????????????????
            commonService.delete(SqlQuery.from(ArchivesLog.class).equal(ArchivesLogInfo.FORMDEFINITIONID,formDefinitionId));
            //??????metadata SELECT * FROM `digital_metadata` where formDefinitionId in (select formDefinitionId from digital_register where id = 'b72bec5ec26846bc83efa27a8c98d20f')
            commonService.delete(SqlQuery.from(Metadata.class).in(MetadataInfo.FORMDEFINITIONID,formDefinitionId));
            //??????jpg????????????
            commonService.delete(SqlQuery.from(JpgQueue.class).equal(JpgQueueInfo.FORMDEFINITIONID,formDefinitionId));
            //??????ofd??????
            commonService.delete(SqlQuery.from(OfdRecord.class).equal(OfdRecordInfo.FORMDEFINITIONID,formDefinitionId));
            //??????????????????
            //????????????????????????
            //????????????
            commonService.delete(SqlQuery.from(Category.class).equal(CategoryInfo.REGISTERID, s));
            //categoryConfig
            commonService.delete(SqlQuery.from(CategoryConfig.class).equal(CategoryConfigInfo.FILEFORMID,formDefinitionId));
            //??????typeFile
            commonService.delete(SqlQuery.from(TypeFile.class).equal(TypeFileInfo.REGISTERID,s));
            //??????file_structure?????????????????????
            commonService.delete(SqlQuery.from(FileStructure.class).equal(FileStructureInfo.REGISTERID,s));
            //????????????
            archiveFormService.deleteForm(formDefinitionId);



        }
        return ResultEntity.success();
    }

    /**
     * ?????????????????? ????????????
     *
     * @param id
     * @return
     */
    @RequestMapping("selectOneRegister")
    public ResultEntity<Register> selectOneRegister(String id) {
        return ResultEntity.success(commonService.selectOne(SqlQuery.from(Register.class).equal(RegisterInfo.ID, id)));
    }

    /**
     * ??????????????????????????????
     *
     * @param request
     * @param query
     * @param type
     * @param status  ????????????????????????????????????
     * @param queryContent
     * @param person
     * @return
     */
    @RequestMapping(value = "/updateStatus")
    public ResultEntity updateStatus(HttpServletRequest request,
                                            BaseQuery query,
                                            String type,
                                            String id,
                                            String status,
                                            @RequestParam(name = ArchiveDataQuery.QUERY_KEY, required = false) String queryContent,
                                            @Current Person person) {
        Assert.isTrue(!StringUtils.isEmpty(type), "???????????????????????????");
        query.parseQuery(queryContent);
        if (!StringUtils.isEmpty(id)) {
            query.getQueryItems().add(new ArchiveDataQuery.QueryItem("id", id, ArchiveDataQuery.QueryType.EQUAL));
        }
        query.getQueryItems().add(new ArchiveDataQuery.QueryItem("people_code", person.getId() + "," + "??????", ArchiveDataQuery.QueryType.IN));
        ResultEntity resultEntity = registerService.lhUpdateType(person, query, type, "", status);
        if (resultEntity.isSuccess()) {
            return ResultEntity.success();
        }else {
            return ResultEntity.error("????????????????????????");
        }

    }

    /**
     * ??????????????? ??????????????????
     *
     * @param request
     * @param query
     * @param type
     * @param queryContent
     * @param person
     * @return
     */
    @RequestMapping(value = "/lhUpdateType")
    public ResultEntity lhUpdateType(HttpServletRequest request,
                                     BaseQuery query,
                                     String type,
                                     String childFormId,
                                     @RequestParam(name = ArchiveDataQuery.QUERY_KEY, required = false) String queryContent,
                                     @Current Person person) {
        Assert.isTrue(!StringUtils.isEmpty(type), "???????????????????????????");
        query.parseQuery(queryContent);
        query.getQueryItems().add(new ArchiveDataQuery.QueryItem("people_code", person.getId() + "," + "??????", ArchiveDataQuery.QueryType.IN));
        return registerService.lhUpdateType(person, query, type, childFormId,"");
    }

    /**
     * ??????????????? ??????????????????
     *
     * @param ids
     * @param status
     * @param formDefinitionId
     * @param childFormId
     * @return
     */
    @RequestMapping(value = "/lhUpdateStatus")
    public ResultEntity lhUpdateStatus(String ids, String status, String formDefinitionId, String childFormId, String registerId, @Current Person person) {
        registerService.lhUpdateStatus(ids, status, formDefinitionId, childFormId, registerId, person);
        return ResultEntity.success();
    }

    /**
     * ????????????????????????
     *
     * @param ids
     * @param personIds
     * @return
     */
    @PostMapping("bindRoleUser")
    public ResultEntity<String> bindRoleUser(String ids, String personIds, String registerId) {
        if (!StringUtils.isEmpty(ids)) {
            for (String id : ids.split(",")) {
                securityManager.bindRoleUsers(id, personIds);
            }
        }
        return ResultEntity.success("????????????!");
    }

    /**
     * ?????????????????????
     *
     * @param ids
     * @return
     */
    @PostMapping("userRoles")
    public ResultEntity<List<Role>> userRoles(String ids) {
        List<Role> list = new ArrayList<>();
        if (!StringUtils.isEmpty(ids)) {
            for (String id : ids.split(",")) {
                list = securityManager.userRoles(id);
            }
        }
        return ResultEntity.success(list);
    }

    /**
     * ?????????????????????
     *
     * @param ids
     * @return
     */
    @PostMapping("userRoles1")
    public ResultEntity userRoles1(String ids) {
        List<Role> list = new ArrayList<>();
        if (!StringUtils.isEmpty(ids)) {
            for (String id : ids.split(",")) {
                list = securityManager.userRoles(id);
            }
        }
        return ResultEntity.success(list);
    }

    @RequestMapping("/getPercentage")
    public ResultEntity getPercentage(String businessId) throws IOException {
        return ResultEntity.success(registerService.getPercentag(businessId));
    }
    @RequestMapping("/getTotalPercentage")
    public ResultEntity getTotalPercentage(){
        return ResultEntity.success(registerService.getTotalPercentage());
    }
    @RequestMapping("/getFormDefinitionIdByRegister")
    public ResultEntity getFormDefinitionIdByRegister(String formDefinitionId){
        List<Register>  registers= commonMapper.selectByQuery(SqlQuery.from(Register.class, false).column(RegisterInfo.ORIGINAL_FORMAT, RegisterInfo.TARGET_FORMAT).equal(RegisterInfo.FORMDEFINITIONID, formDefinitionId));
        return ResultEntity.success(registers);
    }
}
