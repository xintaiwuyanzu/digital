package com.dr.digital.manage.log.service;

import com.dr.digital.manage.log.entity.ArchivesLog;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;

import java.util.List;
import java.util.Map;

public interface ArchivesLogService {

    void addArchiveLog(String registerId, String userName, String dangHao, String link, String title, String describe, String box);

    void addArchiveLog(Person person, ArchivesLog archivesLog);

    void removeAll(String id, boolean isAll, String registerId);

    void addArchiveFlowLog(String registerId, String userName, String status, String type, String judge, String s, String logDescription, String dangHao, String title, String box,String fid);

    List<ArchivesLog> selectArchivesLog(SqlQuery<ArchivesLog> sqlQuery);

    void deleteArchivesLog(SqlQuery<ArchivesLog> sqlQuery);

}
