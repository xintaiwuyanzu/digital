package com.dr.digital.wssplit.service;

import com.dr.framework.common.form.core.model.FormData;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface SplitConditionService {
    //页码是第一页，且只包含全宗号、年度、保管期限、文号、标题、日期、页数的，识别为封面
    boolean judgeCover(File txtFile, FormData formData);

    // 标题为会议记录的，全部识别为正文
    boolean meetingJudgment(File txtFile, FormData formData);

    //标题为年报的，全部识别为正文
    boolean annualReportJudgment(File txtFile, FormData formData);

    //判读是否存在红头文件
    boolean dzHongTou(File txtFile);

    //判读是否存在标题文件
    boolean dzTitle(File txtFile);

    //判读是否是文件结束
    boolean zuNyr(File txtFile);

    //正文开始
    boolean textStartJudgment(File txtFile, FormData formData);

    //最后5行，页面于屏幕右侧单独存在X年X月X日，本页判定为文件结束
    boolean textDzEndJudgment(File txtFile, FormData formData);

    //最后5行，页面于屏幕右侧单独存在X年X月X日，本页判定为正文结束
    //出现下一件 标题 内容 识别上一件为正文结束
    boolean textTitleJudgment(File txtFile);

    //文本中存在“阅办单”或“办理单”的，识别为办理单
    boolean handlingSheetJudgment(File txtFile, FormData formData);

    //文本中同时存在“签发”“会稿”“审稿”“核稿”“事由”“主送”“抄送”“标题”“概述”“密级”等大部分词汇时，判定为办理单
    boolean handlingSheetTwoJudgment(File txtFile, FormData formData);

    //存在“附表”“附件”“附后”“附：”等词汇，判定本份档案疑似拥有附件，
    // 根据可判断的正文、办理单、草稿、封面以外的统一识别为附件
    boolean attachmentJudgment(File txtFile, FormData formData);

    boolean attachmentTwoJudgment(File txtFile, FormData formData, Map<String, List<String>> map);

    //增加签发单判断方法，判断标题字体大小是与正文标题字体大小一致，切内容一致视为签发单。
    boolean handlingSheetSrJudgment(File txtFile, Map<String, List<String>> map);

    //与正文内容一致切标题小于正文标题字号，切页数基本与正文页号大致相等时视为草稿
    boolean manuscriptJudgment(List<String> syList, Map<String, List<String>> map);

    List<Map> readTxtPositions(File file);

    String readTxtContent(File file);

    //读取txt文本的tables内容
    List<List<Map>> readTxtTables(File file);

}
