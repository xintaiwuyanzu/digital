package com.dr.digital.ocr.service;


import com.dr.digital.ocr.bo.TableBo;
import com.dr.digital.ocr.entity.TableResultEntity;
import com.dr.digital.ocr.service.impl.FeignClientTableConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * ofd接口客户端
 *
 * @author caor
 * @date 2021-09-17 9:54
 */
@FeignClient(name = "ocr-table-client", url = "${ocr.base-url}", configuration = FeignClientTableConfig.class)
public interface OcrTableClient {

    /**
     * 通用表格识别
     *
     * @param tableBo
     * @return
     */
    @PostMapping(value = "v1/mage/ocr/table")
    TableResultEntity table(@RequestBody TableBo tableBo);
}
