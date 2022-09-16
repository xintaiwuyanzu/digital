package com.dr.digital.ocr.service;

import com.dr.digital.ocr.bo.GeneralBo;
import com.dr.digital.ocr.entity.GeneralResultEntity;
import com.dr.digital.ocr.service.impl.FeignClientGeneralConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * ocr接口客户端
 *
 * @author caor
 * @date 2021-09-17 9:54
 */

@FeignClient(name = "ocr-general-client", url = "${ocr.base-url}", configuration = FeignClientGeneralConfig.class)
public interface OcrGeneralClient {

    /**
     * 通用文字识别
     *
     * @param generalBo
     * @return
     */
    @PostMapping(value = "v1/mage/ocr/general")
    GeneralResultEntity general(@RequestBody GeneralBo generalBo);
}
