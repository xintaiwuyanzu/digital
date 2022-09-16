package com.dr.digital.dishi;


import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.imperialvision.ivapi.IVApiRequest;
import com.imperialvision.ivapi.IVApiResponse;
import com.imperialvision.ivapi.IVApiServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ImperialVisionClient {
    private final ManagedChannel channel;
    private final IVApiServiceGrpc.IVApiServiceBlockingStub blockingStub;
    Logger logger = LoggerFactory.getLogger(ImperialVisionClient.class);

    public ImperialVisionClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build();
        blockingStub = IVApiServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    //发送请求，得到响应
    public void greet(ByteString bytes_img, String container, byte[] bytes_json, String uuid) {
        IVApiRequest request = IVApiRequest.newBuilder().
                setBytesImg(bytes_img).
                setBytesJson(ByteString.copyFrom(bytes_json)).
                setUuid(uuid).build();
        IVApiResponse response = blockingStub.getIVApiService(request);
        System.out.println(response);
        System.out.println(response.getBegintime());
        System.out.println(response.getEndtime());
        System.out.println(response.getErrorCode());
        System.out.println(response.getOutputsList());
        System.out.println(response.toBuilder());
        System.out.println(response.toString());
        logger.info("的士返回结果：{}"+response.toString());
        logger.info("的士返回结果：{}"+response.getBegintime());
        logger.info("的士返回结果：{}"+response.getEndtime());
        logger.info("的士返回结果：{}"+response.getErrorCode());
        logger.info("的士返回结果：{}"+response.getOutputsList());
        logger.info("的士返回结果：{}"+response.toBuilder());
    }

    public void test(String ip, Integer port) {
        String uuid = UUID.randomUUID().toString();
        JSONObject item = new JSONObject();
        item.put("param", 2);
        item.put("input_path", "/mnt/data/input/038.jpg");
        item.put("output_path", "/mnt/data/output/038.jpg");
        String jsonstring = String.valueOf(item);
        byte[] bytes_img = new byte[]{1, 2, 3};
        byte[] bytesJson = jsonstring.getBytes(StandardCharsets.UTF_8);
        ImperialVisionClient ImperialVisionClient = new ImperialVisionClient(ip, port);
        ImperialVisionClient.greet(ByteString.copyFrom(bytes_img), "", bytesJson, uuid);
        try {
            ImperialVisionClient.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}