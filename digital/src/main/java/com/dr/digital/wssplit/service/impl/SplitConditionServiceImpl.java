
package com.dr.digital.wssplit.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.dr.digital.manage.model.entity.ArchiveEntity;
import com.dr.digital.util.FileUtil;
import com.dr.digital.util.MySimHash;
import com.dr.digital.wssplit.service.SplitConditionService;
import com.dr.framework.common.form.core.model.FormData;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SplitConditionServiceImpl implements SplitConditionService {
    /**
     * 页码是第一页，且只包含全宗号、年度、保管期限、文号、标题、日期、页数的，
     * 识别为  (封面)
     *
     * @param txtFile
     * @param formData
     * @return
     */
    @Override
    public boolean judgeCover(File txtFile, FormData formData) {
        boolean flag = false;
        String fond_code = formData.get(ArchiveEntity.COLUMN_FOND_CODE);//全宗号
        String bgQx = formData.get(ArchiveEntity.COLUMN_SAVE_TERM);//保管期限
        String year = formData.get(ArchiveEntity.COLUMN_YEAR);//年度
        String txtContent = readTxtContent(txtFile);
        if (!StringUtils.isEmpty(txtContent)) {
            if (!StringUtils.isEmpty(fond_code)) {
                if (txtContent.contains(fond_code)) {
                    if (!StringUtils.isEmpty(bgQx)) {//保管期限是否为空
                        if (txtContent.contains(bgQx)) {//是否包含保管期限
                            if (!StringUtils.isEmpty(year)) {
                                if (txtContent.contains(year)) {
                                    flag = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return flag;
    }

    /**
     * 标题为 (会议记录 ) 的，全部识别为正文
     *
     * @param txtFile
     * @param formData
     * @return
     */
    @Override
    public boolean meetingJudgment(File txtFile, FormData formData) {
        boolean flag = false;
        if (formData.containsKey(ArchiveEntity.COLUMN_TITLE)) {
            String title = formData.get(ArchiveEntity.COLUMN_TITLE);
            if (title.contains("会议记录")) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 标题为 (年报) 的，全部识别为正文
     *
     * @param txtFile
     * @param formData
     * @return
     */
    @Override
    public boolean annualReportJudgment(File txtFile, FormData formData) {
        boolean flag = false;
        if (formData.containsKey(ArchiveEntity.COLUMN_TITLE)) {
            String title = formData.get(ArchiveEntity.COLUMN_TITLE);
            if (title.contains("年报")) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 页面前5行最大字体大于XX，包含“榕XX【年度】文号”，
     * 正文开始前5行，一定存在本单位全宗名称
     * 8行于屏幕左侧单独存在“XXXXX:”的，
     * 本页作为 (正文开始)
     *
     * @param txtFile
     * @param formData
     * @return
     */
    @Override
    public boolean textStartJudgment(File txtFile, FormData formData) {
        boolean flag = false;
        String fonds_name = formData.get(ArchiveEntity.COLUMN_FOND_CODE); //全宗名称
        String document_number = formData.get(ArchiveEntity.COLUMN_FILECODE); //文号
        boolean hongTouFlag = dzHongTou(txtFile);
        if (!hongTouFlag) {
            boolean titleFlag = dzTitle(txtFile);
            if (titleFlag) {
                List<Map> list = readTxtPositions(txtFile);
                if (list != null && list.size() > 0) {
                    int sFz = (list.size() / 3);
                    boolean nrZc = false;
                    boolean nrBh = false;
                    for (int i = 0; i < sFz; i++) {
                        Map mapQ8 = list.get(i);
                        String nr = (String) mapQ8.get("content");
                        if (nr != null && !"".equals(nr)) {
                            if (fonds_name != null && !"".equals(fonds_name)) {
                                if (document_number != null && !"".equals(document_number))
                                    if ((nr.contains(document_number)) || nr.contains(fonds_name)) {
                                        nrBh = true;
                                    }
                            }
                            if (nr.contains(":")) {
                                //判读位于页面左侧 x小于1100
                                List<Map> xyList = (List<Map>) mapQ8.get("positions");
                                int fistX = 0;
                                for (int b = 0; b < 1; b++) {
                                    fistX = Integer.parseInt((String) xyList.get(0).get("x"));
                                }
                                if (fistX < 500) { //位于页面左侧
                                    nrZc = true;
                                }
                            }
                        }
                    }
                    if (nrBh && nrZc) {
                        flag = true;
                    }
                }
            }
        } else {
            flag = true;
        }
        return flag;
    }

    /**
     * 单张 判断是否是（正文结束）,页面于屏幕右侧单独存在X年X月X日
     *
     * @param txtFile
     * @param formData
     * @return
     */
    @Override
    public boolean textDzEndJudgment(File txtFile, FormData formData) {
        return zuNyr(txtFile);
    }

    /**
     * 看是否存在标题
     *
     * @param txtFile
     * @return
     */
    @Override
    public boolean textTitleJudgment(File txtFile) {
        List<Map> list = readTxtPositions(txtFile);
        //判断这张是否存在标题
        return dzTitleState(list);
    }

    /**
     * 文本中存在“阅办单”或“办理单”的，识别为办理单
     *
     * @param txtFile
     * @param formData
     * @return
     */
    @Override
    public boolean handlingSheetJudgment(File txtFile, FormData formData) {
        boolean flag = false;
        String txtContent = readTxtContent(txtFile);
        if (!StringUtils.isEmpty(txtContent)) {
            if (txtContent.contains("阅办单") || txtContent.contains("办理单")) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 文本中同时存在“签发”“会稿”“审稿”“核稿”“事由”“主送”“抄送”“标题”“概述”“密级”等大部分词汇时，判定为办理单
     *
     * @param txtFile
     * @param formData
     * @return
     */
    @Override
    public boolean handlingSheetTwoJudgment(File txtFile, FormData formData) {
        boolean flag = false;
        String txtContent = readTxtContent(txtFile);
        if (txtContent.contains("签发") || txtContent.contains("核稿") || txtContent.contains("事由")) {
            if (txtContent.contains("审稿") || txtContent.contains("主送") || txtContent.contains("标题")) {
                if (txtContent.contains("会稿") || txtContent.contains("抄送")) {
                    flag = true;
                } else if (txtContent.contains("密级") || txtContent.contains("概述")) {
                    flag = true;
                }
            }
        }
        return flag;
    }

    /**
     * 正文开始结束之间，存在“附表”“附件”“附后”“附：”等词汇，判定本份档案疑似拥有附件，
     * 根据可判断的正文、办理单、草稿、封面以外的统一识别为附件
     *
     * @param txtFile
     * @param formData
     * @return
     */
    @Override
    public boolean attachmentJudgment(File txtFile, FormData formData) {
        boolean flag = false;
        List<Map> list = readTxtPositions(txtFile);
        if (list != null && list.size() > 3) {
            for (int i = 0; i < 3; i++) {
                Map mapQ3 = list.get(i);
                String nr = (String) mapQ3.get("content");
                if (nr != null && !"".equals(nr)) {
                    if (nr.contains("附表") || nr.contains("附件") || nr.contains("附后") || nr.contains("附")) {
                        flag = true;
                    }
                }
            }
        } else if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                Map mapQ3 = list.get(i);
                String nr = (String) mapQ3.get("content");
                if (nr != null && !"".equals(nr)) {
                    if (nr.contains("附表") || nr.contains("附件") || nr.contains("附后") || nr.contains("附")) {
                        flag = true;
                    }
                }
            }
        }
        return flag;
    }

    /**
     * 查询组合这件的标题 ,与正文中 附 后面的 做对比查看是否存在
     * (附件)
     *
     * @param txtFile
     * @param formData
     * @return
     */
    public boolean attachmentTwoJudgment(File txtFile, FormData formData, Map<String, List<String>> map) {
        boolean flag = false;
        List<Map> list = readTxtPositions(txtFile);
        String titleOne = txtOTitle(list);
        String titleTwo = txtTTitle(list);
        String titleSRee = txtSTitle(list);
        String titleFro = txtFTitle(list);
        String titleDy = txtDyTitle(list);
        String zwContent = "";
        List<String> listStr = map.get(FileUtil.getNewFileName(2));
        if (listStr != null && listStr.size() > 0) {
            for (String str : listStr) {
                File newZw = new File(str);
                zwContent += readTxtContent(newZw);
            }
        }
        if (ifBaoHan(zwContent, titleDy)) {
            flag = true;
        } else if (ifBaoHan(zwContent, titleFro)) {
            flag = true;
        } else if (ifBaoHan(zwContent, titleSRee)) {
            flag = true;
        } else if (ifBaoHan(zwContent, titleTwo)) {
            flag = true;
        } else if (ifBaoHan(zwContent, titleOne)) {
            flag = true;
        }
        return flag;
    }

    /**
     * 增加签发单判断方法，判断标题字体大小是与正文标题字体大小一致，切内容一致视为签发单。
     * （办理单）
     *
     * @param txtFile
     * @param map
     * @return
     */
    @Override
    public boolean handlingSheetSrJudgment(File txtFile, Map<String, List<String>> map) {
        boolean flag = false;
        if (!map.isEmpty()) {
            boolean xsFlag = false;
            //获取当前map下正文内容的第一页 对比内容相似度
            List<String> listZw = map.get(FileUtil.getNewFileName(2));
            String zwXqContent = "";
            int sFz = 0;
            int eFz = 0;
            if (listZw != null && listZw.size() > 0) {
                if (listZw.get(0) != null && !"".equals(listZw.get(0))) {
                    File zwFile = new File(listZw.get(0));
                    String zwTxt = readTxtContent(zwFile);
                    String qfdTxt = readTxtContent(txtFile);
                    //对比文件内容
                    MySimHash hash1 = new MySimHash(zwTxt, 64);
                    MySimHash hash2 = new MySimHash(qfdTxt, 64);
                    double bv = hash1.getSemblance(hash2);
                    // 获取 正文第一张和 办理单的字体个数
                    double zwZs = (zwTxt.length() - 35);//剪切35个归档章的内容
                    double qfdZs = qfdTxt.length();
                    //字体数量 签发单 大于 正文首页 字体数量
                    if ((bv > 0.7) && (qfdZs >= zwZs)) {
                        xsFlag = true;
                    }
                    List<Map> listZwXq = readTxtPositions(zwFile);
                    //获取 正文首页中下区域内容 固定值
                    sFz = (listZwXq.size() / 3);
                    eFz = (listZwXq.size() / 2);
                    //根据页面坐标找内容
                    zwXqContent = xqContent(sFz, eFz, listZwXq);
                }
            }
            if (xsFlag) {
                boolean xqFlag = false;
                List<Map> list = readTxtPositions(txtFile);
                String txtContent = xqContent(sFz, eFz, list);
                if (!"".equals(txtContent) && txtContent != null) {
                    //判断中文选择的内容 与 签发单选取的内容是否 相等。
                    MySimHash hash1 = new MySimHash(zwXqContent, 64);
                    MySimHash hash2 = new MySimHash(txtContent, 64);
                    double bv = hash1.getSemblance(hash2);
                    if (bv > 0.82) {
                        xqFlag = true;
                    }
                }
                if (xqFlag) {
                    flag = true;
                }
            }
        }
        return flag;
    }

    /**
     * 1、草稿标题与正文相似85%以上
     * 2、文章内容大于70%相似度
     * 3、字数与正文相比在正负30%之间
     * <p>
     * 时视为(草稿)
     *
     * @param syList
     * @param map
     * @return
     */
    @Override
    public boolean manuscriptJudgment(List<String> syList, Map<String, List<String>> map) {
        boolean flag = false;
        if (!map.isEmpty()) {
            boolean xsFlag = false;
            //拼接 所有的正文内容
            String zwContent = "";
            List<String> listStr = map.get(FileUtil.getNewFileName(2));
            if (listStr != null && listStr.size() > 0) {
                for (String str : listStr) {
                    File newZw = new File(str);
                    zwContent += readTxtContent(newZw);
                }
            }
            //拼接所有底稿的内容
            String dgTxt = "";
            for (String strOne : syList) {
                File txtFile = new File(strOne);
                dgTxt += readTxtContent(txtFile);
            }
            //对比正文文件内容
            MySimHash hash1 = new MySimHash(zwContent, 64);
            MySimHash hash2 = new MySimHash(dgTxt, 64);
            double bv = hash1.getSemblance(hash2);
            if (bv > 0.7) {
                xsFlag = true;
            }
            if (xsFlag) {
                //字体个数在正负百分之30%之间
                double zwZs = zwContent.length();
                double dgZs = dgTxt.length();
                if (zwZs >= dgZs) {
                    double yz = (dgZs / zwZs);
                    if (yz > 0.71) {
                        flag = true;
                    }
                } else {
                    double yz = (zwZs / dgZs);
                    if (yz > 0.71) {
                        flag = true;
                    }
                }
            }
        }
        return flag;
    }

    /************************************ 下面是共用方法体 **************************************/

    /**
     * 判断是否 存在 大红头的 字体
     *
     * @param txtFile
     * @return
     */
    public boolean dzHongTou(File txtFile) {
        boolean dzFlag = false;
        List<Map> list = readTxtPositions(txtFile);
        if (list != null && list.size() > 0) {
            //先判断txt是正着排序的，还是倒在排序的 true：正序 ； false：倒序；
            boolean pxFlag = sortJudgment(list);
            if (pxFlag) { //正序
                int sFz = (list.size() / 3);
                for (int i = 0; i < sFz; i++) {
                    Map mapQ8 = list.get(i);
                    //判读前5行字体 大小
                    List<Map> listXY = (List<Map>) mapQ8.get("positions");
                    int ya = Integer.parseInt((String) listXY.get(1).get("y"));
                    int yb = Integer.parseInt((String) listXY.get(2).get("y"));
                    int xa = Integer.parseInt((String) listXY.get(0).get("y"));
                    int xb = Integer.parseInt((String) listXY.get(3).get("y"));
                    //获取第2个坐标和第3个坐标的y值判读出字体的大小
                    if ((yb - ya) > 150 || (xb - xa) > 150) {
                        String nr = mapQ8.get("content") + "";
                        if (!"".equals(nr)) {
                            boolean tsZf = nr.matches("[\n`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘]+");
                            if (tsZf) {
                                if (nr.length() >= 3) {
                                    dzFlag = true;
                                }
                            } else {
                                if (nr.length() >= 2) {
                                    dzFlag = true;
                                }
                            }
                        }
                    }
                }
            } else { //倒序
                int sFz = (list.size() - (list.size() / 3));
                for (int i = sFz; i < list.size(); i++) {
                    Map mapQ8 = list.get(i);
                    //判读前5行字体 大小
                    List<Map> listXY = (List<Map>) mapQ8.get("positions");
                    int ya = Integer.parseInt((String) listXY.get(1).get("y"));
                    int yb = Integer.parseInt((String) listXY.get(2).get("y"));
                    int xa = Integer.parseInt((String) listXY.get(0).get("y"));
                    int xb = Integer.parseInt((String) listXY.get(3).get("y"));
                    //获取第2个坐标和第3个坐标的y值判读出字体的大小
                    if ((yb - ya) > 150 || (xb - xa) > 150) {
                        String nr = mapQ8.get("content") + "";
                        if (!"".equals(nr)) {
                            boolean tsZf = nr.matches("[\n`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘]+");
                            if (tsZf) {
                                if (nr.length() >= 3) {
                                    dzFlag = true;
                                }
                            } else {
                                if (nr.length() >= 2) {
                                    dzFlag = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return dzFlag;
    }

    /**
     * 判断是否 存在 除红头外的 (标题)
     *
     * @param txtFile
     * @return
     */
    public boolean dzTitle(File txtFile) {
        boolean dzFlag = false;
        List<Map> list = readTxtPositions(txtFile);
        if (list != null && list.size() > 0) {
            int sFz = (list.size() / 3);
            for (int i = 0; i < sFz; i++) {
                Map mapQ8 = list.get(i);
                List<Map> listXY = (List<Map>) mapQ8.get("positions");
                int ya = Integer.parseInt((String) listXY.get(1).get("y"));
                int yb = Integer.parseInt((String) listXY.get(2).get("y"));
                int xa = Integer.parseInt((String) listXY.get(0).get("y"));
                int xb = Integer.parseInt((String) listXY.get(3).get("y"));
                if ((80 > (yb - ya) && (yb - ya) > 70) && (80 > (xb - xa) && (xb - xa) > 70)) {
                    String nr = mapQ8.get("content") + "";
                    if (!"".equals(nr)) {
                        boolean tsZf = nr.matches("[\n`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘]+");
                        if (tsZf) {
                            if (nr.length() >= 4) {
                                dzFlag = true;
                            }
                        } else {
                            if (nr.length() >= 2) {
                                dzFlag = true;
                            }
                        }
                    }
                }
            }
        }
        return dzFlag;
    }

    public boolean dzTitleState(List<Map> list) {
        boolean dzFlag = false;
        if (list != null && list.size() > 0) {
            //先判断txt是正着排序的，还是倒在排序的 true：正序 ； false：倒序；
            boolean pxFlag = sortJudgment(list);
            if (pxFlag) { //正序
                int sFz = (list.size() / 3);
                for (int i = 0; i < sFz; i++) {
                    Map mapQ8 = list.get(i);
                    //判读前5行字体 大小
                    List<Map> listXY = (List<Map>) mapQ8.get("positions");
                    int ya = Integer.parseInt((String) listXY.get(1).get("y"));
                    int yb = Integer.parseInt((String) listXY.get(2).get("y"));
                    int xa = Integer.parseInt((String) listXY.get(0).get("y"));
                    int xb = Integer.parseInt((String) listXY.get(3).get("y"));
                    if ((83 > (yb - ya) && (yb - ya) > 73) && (83 > (xb - xa) && (xb - xa) > 73)) {
                        String nr = mapQ8.get("content") + "";
                        if (!"".equals(nr)) {
                            boolean tsZf = nr.matches("[\n`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘]+");
                            if (tsZf) {
                                if (nr.length() >= 4) {
                                    dzFlag = true;
                                }
                            } else {
                                if (nr.length() >= 2) {
                                    dzFlag = true;
                                }
                            }
                        }
                    }
                }
            } else { //倒序
                int sFz = (list.size() - (list.size() / 3));
                for (int i = sFz; i < list.size(); i++) {
                    Map mapQ8 = list.get(i);
                    //判读前5行字体 大小
                    List<Map> listXY = (List<Map>) mapQ8.get("positions");
                    int ya = Integer.parseInt((String) listXY.get(1).get("y"));
                    int yb = Integer.parseInt((String) listXY.get(2).get("y"));
                    int xa = Integer.parseInt((String) listXY.get(0).get("y"));
                    int xb = Integer.parseInt((String) listXY.get(3).get("y"));
                    if ((83 > (yb - ya) && (yb - ya) > 73) && (83 > (xb - xa) && (xb - xa) > 73)) {
                        String nr = mapQ8.get("content") + "";
                        if (!"".equals(nr)) {
                            boolean tsZf = nr.matches("[\n`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘]+");
                            if (tsZf) {
                                if (nr.length() >= 4) {
                                    dzFlag = true;
                                }
                            } else {
                                if (nr.length() >= 2) {
                                    dzFlag = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return dzFlag;
    }

    /**
     * 排序看是倒序还是正序
     *
     * @return
     */
    public boolean sortJudgment(List<Map> list) {
        boolean flag = false;
        List<Map> listXq = (List<Map>) list.get(0).get("positions");
        List<Map> listZh = (List<Map>) list.get((list.size() - 1)).get("positions");
        int qXa = Integer.parseInt((String) listXq.get(0).get("y"));
        int hXa = Integer.parseInt((String) listZh.get(0).get("y"));
        if (hXa > qXa) {
            flag = true;
        }
        return flag;
    }

    /**
     * 判断 最后 页面  右侧 是否存在 XXXX年 XX月 XX日
     *
     * @param txtFile
     * @return
     */
    public boolean zuNyr(File txtFile) {
        List<Map> list = readTxtPositions(txtFile);
        boolean zuFlag = false;
        if (list != null && list.size() >= 0) {
            int sFz = (list.size() / 3);
            for (int i = (list.size() - 1); i >= (list.size() - sFz); i--) {
                Map mapQ5 = list.get(i);
                String nr = (String) mapQ5.get("content");
                if ((nr.contains("年") && nr.contains("月")) && nr.contains("日")) {
                    List<Map> zbList = (List<Map>) mapQ5.get("positions");
                    int fistX = 0;
                    for (int b = 0; b < 1; b++) {
                        fistX = Integer.parseInt((String) zbList.get(0).get("x"));
                    }
                    if (fistX > 1100) { //位于页面右侧
                        zuFlag = true;
                    }
                }
            }
        }
        return zuFlag;
    }

    /**
     * 获取一块区域获取值
     *
     * @param sFz
     * @param eFz
     * @param list
     * @return
     */
    public static String xqContent(int sFz, int eFz, List<Map> list) {
        String txtContent = "";
        if (list != null && list.size() > eFz) {
            for (int i = sFz; i <= eFz; i++) {
                if (list.get(i) != null && list.get(i).size() > 0) {
                    txtContent += list.get(i).get("content");
                }
            }
        }
        return txtContent;
    }

    /**
     * 生成标题方法 （70-80）
     *
     * @param list
     * @return
     */
    public static String txtOTitle(List<Map> list) {
        String title = "";
        if (list != null && list.size() >= 0) {
            int sFz = (list.size() / 3);
            for (int i = 0; i < sFz; i++) {
                Map mapQ8 = list.get(i);
                //判读前5行字体 大小
                List<Map> listXY = (List<Map>) mapQ8.get("positions");
                int ya = Integer.parseInt((String) listXY.get(1).get("y"));
                int yb = Integer.parseInt((String) listXY.get(2).get("y"));
                int xa = Integer.parseInt((String) listXY.get(0).get("y"));
                int xb = Integer.parseInt((String) listXY.get(3).get("y"));
                if ((80 > (yb - ya) && (yb - ya) > 70) && (80 > (xb - xa) && (xb - xa) > 70)) {
                    title += mapQ8.get("content");
                }
            }
        }
        return title;
    }

    /**
     * 生成标题方法 （80-100）
     *
     * @param list
     * @return
     */
    public static String txtTTitle(List<Map> list) {
        String title = "";
        if (list != null && list.size() >= 0) {
            int sFz = (list.size() / 3);
            for (int i = 0; i < sFz; i++) {
                Map mapQ8 = list.get(i);
                //判读前5行字体 大小
                List<Map> listXY = (List<Map>) mapQ8.get("positions");
                int ya = Integer.parseInt((String) listXY.get(1).get("y"));
                int yb = Integer.parseInt((String) listXY.get(2).get("y"));
                int xa = Integer.parseInt((String) listXY.get(0).get("y"));
                int xb = Integer.parseInt((String) listXY.get(3).get("y"));
                if ((100 > (yb - ya) && (yb - ya) > 80) || (100 > (xb - xa) && (xb - xa) > 80)) {
                    title += mapQ8.get("content");
                }
            }
        }
        return title;
    }

    /**
     * 生成标题方法 （100-120）
     *
     * @param list
     * @return
     */
    public static String txtSTitle(List<Map> list) {
        String title = "";
        if (list != null && list.size() >= 0) {
            int sFz = (list.size() / 3);
            for (int i = 0; i < sFz; i++) {
                Map mapQ8 = list.get(i);
                //判读前5行字体 大小
                List<Map> listXY = (List<Map>) mapQ8.get("positions");
                int ya = Integer.parseInt((String) listXY.get(1).get("y"));
                int yb = Integer.parseInt((String) listXY.get(2).get("y"));
                int xa = Integer.parseInt((String) listXY.get(0).get("y"));
                int xb = Integer.parseInt((String) listXY.get(3).get("y"));
                if ((120 > (yb - ya) && (yb - ya) > 100) || (120 > (xb - xa) && (xb - xa) > 100)) {
                    title += mapQ8.get("content");
                }
            }
        }
        return title;
    }

    /**
     * 生成标题方法 （120-150）
     *
     * @param list
     * @return
     */
    public static String txtFTitle(List<Map> list) {
        String title = "";
        if (list != null && list.size() >= 0) {
            int sFz = (list.size() / 3);
            for (int i = 0; i < sFz; i++) {
                Map mapQ8 = list.get(i);
                //判读前5行字体 大小
                List<Map> listXY = (List<Map>) mapQ8.get("positions");
                int ya = Integer.parseInt((String) listXY.get(1).get("y"));
                int yb = Integer.parseInt((String) listXY.get(2).get("y"));
                int xa = Integer.parseInt((String) listXY.get(0).get("y"));
                int xb = Integer.parseInt((String) listXY.get(3).get("y"));
                if ((150 > (yb - ya) && (yb - ya) > 120) || (150 > (xb - xa) && (xb - xa) > 120)) {
                    title += mapQ8.get("content");
                }
            }
        }
        return title;
    }

    public static String txtDyTitle(List<Map> list) {
        String title = "";
        if (list != null && list.size() >= 0) {
            int sFz = (list.size() / 3);
            for (int i = 0; i < sFz; i++) {
                Map mapQ8 = list.get(i);
                //判读前5行字体 大小
                List<Map> listXY = (List<Map>) mapQ8.get("positions");
                int ya = Integer.parseInt((String) listXY.get(1).get("y"));
                int yb = Integer.parseInt((String) listXY.get(2).get("y"));
                int xa = Integer.parseInt((String) listXY.get(0).get("y"));
                int xb = Integer.parseInt((String) listXY.get(3).get("y"));
                if ((yb - ya) > 145 || (xb - xa) > 145) {
                    title += mapQ8.get("content");
                }
            }
        }
        return title;
    }

    public boolean ifBaoHan(String zwContent, String title) {
        String regEx = "[\n`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？]";
        boolean flag = false;
        if (!"".equals(title) && title != null) {
            title = title.replaceAll(regEx, "").replaceAll("\\d+", "");
            if (!"".equals(title) && title != null) {
                if (zwContent.contains("附件:")) {
                    zwContent = zwContent.substring(zwContent.lastIndexOf("附件:"));
                    if (zwContent.contains(title)) {
                        flag = true;
                    }
                } else if (zwContent.contains("附表:")) {
                    zwContent = zwContent.substring(zwContent.lastIndexOf("附表:"));
                    if (zwContent.contains(title)) {
                        flag = true;
                    }
                } else if (zwContent.contains("附后:")) {
                    zwContent = zwContent.substring(zwContent.lastIndexOf("附后:"));
                    if (zwContent.contains(title)) {
                        flag = true;
                    }
                }
            }
        }
        return flag;
    }

    /**
     * 读取txt文件内容
     *
     * @param file
     * @return
     */
    public List<Map> readTxtPositions(File file) {
        List<Map> list = new ArrayList<>();
        try {
            String s = "";
            FileInputStream inputStream = new FileInputStream(file);
            InputStreamReader in = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader br = new BufferedReader(in);
            StringBuffer content = new StringBuffer();
            while ((s = br.readLine()) != null) {
                content = content.append(s);
            }
            if (content != null && !"".equals(content.toString()) && content.toString() != "") {
                Map page = (Map) JSONObject.parseObject(content.toString(), Map.class);
                list = (List<Map>) page.get("items");
            }
            inputStream.close();
            in.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 读取txt文件Content文本内容
     *
     * @param file
     * @return
     */
    public String readTxtContent(File file) {
        StringBuffer txtContent = new StringBuffer();
        try {
            String s = "";
            FileInputStream inputStream = new FileInputStream(file);
            InputStreamReader in = new InputStreamReader(new FileInputStream(file), "UTF-8");
            BufferedReader br = new BufferedReader(in);
            StringBuffer content = new StringBuffer();
            while ((s = br.readLine()) != null) {
                content = content.append(s);
            }
            Map page = (Map) JSONObject.parseObject(content.toString(), Map.class);
            List<Map> list = (List<Map>) page.get("items");
            for (Map map : list) {
                txtContent.append(map.get("content"));
            }
            inputStream.close();
            in.close();
        } catch (Exception e) {
            txtContent.append("");
        }
        return txtContent.toString();
    }

    @Override
    public List readTxtTables(File file) {
        List<List> cellList = new ArrayList<>();
        try {
            String s = "";
            FileInputStream inputStream = new FileInputStream(file);
            InputStreamReader in = new InputStreamReader(new FileInputStream(file), "UTF-8");
            BufferedReader br = new BufferedReader(in);
            StringBuffer content = new StringBuffer();
            while ((s = br.readLine()) != null) {
                content = content.append(s);
            }
            if (content != null && !"".equals(content.toString()) && content.toString() != "") {
                Map page = (Map) JSONObject.parseObject(content.toString(), Map.class);
                if (page.containsKey("tables")) {
                    List<Map> list = (List<Map>) page.get("tables");
                    for (Map map : list) {
                        List<Map> cellsList = (List) map.get("cells");
                        cellList.add(cellsList);
                    }
                }
            }
            inputStream.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cellList;
    }
}
