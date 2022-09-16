package com.dr.digital.packet.entity;

import com.alibaba.fastjson.JSONObject;
import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseStatusEntity;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;
import com.dr.framework.core.security.SecurityHolder;

/**
 * 档案元数据变更记录表
 *
 * @author hyj
 */

@Table(name = Constants.TABLE_PREFIX + "record", module = Constants.MODULE_NAME, comment = "档案元数据变更记录表")
public class ArchiveMetadataRecord extends BaseStatusEntity<String> {

    @Column(comment = "调整申请表id", length = 100)
    private String applyId;

    @Column(comment = "表单Id", length = 100)
    private String formDefinitionId;

    @Column(comment = "档案数据id", length = 100)
    private String formDataId;

    @Column(comment = "档号", length = 100)
    private String archiveCode;

    private FormData oldFormData;

    private FormData newFormData;

    @Column(comment = "变更人名称", length = 100)
    private String changePersonName;

    @Column(comment = "变更人id", length = 100)
    private String changePersonId;

    @Column(comment = "变更类型", length = 100)
    private String changeType;

    @Column(comment = "全宗编码", length = 100)
    private String fondCode;

    @Column(comment = "全宗Id", length = 100)
    private String fondId;

    @Column(comment = "全宗名称", length = 100)
    private String fondName;

    @Column(comment = "门类编码", length = 100)
    private String categoryCode;

    @Column(comment = "门类Id", length = 100)
    private String categoryId;

    @Column(comment = "门类名称", length = 100)
    private String categoryName;

    @Column(comment = "老表单数据", length = 2500)
    private String oldStringFormData;

    @Column(comment = "新表单数据", length = 2500)
    private String newStringFormData;

    @Column(comment = "接收类型", length = 100)
    private String receiveType;

    public String getReceiveType() {
        return receiveType;
    }

    public void setReceiveType(String receiveType) {
        this.receiveType = receiveType;
    }

    public ArchiveMetadataRecord(FormData oldFormData, FormData newFormData) {
        Person person = SecurityHolder.get().currentPerson();
        this.formDefinitionId = oldFormData.getFormDefinitionId();
        this.formDataId = oldFormData.getId();
        this.archiveCode = oldFormData.getString("ARCHIVE_CODE");
        this.oldFormData = oldFormData;
        this.newFormData = newFormData;
        this.oldStringFormData = JSONObject.toJSONString(oldFormData);
        this.newStringFormData = JSONObject.toJSONString(newFormData);
        this.changePersonId = person.getId();
        setCreatePerson(person.getId());
        setUpdatePerson(person.getId());
        setCreateDate(System.currentTimeMillis());
        setUpdateDate(System.currentTimeMillis());
        this.changePersonName = person.getUserName();
    }

    public ArchiveMetadataRecord(FormData newFormData) {
        Person person = SecurityHolder.get().currentPerson();
        this.formDefinitionId = newFormData.getFormDefinitionId();
        this.formDataId = newFormData.getId();
        this.archiveCode = newFormData.getString("ARCHIVE_CODE");
        this.newFormData = newFormData;
        this.newStringFormData = JSONObject.toJSONString(newFormData);
        this.changePersonId = person.getId();
        setCreatePerson(person.getId());
        setUpdatePerson(person.getId());
        setCreateDate(System.currentTimeMillis());
        setUpdateDate(System.currentTimeMillis());
        this.changePersonName = person.getUserName();
    }

    public String getFondCode() {
        return fondCode;
    }

    public void setFondCode(String fondCode) {
        this.fondCode = fondCode;
    }

    public String getFondId() {
        return fondId;
    }

    public void setFondId(String fondId) {
        this.fondId = fondId;
    }

    public String getFondName() {
        return fondName;
    }

    public void setFondName(String fondName) {
        this.fondName = fondName;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getChangePersonId() {
        return changePersonId;
    }

    public void setChangePersonId(String changePersonId) {
        this.changePersonId = changePersonId;
    }

    public ArchiveMetadataRecord() {
        super();
    }

    public String getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(String formDataId) {
        this.formDataId = formDataId;
    }

    public String getArchiveCode() {
        return archiveCode;
    }

    public void setArchiveCode(String archiveCode) {
        this.archiveCode = archiveCode;
    }

    public String getChangePersonName() {
        return changePersonName;
    }

    public void setChangePersonName(String changePersonName) {
        this.changePersonName = changePersonName;
    }

    public FormData getOldFormData() {
        return oldFormData;
    }

    public void setOldFormData(FormData oldFormData) {
        this.oldFormData = oldFormData;
    }

    public FormData getNewFormData() {
        return newFormData;
    }

    public void setNewFormData(FormData newFormData) {
        this.newFormData = newFormData;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getOldStringFormData() {
        return oldStringFormData;
    }

    public void setOldStringFormData(String oldStringFormData) {
        this.oldStringFormData = oldStringFormData;
    }

    public String getNewStringFormData() {
        return newStringFormData;
    }

    public void setNewStringFormData(String newStringFormData) {
        this.newStringFormData = newStringFormData;
    }

    public String getApplyId() {
        return applyId;
    }

    public void setApplyId(String applyId) {
        this.applyId = applyId;
    }

    public String getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(String formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }

}
