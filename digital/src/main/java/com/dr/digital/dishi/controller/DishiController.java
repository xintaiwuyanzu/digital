package com.dr.digital.dishi.controller;

import com.alibaba.fastjson.JSONObject;
import com.dr.digital.dishi.ImperialVisionClient;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.rmi.runtime.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@RestController
@RequestMapping("api/dishi")
public class DishiController {
    @Value("${dishi.ip}")
    private String ip;
    @Value("${dishi.port}")
    private Integer port;
    @Value("${filePath}")
    private String filePath;
    Logger logger = LoggerFactory.getLogger(DishiController.class);

    @RequestMapping("/test")
    public void test(String inputPath, String outPath,Long type) {
        // type 0 文件传文件 type1 目录  type 2 输入md5传文件
//        logger.info("文件转md5为==stringMd5=="+stringMd5);
        String uuid = UUID.randomUUID().toString();
        JSONObject item = new JSONObject();
        if (type!=null&&type==0){
            inputPath = "test1"+File.separator+"0033";
            outPath = "test1"+File.separator+"0055";
            item.put("input_path", filePath + File.separator + inputPath);
            item.put("output_path", filePath + File.separator + outPath);
        }else if (type!=null&&type==1){
            inputPath = "test1"+File.separator+"0033"+File.separator +"0033-0001.jpg";
            outPath = "test1"+File.separator+"0033"+File.separator +"001.jpg";
            item.put("input_path", filePath + File.separator + inputPath);
            item.put("output_path", filePath + File.separator + outPath);
        }else if (type!=null&&type==2) {
            inputPath = "test1"+File.separator+"0033"+File.separator +"0033-0001.jpg";
            outPath = "test1"+File.separator+"0033"+File.separator +"001.jpg";
            String stringMd5 =null;
            try {
                stringMd5 = getStringMd5(new File(filePath + File.separator + inputPath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            item.put("input_path",stringMd5);
            item.put("output_path", filePath + File.separator + outPath);
        }else {
            item.put("input_path", filePath + File.separator + inputPath);
            item.put("output_path", filePath + File.separator + outPath);
        }
        String jsonString = String.valueOf(item);
        byte[] bytes_img = new byte[]{1, 2, 3};
        byte[] bytesJson = jsonString.getBytes(StandardCharsets.UTF_8);
        System.out.println("开始调用帝视打印+-----------------" + item);
        logger.info("开始调用帝视打印==item=="+item.toJSONString());
        ImperialVisionClient ImperialVisionClient = new ImperialVisionClient(ip, port);
        System.out.println("帝视返回结果+++");
        ImperialVisionClient.greet(ByteString.copyFrom(bytes_img), "", bytesJson, uuid);
        try {
            ImperialVisionClient.shutdown();
            System.out.println("调用结束+++");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static String getStringMd5(File file) throws FileNotFoundException {
        String value = null;
        FileInputStream in = new FileInputStream(file);
        try {
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }


    @RequestMapping("/testVideo")
    public void testVideo(Integer port) {
        String s1 = UUID.randomUUID().toString();
       /* Transcoding t = new Transcoding();
        byte[] buf = t.getBuf();
        JSONObject jsonObject = t.transcodingParam();
        String s = String.valueOf(jsonObject);
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        ImperialVisionClient ImperialVisionClient = new ImperialVisionClient(ip, port);
        ImperialVisionClient.greet(ByteString.copyFrom(buf), "", bytes, s1);
        //t.testMain();
        System.out.println();*/
    }

}
