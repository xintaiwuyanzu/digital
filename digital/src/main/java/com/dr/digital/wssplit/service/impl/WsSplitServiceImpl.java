package com.dr.digital.wssplit.service.impl;

import com.dr.digital.manage.form.entity.FileStructure;
import com.dr.digital.manage.form.service.ArchiveDataManager;
import com.dr.digital.manage.form.service.FileStructureService;
import com.dr.digital.manage.model.entity.AbstractArchiveEntity;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.model.query.ArchiveDataQuery;
import com.dr.digital.manage.model.query.BaseQuery;
import com.dr.digital.util.UUIDUtils;
import com.dr.digital.wssplit.entity.WssplitTagging;
import com.dr.digital.wssplit.entity.WssplitTaggingInfo;
import com.dr.digital.wssplit.service.WsSplitService;
import com.dr.digital.wssplit.vo.WsSplit;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.entity.TreeNode;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.form.core.service.FormDataService;
import com.dr.framework.common.form.core.service.SqlBuilder;
import com.dr.framework.common.service.CommonService;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.jdbc.Column;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.dr.framework.core.security.SecurityHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class WsSplitServiceImpl implements WsSplitService {
    @Autowired
    FormDataService formDataService;
    @Autowired
    FileStructureService fileStructureService;
    @Autowired
    ArchiveDataManager dataManager;
    @Autowired
    CommonMapper commonMapper;
    @Autowired
    CommonService commonService;
    @Value("${filePath}")
    private String filePath;

    @Override
    public List<TreeNode> formDataPage(ArchiveDataQuery query) {
        List<FormData> formDataList = formDataService.selectFormData(query.getFormDefinitionId(), newBuilder(query));
        List<TreeNode> treeNodes = new ArrayList<>();
        for (FormData formData : formDataList) {
            int start = Integer.parseInt(StringUtils.isEmpty(formData.get(ArchiveEntity.COLUMN_YH)) ? "0" : formData.get(ArchiveEntity.COLUMN_YH));
            File file = new File(filePath + File.separator + "filePath" + File.separator + formData.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + formData.get(ArchiveEntity.COLUMN_AJDH));
            if (!file.exists()) {
                file.mkdirs();
            }
            TreeNode treeNode = new TreeNode(formData.getId(), formData.get(ArchiveEntity.COLUMN_FILE_TYPE), new ArrayList());
            treeNode.setLevel(1);
            treeNode.setDescription(formData.getFormDefinitionId());
            treeNode.setOrder(start);
            treeNode.setParentId(formData.getId());
            treeNode.setData(formData);
            treeNodes.add(treeNode);
        }
        return treeNodes;
    }

    public List<TreeNode> isDirectory(File file, String id, String formDefinitionId) {
        List<TreeNode> imgTreeNode = new ArrayList<>();
        if (file.exists()) {
            File[] files = file.listFiles();
            for (File fileA : files) {
                if (fileA.isFile()) {
                    String fileName = fileA.getName().toUpperCase();
                    // ???????????????
                    if (fileName.indexOf(".JPG") != -1 || fileName.indexOf(".PNG") != -1 || fileName.indexOf(".BMP") != -1 || fileName.indexOf(".TIF") != -1 || fileName.indexOf(".PCX") != -1 || fileName.indexOf(".JPEG") != -1) {
                        TreeNode treeNode = new TreeNode(UUIDUtils.getUUID(), fileA.getName(), formDefinitionId);
                        treeNode.setLevel(2);
                        treeNode.setParentId(id);
                        treeNode.setOrder(Integer.parseInt(fileA.getName().substring(0, fileA.getName().lastIndexOf("."))));
                        imgTreeNode.add(treeNode);
                    }
                }
            }
        }
        return imgTreeNode;
    }

    /**
     * ????????????????????????????????????
     *
     * @param query
     * @return
     */
    private SqlBuilder newBuilder(ArchiveDataQuery query) {
        return ((sqlQuery, wrapper) -> {
            for (ArchiveDataQuery.QueryItem item : query.getQueryItems()) {
                Column column = wrapper.getColumn(item.getKey());
                if (column == null) {
                    continue;
                }
                switch (item.getType()) {
                    case IN:
                        String[] data = item.getValue().split(",");
                        sqlQuery.in(column, data);
                        break;
                    case LIKE:
                        sqlQuery.like(column, item.getValue());
                        break;
                    case EQUAL:
                        sqlQuery.equal(column, item.getValue());
                        break;
                    case END_WITH:
                        sqlQuery.endingWith(column, item.getValue());
                        break;
                    case START_WITH:
                        sqlQuery.startingWith(column, item.getValue());
                        break;
                    default:
                        break;
                }
            }
            sqlQuery.orderBy(wrapper.getColumn(AbstractArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
    }

    /**
     * ????????????
     *
     * @param wsSplit
     */
    @Override
    public void changeTree(WsSplit wsSplit) {
        //???????????????????????????
        FormData bFormData = formDataService.selectOneFormData(wsSplit.getbFormDefinitionId(), wsSplit.getbFomId());
        //?????????????????????????????????
        int bOrder = wsSplit.getbOrder();
        //?????????????????????????????????
        FormData mFormData = formDataService.selectOneFormData(wsSplit.getmFormDefinitionId(), wsSplit.getmFomId());
        //???????????????????????????????????????
        int mOrder = wsSplit.getmOrder();
        if ("after".equals(wsSplit.getDropType()) && bOrder > mOrder) {
            mOrder = mOrder + 1;
        } else if ("inner".equals(wsSplit.getDropType())) {
            if (bOrder + 1 != mOrder && 1 != mOrder && mOrder != bOrder) {
                mOrder = mOrder - 1;
            }
        } else if ("before".equals(wsSplit.getDropType()) && bOrder < mOrder) {
            if (bOrder + 1 != mOrder)
                mOrder = mOrder - 1;
        }
        String path = filePath + File.separator + "filePath" + File.separator + mFormData.get(ArchiveEntity.COLUMN_FOND_CODE) + File.separator + mFormData.get(ArchiveEntity.COLUMN_AJDH);
        //todo ???????????????????????????????????????????????????
        boolean flag = execSplit(wsSplit, path, bOrder, mOrder);
        //?????????????????????????????????????????????
        if (flag) {
            //??????????????????????????????????????????????????????????????????
            if (!wsSplit.getbFomId().equals(wsSplit.getmFomId())) {
                //??????????????????????????????
                List<FormData> formDataList = formDataService.selectFormData(bFormData.getFormDefinitionId(), (sqlQuery, formRelationWrapper) -> {
                    sqlQuery.like(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_AJDH), bFormData.get(ArchiveEntity.COLUMN_AJDH))
                            .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), bFormData.get(ArchiveEntity.COLUMN_STATUS) + "")
                            .orderBy(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
                });
                //?????????????????????????????????
                int bYH = Integer.parseInt(StringUtils.isEmpty(bFormData.get(ArchiveEntity.COLUMN_YH)) ? "0" : bFormData.get(ArchiveEntity.COLUMN_YH));
                int bYS = Integer.parseInt(StringUtils.isEmpty(bFormData.get(ArchiveEntity.COLUMN_YS)) ? "0" : bFormData.get(ArchiveEntity.COLUMN_YS));
                //???????????????????????????
                int mYS = Integer.parseInt(StringUtils.isEmpty(mFormData.get(ArchiveEntity.COLUMN_YS)) ? "0" : mFormData.get(ArchiveEntity.COLUMN_YS));
                int mYH = Integer.parseInt(StringUtils.isEmpty(mFormData.get(ArchiveEntity.COLUMN_YH)) ? "0" : mFormData.get(ArchiveEntity.COLUMN_YH));
                //?????????????????????????????????
                int sFlag = mOrder - bOrder;
                //???????????? ?????? ???????????? ??????0 ??????????????????
                if (sFlag > 0) { //?????????
                    //?????????????????????1
                    int newBYs = bYS - 1;
                    //?????????????????????1
                    int newMys = mYS + 1;
                    int newMyh = mYH - 1;
                    countZJYeHao(formDataList, bYH, mYH, -1);
                    //???????????????????????????
                    FormData nBFormData = formDataService.selectOneFormData(wsSplit.getbFormDefinitionId(), wsSplit.getbFomId());
                    nBFormData.put(ArchiveEntity.COLUMN_YS, newBYs);
                    formDataService.updateFormDataIgnoreNullById(nBFormData);
                    //?????????????????????????????????
                    FormData nMFormData = formDataService.selectOneFormData(wsSplit.getmFormDefinitionId(), wsSplit.getmFomId());
                    nMFormData.put(ArchiveEntity.COLUMN_YS, newMys);
                    nMFormData.put(ArchiveEntity.COLUMN_YH, newMyh);
                    formDataService.updateFormDataIgnoreNullById(nMFormData);
                } else if (sFlag == 0) {
                    //?????????????????????1
                    int nBYs = bYS - 1;
                    int nMys = mYS + 1;
                    countJXYeHao(formDataList, bYH, mYH, 1);
                    //???????????????????????????
                    FormData nBFormData = formDataService.selectOneFormData(wsSplit.getbFormDefinitionId(), wsSplit.getbFomId());
                    nBFormData.put(ArchiveEntity.COLUMN_YS, nBYs);
                    formDataService.updateFormDataIgnoreNullById(nBFormData);
                    //?????????????????????????????????
                    FormData nMFormData = formDataService.selectOneFormData(wsSplit.getmFormDefinitionId(), wsSplit.getmFomId());
                    nMFormData.put(ArchiveEntity.COLUMN_YS, nMys);
                    if (0 == mYS) {
                        nMFormData.put(ArchiveEntity.COLUMN_YH, mOrder);
                    }
                    formDataService.updateFormDataIgnoreNullById(nMFormData);
                } else {//??????
                    //?????????????????????1
                    int nBYs = bYS - 1;
                    int nBYh = bYH + 1;
                    int nMys = mYS + 1;
                    countJXYeHao(formDataList, bYH, mYH, 1);
                    //???????????????????????????
                    FormData nBFormData = formDataService.selectOneFormData(wsSplit.getbFormDefinitionId(), wsSplit.getbFomId());
                    nBFormData.put(ArchiveEntity.COLUMN_YS, nBYs);
                    nBFormData.put(ArchiveEntity.COLUMN_YH, nBYh);
                    formDataService.updateFormDataIgnoreNullById(nBFormData);
                    //?????????????????????????????????
                    FormData nMFormData = formDataService.selectOneFormData(wsSplit.getmFormDefinitionId(), wsSplit.getmFomId());
                    nMFormData.put(ArchiveEntity.COLUMN_YS, nMys);
                    formDataService.updateFormDataIgnoreNullById(nMFormData);
                }
            }
        }
    }

    @Override
    public void saveChangeTree(String pthohParaM, String volumesDataParaM) {
        String[] photoArray = pthohParaM.split(",");
        List<String> needDeal = new ArrayList();
        for (String photo : photoArray) {
            if ("".equals(photo) || photo == null) {
                continue;
            }
            String[] photoOne = photo.split("@");
            String oldFilePath = photoOne[2].replace("filePath", String.join(File.separator, filePath, "filePath"));
            File oldfile = new File(oldFilePath);
            String newTempFilePath = oldFilePath.substring(0, oldFilePath.lastIndexOf(File.separator)) + File.separator + getNewFileName(Integer.parseInt(photoOne[0])) + "_" + System.currentTimeMillis() + oldFilePath.substring(oldFilePath.lastIndexOf("."), oldFilePath.length());
            File newTempfile = new File(newTempFilePath);
            oldfile.renameTo(newTempfile);
            needDeal.add(newTempFilePath);
            //??????txt??????
            String txtFilePath = oldFilePath.replace("filePath", "txt").replace(".jpg", ".txt");
            File oldTxtFile = new File(txtFilePath);
            String newTxtFilePath = newTempFilePath.replace("filePath", "txt").replace(".jpg", ".txt");
            File newTxtFile = new File(newTxtFilePath);
            oldTxtFile.renameTo(newTxtFile);
        }
        String[] volumesData = volumesDataParaM.split(",");
        for (String volumesDataOne : volumesData) {
            if ("".equals(volumesDataOne) || volumesDataOne == null) {
                continue;
            }
            String[] volumesDataOneArray = volumesDataOne.split("@");
            FileStructure fileStructure = fileStructureService.selectOneFileStructure(volumesDataOneArray[5], volumesDataOneArray[6]);
            fileStructure.setTotal_number_of_pages(volumesDataOneArray[1]);
            fileStructure.setPage_number(volumesDataOneArray[0]);
            fileStructure.setFile_type(volumesDataOneArray[4]);
            if (!"undefined".equals(volumesDataOneArray[3])) {
                fileStructure.setDescription(volumesDataOneArray[3]);
            }
            fileStructure.setArchives_item_number(volumesDataOneArray[2]);
            fileStructureService.updateById(fileStructure);
            //?????????????????? ????????????????????????
        }
    }

    @Override
    public boolean uniquenessData(BaseQuery query) {
        //??????form????????????????????????????????????
        List<FormData> dataList = dataManager.findDataByQuery(query);
        if (dataList.size() > 0) {
            for (FormData formData : dataList) {
                Person person = SecurityHolder.get().currentPerson();
                String id = formData.get(ArchiveEntity.PEOPEL_CODE);
                //??????????????????????????????????????????????????????????????????????????????????????????
                //?????????????????????????????????
                if (!StringUtils.isEmpty(id)) {
                    if (id.equals(person.getId())) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    //???????????????
                    formData.put(ArchiveEntity.PEOPLE_NAME, person.getUserName());
                    formData.put(ArchiveEntity.PEOPEL_CODE, person.getId());
                    formDataService.updateFormDataById(formData);
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }


    /**
     * ??????????????????
     *
     * @param wsSplit
     * @param path
     * @param bOrder
     * @param mOrder
     * @return
     */
    public Boolean execSplit(WsSplit wsSplit, String path, int bOrder, int mOrder) {
        if ("inner".equals(wsSplit.getDropType()) && (bOrder + 1 == mOrder || bOrder == mOrder)) {
            return true;
        } else if ("before".equals(wsSplit.getDropType()) && bOrder + 1 == mOrder) {
            return true;
        }
        String houZu = wsSplit.getbImg().substring(wsSplit.getbImg().indexOf("."));
        //????????????????????????????????????????????????
        File newDir = new File(filePath + File.separator + "temp" + houZu);
        File bFile = new File(path + File.separator + wsSplit.getbImg());
        if (!bFile.isDirectory()) {
            bFile.renameTo(newDir);
        }
        boolean flag = false;
        //?????????????????????????????????
        int sFlag = mOrder - bOrder;
        //???????????? ?????? ???????????? ??????0 ??????????????????
        if (sFlag > 0) { //?????????
            for (int i = bOrder; i < mOrder; i++) {
                //?????????????????????????????????????????????????????????
                File oldFile = new File(path + File.separator + getNewFileName((i + 1)) + houZu);
                File newFile = new File(path + File.separator + getNewFileName(i) + houZu);
                if (!oldFile.isDirectory()) {
                    oldFile.renameTo(newFile);
                }
            }
        } else { //?????????
            for (int i = bOrder; i > mOrder; i--) {
                //?????????????????????????????????????????????????????????
                File oldFile = new File(path + File.separator + getNewFileName((i - 1)) + houZu);
                File newFile = new File(path + File.separator + getNewFileName(i) + houZu);
                if (!oldFile.isDirectory()) {
                    oldFile.renameTo(newFile);
                }
            }
        }
        //?????????????????????????????? ???????????? ??????
        File mFile = new File(path + File.separator + getNewFileName(mOrder) + houZu);
        if (!newDir.isDirectory()) {
            flag = newDir.renameTo(mFile);
        }
        return flag;
    }

    /**
     * ?????????????????????????????????
     *
     * @param formDataList
     * @param bYh
     * @param mYh
     * @param num
     */
    private void countZJYeHao(List<FormData> formDataList, int bYh, int mYh, int num) {
        for (FormData list : formDataList) {
            if ((mYh > Integer.parseInt(list.get(ArchiveEntity.COLUMN_YH)) && Integer.parseInt(list.get(ArchiveEntity.COLUMN_YH)) > bYh)) {
                int s = Integer.parseInt(list.get(ArchiveEntity.COLUMN_YH)) + num;
                list.put(ArchiveEntity.COLUMN_YH, getNewFileName(s));
                formDataService.updateFormDataById(list);
            }
        }
    }

    private void countJXYeHao(List<FormData> formDataList, int bYh, int mYh, int num) {
        for (FormData list : formDataList) {
            if (bYh > Integer.parseInt(list.get(ArchiveEntity.COLUMN_YH)) && Integer.parseInt(list.get(ArchiveEntity.COLUMN_YH)) > mYh) {
                int s = Integer.parseInt(list.get(ArchiveEntity.COLUMN_YH)) + num;
                list.put(ArchiveEntity.COLUMN_YH, getNewFileName(s));
                formDataService.updateFormDataById(list);
            }
        }
    }

    /**
     * ???????????????????????????
     *
     * @param num
     * @return
     */
    public String getNewFileName(int num) {
        String photoName = "";
        if (num < 10) {
            photoName = "00" + num;
        } else if (num >= 10 && num < 100) {
            photoName = "0" + num;
        } else {
            photoName = "" + num;
        }
        return photoName;
    }

    @Override
    public ResultEntity checkData(String formDefinitionId, String id) {

        List<FormData> formData = dataManager.selectLinkcheckData(formDefinitionId, id);
        if (formData.size() > 0) {
            for (FormData formDate : formData) {
                //ocr????????????
                String OCRD = formDate.get(ArchiveEntity.COLUMN_DISTINGUISH_STATE);
                //????????????
                String split = formDate.get(ArchiveEntity.COLUMN_SPLIT_STATE);
                //????????????
                String transition = formDate.get(ArchiveEntity.COLUMN_TRANSITION_STATE);
                //????????????
                String chaiJian = formDate.get(ArchiveEntity.COLUMN_DISASSEMBLY_STATE);
                if ("1".equals(OCRD) && "1".equals(split) && "1".equals(transition)&&!"0".equals(chaiJian)) {
                    return ResultEntity.success();
                } else {
                    return ResultEntity.error("?????????????????????????????????????????????");
                }
            }
            return ResultEntity.error("");
        } else {
            return ResultEntity.error("");
        }
    }

    @Override
    public void splitWsTagging(WssplitTagging wssplitTagging) {
        wssplitTagging.setWssplitTaggingCondition("1");
        commonService.insert(wssplitTagging);
        FormData formData = dataManager.selectOneFormData(wssplitTagging.getFormDefinitionId(), wssplitTagging.getArchivesId());
        formData.put(ArchiveEntity.DISASSEMBLY_TAGGING,"1");
        formDataService.updateFormDataIgnoreNullById(formData);
    }
    @Override
    public void splitWsTaggingUpdate(WssplitTagging wssplitTagging) {
        commonService.update(wssplitTagging);
    }
    @Override
    public WssplitTagging splitWsTaggingSelect(WssplitTagging wssplitTagging) {
        return commonService.selectOne(SqlQuery.from(WssplitTagging.class).equal(WssplitTaggingInfo.ARCHIVESID,wssplitTagging.getArchivesId()));
    }
    @Override
    public void splitWsTaggingDelete(WssplitTagging wssplitTagging) {
        FormData formData = dataManager.selectOneFormData(wssplitTagging.getFormDefinitionId(), wssplitTagging.getArchivesId());
        formData.put(ArchiveEntity.DISASSEMBLY_TAGGING,"0");
        formDataService.updateFormDataIgnoreNullById(formData);
        commonService.delete(SqlQuery.from(WssplitTagging.class).equal(WssplitTaggingInfo.ARCHIVESID,wssplitTagging.getArchivesId()));
    }
    @Override
    public void splitWsTaggingType(WssplitTagging wssplitTagging) {
        FormData formData = dataManager.selectOneFormData(wssplitTagging.getFormDefinitionId(), wssplitTagging.getArchivesId());
        formData.put(ArchiveEntity.DISASSEMBLY_TAGGING,wssplitTagging.getWssplitTaggingCondition());
        formDataService.updateFormDataIgnoreNullById(formData);
    }

    @Override
    public boolean taggingType(WssplitTagging wssplitTagging) {
        FormData formData = dataManager.selectOneFormData(wssplitTagging.getFormDefinitionId(), wssplitTagging.getArchivesId());
        if (formData.get(ArchiveEntity.DISASSEMBLY_TAGGING).equals("0")){
                return false;
        }
        return true;
    }
    @Override
    public void updateSplitStatus(String formDefinitionId,String formDataId){
        FormData formData = dataManager.selectOneFormData(formDefinitionId, formDataId);
        formData.put(ArchiveEntity.COLUMN_DISASSEMBLY_STATE, "2");
        formDataService.updateFormDataIgnoreNullById(formData);
    }
}