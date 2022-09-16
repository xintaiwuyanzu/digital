package com.dr.digital.packet.service.impl;

import com.dr.digital.packet.entity.ArchiveMetadataRecord;
import com.dr.digital.packet.service.ArchiveMetadataRecordService;
import com.dr.framework.common.service.DefaultBaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ArchiveMetadataRecordServiceImpl extends DefaultBaseService<ArchiveMetadataRecord> implements ArchiveMetadataRecordService {

}
