package com.dr.digital.ofd.service;


import com.dr.digital.ofd.bo.TokenInfo;
import com.dr.digital.ofd.bo.TokenResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "tokenClient", url = "${ofd.baseIp}")
public interface TokenClient {
    /**
     * 获取token接口
     *
     * @param tokenInfo
     */
    @RequestMapping(value = "gsdk-service/open/auth", method = RequestMethod.POST)
    TokenResult token(TokenInfo tokenInfo);
}
