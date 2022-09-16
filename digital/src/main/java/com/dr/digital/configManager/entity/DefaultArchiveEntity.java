package com.dr.digital.configManager.entity;

import com.dr.digital.manage.model.entity.ArchiveEntity;

import java.util.HashMap;
import java.util.Map;

public class DefaultArchiveEntity {

    private String title = ArchiveEntity.COLUMN_TITLE;

    private String archiveCode = ArchiveEntity.COLUMN_ARCHIVE_CODE;

    private String fondCode = ArchiveEntity.COLUMN_FOND_CODE;

    private String transitionStatus = ArchiveEntity.COLUMN_TRANSITION_STATE;

    private String packetStatus = ArchiveEntity.COLUMN_PACKET_STATE;

    private String fileType = ArchiveEntity.COLUMN_FILE_TYPE;

    public Map<String, String> getDefaultArchiveEntity() {
        Map<String, String> map = new HashMap();
        map.put(title, "题名");
        map.put(archiveCode, "档号");
        map.put(fondCode, "全宗号");
        map.put(transitionStatus, "转换状态");
        map.put(packetStatus, "封包上传状态");
        map.put(fileType, "文件类型");
        return map;
    }

}
