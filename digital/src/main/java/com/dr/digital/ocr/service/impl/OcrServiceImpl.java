package com.dr.digital.ocr.service.impl;

import com.dr.digital.configManager.entity.TypeFile;
import com.dr.digital.configManager.entity.TypeFileInfo;
import com.dr.digital.manage.form.entity.FileStructure;
import com.dr.digital.manage.form.entity.FileStructureInfo;
import com.dr.digital.manage.form.service.ArchiveDataManager;
import com.dr.digital.manage.form.service.impl.DefaultArchiveDataManager;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.manage.model.query.ArchiveDataQuery;
import com.dr.digital.ocr.bo.GeneralBo;
import com.dr.digital.ocr.bo.TableBo;
import com.dr.digital.ocr.entity.*;
import com.dr.digital.ocr.query.OcrQuery;
import com.dr.digital.ocr.service.OcrGeneralClient;
import com.dr.digital.ocr.service.OcrService;
import com.dr.digital.ocr.service.OcrTableClient;
import com.dr.digital.util.FileUtil;
import com.dr.digital.util.UUIDUtils;
import com.dr.digital.wssplit.entity.SplitRule;
import com.dr.digital.wssplit.entity.SplitRuleInfo;
import com.dr.digital.wssplit.entity.WssplitTagging;
import com.dr.digital.wssplit.service.SplitConditionService;
import com.dr.framework.common.dao.CommonMapper;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.common.form.core.service.FormDataService;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.fasterxml.jackson.databind.ObjectMapper;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class OcrServiceImpl implements OcrService {
    private Logger logger = LoggerFactory.getLogger(OcrServiceImpl.class);
    @Autowired
    ArchiveDataManager dataManager;
    @Autowired
    FormDataService formDataService;
    @Autowired
    DefaultArchiveDataManager defaultArchiveDataManager;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    CommonMapper commonMapper;
    @Autowired
    SplitConditionService splitConditionService;
    @Autowired
    OcrGeneralClient ocrGeneralClient;
    @Autowired
    OcrTableClient ocrTableClient;
    @Value("${filePath}")
    private String filePath;
    //?????????
    ExecutorService executorService = Executors.newFixedThreadPool(6);

    /**
     * ?????????????????????base64?????????(????????????????????????)
     *
     * @param imgURL ??????????????????
     * @return
     */
    @Override
    public String imageToBase64ByOnline(String imgURL) {
        String fileName = imgURL.split("\\?")[0];
        String fileUrl = "";
        if (imgURL.indexOf("filePath") != -1) {
            fileUrl = filePath + File.separator + fileName;
        } else if (imgURL.indexOf("splitPath") != -1) {
            fileUrl = filePath + File.separator + fileName;
        }
        File imageFile = new File(fileUrl);
        String img = null;
        try {
            img = Base64Utils.encodeToString(FileUtils.readFileToByteArray(imageFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    /**
     * ??????????????????
     * // ????????? 10????????????????????????5??????
     *
     * @param imgURL
     * @return
     */
    @Override
    public GeneralResultEntity shiBieOcrTxt(String imgURL) {
        List<String> img_base64List = new ArrayList<>();
        img_base64List.add(imgURL);
        GeneralBo generalBo = new GeneralBo(true, true, img_base64List);
        GeneralResultEntity generalResultEntity = ocrGeneralClient.general(generalBo);
        logger.info("????????????????????????:{}", generalResultEntity);
        return generalResultEntity;
    }

    @Override
    public TableResultEntity shiBieOcrTable(String imgURL) {
        List<String> img_base64List = new ArrayList<>();
        img_base64List.add(imgURL);
        TableBo tableBo = new TableBo(img_base64List);
        TableResultEntity tableResultEntity = ocrTableClient.table(tableBo);
        logger.info("????????????????????????:{}", tableResultEntity);
        return tableResultEntity;
    }

    @Override
    public void chaiJIan(OcrQuery query, Person person) {
        ArchiveDataQuery queryArchive = new ArchiveDataQuery();
        if (query.getAjFormDefinitionId() != null && !"".equals(query.getAjFormDefinitionId())) {
            queryArchive.setFormDefinitionId(query.getAjFormDefinitionId());
        } else {
            queryArchive.setFormDefinitionId(query.getWjFormDefinitionId());
        }
        queryArchive.setQueryItems(query.getQueryItems());
        //??????????????????????????????????????????
        List<FormData> list = dataManager.findDataByQuery(queryArchive);
        if (list.size() > 0) {
            //????????????????????????????????????????????????
            List<SplitRule> splitRules = commonMapper.selectByQuery(SqlQuery.from(SplitRule.class).equal(SplitRuleInfo.ISENABLE, "1").orderBy(SplitRuleInfo.IFORDER));
            //???????????????????????? ????????????
            for (FormData formData : list) {
                executorService.execute(() -> wsSplintChaiJan(query.getFormId(), formData, splitRules));
            }
        }
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param registerId
     * @param formData
     * @param splitRules
     */
    public void wsSplintChaiJan(String registerId, FormData formData, List<SplitRule> splitRules) {
        //?????????????????? ????????????????????????
        SqlQuery<FileStructure> sqlQuery = SqlQuery.from(FileStructure.class)
                .equal(FileStructureInfo.ARCHIVERS_CATEGORY_CODE, formData.get(ArchiveEntity.COLUMN_CATEGORY_CODE) + "")
                .equal(FileStructureInfo.AJ_ARCHIVAL_CODE, formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "")
                .equal(FileStructureInfo.REGISTERID, registerId)
                .equal(FileStructureInfo.DEFAULT_STATE, "1");
        List<FileStructure> VolumesList = commonMapper.selectByQuery(sqlQuery);
        //????????????  ??????????????????????????? ????????????0????????????????????? ??????0?????????
        if (VolumesList.size() <= 0) {
            //?????????
            String fond_code = formData.get(ArchiveEntity.COLUMN_FOND_CODE);
            //??????
            String archive_code = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
            //????????????
            DisassemblyRecord disassemblyRecord = new DisassemblyRecord();
            disassemblyRecord.setId(UUIDUtils.getUUID());
            disassemblyRecord.setFormDefinitionId(formData.getFormDefinitionId());
            disassemblyRecord.setRegisterId(registerId);
            disassemblyRecord.setArchivesId(formData.get(ArchiveEntity.ID_COLUMN_NAME));
            disassemblyRecord.setArchive_code(archive_code);
            commonMapper.insert(disassemblyRecord);
            DisassemblyRecordDetail disassemblyRecordDetail = new DisassemblyRecordDetail();
            disassemblyRecordDetail.setDisassemblyRecordDetailID(disassemblyRecord.getId());
            disassemblyRecordDetail.setFormDefinitionId(formData.getFormDefinitionId());

            //?????????????????????????????????
            String jpgFilePath = String.join(File.separator, filePath, "filePath", fond_code, archive_code);
            File file = new File(jpgFilePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            //???????????????????????? map
            Map<String, List<String>> map = new HashMap<>();
            //??????????????????ocr???????????????
            File txtFiles = new File(String.join(File.separator, filePath, "txt", fond_code, archive_code));
            if (!txtFiles.exists()) {
                txtFiles.mkdirs();
            }
            //????????????
            if (txtFiles.listFiles().length > 0) {
                //???????????????????????????
                List<File> orderFiles = FileUtil.orderByName(txtFiles);
                //1??????????????????????????????????????????????????? ?????????????????????????????????
                for (File txtFile : orderFiles) {
                    boolean flag = false;
                    List<String> fileList = new ArrayList<>();
                    for (SplitRule splitRule : splitRules) {
                        if ("FM".equals(splitRule.getConditionType()) && txtFile.getName().indexOf("001") != -1) {
                            //??????
                            flag = splitConditionService.judgeCover(txtFile, formData);
                            if (flag) {
                                if (map.containsKey(splitRule.getFileType())) {
                                    List<String> list1 = map.get(splitRule.getFileType());
                                    list1.add(txtFile.getPath());
                                    map.put(splitRule.getFileType(), list1);
                                } else {
                                    fileList.add(txtFile.getPath());
                                    map.put(splitRule.getFileType(), fileList);
                                }
                                //???????????????????????????001????????? ?????? ??????????????? ????????????
                                String disassemblyText = "???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????";
                                disassemblyRecordDetail.setFilePosition("??????");
                                addDisassemblyRecordDetail(disassemblyRecordDetail, txtFile.getName(), disassemblyText);
                                break;
                            }
                        } else if ("HY".equals(splitRule.getConditionType())) {
                            //??????
                            flag = splitConditionService.meetingJudgment(txtFile, formData);
                            if (flag) {
                                if (map.containsKey(splitRule.getFileType())) {
                                    List<String> list1 = map.get(splitRule.getFileType());
                                    list1.add(txtFile.getPath());
                                    map.put(splitRule.getFileType(), list1);
                                } else {
                                    fileList.add(txtFile.getPath());
                                    map.put(splitRule.getFileType(), fileList);
                                }
                                //?????? ?????????????????? ???????????????????????????
                                String disassemblyText = "????????????????????????????????????????????????";
                                disassemblyRecordDetail.setFilePosition("??????");
                                addDisassemblyRecordDetail(disassemblyRecordDetail, txtFile.getName(), disassemblyText);
                                break;
                            }
                        } else if ("NB".equals(splitRule.getConditionType())) {
                            //??????
                            flag = splitConditionService.annualReportJudgment(txtFile, formData);
                            if (flag) {
                                if (map.containsKey(splitRule.getFileType())) {
                                    List<String> list1 = map.get(splitRule.getFileType());
                                    list1.add(txtFile.getPath());
                                    map.put(splitRule.getFileType(), list1);
                                } else {
                                    fileList.add(txtFile.getPath());
                                    map.put(splitRule.getFileType(), fileList);
                                }
                                String disassemblyText = "??????????????????????????????????????????";
                                disassemblyRecordDetail.setFilePosition("??????");
                                addDisassemblyRecordDetail(disassemblyRecordDetail, txtFile.getName(), disassemblyText);
                                break;
                            }
                        } else if ("BLD".equals(splitRule.getConditionType())) {
                            //?????????
                            flag = splitConditionService.handlingSheetJudgment(txtFile, formData);
                            if (flag) {
                                if (map.containsKey(splitRule.getFileType())) {
                                    List<String> list1 = map.get(splitRule.getFileType());
                                    list1.add(txtFile.getPath());
                                    map.put(splitRule.getFileType(), list1);
                                } else {
                                    fileList.add(txtFile.getPath());
                                    map.put(splitRule.getFileType(), fileList);
                                }
                                String disassemblyText = "????????????????????????????????????????????????????????????????????????";
                                disassemblyRecordDetail.setFilePosition("?????????");
                                addDisassemblyRecordDetail(disassemblyRecordDetail, txtFile.getName(), disassemblyText);
                                break;
                            }
                        } else if ("BLDT".equals(splitRule.getConditionType())) {
                            //?????????
                            flag = splitConditionService.handlingSheetTwoJudgment(txtFile, formData);
                            if (flag) {
                                if (map.containsKey(splitRule.getFileType())) {
                                    List<String> list1 = map.get(splitRule.getFileType());
                                    list1.add(txtFile.getPath());
                                    map.put(splitRule.getFileType(), list1);
                                } else {
                                    fileList.add(txtFile.getPath());
                                    map.put(splitRule.getFileType(), fileList);
                                }
                                String disassemblyText = "???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????";
                                disassemblyRecordDetail.setFilePosition("?????????");
                                addDisassemblyRecordDetail(disassemblyRecordDetail, txtFile.getName(), disassemblyText);
                                break;
                            }
                        }
                    }
                    //???????????????????????? ???????????????
                    if (!flag) {
                        if (map.containsKey(FileUtil.getNewFileName(9))) {
                            List<String> list1 = map.get(FileUtil.getNewFileName(9));
                            list1.add(txtFile.getPath());
                            map.put(FileUtil.getNewFileName(9), list1);
                        } else {
                            List<String> list1 = new ArrayList<>();
                            list1.add(txtFile.getPath());
                            map.put(FileUtil.getNewFileName(9), list1);
                        }
                    }
                }
                //2????????????????????????????????????????????????,??????????????????
                List<List> transList = new ArrayList();
                //???????????????????????? transList,????????????????????????????????????
                List<String> secondList = map.get(FileUtil.getNewFileName(9));
                if (secondList != null && secondList.size() > 0) {
                    List<String> list = new ArrayList();//??????????????????
                    for (String str : secondList) {
                        File fJFile = new File(str);
                        //??????????????????????????????
                        boolean hongTouFlag = splitConditionService.dzHongTou(fJFile);
                        //?????????????????????
                        boolean titleFlag = splitConditionService.textTitleJudgment(fJFile);
                        //?????????????????????
                        boolean seFlag = splitConditionService.zuNyr(fJFile);
                        if (seFlag) {
                            list.add(str);//???????????????
                            List tempList = new ArrayList();
                            for (String one : list) {
                                tempList.add(one);
                            }
                            transList.add(tempList);
                            list.clear();
                            continue;
                        } else if (hongTouFlag || titleFlag) {
                            if (list.size() > 0) {
                                List tempList = new ArrayList();
                                for (String one : list) {
                                    tempList.add(one);
                                }
                                transList.add(tempList);
                                list.clear();
                                list.add(str);
                                continue;
                            }
                        }
                        list.add(str);
                    }
                    if (list.size() > 0) {
                        List tempList = new ArrayList();
                        for (String one : list) {
                            tempList.add(one);
                        }
                        transList.add(tempList);
                    }
                }
                //3??????????????????????????????????????????????????????    ????????????&&??????????????????
                if (secondList != null && transList.size() > 0) {
                    //??????????????????????????? 3??????3?????????(??????????????????)
                    if (transList.size() >= 3) {
                        int zwYs = 0;
                        //?????????????????????????????????
                        for (int a = 0; a < 3; a++) {
                            List<String> txtList = transList.get(a);
                            if (txtList.size() > 0) {
                                File txtFile = new File(txtList.get(0));
                                //????????????
                                boolean zwFlag = splitConditionService.textStartJudgment(txtFile, formData);
                                if (zwFlag) {
                                    map.put(FileUtil.getNewFileName(2), txtList);
                                    zwYs = txtList.size();
                                    String disassemblyText = "???????????????";
                                    disassemblyRecordDetail.setFilePosition("??????");
                                    addDisassemblyRecordDetail(disassemblyRecordDetail, txtFile.getName(), disassemblyText);
                                    transList.remove(a);
                                    break;
                                }
                            }
                        }
                        //??????????????????????????????????????? ??????????????????
                        if (!map.containsKey(FileUtil.getNewFileName(2))) {
                            //?????????????????? ??????????????????????????????
                            for (int a = 0; a < transList.size(); a++) {
                                boolean xpdFlag = false;
                                List<String> txtList = transList.get(a);
                                File txtFile = new File(txtList.get(0));
                                for (SplitRule splitRule : splitRules) {
                                    if ("FJC".equals(splitRule.getConditionType())) {
                                        //??????
                                        xpdFlag = splitConditionService.attachmentJudgment(txtFile, formData);
                                        if (xpdFlag) {
                                            if (map.containsKey(splitRule.getFileType())) {
                                                List<String> list1 = map.get(splitRule.getFileType());
                                                for (String str : txtList) {
                                                    list1.add(str);
                                                }
                                                map.put(splitRule.getFileType(), list1);
                                            } else {
                                                map.put(splitRule.getFileType(), txtList);
                                            }
                                            String disassemblyText = splitRule.getContent();
                                            disassemblyRecordDetail.setFilePosition("??????");
                                            addDisassemblyRecordDetail(disassemblyRecordDetail, txtList, disassemblyText);
                                            transList.remove(a);
                                            break;
                                        }
                                    }
                                }
                            }
                            //???????????????????????????
                            if (transList != null && transList.size() > 0) {
                                map.put(FileUtil.getNewFileName(2), transList.get(0));
                                List<String> list = transList.get(0);
                                String disassemblyText = "????????????????????????????????????????????????";
                                disassemblyRecordDetail.setFilePosition("??????");
                                addDisassemblyRecordDetail(disassemblyRecordDetail, list, disassemblyText);
                                transList.remove(0);
                            }
                        }
                        //????????????????????????????????????????????????
                        for (List<String> syList : transList) {
                            boolean flag = false;
                            File txtFile = new File(syList.get(0));
                            for (SplitRule splitRule : splitRules) {
                                if (syList.size() == 1) {
                                    if ("BLDS".equals(splitRule.getConditionType())) {
                                        //?????????
                                        flag = splitConditionService.handlingSheetSrJudgment(txtFile, map);
                                        if (flag) {
                                            if (map.containsKey(splitRule.getFileType())) {
                                                List<String> list1 = map.get(splitRule.getFileType());
                                                for (String str : syList) {
                                                    list1.add(str);
                                                }
                                                map.put(splitRule.getFileType(), list1);
                                            } else {
                                                map.put(splitRule.getFileType(), syList);
                                            }
                                            String disassemblyText = splitRule.getContent();
                                            disassemblyRecordDetail.setFilePosition("?????????");
                                            addDisassemblyRecordDetail(disassemblyRecordDetail, syList, disassemblyText);
                                            break;
                                        }
                                    }
                                }
                                if ("FJC".equals(splitRule.getConditionType())) {
                                    //??????
                                    flag = splitConditionService.attachmentJudgment(txtFile, formData);
                                    if (flag) {
                                        if (map.containsKey(splitRule.getFileType())) {
                                            List<String> list1 = map.get(splitRule.getFileType());
                                            for (String str : syList) {
                                                list1.add(str);
                                            }
                                            map.put(splitRule.getFileType(), list1);
                                        } else {
                                            map.put(splitRule.getFileType(), syList);
                                        }
                                        String disassemblyText = splitRule.getContent();
                                        disassemblyRecordDetail.setFilePosition("??????");
                                        addDisassemblyRecordDetail(disassemblyRecordDetail, syList, disassemblyText);
                                        break;
                                    }
                                } else if ("FJ".equals(splitRule.getConditionType())) {
                                    //??????
                                    flag = splitConditionService.attachmentTwoJudgment(txtFile, formData, map);
                                    if (flag) {
                                        if (map.containsKey(splitRule.getFileType())) {
                                            List<String> list1 = map.get(splitRule.getFileType());
                                            for (String str : syList) {
                                                list1.add(str);
                                            }
                                            map.put(splitRule.getFileType(), list1);
                                        } else {
                                            map.put(splitRule.getFileType(), syList);
                                        }
                                        String disassemblyText = splitRule.getContent();
                                        disassemblyRecordDetail.setFilePosition("??????");
                                        addDisassemblyRecordDetail(disassemblyRecordDetail, syList, disassemblyText);
                                        break;
                                    }
                                }
                                if ((zwYs + 1) >= syList.size() && syList.size() >= zwYs - 1) {
                                    if ("DG".equals(splitRule.getConditionType())) {
                                        //??????
                                        flag = splitConditionService.manuscriptJudgment(syList, map);
                                        if (flag) {
                                            if (map.containsKey(splitRule.getFileType())) {
                                                List<String> list1 = map.get(splitRule.getFileType());
                                                for (String str : syList) {
                                                    list1.add(str);
                                                }
                                                map.put(splitRule.getFileType(), list1);
                                            } else {
                                                map.put(splitRule.getFileType(), syList);
                                            }
                                            String disassemblyText = splitRule.getContent();
                                            disassemblyRecordDetail.setFilePosition("??????");
                                            addDisassemblyRecordDetail(disassemblyRecordDetail, syList, disassemblyText);
                                            break;
                                        }
                                    }
                                }
                            }
                            if (!flag) {
                                if (map.containsKey(FileUtil.getNewFileName(3))) {
                                    List<String> list1 = map.get(FileUtil.getNewFileName(3));
                                    for (String str : syList) {
                                        list1.add(str);
                                    }
                                    map.put(FileUtil.getNewFileName(3), list1);
                                } else {
                                    map.put(FileUtil.getNewFileName(3), syList);
                                }
                                String disassemblyText = "?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????";
                                disassemblyRecordDetail.setFilePosition("??????");
                                addDisassemblyRecordDetail(disassemblyRecordDetail, syList, disassemblyText);
                            }
                        }
                    } else if (transList.size() >= 2) {
                        int zwYs = 0;
                        //?????????????????????????????????
                        for (int a = 0; a < 2; a++) {
                            List<String> txtList = transList.get(a);
                            if (txtList.size() > 0) {
                                File txtFile = new File(txtList.get(0));
                                //????????????
                                boolean zwFlag = splitConditionService.textStartJudgment(txtFile, formData);
                                if (zwFlag) {
                                    map.put(FileUtil.getNewFileName(2), txtList);
                                    zwYs = txtList.size();
                                    String disassemblyText = "????????????";
                                    disassemblyRecordDetail.setFilePosition("??????");
                                    addDisassemblyRecordDetail(disassemblyRecordDetail, txtList, disassemblyText);
                                    transList.remove(a);
                                    break;
                                }
                            }
                        }
                        //??????????????????????????????????????? ??????????????????
                        if (!map.containsKey(FileUtil.getNewFileName(2))) {
                            //?????????????????? ??????????????????????????????
                            for (int a = 0; a < transList.size(); a++) {
                                boolean xpdFlag = false;
                                List<String> txtList = transList.get(a);
                                File txtFile = new File(txtList.get(0));
                                for (SplitRule splitRule : splitRules) {
                                    if ("FJC".equals(splitRule.getConditionType())) {
                                        //??????
                                        xpdFlag = splitConditionService.attachmentJudgment(txtFile, formData);
                                        if (xpdFlag) {
                                            if (map.containsKey(splitRule.getFileType())) {
                                                List<String> list1 = map.get(splitRule.getFileType());
                                                for (String str : txtList) {
                                                    list1.add(str);
                                                }
                                                map.put(splitRule.getFileType(), list1);
                                            } else {
                                                map.put(splitRule.getFileType(), txtList);
                                            }
                                            String disassemblyText = splitRule.getContent();
                                            disassemblyRecordDetail.setFilePosition("??????");
                                            addDisassemblyRecordDetail(disassemblyRecordDetail, txtList, disassemblyText);
                                            transList.remove(a);
                                            break;
                                        }
                                    }
                                }
                            }
                            //???????????????????????????
                            if (transList != null && transList.size() > 0) {
                                map.put(FileUtil.getNewFileName(2), transList.get(0));
                                String disassemblyText = "?????????????????????????????????";
                                disassemblyRecordDetail.setFilePosition("??????");
                                addDisassemblyRecordDetail(disassemblyRecordDetail, transList.get(0), disassemblyText);
                                transList.remove(0);
                            }
                        }
                        //????????????????????????????????????????????????
                        if (transList != null && transList.size() > 0) {
                            for (List<String> syList : transList) {
                                boolean flag = false;
                                File txtFile = new File(syList.get(0));
                                for (SplitRule splitRule : splitRules) {
                                    if (syList.size() == 1) {
                                        if ("BLDS".equals(splitRule.getConditionType())) {
                                            //?????????
                                            flag = splitConditionService.handlingSheetSrJudgment(txtFile, map);
                                            if (flag) {
                                                if (map.containsKey(splitRule.getFileType())) {
                                                    List<String> list1 = map.get(splitRule.getFileType());
                                                    for (String str : syList) {
                                                        list1.add(str);
                                                    }
                                                    map.put(splitRule.getFileType(), list1);
                                                } else {
                                                    map.put(splitRule.getFileType(), syList);
                                                }
                                                String disassemblyText = splitRule.getContent();
                                                disassemblyRecordDetail.setFilePosition("?????????");
                                                addDisassemblyRecordDetail(disassemblyRecordDetail, syList, disassemblyText);
                                                break;
                                            }
                                        }
                                    }
                                    if ("FJC".equals(splitRule.getConditionType())) {
                                        //??????
                                        flag = splitConditionService.attachmentJudgment(txtFile, formData);
                                        if (flag) {
                                            if (map.containsKey(splitRule.getFileType())) {
                                                List<String> list1 = map.get(splitRule.getFileType());
                                                for (String str : syList) {
                                                    list1.add(str);
                                                }
                                                map.put(splitRule.getFileType(), list1);
                                            } else {
                                                map.put(splitRule.getFileType(), syList);
                                            }
                                            String disassemblyText = splitRule.getContent();
                                            disassemblyRecordDetail.setFilePosition("??????");
                                            addDisassemblyRecordDetail(disassemblyRecordDetail, syList, disassemblyText);
                                            break;
                                        }
                                    } else if ("FJ".equals(splitRule.getConditionType())) {
                                        //??????
                                        flag = splitConditionService.attachmentTwoJudgment(txtFile, formData, map);
                                        if (flag) {
                                            if (map.containsKey(splitRule.getFileType())) {
                                                List<String> list1 = map.get(splitRule.getFileType());
                                                for (String str : syList) {
                                                    list1.add(str);
                                                }
                                                map.put(splitRule.getFileType(), list1);
                                            } else {
                                                map.put(splitRule.getFileType(), syList);
                                            }
                                            String disassemblyText = splitRule.getContent();
                                            disassemblyRecordDetail.setFilePosition("??????");
                                            addDisassemblyRecordDetail(disassemblyRecordDetail, syList, disassemblyText);
                                            break;
                                        }
                                    }
                                    if ((zwYs + 1) >= syList.size() && syList.size() >= zwYs - 1) {
                                        if ("DG".equals(splitRule.getConditionType())) {
                                            //??????
                                            flag = splitConditionService.manuscriptJudgment(syList, map);
                                            if (flag) {
                                                if (map.containsKey(splitRule.getFileType())) {
                                                    List<String> list1 = map.get(splitRule.getFileType());
                                                    list1.add(txtFile.getPath());
                                                    map.put(splitRule.getFileType(), list1);
                                                } else {
                                                    map.put(splitRule.getFileType(), syList);
                                                }
                                                String disassemblyText = splitRule.getContent();
                                                disassemblyRecordDetail.setFilePosition("??????");
                                                addDisassemblyRecordDetail(disassemblyRecordDetail, syList, disassemblyText);
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (!flag) {
                                    if (map.containsKey(FileUtil.getNewFileName(3))) {
                                        List<String> list1 = map.get(FileUtil.getNewFileName(3));
                                        for (String str : syList) {
                                            list1.add(str);
                                        }
                                        map.put(FileUtil.getNewFileName(3), list1);
                                    } else {
                                        map.put(FileUtil.getNewFileName(3), syList);
                                    }
                                    String disassemblyText = "?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????";
                                    disassemblyRecordDetail.setFilePosition("??????");
                                    addDisassemblyRecordDetail(disassemblyRecordDetail, syList, disassemblyText);
                                }
                            }
                        }
                    } else { //?????????????????????????????????
                        for (List list : transList) {
                            map.put(FileUtil.getNewFileName(2), list);
                            String disassemblyText = "???????????????";
                            disassemblyRecordDetail.setFilePosition("??????");
                            addDisassemblyRecordDetail(disassemblyRecordDetail, list, disassemblyText);
                        }
                    }
                }
                //5??????????????????????????????
                map.remove(FileUtil.getNewFileName(9));
            } else {
                if (file.listFiles().length > 0) { //????????????txt???????????????????????? ????????????
                    List<File> orderFiles = FileUtil.orderByName(file);
                    for (File jspFile : orderFiles) {
                        List<String> fileList = new ArrayList<>();
                        fileList.add(jspFile.getPath());
                        map.put(FileUtil.getNewFileName(2), fileList);
                        String disassemblyText = "???????????????txt,???????????????????????????";
                        disassemblyRecordDetail.setFilePosition("??????");
                        addDisassemblyRecordDetail(disassemblyRecordDetail, fileList, disassemblyText);
                    }
                }
            }
            String[] type = {"001", "002", "003", "004", "005"};
            //???????????????????????????
            for (String str : type) {
                if (!map.containsKey(str)) {
                    map.put(str, new ArrayList<>());
                }
            }
            //6??????map?????????????????????????????????????????????
            Map<String, List<String>> result = new LinkedHashMap<>(map.size());
            map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
            int yh = 1;
            List<String> jspList = new ArrayList<>();
            //????????????????????????????????????
            commonMapper.deleteByQuery(SqlQuery.from(FileStructure.class)
                    .equal(FileStructureInfo.REGISTERID,registerId)
                    .equal(FileStructureInfo.DEFAULT_STATE,0)
                    .equal(FileStructureInfo.ARCHIVESID,formData.get(ArchiveEntity.ID_COLUMN_NAME).toString()));
            //7??????map????????????????????????
            for (String key : result.keySet()) {
                FileStructure data = new FileStructure();
                String fileType = "";
                //??????????????????????????????????????????  ???????????????
                List<TypeFile> typeFile = commonMapper.selectByQuery(SqlQuery.from(TypeFile.class)
                        .equal(TypeFileInfo.YHCODE, key)
                        .equal(TypeFileInfo.REGISTERID, registerId));
                if (typeFile.size() > 0) {
                    //??????????????????????????? ???????????????
                    fileType = typeFile.get(0).getFileName();
                } else {
                    //??????????????? ???????????????????????????
                    switch (key) {
                        case "001":
                            fileType = "??????";
                            break;
                        case "002":
                            fileType = "??????";
                            break;
                        case "003":
                            fileType = "??????";
                            break;
                        case "004":
                            fileType = "?????????";
                            break;
                        case "005":
                            fileType = "??????";
                            break;
                    }
                }
                data.setArchives_item_number(key);
                data.setFonds_identifier(fond_code);
                data.setArchivers_category_code(formData.get(ArchiveEntity.COLUMN_CATEGORY_CODE));
                data.setAj_archival_code(archive_code);
                data.setArchival_code(archive_code + "-" + key);
                data.setPage_number(FileUtil.getNewFileName(yh));
                data.setTotal_number_of_pages(result.get(key).size() + "");
                data.setRegisterId(registerId);
                data.setArchivesID(formData.get(ArchiveEntity.ID_COLUMN_NAME));
                data.setFile_type(fileType);
                data.setStatus("RECEIVE");
                //?????????????????????????????????
                data.setDefault_state("1");
                data.setId(UUIDUtils.getUUID());
                commonMapper.insert(data);
                formData.put(ArchiveEntity.COLUMN_DISASSEMBLY_STATE, '1');
                formDataService.updateFormDataById(formData);//
                //????????????
                yh = yh + result.get(key).size();
            }
            Collection<List<String>> listList = result.values();
            if (listList.size() > 0) {
                for (List<String> listS : listList) {
                    if (listS.size() > 0) {
                        for (String str : listS) {
                            jspList.add(str);
                        }
                    }
                }
            }
            //8??????????????????????????????
            for (int i = 0; i < jspList.size(); i++) {
                String txtPath = jspList.get(i).substring(0, jspList.get(i).lastIndexOf(File.separator));
                String txtName = jspList.get(i).substring(jspList.get(i).lastIndexOf(File.separator) + 1);
                //String newName = FileUtil.getNewFileName(i + 1) + "_";
                //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                String newName = txtName.replace(".txt", "_");
                String jspName = txtName.replace(".txt", ".jpg");
                //?????????txt????????????
                File txtFile = new File(jspList.get(i));
                File txtNewFile = new File(txtPath + File.separator + newName + ".txt");
                txtFile.renameTo(txtNewFile);
                //?????????jsp????????????
                File jspFile = new File(jpgFilePath + File.separator + jspName);
                File jspNewFile = new File(jpgFilePath + File.separator + newName + ".jpg");
                jspFile.renameTo(jspNewFile);
            }
        }
    }

    /**
     * ??????????????????????????? ????????????
     *
     * @param disassemblyRecordDetail ??????
     * @param fileName                ??????
     * @param disassemblyRecord       ????????????
     */
    public void addDisassemblyRecordDetail(DisassemblyRecordDetail disassemblyRecordDetail, String fileName, String disassemblyRecord) {
        disassemblyRecordDetail.setId(UUIDUtils.getUUID());
        //?????????
        String name = fileName.substring(0, fileName.lastIndexOf("."));
        disassemblyRecordDetail.setFileName(name);
        //??????????????????
        disassemblyRecordDetail.setDisassemblyRecord(disassemblyRecord);
        commonMapper.insert(disassemblyRecordDetail);
    }

    /**
     * ??????????????????????????? ????????????
     *
     * @param disassemblyRecordDetail ??????
     * @param txtList                 ??????
     * @param disassemblyRecord       ????????????
     */
    public void addDisassemblyRecordDetail(DisassemblyRecordDetail disassemblyRecordDetail, List<String> txtList, String disassemblyRecord) {
        //?????????
        int i = 0;
        for (String txt : txtList) {
            File txtFile = new File(txtList.get(i));
            i++;
            String fileName = txtFile.getName();
            addDisassemblyRecordDetail(disassemblyRecordDetail, fileName, disassemblyRecord);
        }
    }




    public void txtToTextHb(FormData formData) {
        String fond_code = formData.get(ArchiveEntity.COLUMN_FOND_CODE);//?????????
        String archive_code = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);//??????
        //??????????????????ocr???????????????
        File txtFiles = new File(String.join(File.separator, filePath, "txt", fond_code, archive_code));
        if (!txtFiles.exists()) {
            txtFiles.mkdirs();
        }
        String nrContent = "";
        if (txtFiles.listFiles().length > 0) {
            //???????????????????????????
            List<File> orderFiles = FileUtil.orderByName(txtFiles);
            for (File txtFile : orderFiles) {
                nrContent += splitConditionService.readTxtContent(txtFile) + System.getProperty("line.separator");
            }
            //??????txt????????????
            File textFile = new File(String.join(File.separator, filePath, "txtHb", fond_code, archive_code));
            if (!textFile.exists()) {
                textFile.mkdirs();
            }
            try {
                FileWriter fw = new FileWriter(textFile + File.separator + archive_code + ".txt");
                fw.write(nrContent);
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param file
     * @return
     */
    List<String> fileList3 = new ArrayList<>();

    public List<String> isDirectory2(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                String fileName = file.getName().toUpperCase();
                // ???????????????
                if (fileName.indexOf(".JPG") != -1 || fileName.indexOf(".jpg") != -1) {
                    fileList3.add(file.getPath());
                }
            } else {
                File[] list = file.listFiles();
                if (list.length != 0) {
                    for (int i = 0; i < list.length; i++) {
                        isDirectory2(list[i]);
                    }
                }
            }
        } else {
            System.out.println("??????????????????");
        }
        return fileList3;
    }

    /**
     * ??????????????????????????????
     *
     * @param
     * @param person
     */
    @Override
    @Async
    public void batchDisassembly(String formDefinitionId, String registerId, Person person) {
        //???????????????????????????????????????
        List<FormData> dataList = formDataService.selectFormData(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
            sqlQuery.in(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_STATUS), "RECEIVE", "WSSPLIT")
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_TRANSITION_STATE), '1')
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_SPLIT_STATE), '1')
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_DISTINGUISH_STATE), '1')
                    .equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_DISASSEMBLY_STATE), '0')
                    .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
        if (dataList.size() > 0) {
            for (FormData data : dataList) {
                if (data.get(ArchiveEntity.COLUMN_CATEGORY_CODE).toString().contains("WS")) {
                    //????????????????????????
                    WssplitTagging wssplitTagging = new WssplitTagging();
                    wssplitTagging.setFormDefinitionId(formDefinitionId);
                    wssplitTagging.setArchivesId(data.get(ArchiveEntity.ID_COLUMN_NAME));
                    ResultEntity resultEntity = defaultArchiveDataManager.uniquenessJudge(wssplitTagging);
                    data.setFormDefinitionId(formDefinitionId);
                    if (resultEntity.isSuccess()) {
                        //??????????????????
                        List<SplitRule> splitRules = commonMapper.selectByQuery(SqlQuery.from(SplitRule.class)
                                .equal(SplitRuleInfo.ISENABLE, "1").orderBy(SplitRuleInfo.IFORDER));
                        wsSplintChaiJan(registerId, data, splitRules);
                    }
                    ;
                }
            }
        }
    }

    /**
     * ????????? txt to Excel
     *
     * @param formDefinitionId
     * @param registerId
     * @param person
     */
    @Override
    public void ocrTxtToExcel(String formDefinitionId, String registerId, Person person) {
        //????????????ocr??? txt ??????
        List<FormData> dataList = formDataService.selectFormData(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
            sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_DISTINGUISH_STATE), '1')
                    .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
        for (FormData formData : dataList) {
            String fondCode = formData.get(ArchiveEntity.COLUMN_FOND_CODE);
            String archivalCode = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
            //??????txt????????????
            File txtFiles = new File(String.join(File.separator, filePath, "txt", fondCode, archivalCode));
            if (!txtFiles.exists()) {
                txtFiles.mkdirs();
            }
            //????????????
            if (txtFiles.listFiles().length > 0) {
                //???????????????????????????
                List<File> orderFiles = FileUtil.orderByName(txtFiles);
                //???????????????????????????????????????????????? tables ?????????
                for (int i = 0; i < orderFiles.size(); i++) {
                    //??????Excel????????????
                    File excelFiles = new File(String.join(File.separator, filePath, "excel", fondCode, archivalCode, FileUtil.getNewFileName((i + 1)) + ".xlsx"));
                    if (!excelFiles.getParentFile().exists()) {
                        excelFiles.getParentFile().mkdirs();
                    }
                    if (!excelFiles.exists()) {
                        try {
                            excelFiles.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    List<List<Map>> tableList = splitConditionService.readTxtTables(orderFiles.get(i));
                    if (tableList.size() > 0) {
                        //???????????????
                        try {
                            WritableWorkbook workbook = Workbook.createWorkbook(excelFiles);
                            WritableSheet sheet1 = workbook.createSheet("sheet1", 0);
                            Label label = null;
                            int count = 0;
                            for (int j = 0; j < tableList.size(); j++) {
                                List<Map> cellList = tableList.get(j);
                                //???list?????????????????????????????????
                                for (Map map : cellList) {
                                    count++;
                                    label = new Label(Integer.valueOf(map.get("start_row").toString()), Integer.valueOf(map.get("start_row").toString()), map.get("content") + "");
                                    sheet1.addCell(label);
                                }
                            }
                            workbook.write();
                            workbook.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (RowsExceededException e) {
                            e.printStackTrace();
                        } catch (WriteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * txt ??????
     *
     * @param query
     * @param person
     */
    @Override
    public void txtHb(OcrQuery query, Person person) {
        ArchiveDataQuery queryArchive = new ArchiveDataQuery();
        if (query.getAjFormDefinitionId() != null && !"".equals(query.getAjFormDefinitionId())) {
            queryArchive.setFormDefinitionId(query.getAjFormDefinitionId());
        } else {
            queryArchive.setFormDefinitionId(query.getWjFormDefinitionId());
        }
        queryArchive.setQueryItems(query.getQueryItems());
        //??????????????????????????????????????????
        List<FormData> list = dataManager.findDataByQuery(queryArchive);
        if (list.size() > 0) {
            //???????????????????????? ????????????
            for (FormData formData : list) {
                executorService.execute(() -> txtToTextHb(formData));
            }
        }
    }

    @Override
    public List<DisassemblyRecordDetail> disassemblyRecord(DisassemblyRecord disassemblyRecord) {
        List<DisassemblyRecord> disassemblyRecords = commonMapper.selectByQuery(SqlQuery.from(DisassemblyRecord.class).equal(DisassemblyRecordInfo.FORMDEFINITIONID, disassemblyRecord.getFormDefinitionId())
                .equal(DisassemblyRecordInfo.ARCHIVESID,disassemblyRecord.getArchivesId()));
        for (DisassemblyRecord data:disassemblyRecords){
            List<DisassemblyRecordDetail> disassemblyRecordDetails = commonMapper.selectByQuery(SqlQuery.from(DisassemblyRecordDetail.class).equal(DisassemblyRecordDetailInfo.DISASSEMBLYRECORDDETAILID, data.getId()));
            return disassemblyRecordDetails;
        }
        return null;
    }

}
