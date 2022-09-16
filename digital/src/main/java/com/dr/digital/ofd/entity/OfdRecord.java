package com.dr.digital.ofd.entity;

import com.dr.digital.common.entity.abstractRecord;
import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseDescriptionEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;
import com.dr.framework.core.security.bo.PermissionResource;

@Table(name = Constants.TABLE_PREFIX + "OFDRECORD", module = Constants.MODULE_NAME, comment = "ofd转换记录表")
public class OfdRecord extends abstractRecord {
    
}
