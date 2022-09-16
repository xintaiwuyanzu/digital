package com.dr.digital.manage.form.service.impl;

import com.dr.digital.manage.form.entity.FileStructure;
import com.dr.digital.manage.form.entity.FileStructureInfo;
import com.dr.digital.manage.form.service.FileStructureService;
import com.dr.framework.common.entity.TreeNode;
import com.dr.framework.common.service.DefaultBaseService;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileStructureServiceImpl extends DefaultBaseService<FileStructure> implements FileStructureService {
    @Value("${filePath}")
    private String filePath;

    @Override
    public List<TreeNode> wjJgDataTree(FileStructure fileStructure) {
        List<FileStructure> formDataList = selectWjJgData(fileStructure);
        List<TreeNode> treeNodes = new ArrayList<>();
        for (FileStructure structure : formDataList) {
            int start = Integer.parseInt(StringUtils.isEmpty(structure.getPage_number()) ? "0" : structure.getPage_number());
            File file = new File(filePath + File.separator + "filePath" + File.separator + structure.getFonds_identifier() + File.separator + structure.getAj_archival_code());
            if (!file.exists()) {
                file.mkdirs();
            }
            TreeNode treeNode = new TreeNode(structure.getId(), structure.getFile_type(), new ArrayList());
            treeNode.setLevel(1);
            treeNode.setDescription(structure.getRegisterId());
            treeNode.setOrder(start);
            treeNode.setParentId(structure.getId());
            treeNode.setData(structure);
            treeNodes.add(treeNode);
        }
        return treeNodes;
    }

    @Override
    public List<FileStructure> selectWjJgData(FileStructure fileStructure) {
        //查询这条案卷是否存在子类数据 如果存在将跳过，不存在才创建
        SqlQuery<FileStructure> sqlQuery = SqlQuery.from(FileStructure.class)
                .equal(FileStructureInfo.ARCHIVERS_CATEGORY_CODE, fileStructure.getArchivers_category_code())
                .equal(FileStructureInfo.AJ_ARCHIVAL_CODE, fileStructure.getAj_archival_code())
                .equal(FileStructureInfo.REGISTERID, fileStructure.getRegisterId())
                .orderBy(FileStructureInfo.ARCHIVAL_CODE);
        return commonMapper.selectByQuery(sqlQuery);
    }

    @Override
    public FileStructure selectOneFileStructure(String registerId, String id) {
        SqlQuery<FileStructure> sqlQuery = SqlQuery.from(FileStructure.class);
        sqlQuery.equal(FileStructureInfo.ID, id).equal(FileStructureInfo.REGISTERID, registerId);
        return commonMapper.selectOneByQuery(sqlQuery);
    }
    
}
