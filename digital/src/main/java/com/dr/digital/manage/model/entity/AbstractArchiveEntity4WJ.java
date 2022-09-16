package com.dr.digital.manage.model.entity;

import com.dr.framework.core.orm.annotations.Column;

/**
 * 文件基本信息表
 *
 * @author jjl
 */
public class AbstractArchiveEntity4WJ extends AbstractArchiveEntity {
    @Column(name = COLUMN_AJH, comment = "馆编案卷号", length = 100)
    private String archives_file_number;
    @Column(name = COLUMN_AJDH, comment = "案卷档号")
    private String aj_archival_code;

    public String getArchives_file_number() {
        return archives_file_number;
    }

    public void setArchives_file_number(String archives_file_number) {
        this.archives_file_number = archives_file_number;
    }

    public String getAj_archival_code() {
        return aj_archival_code;
    }

    public void setAj_archival_code(String aj_archival_code) {
        this.aj_archival_code = aj_archival_code;
    }
}
