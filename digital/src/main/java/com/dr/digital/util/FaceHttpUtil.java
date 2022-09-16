package com.dr.digital.util;


import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class FaceHttpUtil {
    /**
     * POST请求（JSON）
     *
     * @param url
     * @param JSONBody
     * @return
     * @throws Exception
     */
    public static String sendHttpPost(String url, String JSONBody) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        //"{\r\n  \"id\": \"123456789\",\r\n  \"name\": \"崔迎杰\",\r\n  \"no\": \"19940925\",\r\n  \"policyId\": \"23d73bc32bb843af85a7a43751b9fde1\",\r\n  \"icNumber\": \"123456\",\r\n  \"idNumber\": \"140428199409250014\",\r\n  \"wgNumber\": \"123456\",\r\n  \"password\": \"1234\",\r\n  \"label\": \"软件部\",\r\n  \"imagePath\": \"/api/v1/person/cache_image/NanoHTTPD-4326112674204650981_process\"\r\n}"

        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpPost.setEntity(new StringEntity(JSONBody, ContentType.create("text/json", "UTF-8")));
//        httpPost.addHeader("Cookie", "__SESSION_ID__=" + session + "; Cookie_1=value");
        CloseableHttpResponse response = httpClient.execute(httpPost);


        HttpEntity entity = response.getEntity();
        String responseContent = EntityUtils.toString(entity, "UTF-8");
        if (response.getStatusLine().getStatusCode() != 200) {
            System.out.println(response.getStatusLine().getStatusCode() + "\n");
            System.out.println(responseContent);
        }
        response.close();
        httpClient.close();
        return responseContent;
    }


    public static String httpGet(String url, String session)
            throws HttpException, IOException {
        String json = null;
        HttpGet httpGet = new HttpGet();
        HttpClient client = HttpClients.createDefault();
        // 设置参数
        try {
            httpGet.setURI(new URI(url));
        } catch (URISyntaxException e) {
            throw new HttpException("请求url格式错误。" + e.getMessage());
        }
        // 发送请求

        httpGet.addHeader("Cookie", "__SESSION_ID__=" + session + "; Cookie_1=value");
        HttpResponse httpResponse = client.execute(httpGet);
        // 获取返回的数据
        HttpEntity entity = httpResponse.getEntity();
        byte[] body = EntityUtils.toByteArray(entity);
        StatusLine sL = httpResponse.getStatusLine();
        int statusCode = sL.getStatusCode();
        if (statusCode == 200) {
            json = new String(body, "UTF-8");
            entity.consumeContent();
        } else {
            throw new HttpException("statusCode=" + statusCode);
        }
        return json;
    }

    public static String HttpPost(String url, String JSONBody) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpPost.setEntity(new StringEntity(JSONBody, ContentType.create("text/json", "UTF-8")));
        CloseableHttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        String responseContent = EntityUtils.toString(entity, "UTF-8");
        if (response.getStatusLine().getStatusCode() != 200) {
            System.out.println(response.getStatusLine().getStatusCode() + "\n");
            System.out.println(responseContent);
        }
        response.close();
        httpClient.close();
        return responseContent;
    }
}