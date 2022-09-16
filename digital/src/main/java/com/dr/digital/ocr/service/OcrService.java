package com.dr.digital.ocr.service;

import com.dr.digital.ocr.entity.DisassemblyRecord;
import com.dr.digital.ocr.entity.DisassemblyRecordDetail;
import com.dr.digital.ocr.entity.GeneralResultEntity;
import com.dr.digital.ocr.entity.TableResultEntity;
import com.dr.digital.ocr.query.OcrQuery;
import com.dr.digital.wssplit.entity.SplitRule;
import com.dr.framework.common.form.core.model.FormData;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.web.annotations.Current;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author caor
 * @date 2021-09-17 11:10
 */
public interface OcrService {
    /**
     * 在线图片转换成base64字符串
     *
     * @param imgURL 图片线上路径
     * @return
     */
    String imageToBase64ByOnline(String imgURL) throws UnsupportedEncodingException;

    GeneralResultEntity shiBieOcrTxt(String imgURL);

    TableResultEntity shiBieOcrTable(String imgURL);

    /**
     * 自动拆件方法
     *
     * @param query
     * @param person
     */
    void chaiJIan(OcrQuery query, Person person);

    /**
     * 执行（文书档案）自动拆件逻辑
     *
     * @param registerId
     * @param formData
     * @param splitRules
     */
    void wsSplintChaiJan(String registerId, FormData formData, List<SplitRule> splitRules);

    /**
     * 批次上的拆件
     *
     * @param formDefinitionId
     * @param registerId
     * @param person
     */
    void batchDisassembly(String formDefinitionId, String registerId, Person person);

    /**
     * 批次上 txt to Excel
     *
     * @param formDefinitionId
     * @param registerId
     * @param person
     */
    void ocrTxtToExcel(String formDefinitionId, String registerId, @Current Person person);

    /**
     * txt 合并
     *
     * @param query
     * @param person
     */
    void txtHb(OcrQuery query, Person person);

    List<DisassemblyRecordDetail> disassemblyRecord(DisassemblyRecord disassemblyRecord);
}
