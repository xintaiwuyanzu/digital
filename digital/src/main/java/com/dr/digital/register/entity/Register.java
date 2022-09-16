package com.dr.digital.register.entity;

import com.dr.digital.util.Constants;
import com.dr.framework.common.entity.BaseDescriptionEntity;
import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.Table;
import com.dr.framework.core.security.bo.PermissionResource;

@Table(name = Constants.TABLE_PREFIX + "REGISTER", module = Constants.MODULE_NAME, comment = "登记批次表")
public class Register extends BaseDescriptionEntity<String> implements PermissionResource {

    @Column(comment = "系统编号", length = 50, order = 1)
    private String system_code;
    @Column(comment = "批次号", length = 50, order = 2)
    private String batch_no;
    @Column(comment = "批次名称", length = 50, order = 2)
    private String batch_name;

    @Column(comment = "数据交换时间", length = 100, order = 3)
    private String send_time;

    @Column(comment = "归档信息包移交数量", length = 50, order = 4)
    private String send_number;

    @Column(comment = "归档信息包总字节数", length = 100, order = 5)
    private String send_size;

    @Column(comment = "移交人", length = 100, order = 6)
    private String transactor;

    @Column(comment = "全宗号", length = 50, order = 7)
    private String fonds_identifier;

    @Column(comment = "档案门类代码", length = 50, order = 8)
    private String archivers_category_code;

    @Column(comment = "表单方案", length = 50, order = 9)
    private String form_scheme;

    @Column(comment = "行政区划编码", length = 50, order = 10)
    private String region_code;

    @Column(comment = "机构编码", length = 50, order = 11)
    private String social_code;

    @Column(comment = "登记人", length = 200, order = 12)
    private String receiver;

    @Column(comment = "机构ID", length = 50, order = 13)
    private String organiseId;

    @Column(comment = "机构名称", length = 500, order = 14)
    private String organiseName;

    @Column(comment = "人员Id", length = 1000, order = 15)
    private String personId;

    @Column(comment = "角色Id", length = 1000, order = 15)
    private String roleId;

    @Column(comment = "整理方式", length = 50, order = 15)
    private String arrange;

    @Column(comment = "移交状态", length = 500, order = 15)
    private String handoverStatus;
    /**
     * 元数据显示信息  DA/T 46-2009 文书类电子文件元数据方案|纸质|件|2022-04-01至今
     */
    @Column(name = "metadataName", comment = "元数据名称")
    private String metadataName;
    /**
     * 门类名称  文书档案/公文
     */
    @Column(comment = "门类名称")
    private String archivers_category_code_name;
    /**
     * 方案名称  DA/T 46-2009 文书类电子文件元数据方案
     */
    @Column(comment = "方案名称")
    private String codeFormData;
    /**
     * 案卷 ： 1:案件 2：案卷---
     */
    @Column(comment = "案卷名称")
    private String arrangeFormData;

    @Column(comment = "表单id")
    private String formDefinitionId;

    /**
     * 1.PDF 2.TIF 3.纸质
     */
    @Column(comment = "原文格式", length = 50, order = 10)
    private String original_format;

    /**
     * 1.PDF 2.OFD
     */
    @Column(comment = "目标格式", length = 50, order = 10)
    private String target_format;
    /**
     * 1.最高 2.一般 3.最低（初始值,系统默认）
     */
    @Column(comment = "批次优先级", length = 50, order = 10)
    private String priority;

    public Register(){
        this.setPriority("3");
    }
    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getBatch_no() {
        return batch_no;
    }

    public void setBatch_no(String batch_no) {
        this.batch_no = batch_no;
    }

    public String getFormDefinitionId() {
        return formDefinitionId;
    }

    public void setFormDefinitionId(String formDefinitionId) {
        this.formDefinitionId = formDefinitionId;
    }

    public String getMetadataName() {
        return metadataName;
    }

    public void setMetadataName(String metadataName) {
        this.metadataName = metadataName;
    }

    public String getArchivers_category_code_name() {
        return archivers_category_code_name;
    }

    public void setArchivers_category_code_name(String archivers_category_code_name) {
        this.archivers_category_code_name = archivers_category_code_name;
    }

    public String getCodeFormData() {
        return codeFormData;
    }

    public void setCodeFormData(String codeFormData) {
        this.codeFormData = codeFormData;
    }

    public String getArrangeFormData() {
        return arrangeFormData;
    }

    public void setArrangeFormData(String arrangeFormData) {
        this.arrangeFormData = arrangeFormData;
    }

    public String getSystem_code() {
        return system_code;
    }

    public void setSystem_code(String system_code) {
        this.system_code = system_code;
    }

    public String getBatch_name() {
        return batch_name;
    }

    public void setBatch_name(String batch_name) {
        this.batch_name = batch_name;
    }

    public String getSend_time() {
        return send_time;
    }

    public void setSend_time(String send_time) {
        this.send_time = send_time;
    }

    public String getSend_number() {
        return send_number;
    }

    public void setSend_number(String send_number) {
        this.send_number = send_number;
    }

    public String getSend_size() {
        return send_size;
    }

    public void setSend_size(String send_size) {
        this.send_size = send_size;
    }

    public String getTransactor() {
        return transactor;
    }

    public void setTransactor(String transactor) {
        this.transactor = transactor;
    }

    public String getFonds_identifier() {
        return fonds_identifier;
    }

    public void setFonds_identifier(String fonds_identifier) {
        this.fonds_identifier = fonds_identifier;
    }

    public String getArchivers_category_code() {
        return archivers_category_code;
    }

    public void setArchivers_category_code(String archivers_category_code) {
        this.archivers_category_code = archivers_category_code;
    }

    public String getForm_scheme() {
        return form_scheme;
    }

    public void setForm_scheme(String form_scheme) {
        this.form_scheme = form_scheme;
    }

    public String getRegion_code() {
        return region_code;
    }

    public void setRegion_code(String region_code) {
        this.region_code = region_code;
    }

    public String getSocial_code() {
        return social_code;
    }

    public void setSocial_code(String social_code) {
        this.social_code = social_code;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getOrganiseId() {
        return organiseId;
    }

    public void setOrganiseId(String organiseId) {
        this.organiseId = organiseId;
    }

    public String getOrganiseName() {
        return organiseName;
    }

    public void setOrganiseName(String organiseName) {
        this.organiseName = organiseName;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getArrange() {
        return arrange;
    }

    public void setArrange(String arrange) {
        this.arrange = arrange;
    }

    public String getHandoverStatus() {
        return handoverStatus;
    }

    public void setHandoverStatus(String handoverStatus) {
        this.handoverStatus = handoverStatus;
    }

    public String getOriginal_format() {
        return original_format;
    }

    public void setOriginal_format(String original_format) {
        this.original_format = original_format;
    }

    public String getTarget_format() {
        return target_format;
    }

    public void setTarget_format(String target_format) {
        this.target_format = target_format;
    }
}
