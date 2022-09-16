package com.dr.digital.portal.controller;

import com.dr.digital.portal.service.PortalService;
import com.dr.framework.common.entity.ResultEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mr.Zhu
 * @date 2022/8/8 - 18:12
 */
@RestController
@RequestMapping("/api/portal")
public class PortalController {
    @Autowired
    PortalService portalService;
    /**
     * 门户系统-获取门户地址信息
     */
    @RequestMapping("/getPortalSystem")
    public ResultEntity getPortalSystem() {
        return ResultEntity.success(portalService.getPortalSystem());
    }
}
