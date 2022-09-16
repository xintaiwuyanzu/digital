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
    //线程池
    ExecutorService executorService = Executors.newFixedThreadPool(6);

    /**
     * 在线图片转换成base64字符串(相对路径模板使用)
     *
     * @param imgURL 图片线上路径
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
     * 通用文字识别
     * // 共尝试 10次调用，调用间隔5分钟
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
        logger.info("通用文字识别成功:{}", generalResultEntity);
        return generalResultEntity;
    }

    @Override
    public TableResultEntity shiBieOcrTable(String imgURL) {
        List<String> img_base64List = new ArrayList<>();
        img_base64List.add(imgURL);
        TableBo tableBo = new TableBo(img_base64List);
        TableResultEntity tableResultEntity = ocrTableClient.table(tableBo);
        logger.info("通用表格识别成功:{}", tableResultEntity);
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
        //查询选择的门类的所以档案数据
        List<FormData> list = dataManager.findDataByQuery(queryArchive);
        if (list.size() > 0) {
            //根据判断顺序排序查询所有拆分规则
            List<SplitRule> splitRules = commonMapper.selectByQuery(SqlQuery.from(SplitRule.class).equal(SplitRuleInfo.ISENABLE, "1").orderBy(SplitRuleInfo.IFORDER));
            //根据案卷数据查询 原文数据
            for (FormData formData : list) {
                executorService.execute(() -> wsSplintChaiJan(query.getFormId(), formData, splitRules));
            }
        }
    }

    /**
     * 执行（文书档案）自动拆件逻辑
     *
     * @param registerId
     * @param formData
     * @param splitRules
     */
    public void wsSplintChaiJan(String registerId, FormData formData, List<SplitRule> splitRules) {
        //根据表单数据 查询文件结构数据
        SqlQuery<FileStructure> sqlQuery = SqlQuery.from(FileStructure.class)
                .equal(FileStructureInfo.ARCHIVERS_CATEGORY_CODE, formData.get(ArchiveEntity.COLUMN_CATEGORY_CODE) + "")
                .equal(FileStructureInfo.AJ_ARCHIVAL_CODE, formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE) + "")
                .equal(FileStructureInfo.REGISTERID, registerId)
                .equal(FileStructureInfo.DEFAULT_STATE, "1");
        List<FileStructure> VolumesList = commonMapper.selectByQuery(sqlQuery);
        //执行逻辑  判断是否为拆件过的 如果大于0说明存在文件夹 小于0才拆件
        if (VolumesList.size() <= 0) {
            //全宗号
            String fond_code = formData.get(ArchiveEntity.COLUMN_FOND_CODE);
            //档号
            String archive_code = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
            //拆建信息
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

            //查询该档案下的图像数据
            String jpgFilePath = String.join(File.separator, filePath, "filePath", fond_code, archive_code);
            File file = new File(jpgFilePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            //第一轮中存储对象 map
            Map<String, List<String>> map = new HashMap<>();
            //查询该档案下ocr识别的结果
            File txtFiles = new File(String.join(File.separator, filePath, "txt", fond_code, archive_code));
            if (!txtFiles.exists()) {
                txtFiles.mkdirs();
            }
            //存在数据
            if (txtFiles.listFiles().length > 0) {
                //对文件进行排序操作
                List<File> orderFiles = FileUtil.orderByName(txtFiles);
                //1、每一个文件的内容都去匹配拆分规则 把固定的文件过滤出去。
                for (File txtFile : orderFiles) {
                    boolean flag = false;
                    List<String> fileList = new ArrayList<>();
                    for (SplitRule splitRule : splitRules) {
                        if ("FM".equals(splitRule.getConditionType()) && txtFile.getName().indexOf("001") != -1) {
                            //封面
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
                                //符合封面逻辑，判断001为封面 增加 该档案拆件 逻辑记录
                                String disassemblyText = "页码是第一页，且只包含全宗号、年度、保管期限、文号、标题、日期、页数的，识别为封面";
                                disassemblyRecordDetail.setFilePosition("封面");
                                addDisassemblyRecordDetail(disassemblyRecordDetail, txtFile.getName(), disassemblyText);
                                break;
                            }
                        } else if ("HY".equals(splitRule.getConditionType())) {
                            //会议
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
                                //标题 存在会议记录 直接判定全部为正文
                                String disassemblyText = "标题为会议记录的，全部识别为正文";
                                disassemblyRecordDetail.setFilePosition("正文");
                                addDisassemblyRecordDetail(disassemblyRecordDetail, txtFile.getName(), disassemblyText);
                                break;
                            }
                        } else if ("NB".equals(splitRule.getConditionType())) {
                            //年报
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
                                String disassemblyText = "标题为年报的，全部识别为正文";
                                disassemblyRecordDetail.setFilePosition("正文");
                                addDisassemblyRecordDetail(disassemblyRecordDetail, txtFile.getName(), disassemblyText);
                                break;
                            }
                        } else if ("BLD".equals(splitRule.getConditionType())) {
                            //办理单
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
                                String disassemblyText = "文本中存在“阅办单”或“办理单”的，识别为办理单";
                                disassemblyRecordDetail.setFilePosition("办理单");
                                addDisassemblyRecordDetail(disassemblyRecordDetail, txtFile.getName(), disassemblyText);
                                break;
                            }
                        } else if ("BLDT".equals(splitRule.getConditionType())) {
                            //办理单
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
                                String disassemblyText = "文本中同时存在“签发”“会稿”“审稿”“核稿”“事由”“主送”“抄送”“标题”“概述”“密级”等大部分词汇时，判定为办理单";
                                disassemblyRecordDetail.setFilePosition("办理单");
                                addDisassemblyRecordDetail(disassemblyRecordDetail, txtFile.getName(), disassemblyText);
                                break;
                            }
                        }
                    }
                    //如果没有则先放到 共有类里面
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
                //2、循环判读第一轮未判断出来的数据,进行文件判断
                List<List> transList = new ArrayList();
                //将未分组的存放进 transList,存储后再后续步骤进行判断
                List<String> secondList = map.get(FileUtil.getNewFileName(9));
                if (secondList != null && secondList.size() > 0) {
                    List<String> list = new ArrayList();//中间存放用，
                    for (String str : secondList) {
                        File fJFile = new File(str);
                        //判读是否存在红头文件
                        boolean hongTouFlag = splitConditionService.dzHongTou(fJFile);
                        //判读是否是标题
                        boolean titleFlag = splitConditionService.textTitleJudgment(fJFile);
                        //判读是否是结束
                        boolean seFlag = splitConditionService.zuNyr(fJFile);
                        if (seFlag) {
                            list.add(str);//中间存放用
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
                //3、根据条件判断每份文件属于什么文件夹    未分组的&&第二轮判断的
                if (secondList != null && transList.size() > 0) {
                    //看剩余的件数是否是 3件或3件之上(进行寻找正文)
                    if (transList.size() >= 3) {
                        int zwYs = 0;
                        //循环前三件进行正文判断
                        for (int a = 0; a < 3; a++) {
                            List<String> txtList = transList.get(a);
                            if (txtList.size() > 0) {
                                File txtFile = new File(txtList.get(0));
                                //正文判断
                                boolean zwFlag = splitConditionService.textStartJudgment(txtFile, formData);
                                if (zwFlag) {
                                    map.put(FileUtil.getNewFileName(2), txtList);
                                    zwYs = txtList.size();
                                    String disassemblyText = "识别为正文";
                                    disassemblyRecordDetail.setFilePosition("正文");
                                    addDisassemblyRecordDetail(disassemblyRecordDetail, txtFile.getName(), disassemblyText);
                                    transList.remove(a);
                                    break;
                                }
                            }
                        }
                        //如果没有判读出正文将第一件 做为正文存储
                        if (!map.containsKey(FileUtil.getNewFileName(2))) {
                            //没有正文先把 带附件开头的这种筛出
                            for (int a = 0; a < transList.size(); a++) {
                                boolean xpdFlag = false;
                                List<String> txtList = transList.get(a);
                                File txtFile = new File(txtList.get(0));
                                for (SplitRule splitRule : splitRules) {
                                    if ("FJC".equals(splitRule.getConditionType())) {
                                        //附件
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
                                            disassemblyRecordDetail.setFilePosition("附件");
                                            addDisassemblyRecordDetail(disassemblyRecordDetail, txtList, disassemblyText);
                                            transList.remove(a);
                                            break;
                                        }
                                    }
                                }
                            }
                            //再取第一件做为正文
                            if (transList != null && transList.size() > 0) {
                                map.put(FileUtil.getNewFileName(2), transList.get(0));
                                List<String> list = transList.get(0);
                                String disassemblyText = "标题为会议记录的，全部识别为正文";
                                disassemblyRecordDetail.setFilePosition("正文");
                                addDisassemblyRecordDetail(disassemblyRecordDetail, list, disassemblyText);
                                transList.remove(0);
                            }
                        }
                        //出现正文以后判断是不是剩余的类型
                        for (List<String> syList : transList) {
                            boolean flag = false;
                            File txtFile = new File(syList.get(0));
                            for (SplitRule splitRule : splitRules) {
                                if (syList.size() == 1) {
                                    if ("BLDS".equals(splitRule.getConditionType())) {
                                        //办理单
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
                                            disassemblyRecordDetail.setFilePosition("办理单");
                                            addDisassemblyRecordDetail(disassemblyRecordDetail, syList, disassemblyText);
                                            break;
                                        }
                                    }
                                }
                                if ("FJC".equals(splitRule.getConditionType())) {
                                    //附件
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
                                        disassemblyRecordDetail.setFilePosition("附件");
                                        addDisassemblyRecordDetail(disassemblyRecordDetail, syList, disassemblyText);
                                        break;
                                    }
                                } else if ("FJ".equals(splitRule.getConditionType())) {
                                    //附件
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
                                        disassemblyRecordDetail.setFilePosition("附件");
                                        addDisassemblyRecordDetail(disassemblyRecordDetail, syList, disassemblyText);
                                        break;
                                    }
                                }
                                if ((zwYs + 1) >= syList.size() && syList.size() >= zwYs - 1) {
                                    if ("DG".equals(splitRule.getConditionType())) {
                                        //底稿
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
                                            disassemblyRecordDetail.setFilePosition("底稿");
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
                                String disassemblyText = "存在“附表”“附件”“附后”“附：”等词汇，判定本份档案疑似拥有附件，根据可判断的正文、办理单、草稿、封面以外的统一识别为附件";
                                disassemblyRecordDetail.setFilePosition("附件");
                                addDisassemblyRecordDetail(disassemblyRecordDetail, syList, disassemblyText);
                            }
                        }
                    } else if (transList.size() >= 2) {
                        int zwYs = 0;
                        //循环前三件进行正文判断
                        for (int a = 0; a < 2; a++) {
                            List<String> txtList = transList.get(a);
                            if (txtList.size() > 0) {
                                File txtFile = new File(txtList.get(0));
                                //正文判断
                                boolean zwFlag = splitConditionService.textStartJudgment(txtFile, formData);
                                if (zwFlag) {
                                    map.put(FileUtil.getNewFileName(2), txtList);
                                    zwYs = txtList.size();
                                    String disassemblyText = "正文开始";
                                    disassemblyRecordDetail.setFilePosition("正文");
                                    addDisassemblyRecordDetail(disassemblyRecordDetail, txtList, disassemblyText);
                                    transList.remove(a);
                                    break;
                                }
                            }
                        }
                        //如果没有判读出正文将第一件 做为正文存储
                        if (!map.containsKey(FileUtil.getNewFileName(2))) {
                            //没有正文先把 带附件开头的这种筛出
                            for (int a = 0; a < transList.size(); a++) {
                                boolean xpdFlag = false;
                                List<String> txtList = transList.get(a);
                                File txtFile = new File(txtList.get(0));
                                for (SplitRule splitRule : splitRules) {
                                    if ("FJC".equals(splitRule.getConditionType())) {
                                        //附件
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
                                            disassemblyRecordDetail.setFilePosition("附件");
                                            addDisassemblyRecordDetail(disassemblyRecordDetail, txtList, disassemblyText);
                                            transList.remove(a);
                                            break;
                                        }
                                    }
                                }
                            }
                            //再取第一件做为正文
                            if (transList != null && transList.size() > 0) {
                                map.put(FileUtil.getNewFileName(2), transList.get(0));
                                String disassemblyText = "与正文类似，判断为正文";
                                disassemblyRecordDetail.setFilePosition("正文");
                                addDisassemblyRecordDetail(disassemblyRecordDetail, transList.get(0), disassemblyText);
                                transList.remove(0);
                            }
                        }
                        //出现正文以后判断是不是剩余的类型
                        if (transList != null && transList.size() > 0) {
                            for (List<String> syList : transList) {
                                boolean flag = false;
                                File txtFile = new File(syList.get(0));
                                for (SplitRule splitRule : splitRules) {
                                    if (syList.size() == 1) {
                                        if ("BLDS".equals(splitRule.getConditionType())) {
                                            //办理单
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
                                                disassemblyRecordDetail.setFilePosition("办理单");
                                                addDisassemblyRecordDetail(disassemblyRecordDetail, syList, disassemblyText);
                                                break;
                                            }
                                        }
                                    }
                                    if ("FJC".equals(splitRule.getConditionType())) {
                                        //附件
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
                                            disassemblyRecordDetail.setFilePosition("附件");
                                            addDisassemblyRecordDetail(disassemblyRecordDetail, syList, disassemblyText);
                                            break;
                                        }
                                    } else if ("FJ".equals(splitRule.getConditionType())) {
                                        //附件
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
                                            disassemblyRecordDetail.setFilePosition("附件");
                                            addDisassemblyRecordDetail(disassemblyRecordDetail, syList, disassemblyText);
                                            break;
                                        }
                                    }
                                    if ((zwYs + 1) >= syList.size() && syList.size() >= zwYs - 1) {
                                        if ("DG".equals(splitRule.getConditionType())) {
                                            //底稿
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
                                                disassemblyRecordDetail.setFilePosition("底稿");
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
                                    String disassemblyText = "存在“附表”“附件”“附后”“附：”等词汇，判定本份档案疑似拥有附件，根据可判断的正文、办理单、草稿、封面以外的统一识别为附件";
                                    disassemblyRecordDetail.setFilePosition("附件");
                                    addDisassemblyRecordDetail(disassemblyRecordDetail, syList, disassemblyText);
                                }
                            }
                        }
                    } else { //只剩一件直接放正文里面
                        for (List list : transList) {
                            map.put(FileUtil.getNewFileName(2), list);
                            String disassemblyText = "判断为正文";
                            disassemblyRecordDetail.setFilePosition("正文");
                            addDisassemblyRecordDetail(disassemblyRecordDetail, list, disassemblyText);
                        }
                    }
                }
                //5、删除公共区域的内容
                map.remove(FileUtil.getNewFileName(9));
            } else {
                if (file.listFiles().length > 0) { //如果没有txt就将所有图像放在 正文里面
                    List<File> orderFiles = FileUtil.orderByName(file);
                    for (File jspFile : orderFiles) {
                        List<String> fileList = new ArrayList<>();
                        fileList.add(jspFile.getPath());
                        map.put(FileUtil.getNewFileName(2), fileList);
                        String disassemblyText = "没有获取到txt,所有图片识别为正文";
                        disassemblyRecordDetail.setFilePosition("正文");
                        addDisassemblyRecordDetail(disassemblyRecordDetail, fileList, disassemblyText);
                    }
                }
            }
            String[] type = {"001", "002", "003", "004", "005"};
            //添加没匹配到的数据
            for (String str : type) {
                if (!map.containsKey(str)) {
                    map.put(str, new ArrayList<>());
                }
            }
            //6、对map进行排序，确保页数页号计算正确
            Map<String, List<String>> result = new LinkedHashMap<>(map.size());
            map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
            int yh = 1;
            List<String> jspList = new ArrayList<>();
            //删除自动生成的文件夹结构
            commonMapper.deleteByQuery(SqlQuery.from(FileStructure.class)
                    .equal(FileStructureInfo.REGISTERID,registerId)
                    .equal(FileStructureInfo.DEFAULT_STATE,0)
                    .equal(FileStructureInfo.ARCHIVESID,formData.get(ArchiveEntity.ID_COLUMN_NAME).toString()));
            //7、将map内的图片进行排序
            for (String key : result.keySet()) {
                FileStructure data = new FileStructure();
                String fileType = "";
                //查询获取的智能归档配置系统的  文件夹结构
                List<TypeFile> typeFile = commonMapper.selectByQuery(SqlQuery.from(TypeFile.class)
                        .equal(TypeFileInfo.YHCODE, key)
                        .equal(TypeFileInfo.REGISTERID, registerId));
                if (typeFile.size() > 0) {
                    //如果有结构则取当前 文件夹结构
                    fileType = typeFile.get(0).getFileName();
                } else {
                    //如果没有则 默认一套文件夹结构
                    switch (key) {
                        case "001":
                            fileType = "封面";
                            break;
                        case "002":
                            fileType = "正文";
                            break;
                        case "003":
                            fileType = "附件";
                            break;
                        case "004":
                            fileType = "办理单";
                            break;
                        case "005":
                            fileType = "底稿";
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
                //自动生成文件夹识别字段
                data.setDefault_state("1");
                data.setId(UUIDUtils.getUUID());
                commonMapper.insert(data);
                formData.put(ArchiveEntity.COLUMN_DISASSEMBLY_STATE, '1');
                formDataService.updateFormDataById(formData);//
                //设置页号
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
            //8、更新文件夹内的图片
            for (int i = 0; i < jspList.size(); i++) {
                String txtPath = jspList.get(i).substring(0, jspList.get(i).lastIndexOf(File.separator));
                String txtName = jspList.get(i).substring(jspList.get(i).lastIndexOf(File.separator) + 1);
                //String newName = FileUtil.getNewFileName(i + 1) + "_";
                //使用上面的方法，页面的文件的顺序是不会错，但是拆建逻辑检测的编号会实现不了，或者在这里查询出实现的编号，修改数据库数据。
                String newName = txtName.replace(".txt", "_");
                String jspName = txtName.replace(".txt", ".jpg");
                //先修改txt文件顺序
                File txtFile = new File(jspList.get(i));
                File txtNewFile = new File(txtPath + File.separator + newName + ".txt");
                txtFile.renameTo(txtNewFile);
                //再修改jsp文件顺序
                File jspFile = new File(jpgFilePath + File.separator + jspName);
                File jspNewFile = new File(jpgFilePath + File.separator + newName + ".jpg");
                jspFile.renameTo(jspNewFile);
            }
        }
    }

    /**
     * 存放拆件的详细逻辑 （单个）
     *
     * @param disassemblyRecordDetail 对象
     * @param fileName                名称
     * @param disassemblyRecord       详细说明
     */
    public void addDisassemblyRecordDetail(DisassemblyRecordDetail disassemblyRecordDetail, String fileName, String disassemblyRecord) {
        disassemblyRecordDetail.setId(UUIDUtils.getUUID());
        //文件名
        String name = fileName.substring(0, fileName.lastIndexOf("."));
        disassemblyRecordDetail.setFileName(name);
        //文件拆件说明
        disassemblyRecordDetail.setDisassemblyRecord(disassemblyRecord);
        commonMapper.insert(disassemblyRecordDetail);
    }

    /**
     * 存放拆件的详细逻辑 （多个）
     *
     * @param disassemblyRecordDetail 对象
     * @param txtList                 组合
     * @param disassemblyRecord       详细说明
     */
    public void addDisassemblyRecordDetail(DisassemblyRecordDetail disassemblyRecordDetail, List<String> txtList, String disassemblyRecord) {
        //文件名
        int i = 0;
        for (String txt : txtList) {
            File txtFile = new File(txtList.get(i));
            i++;
            String fileName = txtFile.getName();
            addDisassemblyRecordDetail(disassemblyRecordDetail, fileName, disassemblyRecord);
        }
    }




    public void txtToTextHb(FormData formData) {
        String fond_code = formData.get(ArchiveEntity.COLUMN_FOND_CODE);//全宗号
        String archive_code = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);//档号
        //查询该档案下ocr识别的结果
        File txtFiles = new File(String.join(File.separator, filePath, "txt", fond_code, archive_code));
        if (!txtFiles.exists()) {
            txtFiles.mkdirs();
        }
        String nrContent = "";
        if (txtFiles.listFiles().length > 0) {
            //对文件进行排序操作
            List<File> orderFiles = FileUtil.orderByName(txtFiles);
            for (File txtFile : orderFiles) {
                nrContent += splitConditionService.readTxtContent(txtFile) + System.getProperty("line.separator");
            }
            //拼写txt相对路径
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
     * 查询文件夹下面的图片
     *
     * @param file
     * @return
     */
    List<String> fileList3 = new ArrayList<>();

    public List<String> isDirectory2(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                String fileName = file.getName().toUpperCase();
                // 只检测图片
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
            System.out.println("文件不存在！");
        }
        return fileList3;
    }

    /**
     * 批次上进行的拆件操作
     *
     * @param
     * @param person
     */
    @Override
    @Async
    public void batchDisassembly(String formDefinitionId, String registerId, Person person) {
        //获取批次下所有未拆件的档案
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
                    //添加操作人信息。
                    WssplitTagging wssplitTagging = new WssplitTagging();
                    wssplitTagging.setFormDefinitionId(formDefinitionId);
                    wssplitTagging.setArchivesId(data.get(ArchiveEntity.ID_COLUMN_NAME));
                    ResultEntity resultEntity = defaultArchiveDataManager.uniquenessJudge(wssplitTagging);
                    data.setFormDefinitionId(formDefinitionId);
                    if (resultEntity.isSuccess()) {
                        //查询配置信息
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
     * 批次上 txt to Excel
     *
     * @param formDefinitionId
     * @param registerId
     * @param person
     */
    @Override
    public void ocrTxtToExcel(String formDefinitionId, String registerId, Person person) {
        //获取已经ocr的 txt 文本
        List<FormData> dataList = formDataService.selectFormData(formDefinitionId, (sqlQuery, formRelationWrapper) -> {
            sqlQuery.equal(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_DISTINGUISH_STATE), '1')
                    .orderByDesc(formRelationWrapper.getColumn(ArchiveEntity.COLUMN_ARCHIVE_CODE));
        });
        for (FormData formData : dataList) {
            String fondCode = formData.get(ArchiveEntity.COLUMN_FOND_CODE);
            String archivalCode = formData.get(ArchiveEntity.COLUMN_ARCHIVE_CODE);
            //获取txt文本地址
            File txtFiles = new File(String.join(File.separator, filePath, "txt", fondCode, archivalCode));
            if (!txtFiles.exists()) {
                txtFiles.mkdirs();
            }
            //存在数据
            if (txtFiles.listFiles().length > 0) {
                //对文件进行排序操作
                List<File> orderFiles = FileUtil.orderByName(txtFiles);
                //每一个文件的内容都去匹配是否存在 tables 标签。
                for (int i = 0; i < orderFiles.size(); i++) {
                    //创建Excel存放位置
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
                        //创建工作簿
                        try {
                            WritableWorkbook workbook = Workbook.createWorkbook(excelFiles);
                            WritableSheet sheet1 = workbook.createSheet("sheet1", 0);
                            Label label = null;
                            int count = 0;
                            for (int j = 0; j < tableList.size(); j++) {
                                List<Map> cellList = tableList.get(j);
                                //将list中的数据添加至工作簿中
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
     * txt 合并
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
        //查询选择的门类的所以档案数据
        List<FormData> list = dataManager.findDataByQuery(queryArchive);
        if (list.size() > 0) {
            //根据案卷数据查询 原文数据
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
