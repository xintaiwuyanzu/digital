package com.dr.digital.packet.controller;

import com.dr.digital.ofd.service.PacketRecordService;
import com.dr.digital.packet.entity.PacketRecord;
import com.dr.digital.packet.entity.PacketRecordInfo;
import com.dr.framework.common.controller.BaseController;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/packetRecord")
public class PacketRecordController extends BaseController<PacketRecord> {

    @Autowired
    PacketRecordService packetRecordService;

    protected void onBeforePageQuery(HttpServletRequest request, SqlQuery<PacketRecord> sqlQuery, PacketRecord entity) {
        if (!StringUtils.isEmpty(entity.getTitle())) {
            sqlQuery.like(PacketRecordInfo.TITLE, entity.getTitle());
        }
        if (!StringUtils.isEmpty(entity.getArchiveCode())) {
            sqlQuery.like(PacketRecordInfo.ARCHIVECODE, entity.getArchiveCode());
        }
        super.onBeforePageQuery(request, sqlQuery, entity);
    }

    @RequestMapping("/removePacket")
    public ResultEntity removePacket(String id){
        packetRecordService.removePacket(id);
        return ResultEntity.success();
    }
}
