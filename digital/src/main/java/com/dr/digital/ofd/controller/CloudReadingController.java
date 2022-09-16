package com.dr.digital.ofd.controller;


import com.dr.framework.common.entity.ResultEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/reading")
public class CloudReadingController {


    @RequestMapping("/getFilePath")
    public ResultEntity getFilePath(String id){

        return ResultEntity.success();
    }
}
