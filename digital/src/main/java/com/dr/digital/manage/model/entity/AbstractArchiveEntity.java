package com.dr.digital.manage.model.entity;

import com.dr.framework.core.orm.annotations.Column;
import com.dr.framework.core.orm.annotations.ColumnType;

/**
 * 档案表抽象父类，用来统一规定各种类型的档案基本字段
 * <p>
 * 业务表可以继承这个类，然后添加自己的字段即可
 * <p>
 *
 * <strong>
 * 这个类可以用来做业务逻辑判断，增删改查不能用该类
 * <strong/>
 *
 * @author dr
 */
public class AbstractArchiveEntity implements ArchiveEntity {
    @Column(name = COLUMN_ORDER_NAME, comment = "排序", length = 10, order = 36)
    private String order_info;
    @Column(name = COLUMN_DANGANGUAN_CODE, type = ColumnType.VARCHAR, comment = "档案馆代码", length = 100, order = 2)
    private String archivers_identifier;
    @Column(name = COLUMN_CATEGORY_CODE, comment = "档案门类代码", order = 3)
    private String archivers_category_code;
    @Column(name = COLUMN_ARCHIVE_CODE, comment = "档号", order = 4)
    private String archival_code;
    @Column(name = COLUMN_FOND_CODE, comment = "全宗号", order = 5)
    private String fonds_identifier;
    @Column(name = COLUMN_FONDS_NAME, comment = "全宗名称", order = 6)
    private String fonds_name;
    @Column(name = COLUMN_CATALOGUE_CODE, comment = "目录号", order = 7)
    private String catalogue_number;
    @Column(name = COLUMN_SAVE_TERM, comment = "保管期限", length = 100, order = 8)
    private String retention_period;
    @Column(name = COLUMN_YEAR, comment = "年度", length = 100, order = 9)
    private String year;
    @Column(name = COLUMN_ORG_CODE, comment = "机构或问题", order = 10)
    private String organizational_structure_or_function;
    @Column(name = COLUMN_JH, comment = "馆编件号", order = 7)
    private String archives_item_number;
    @Column(name = COLUMN_TITLE, comment = "题名", length = 500, order = 11)
    private String title;
    @Column(name = COLUMN_DUTY_PERSON, comment = "责任者", length = 100, order = 12)
    private String author;
    @Column(name = COLUMN_FILECODE, comment = "文件编号", order = 13)
    private String document_number;
    @Column(name = COLUMN_YH, comment = "页号", order = 14)
    private String page_number;
    @Column(name = COLUMN_YS, comment = "页数", order = 15)
    private String total_number_of_pages;
    @Column(name = COLUMN_FILETIME, comment = "文件形成日期", length = 80, order = 16)
    private String date_time;
    @Column(name = COLUMN_STATUS, comment = "当前环节", length = 10, order = 36)
    private String status;
    @Column(name = COLUMN_TRANSITION_STATE, comment = "转换状态", length = 10, order = 36)
    private String transition_state;
    @Column(name = COLUMN_SPLIT_STATE, comment = "拆分状态", length = 10, order = 36)
    private String split_state;
    @Column(name = COLUMN_DISTINGUISH_STATE, comment = "识别状态", length = 10, order = 36)
    private String distinguish_state;
    @Column(name = COLUMN_DISASSEMBLY_STATE, comment = "拆件状态", length = 10, order = 36)
    private String disassembly_state;
    @Column(name = COLUMN_PACKET_STATE, comment = "封包上传状态", length = 10, order = 37)
    private String packet_state;
    @Column(name = COLUMN_QUALITY_STATE, comment = "质检状态")
    private String quality_state;
    @Column(name = COLUMN_QUALITY_CONDUCT_STATE, comment = "质检进行状态")
    private String quality_conduct_state;
    @Column(name = COLUMN_ASYNC_STATE, comment = "异步状态")
    private String async_state;
    @Column(name = COLUMN_FOLDER_STATE, comment = "文件夹状态")
    private String folder_state;
    @Column(name = COLUMN_EXIT_FLOW_PATH, comment = "退回环节", type = ColumnType.CLOB)
    private String exit_flow_path;
    @Column(name = DISASSEMBLY_TAGGING, comment = "标注", type = ColumnType.CLOB)
    private String disassembly_tagging;
    @Column(name = COLUMN_NOTE, comment = "备注", type = ColumnType.CLOB)
    private String note;
    @Column(name = PEOPLE_NAME, comment = "操作人", type = ColumnType.CLOB, order = 42)
    private String people_name;
    @Column(name = PEOPEL_CODE, comment = "操作人标识", type = ColumnType.CLOB, order = 41)
    private String people_code;

