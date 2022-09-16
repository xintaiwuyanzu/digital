package com.dr.digital.manage.task.controller;

import com.dr.digital.manage.task.service.ArchiveBatchService;
import com.dr.digital.util.FileUtil;
import com.dr.framework.common.entity.ResultEntity;
import com.dr.framework.common.file.autoconfig.CommonFileConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author caor
 * @date 2020/12/7 23:41
 */
@RestController
@RequestMapping({"${common.api-path:/api}/download"})
public class DownloadController {
    @Autowired
    CommonFileConfig fileConfig;
    @Autowired
    ArchiveBatchService archiveBatchService;
    @Value("${filePath}")
    private String filePath;

    /**
     * 导入导出下载
     *
     * @param id
     * @param type
     * @param fileFullPath
     * @return
     */
    /*@RequestMapping("/getUploadDownLoadPath")
    public ResultEntity getUploadDownLoadPath(String id, String type, String fileFullPath) {
        String path = fileFullPath.replace(filePath,"");
        return ResultEntity.success(fileFullPath.replace(filePath,""));
    }*/
    @RequestMapping("/getUploadDownLoadPath")
    public void getUploadDownLoadPath(String id, String type, String fileFullPath, HttpServletResponse response) {
        try {
            // path： 欲下载的文件的路径
            File file = new File(fileFullPath);
            // 获取文件名 - 设置字符集
            String downloadFileName = new String(file.getName().getBytes(StandardCharsets.UTF_8), "iso-8859-1");
            // 以流的形式下载文件
            InputStream fis;
            fis = new BufferedInputStream(new FileInputStream(fileFullPath));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            // 清空response
            response.reset();
            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + downloadFileName);
            response.addHeader("Content-Length", "" + file.length());
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * OFD工具下载
     *
     * @param id
     * @param type
     * @param fileFullPath
     * @return
     */
    @RequestMapping("/getUploadDownLoadOfd")
    public void getUploadDownLoadOfd(String id, String type, String fileFullPath, HttpServletResponse response) throws IOException {
        try {
            URL url = this.getClass().getResource("DownloadController.class");
            String substring = url.toString().substring(0, url.toString().indexOf(":"));
            InputStream fis;
            String path =  "static/ofd.msi";
            if(substring.equals("file")){
                path = FileUtil.getRootPath() + File.separator + "web" + File.separator + "public" + File.separator + "ofd.msi";
                fis = new BufferedInputStream(new FileInputStream(path));
            }else{
                path = FileUtil.getRootPath().substring(FileUtil.getRootPath().indexOf("/")+1) + File.separator + "static" + File.separator + "ofd.msi";
                fis = this.getClass().getClassLoader().getResourceAsStream("ofd.msi");
            }
            // path： 欲下载的文件的路径
            File file = new File(path);
            // 获取文件名 - 设置字符集
            String downloadFileName = new String(file.getName().getBytes(StandardCharsets.UTF_8), "iso-8859-1");
            // 以流的形式下载文件
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            // 清空response
            response.reset();
            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + downloadFileName);
            //response.addHeader("Content-Length", "" + file.length());
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
