package com.dr.digital.configManager.service;

import com.dr.digital.configManager.bo.*;
import com.dr.digital.configManager.entity.ParamEntity;
import com.dr.digital.register.entity.Register;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.core.organise.entity.Person;

import java.util.ArrayList;
import java.util.List;

public interface FondDataService {
    String DICT_ARCHIVE_METADATA = "archive.metadata.";
    String DICT_ARCHIVE_METADATA_DEFAULT = "archive.metadata.default";

    List<FondInfo> getFondByOrgCode(Person person);

    List<CategoryInfo> getCategory(String arcTypes);

    List<CategoryBspDict> getArchiveBspDict(String typeId);

    ArrayList<ArchivedTypeFile> getArchivedTypeFile(String batchId,String code, String classify,  String arrange);

    List<MataDataInfo> getCategoryMetadata(String code, String classify, String standard, String arrange);

    ResultEntity insertRegister(Register register,Metadata metadata,Person person,String segment,String segmentName);

    void updateRegister(Register register, ParamEntity paramEntity, Person person);

    void bindRoleUsers(String id, String personIds);

    List<Metadata> getArchiveTypeSchema(String code);

    String getCategoryName(String code);
}
