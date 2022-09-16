package com.dr.digital.manage.log.entity;


import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.ColumnType;
import com.dr.framework.core.orm.annotations.Table;

@Table(name = Constants.TABLE_PREFIX + "ARCHIVESLOG", module = Constants.MODULE_NAME, comment = "档案操作日志表")
public class ArchivesLog extends BaseEntity {

    @Column(comment = "批次ID", length = 100, order = 1)
    private String registerId;

    @Column(comment = "批次表单id", length = 100, order = 1)
    private String formDefinitionId;

    @Column(comment = "案卷题名", length = 500, order = 2)
    private String anJuanTiMing;

    @Column(comment = "档号", length = 500, order = 6)
    private String dangHao;

    @Column(comment = "盒号", length = 500, order = 6)
    private String boxNumber;

    @Column(comment = "操作人", length = 500, order = 9)
    private String operatorName;

    @Column(comment = "操作时间", length = 500, order = 10, type = ColumnType.DATE)
    private long operatorDate;

    @Column(comment = "操作环节", length = 500, order = 11)
    private String caoZuoHuanJie;

    @Column(comment = "目标环节", length = 500, order = 11)
    private String muBiaoHuanJie;

    @Column(comment = "操作类型", length = 500, order = 11)
    private String caoZuoLeiXing;

    @Column(name = "logDescription", comment = "日志描述")
    private String logDescription;

    private String archiveId;

    public String getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(String archiveId) {
        this.archiveId = archiveId;
    }

    public String getRegisterId() {
        return registerId;
    }

    public void setRegisterId(String registerId) {
        this.registerId = registerId;
    }

    public String getAnJuanTiMing() {
        return anJuanTiMing;
    }

    public void setAnJuanTiMing(String anJuanTiMing) {
        this.anJuanTiMing = anJuanTiMing;
    }

    public String getDangHao() {
        return dangHao;
    }

    public void setDangHao(String dangHao) {
        this.dangHao = dangHao;
    }

    public String getBoxNumber() {
        return boxNumber;
    }

    public void setBoxNumber(String boxNumber) {
        this.boxNumber = boxNumber;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public long getOperatorDate() {
        return operatorDate;
    }

    public void setOperatorDate(long operatorDate) {
        this.operatorDate = operatorDate;
    }

    public String getCaoZuoHuanJie() {
        return caoZuoHuanJie;
    }

    public void setCaoZuoHuanJie(String caoZuoHuanJie) {
        this.caoZuoHuanJie = caoZuoHuanJie;
    }

    public String getLogDescription() {
        return logDescription;
    }

    public void setLogDescription(String logDescription) {
        this.logDescription = logDescription;
    }

    public String getMuBiaoHuanJie() {
        return muBiaoHuanJie;
    }

    public void setMuBiaoHuanJie(String muBiaoHuanJie) {
        this.muBiaoHuanJie = muBiaoHuanJie;
    }

    public String getCaoZuoLeiXing() {
        return caoZuoLeiXing;
    }

    public void setCaoZuoLeiXing(String caoZuoLeiXing) {
        this.caoZuoLeiXing = caoZuoLeiXing;
    }

    public String getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(String formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }
}
