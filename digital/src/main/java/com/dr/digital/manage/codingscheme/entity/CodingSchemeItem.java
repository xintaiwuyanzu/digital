package com.dr.digital.manage.codingscheme.entity;

import com.dr.digital.manage.model.entity.BaseBusinessIdEntity;
import com.dr.digital.util.Constants;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;


/**
 * 编码方案项
 *
 * @author tzl
 * @date 2020/6/2 11:32
 */
@Table(name = Constants.TABLE_PREFIX + "CODING_SCHEME_ITEM", module = Constants.MODULE_NAME, comment = "编码方案项")
public class CodingSchemeItem extends BaseBusinessIdEntity {
    /**
     * 下拉选择或者输入，前端写死
     */
    @Column(comment = "连接符", length = 50, order = 4)
    private String connector;

    public String getConnector() {
        return connector;
    }

    public void setConnector(String connector) {
        this.connector = connector;
    }

}
