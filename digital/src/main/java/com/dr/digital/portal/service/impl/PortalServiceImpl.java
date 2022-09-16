package com.dr.digital.portal.service.impl;

import com.dr.digital.portal.service.PortalService;
import com.dr.framework.common.entity.ResultEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mr.Zhu
 * @date 2022/8/8 - 18:13
 */
@Service
public class PortalServiceImpl implements PortalService {
    @Value("${portal.portalSystem}")
    private String portalSystem;

    @Override
    public String getPortalSystem() {
        return portalSystem;
    }
}
