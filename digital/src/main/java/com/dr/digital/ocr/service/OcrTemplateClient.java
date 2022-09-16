package com.dr.digital.ocr.service;

import com.dr.digital.ocr.bo.TemplateBo;
import com.dr.digital.ocr.entity.TemplateResultEntity;
import com.dr.digital.ocr.service.impl.FeignClientTemplateConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * ofd接口客户端
 *
 * @author caor
 * @date 2021-09-17 9:54
 */
@FeignClient(name = "ocr-template-client", url = "${ocr.base-url}", configuration = FeignClientTemplateConfig.class)
public interface OcrTemplateClient {
    /**
     * 自定义模板识别
     *
     * @param templateBo
     * @return
     */
    @PostMapping(value = "v1/document/ocr/template")
    TemplateResultEntity template(@RequestBody TemplateBo templateBo);
}
