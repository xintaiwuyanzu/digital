package com.dr.digital.ocr.controller;

import com.dr.digital.manage.model.query.ArchiveDataQuery;
import com.dr.digital.ocr.bo.TemplateBo;
import com.dr.digital.ocr.entity.DisassemblyRecord;
import com.dr.digital.ocr.entity.DisassemblyRecordDetail;
import com.dr.digital.ocr.entity.Results;
import com.dr.digital.ocr.entity.TemplateResultEntity;
import com.dr.digital.ocr.query.OcrQuery;
import com.dr.digital.ocr.service.OcrGeneralClient;
import com.dr.digital.ocr.service.OcrService;
import com.dr.digital.ocr.service.OcrTemplateClient;
import com.dr.digital.template.entity.Template;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.service.CommonService;
import com.dr.framework.core.organise.entity.Person;
import com.dr.framework.core.orm.sql.support.SqlQuery;
import com.dr.framework.core.web.annotations.Current;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ocr")
public class OcrController {
    Logger logger = LoggerFactory.getLogger(OcrController.class);
    @Autowired
    OcrService ocrService;
    @Autowired
    OcrGeneralClient ocrGeneralClient;
    @Autowired
    OcrTemplateClient ocrTemplateClient;
    @Autowired
    CommonService commonService;
    @Value("${scan}")
    private String scan;

    /**
     * 通用文字识别
     *
     * @param imgURL
     * @return
     */
    @RequestMapping("/imageToBase64ByOnline")
    public ResultEntity imageToBase64ByOnline(String imgURL) throws UnsupportedEncodingException {
        String img = ocrService.imageToBase64ByOnline(imgURL);
        return ResultEntity.success(ocrService.shiBieOcrTxt(img).getData());
    }

    /**
     * 模板识别
     *
     * @param imgURL
     * @return
     */
    @RequestMapping("/template")
    public ResultEntity template(String imgURL) throws IOException {
        Map<String, String> result = new HashMap<>();
        String img = ocrService.imageToBase64ByOnline(imgURL);
        TemplateBo templateBo = new TemplateBo(true, true, img);
        TemplateResultEntity templateResultEntity = ocrTemplateClient.template(templateBo);
        logger.info("自定义模板识别成功:{}", templateResultEntity.getData().getResults());
        List<Template> templateList = commonService.selectList(SqlQuery.from(Template.class));
        //将模板管理里面的字段放入map得到值跟字段对象
        Map<String, Template> map = new HashMap();
        for (Template template : templateList) {
            map.put(template.getField(), template);
        }
        //获取到的自定数据，根据名称相等，将值跟识别的数据组装到result返回给前台，便于form读取
        List list = templateResultEntity.getData().getResults();
        for (int i = 0; i < list.size(); i++) {
            Results results = (Results) list.get(i);
            if (map.containsKey(results.getField_name())) {
                Template template2 = map.get(results.getField_name());
                result.put(template2.getFieldval(), results.getResults().get(0));
            }
        }
        return ResultEntity.success(result);
    }

    /**
     * 获取table
     *
     * @param imgURL
     * @return
     */
    @RequestMapping("/table")
    public ResultEntity table(String imgURL) throws UnsupportedEncodingException {
        String img = ocrService.imageToBase64ByOnline(imgURL);
        return ResultEntity.success((ocrService.shiBieOcrTable(img).getData()));
    }

    /**
     * 获取后台application中配置的扫描仪动态地址
     *
     * @return
     */
    @RequestMapping("/getScan")
    public ResultEntity getScan() {
        return ResultEntity.success((scan));
    }

    /**
     * 根据 OCR 结果拆分文件类型
     *
     * @return
     */
    @RequestMapping("/chaiJIan")
    public ResultEntity chaiJIan(HttpServletRequest request, OcrQuery query,
                                 @RequestParam(name = ArchiveDataQuery.QUERY_KEY, required = false) String queryContent,
                                 @Current Person person) {
        query.parseQuery(queryContent);
        ocrService.chaiJIan(query, person);
        return ResultEntity.success();
    }

    /**
     * 批次上进行的拆件操作
     *
     * @return
     */
    @RequestMapping("/batchChaiJIan")
    public ResultEntity batchChaiJIan(HttpServletRequest request, String formDefinitionId, String registerId,
                                      @RequestParam(name = ArchiveDataQuery.QUERY_KEY, required = false) String queryContent,
                                      @Current Person person) {
        ocrService.batchDisassembly(formDefinitionId, registerId, person);
        return ResultEntity.success();
    }

    /**
     * OCR TXT文本数据tables部分 抽取excel
     *
     * @return
     */
    @RequestMapping("/ocrTxtToExcel")
    public ResultEntity ocrTxtToExcel(String formDefinitionId, String registerId, @Current Person person) {
        ocrService.ocrTxtToExcel(formDefinitionId, registerId, person);
        return ResultEntity.success();
    }

    /**
     * ocr结果txt文件合并
     *
     * @return
     */
    @RequestMapping("/txtHb")
    public ResultEntity txtHb(HttpServletRequest request, OcrQuery query,
                              @RequestParam(name = ArchiveDataQuery.QUERY_KEY, required = false) String queryContent,
                              @Current Person person) {
        query.parseQuery(queryContent);
        ocrService.txtHb(query, person);
        return ResultEntity.success();
    }
    @RequestMapping("/disassemblyRecord")
    public ResultEntity disassemblyRecord(DisassemblyRecord disassemblyRecord) {
        List<DisassemblyRecordDetail> disassemblyRecordDetails = ocrService.disassemblyRecord(disassemblyRecord);
        return ResultEntity.success(disassemblyRecordDetails);
    }

}
