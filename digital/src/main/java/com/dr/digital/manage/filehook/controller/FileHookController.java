package com.dr.digital.manage.filehook.controller;

import com.dr.digital.manage.filehook.service.service.FileHookService;
import com.dr.framework.common.entity.ResultEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author caor
 * @date 2021-03-11 8:57
 */
@RestController
@RequestMapping("api/filehook")
public class FileHookController {
    @Autowired
    FileHookService fileHookService;

    @RequestMapping("/quantityHook")
    public ResultEntity quantityHook(HttpServletRequest request) {
        String souceFilesPath = request.getParameter("souceFilesPath");
        Assert.isTrue(!StringUtils.isEmpty(souceFilesPath), "请选择路径！");
        Assert.isTrue(fileHookService.isExists(souceFilesPath), "路径不存在，请重新选择");
        fileHookService.quantityHook(souceFilesPath);
        return ResultEntity.success("原文批量挂接中，请稍后查看结果");
    }

}
