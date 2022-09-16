package com.dr.digital.packet.entity;

import com.dr.digital.common.entity.abstractRecord;
import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseStatusEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;

/**
 * 封包记录
 */
@Table(name = Constants.TABLE_PREFIX + "PACKETRECORD", module = Constants.MODULE_NAME, comment = "封包记录表")
public class PacketRecord extends abstractRecord {

    @Column(comment = "封包批次号", length = 50, order = 2)
    private String batch_name;


    public String getBatch_name() {
        return batch_name;
    }

    public void setBatch_name(String batch_name) {
        this.batch_name = batch_name;
    }


}
