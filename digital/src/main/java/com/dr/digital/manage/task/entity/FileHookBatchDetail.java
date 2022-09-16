package com.dr.digital.manage.task.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.core.orm.annotations.Table;

/**
 * @author caor
 * @date 2021-03-29 14:16
 */
@Table(name = Constants.TABLE_PREFIX + "BATCH_DETAIL_HOOK", comment = "挂接批次详情信息", module = Constants.MODULE_NAME)
public class FileHookBatchDetail extends AbstractBatchDetailEntity {
}
