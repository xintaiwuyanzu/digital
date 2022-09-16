package com.dr.digital.dishi.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dr.digital.dishi.ImperialVisionClient;
import com.dr.digital.util.FaceHttpUtil;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.service.CommonService;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/test")
public class TestImperialVisionClientController {
    Logger logger = LoggerFactory.getLogger(TestImperialVisionClientController.class);
    @Autowired
    CommonService commonService;

    @RequestMapping("/reqImperialVision")
    public ResultEntity<String> reqImperialVision(String inputPath, String outputPath) {
        try {
            String uuid = UUID.randomUUID().toString();
            JSONObject item = new JSONObject();
            item.put("param", 2);
            Assert.isTrue(!StringUtils.isEmpty(inputPath), "inputPath不能为空!");
            Assert.isTrue(!StringUtils.isEmpty(outputPath), "outputPath不能为空!");
            item.put("input_path", inputPath);
            item.put("output_path", outputPath);
            String jsonstring = String.valueOf(item);
            byte[] bytes_img = new byte[]{1, 2, 3};
            byte[] bytesJson = jsonstring.getBytes(StandardCharsets.UTF_8);
            ImperialVisionClient ImperialVisionClient = new ImperialVisionClient("192.168.3.34", 42009);
            ImperialVisionClient.greet(ByteString.copyFrom(bytes_img), "", bytesJson, uuid);
            ImperialVisionClient.shutdown();
        } catch (InterruptedException e) {
            logger.error("调用出错", e);
        }
        ResultEntity<String> res = ResultEntity.success("成功");
        return res;
    }

    @GetMapping("/transcoding")
    public ResultEntity<String> transcoding(HttpServletRequest request) {

        JSONObject transcoding = null;
        String resp = "";
        String id = request.getParameter("id");
        try {
            transcoding = transcoding();
            int appid = 2;
            String key = "004.mp4";
            //sign = md5(appid+key)
            //TODO MD5加密 sign等于appid+key加密后的值,这个写法可能有问题
            String sign = DigestUtils.md5DigestAsHex((String.valueOf(appid) + key).getBytes());
            transcoding.put("sign", sign);
            //TODO替换ID
            transcoding.put("id", id);
            transcoding.put("debug", "iv20161102");
            System.out.println(String.valueOf(transcoding));
            resp = FaceHttpUtil.sendHttpPost("http://192.168.3.34:31002/v1/jobs/transcoding", String.valueOf(transcoding));
            //resp =  FaceHttpUtil.sendHttpPost("http://ai.imperial-vision.com:31102/v1/jobs/transcoding", String.valueOf(transcoding));
        } catch (Exception e) {
            logger.error("视频转码调用出错", e);
        }
        ResultEntity<String> res = ResultEntity.success(resp);
        return res;
    }

    /**
     * 拼装入参的方法
     */
    public static JSONObject transcoding() throws Exception {
        //video的codecOptions
        JSONObject codecOptions = new JSONObject();
        codecOptions.put("preset", "medium");
        codecOptions.put("profile", "high");
        //presetCfg的video
        JSONObject video = new JSONObject();
        //video用下面这些固定值
        video.put("bitrateMode", "cbr");
        video.put("ai_enhance", 1);
        video.put("bf", 2);
        video.put("bitrate", 8000);
        video.put("codec", "H264");
        video.put("codecOptions", codecOptions);
        video.put("crf", 0);
        video.put("deinterlace", 1);
        video.put("denoise", 10);
        video.put("frameFiledMode", "frame");
        video.put("frameRate", 25);
        video.put("gop", 50);
        video.put("hdr_trc", 1);
        video.put("height", 1080);
        video.put("referenceFrame", 3);
        video.put("width", 1920);
        //presetCfg的audio
        JSONObject audio = new JSONObject();
        //audio用下面这些固定值
        audio.put("bitrate", 128);
        audio.put("channel", "stereo");
        audio.put("codec", "AAC");
        audio.put("sampleRateInHz", 44100);
        audio.put("volume", 1);
        //target的presetCfg
        JSONObject presetCfg = new JSONObject();
        presetCfg.put("container", "mp4");//视频MP4
        presetCfg.put("audio", audio);
        presetCfg.put("video", video);
        //JSONObject的target
        JSONObject target = new JSONObject();
        target.put("bucket", "output");
        // targets.put("inserts" , new int[0]);
        //key 视频的名字
        target.put("key", "004.mp4");
        target.put("objectBucket", "/mnt/disk/filePath/input");
        target.put("url", "http://192.168.3.34:31002");
        target.put("mountType", "s3");
        target.put("presetCfg", presetCfg);
        //targets是数组形式，其他参数为集合
        JSONArray targets = new JSONArray();
        targets.add(target);
        JSONObject preview = new JSONObject();
        preview.put("url", "http://192.168.3.34:31002");
        preview.put("bucket", "input");
        preview.put("key", "004.png");
        preview.put("mountType", "s3");
        preview.put("objectBucket", "/mnt/disk/filePath/input");
        //JSONObject的source
        JSONObject source = new JSONObject();
        source.put("bucket", "input");
        source.put("key", "004.mp4");
        source.put("objectBucket", "/mnt/disk/filePath/outinput");
        source.put("url", "http://192.168.3.34:31002");
        // source.put("clips",null);
        source.put("mountType", "s3");
        source.put("preview", preview);
        JSONObject jobConfig = new JSONObject();
        jobConfig.put("source", source);
        jobConfig.put("targets", targets);
        JSONObject item = new JSONObject();
        int appid = 2;
        String key = "004.mp4";
        item.put("appid", 2);
        //sign = md5(appid+key)
        //TODO MD5加密 sign等于appid+key加密后的值,这个写法可能有问题
        String sign = DigestUtils.md5DigestAsHex((String.valueOf(appid) + key).getBytes());
        item.put("sign", sign);
        //item.put("sign","01fafc55b9f63f6fa3abd2acadf7b551");
        //TODO替换ID
        item.put("id", "2881");
        item.put("jobConfig", jobConfig);
        item.put("debug", "iv20161102");
        return item;
    }

}