    @Column(name = DATA_CLEANING, comment = "数据清洗标识", type = ColumnType.CLOB, order = 42)
    private String data_cleaning;

    public String getFolder_state() {
        return folder_state;
    }

    public void setFolder_state(String folder_state) {
        this.folder_state = folder_state;
    }

    public String getAsync_state() {
        return async_state;
    }

    public void setAsync_state(String async_state) {
        this.async_state = async_state;
    }

    public String getQuality_conduct_state() {
        return quality_conduct_state;
    }

    public void setQuality_conduct_state(String quality_conduct_state) {
        this.quality_conduct_state = quality_conduct_state;
    }

    public String getData_cleaning() {
        return data_cleaning;
    }

    public void setData_cleaning(String data_cleaning) {
        this.data_cleaning = data_cleaning;
    }

    public String getExit_flow_path() {
        return exit_flow_path;
    }

    public void setExit_flow_path(String exit_flow_path) {
        this.exit_flow_path = exit_flow_path;
    }

    public String getPeople_name() {
        return people_name;
    }

    public void setPeople_name(String people_name) {
        this.people_name = people_name;
    }

    public String getPeople_code() {
        return people_code;
    }

    public void setPeople_code(String people_code) {
        this.people_code = people_code;
    }

    public String getQuality_state() {
        return quality_state;
    }

    public String getDisassembly_tagging() {
        return disassembly_tagging;
    }

    public void setDisassembly_tagging(String disassembly_tagging) {
        this.disassembly_tagging = disassembly_tagging;
    }

    public void setQuality_state(String quality_state) {
        this.quality_state = quality_state;
    }

    public String getOrder_info() {
        return order_info;
    }

    public void setOrder_info(String order_info) {
        this.order_info = order_info;
    }

    public String getArchivers_identifier() {
        return archivers_identifier;
    }

    public void setArchivers_identifier(String archivers_identifier) {
        this.archivers_identifier = archivers_identifier;
    }

    public String getArchivers_category_code() {
        return archivers_category_code;
    }

    public void setArchivers_category_code(String archivers_category_code) {
        this.archivers_category_code = archivers_category_code;
    }

    public String getArchival_code() {
        return archival_code;
    }

    public void setArchival_code(String archival_code) {
        this.archival_code = archival_code;
    }

    public String getFonds_identifier() {
        return fonds_identifier;
    }

    public void setFonds_identifier(String fonds_identifier) {
        this.fonds_identifier = fonds_identifier;
    }

    public String getFonds_name() {
        return fonds_name;
    }

    public void setFonds_name(String fonds_name) {
        this.fonds_name = fonds_name;
    }

    public String getCatalogue_number() {
        return catalogue_number;
    }

    public void setCatalogue_number(String catalogue_number) {
        this.catalogue_number = catalogue_number;
    }

    public String getRetention_period() {
        return retention_period;
    }

    public void setRetention_period(String retention_period) {
        this.retention_period = retention_period;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getOrganizational_structure_or_function() {
        return organizational_structure_or_function;
    }

    public void setOrganizational_structure_or_function(String organizational_structure_or_function) {
        this.organizational_structure_or_function = organizational_structure_or_function;
    }

    public String getArchives_item_number() {
        return archives_item_number;
    }

    public void setArchives_item_number(String archives_item_number) {
        this.archives_item_number = archives_item_number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDocument_number() {
        return document_number;
    }

    public void setDocument_number(String document_number) {
        this.document_number = document_number;
    }

    public String getPage_number() {
        return page_number;
    }

    public void setPage_number(String page_number) {
        this.page_number = page_number;
    }

    public String getTotal_number_of_pages() {
        return total_number_of_pages;
    }

    public void setTotal_number_of_pages(String total_number_of_pages) {
        this.total_number_of_pages = total_number_of_pages;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransition_state() {
        return transition_state;
    }

    public void setTransition_state(String transition_state) {
        this.transition_state = transition_state;
    }

    public String getSplit_state() {
        return split_state;
    }

    public void setSplit_state(String split_state) {
        this.split_state = split_state;
    }

    public String getDistinguish_state() {
        return distinguish_state;
    }

    public void setDistinguish_state(String distinguish_state) {
        this.distinguish_state = distinguish_state;
    }

    public String getDisassembly_state() {
        return disassembly_state;
    }

    public void setDisassembly_state(String disassembly_state) {
        this.disassembly_state = disassembly_state;
    }

    public String getPacket_state() {
        return packet_state;
    }

    public void setPacket_state(String packet_state) {
        this.packet_state = packet_state;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
