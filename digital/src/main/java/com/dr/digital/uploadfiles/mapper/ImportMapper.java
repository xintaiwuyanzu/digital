package com.dr.digital.uploadfiles.mapper;

import com.dr.digital.uploadfiles.entity.UploadFiles;
import com.dr.framework.core.orm.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

import static com.dr.digital.util.Constants.MODULE_NAME;

/**
 * @Description: 自定义mapper
 * @Author: Wang
 * @CreateDate: 2019-11-4 9:28
 */
@Mapper(module = MODULE_NAME)
public interface ImportMapper {

    /**
     * 表名、字段不固定的情况下，动态插入数据
     *
     * @param tablename
     * @param map
     * @return
     */
    @Insert("<script>" +
            " insert into ${tablename} " +
            " ( <foreach collection='map' index='key' item='value' separator=','>${key}</foreach> ) " +
            " values " +
            " ( <foreach collection='map' index='key' item='value' separator=','>#{value}</foreach> ) " +
            "</script>")
    void insetExcelData(@Param("tablename") String tablename, @Param("map") Map<String, Object> map);

    @Select("<script>" +
            "select A.updateDate as updateDate, A.status_info as status, A.srcName as srcName, A.filesTatus as filesTatus, A.thumbnailPath as thumbnailPath, A.parentId as parentId, A.filePath as filePath, A.fileSize as fileSize, A.batch as batch, A.bussinessId as bussinessId, A.updatePerson as updatePerson, A.thumbnailAbsolutePath as thumbnailAbsolutePath, A.absolutePath as absolutePath, A.id as id, A.createDate as createDate, A.createPerson as createPerson, A.order_info as orderBy, A.nextId as nextId from DIGITAL_UPLOADFILES A where"
            + " A.parentId = '' AND A.bussinessId = #{archivesId}" +
            "</script>")
    UploadFiles findUploadFilesByNullParentId(@Param("archivesId") String archivesId);

}
