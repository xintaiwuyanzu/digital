package com.dr.digital.bsp.controller;

import com.dr.digital.bsp.service.BspSyncService;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.service.CommonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bsp")
public class BspSyncController {
    Logger logger = LoggerFactory.getLogger(BspSyncController.class);
    @Autowired
    private BspSyncService bspSyncService;
    @Autowired
    CommonService commonService;

    /**
     * 组织机构 人员同步
     *
     * @return
     */
    @PostMapping("/syncBspData")
    public ResultEntity syncBspData() {
        return ResultEntity.success(bspSyncService.syncBspData());
    }

    /**
     * 每天凌晨执行一次 同步任务
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @GetMapping("/printBspData")
    public void printBspData() {
        bspSyncService.syncBspData();
    }

}
