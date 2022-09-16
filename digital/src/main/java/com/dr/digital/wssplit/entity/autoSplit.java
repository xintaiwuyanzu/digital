package com.dr.digital.wssplit.entity;

import com.dr.digital.common.entity.abstractRecord;
import com.dr.digital.util.Constants;
import com.dr.framework.core.orm.annotations.Table;

/**
 * @author Mr.Zhu
 * @date 2022/8/15 - 16:57
 */
@Table(name = Constants.TABLE_PREFIX + "AUTOSPLIT", module = Constants.MODULE_NAME, comment = "拆分规则表")
public class autoSplit extends abstractRecord {


}
