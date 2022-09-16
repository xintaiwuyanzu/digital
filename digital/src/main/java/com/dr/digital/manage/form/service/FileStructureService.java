package com.dr.digital.manage.form.service;

import com.dr.digital.manage.form.entity.FileStructure;
import com.dr.digital.manage.model.query.ArchiveDataQuery;
import com.dr.framework.common.entity.TreeNode;
import com.dr.framework.common.service.BaseService;

import java.util.List;

public interface FileStructureService extends BaseService<FileStructure> {
    //给前端树用的接口
    List<TreeNode> wjJgDataTree(FileStructure fileStructure);

    List<FileStructure> selectWjJgData(FileStructure fileStructure);

    FileStructure selectOneFileStructure(String registerId,String id);

}
