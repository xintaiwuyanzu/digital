package com.dr.digital.packet.service;

import com.dr.digital.packet.entity.ArchiveReceiveBo;
import com.dr.framework.rpc.ResultMapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@ResultMapper
@FeignClient(url = "${packetsUrl}", name = "packets")
public interface PacketsClient {

    /**
     * 数据传输接口
     *
     * @param jsonData
     * @return
     */
    @PostMapping("/receive/online/receiveOnline")
    String getReceiveDownload(ArchiveReceiveBo jsonData);

}
