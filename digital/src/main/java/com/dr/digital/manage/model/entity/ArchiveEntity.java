package com.dr.digital.manage.model.entity;

/**
 * 抽象接口
 * 用来定义档案相关变量和基础类
 *
 * @author dr
 */
public interface ArchiveEntity {
    /*****这里定义档案表单常用的变量名称*********/
    /**
     * 来源
     */
    String COLUMN_SOURCE_TYPE = "provenance";
    /**
     * 档案馆代码
     */
    String COLUMN_DANGANGUAN_CODE = "archivers_identifier";
    /**
     * 档案门类代码
     */
    String COLUMN_CATEGORY_CODE = "archivers_category_code";
    /**
     * 分类号
     */
    String COLUMN_CLASS_CODE = "class_code";

    /**
     * 档号
     */
    String COLUMN_ARCHIVE_CODE = "archival_code";
    /**
     * 全宗号
     */
    String COLUMN_FOND_CODE = "fonds_identifier";
    /**
     * 全宗名称
     */
    String COLUMN_FONDS_NAME = "fonds_name";

    /**
     * 立档单位名称
     */
    String COLUMN_FONDS_CONSTITUTING_UNIT_NAME = "fonds_constituting_unit_name";

    /**
     * 目录号
     */
    String COLUMN_CATALOGUE_CODE = "catalogue_number";
    /**
     * 年度
     */
    String COLUMN_YEAR = "archivers_year";
    /**
     * 保管期限
     */
    String COLUMN_SAVE_TERM = "retention_period";
    /**
     * 机构或问题
     */
    String COLUMN_ORG_CODE = "organizational_structure_or_function";
    /**
     * 页号
     */
    String COLUMN_YH = "page_number";

    /**
     * 题名
     */
    String COLUMN_TITLE = "title";
    /**
     * 责任者
     */
    String COLUMN_DUTY_PERSON = "author";
    /**
     * 文号
     */
    String COLUMN_FILECODE = "document_number";
    /**
     * 文件形成日期
     */
    String COLUMN_FILETIME = "date_time";
    /**
     * 主题词
     */
    String COLUMN_DESCRIPTOR = "descriptor";
    /**
     * 关键词
     */
    String COLUMN_KEY_WORDS = "keyword";
    /**
     * 密级
     */
    String COLUMN_SECURITY_LEVEL = "security_classification";

    /**
     * 页数
     */
    String COLUMN_YS = "total_number_of_pages";
    /**
     * 件数
     */
    String COLUMN_JS = "total_number_of_items";
    /**
     * 馆编件号
     */
    String COLUMN_JH = "archives_item_number";
    /**
     * 馆编案卷号
     */
    String COLUMN_AJH = "archives_file_number";

    /**
     * 存储位置
     */
    String COLUMN_WZ = "storage_location";
    /**
     * 解密划控
     */
    String COLUMN_DECRYPTION_CONTROL = "decryption_control";
    /**
     * 备注
     */
    String COLUMN_NOTE = "note";


    /************************** 业务常量 ********************************************/
    /**
     * 目录页数
     */
    String COLUMN_CATALOG_NUM = "catalog_num";
    /**
     * 是否有原文信息
     */
    String COLUMN_YW_HAVE = "yw_have";
    /**
     * 案卷档号
     */
    String COLUMN_AJDH = "aj_archival_code";
    /**
     * 转换状态 0:初始值 1：jpg 2：pdf 3:ofd
     */
    String COLUMN_TRANSITION_STATE = "transition_state";
    /**
     * 拆分状态  1
     */
    String COLUMN_SPLIT_STATE = "split_state";
    /**
     * 识别状态 1
     */
    String COLUMN_DISTINGUISH_STATE = "distinguish_state";
    /**
     * 拆件状态 1
     */
    String COLUMN_DISASSEMBLY_STATE = "disassembly_state";
    /**
     * 封包上传状态
     */
    String COLUMN_PACKET_STATE = "packet_state";
    /**
     * 质检状态
     */
    String COLUMN_QUALITY_STATE = "quality_state";

    /**
     * 质检进行状态 0未质检 1正在质检 2质检结束
     */
    String COLUMN_QUALITY_CONDUCT_STATE = "quality_conduct_state";

    /**
     * 异步状态 0未异步 1正在异步操作
     */
    String COLUMN_ASYNC_STATE = "async_state";

    /**
     * 拆件之后是否都在文件夹内状态 0没有都在文件夹内 1都在文件夹内
     */
    String COLUMN_FOLDER_STATE = "folder_state";

    /**
     * 退回环节
     */
    String COLUMN_EXIT_FLOW_PATH = "exit_flow_path";

    /**
     * 手动拆件标注 0无标注，1，有标注
     */
    String DISASSEMBLY_TAGGING= "disassembly_tagging";
    /**
     * 文件类型
     */
    String COLUMN_FILE_TYPE = "file_type";
    /**
     * 当前环节
     */
    String COLUMN_STATUS = "status_info";
    /**
     * 排序
     */
    String COLUMN_ORDER_NAME = "order_info";

    /**
     * 主键
     */
    String ID_COLUMN_NAME = "id";
    //****************************临时占用这两个字段。添加新字段后改这两个即可
    /**
     * 操作人名称
     */
    String PEOPLE_NAME = "people_name";
    /**
     * 操作人唯一标识
     */
    String PEOPEL_CODE = "people_code";

    /**
     * 数据清洗标志
     */
    String DATA_CLEANING = "data_cleaning";
}
