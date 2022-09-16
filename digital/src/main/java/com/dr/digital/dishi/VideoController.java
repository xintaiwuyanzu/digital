package com.dr.digital.dishi;

import com.alibaba.fastjson.JSONObject;
import com.dr.digital.dishi.controller.TestImperialVisionClientController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/Video")
public class VideoController {
    Logger logger = LoggerFactory.getLogger(VideoController.class);

    /**
     * @return
     */
    @PostMapping("/TranscodeBegin")
    public String TranscodeBegin(@RequestBody JSONObject param) {
        logger.info("第二步，转码开始接收开始时间请求报文：" + param.toString());
        JSONObject item = new JSONObject();
        item.put("Code", "200");
        item.put("Description", "Success");
        logger.info("第二步，转码开始接收开始时间响应报文：" + item.toString());
        return String.valueOf(item);
    }

    /**
     * @return
     */
    @PostMapping("/TranscodeWithSourceInfo")
    public String TranscodeWithSourceInfo(@RequestBody JSONObject param) {
        logger.info("第一步，转码中接收源视频信息请求报文：" + param.toString());
        JSONObject item = new JSONObject();
        item.put("Code", "200");
        item.put("Description", "Success");
        logger.info("第一步，转码中接收源视频信息响应报文：" + item.toString());
        return String.valueOf(item);
    }

    /**
     * @return
     */
    @PostMapping("/ranscodeWithTargetInfo")
    public String ranscodeWithTargetInfo(@RequestBody JSONObject param) {
        logger.info("第三步，转码中接收输出信息请求报文：" + param.toString());
        JSONObject item = new JSONObject();
        item.put("Code", "200");
        item.put("Description", "Success");
        logger.info("第三步，转码中接收输出信息响应报文：" + item.toString());
        return String.valueOf(item);
    }

    /**
     * @return
     */
    @PostMapping("/TranscodeEnd")
    public String TranscodeEnd(@RequestBody JSONObject param) {
        logger.info("第四步，转码完成接收转码结束时间请求报文：" + param.toString());
        JSONObject item = new JSONObject();
        item.put("Code", "200");
        item.put("Description", "Success");
        logger.info("第四步，转码完成接收转码结束时间响应报文：" + item.toString());
        return String.valueOf(item);
    }

    /**
     * @return
     */
    @PostMapping("/TranscodeFail")
    public String TranscodeFail(@RequestBody JSONObject param) {
        logger.info("转码失败接收转码失败原因请求报文：" + param.toString());
        JSONObject item = new JSONObject();
        item.put("Code", "200");
        item.put("Description", "Success");
        logger.info("转码失败接收转码失败原因响应报文：" + item.toString());
        return String.valueOf(item);
    }

}
