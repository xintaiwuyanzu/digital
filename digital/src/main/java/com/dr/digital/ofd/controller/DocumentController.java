package com.dr.digital.ofd.controller;


import com.dr.framework.common.entity.BaseStatusEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 这个文件是云阅读文档说明必须存在的文件 功能是实现文件流的处理
 */
@Controller
@RequestMapping("/api/ofd")
public class DocumentController extends BaseController<BaseStatusEntity<String>> {

    /**
     * @param request
     * @param response
     * @param fileId   返回的网络文件地址，生成加密连接后福昕回调此接口会将这个参数传回来
     */
    @ResponseBody
    @RequestMapping(value = "/document", method = RequestMethod.GET)
    public void getDocument(HttpServletRequest request, HttpServletResponse response
            , @RequestParam("fileId") String fileId
    ) {
        byte[] pdfBytes = null;
        String lastModified = null;
        String fileName = null;
        try {
            URL url = new URL(fileId);
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("Content-Type", "charset=UTF-8");
            InputStream inputstream = conn.getInputStream();
            fileName = getDocuemntDisplayName(fileId, conn.getHeaderField("Content-Disposition"));
            fileName = URLDecoder.decode(fileName, "UTF-8");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bytesReadLen = 0;
            byte[] buffer = new byte[4096];
            while (-1 != (bytesReadLen = inputstream.read(buffer))) {
                baos.write(buffer, 0, bytesReadLen);
            }
            pdfBytes = baos.toByteArray();
            inputstream.close();
            baos.close();
            lastModified = conn.getHeaderField("Last-Modified");

            response.setHeader("Last-Modified", lastModified);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.setHeader("File-Id", fileId);
            writeFile(request, response, fileName, pdfBytes);
        } catch (Exception e) {

            return;
        }

    }

    private String getAttachMentFileName(String contentDisposition) {
        if (null == contentDisposition || contentDisposition.isEmpty()) {
            return "";
        }
        String fileName = "";
        // get filename,example:Content-Disposition:
        // attachment;filename="10.pdf"
        Matcher m = Pattern.compile(".*filename=\"(.*)\"").matcher(contentDisposition.toLowerCase());
        if (m.find()) {
            fileName = m.group(1);
        }
        return fileName;
    }

    private String getFileNameFromUrl(String url) {
        if (url == null) {
            return "";
        }
        try {
            url = URLDecoder.decode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // get filename form url;
        int begin = url.lastIndexOf("/");
        int end = url.indexOf("?");
        if (begin + 1 < end) {
            return url.substring(begin + 1, end);
        } else {
            return url.substring(begin + 1);
        }
    }

    private String getDocuemntDisplayName(String url, String contentDisposition) {
        // get filename form Content-Disposition;
        String filename = getAttachMentFileName(contentDisposition);
        if (!filename.isEmpty()) {
            try {
                filename = new String(filename.getBytes("ISO-8859-1"), "utf8");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return filename;
        }

        return getFileNameFromUrl(url);

    }

}
