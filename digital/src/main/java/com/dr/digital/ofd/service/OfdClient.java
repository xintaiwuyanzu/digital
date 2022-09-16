package com.dr.digital.ofd.service;

import com.dr.digital.ofd.bo.FileByteInfo;
import com.dr.digital.ofd.bo.FileStreamResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * ofd接口客户端
 *
 * @author dr
 */
@FeignClient(name = "ofdClient", url = "${ofd.baseIp}")
public interface OfdClient {

    /**
     * 转换统一接口
     *
     * @param fileByteInfo
     */
    @PostMapping("gsdk-service/open/convertor/contents")
    FileStreamResult turnOFD(@RequestBody FileByteInfo fileByteInfo);

    /**
     * 无token版
     *
     * @param fileByteInfo
     * @return

    @PostMapping("gsdk-service/convert/submit")
    FileStreamResult pdfturnOFD(@RequestBody FileByteInfo fileByteInfo);
     */
}
