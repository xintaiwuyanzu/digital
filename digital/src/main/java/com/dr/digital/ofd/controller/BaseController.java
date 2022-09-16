package com.dr.digital.ofd.controller;

import com.dr.framework.common.entity.BaseStatusEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Base controller class.
 */
public class BaseController<P extends BaseStatusEntity<String>> {
    /**
     * Write buffer to the file specified by the file name.
     *
     * @param request
     * @param response
     * @param fileName
     * @param buffer
     * @throws IOException
     */
    public void writeFile(HttpServletRequest request, HttpServletResponse response, String fileName, byte[] buffer)
            throws IOException {
        if (buffer == null) {
            return;
        }
        response.setContentType("application/octet-stream");
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            userAgent = "";
        }
        if (userAgent.contains("MSIE") || userAgent.contains("Trident") || userAgent.contains("Edge")) {
            String encodeFileName =
                    URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20").replaceAll("%28", "\\(").replaceAll("%29", "\\)")
                            .replaceAll("%3B", ";").replaceAll("%40", "@").replaceAll("%23", "\\#").replaceAll("%26", "\\&");
            response.setHeader("Content-Disposition", String.format("attachment;filename=\"%s\"", encodeFileName));
        } else {
            String encodeFileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1"); // ����safari����������
            response.setHeader("Content-disposition", String.format("attachment; filename=\"%s\"", encodeFileName));
        }
        response.setCharacterEncoding("UTF-8");
        response.setContentLength(buffer.length);
        response.getOutputStream().write(buffer);
    }

}
