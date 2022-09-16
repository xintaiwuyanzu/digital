package com.dr.digital.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;


/**
 * 图片转pdf工具类
 *
 * @author Administrator
 */
public class Img2PdfUtil {
    /**
     * 图片合并为PDF
     *
     * @param outPdfFilepath 生成pdf文件目录
     * @param imagesPath     需要转换的图片File的路径
     * @param id             生成的文件
     */
    public static String imagesToPdf(String outPdfFilepath, String imagesPath, String id) throws Exception {
        File fileimg = new File(imagesPath);
        File[] imageFiles = fileimg.listFiles();
        String path = outPdfFilepath + File.separator + id + ".pdf";
        File file = new File(path);
        // 第一步：创建一个document对象。
        Document document = new Document();
        document.setMargins(0, 0, 0, 0);
        // 第二步：
        // 创建一个PdfWriter实例，
        PdfWriter.getInstance(document, new FileOutputStream(file));
        // 第三步：打开文档。
        document.open();
        // 第四步：在文档中增加图片。
        int len = imageFiles.length;
        for (int i = 0; i < len; i++) {
            if (imageFiles[i].getName().toLowerCase().endsWith(".bmp") || imageFiles[i].getName().toLowerCase().endsWith(".jpg") || imageFiles[i].getName().toLowerCase().endsWith(".jpeg") || imageFiles[i].getName().toLowerCase().endsWith(".gif") || imageFiles[i].getName().toLowerCase().endsWith(".png")) {
                String temp = imageFiles[i].getAbsolutePath();
                Image img = Image.getInstance(temp);
                img.setAlignment(Image.ALIGN_CENTER);
                img.scaleAbsolute(597, 844);// 直接设定显示尺寸
                // 根据图片大小设置页面，一定要先设置页面，再newPage（），否则无效
                //document.setPageSize(new Rectangle(img.getWidth(), img.getHeight()));
                document.setPageSize(new Rectangle(597, 844));
                document.newPage();
                document.add(img);
            }
        }
        // 第五步：关闭文档。
        document.close();
        return path;
    }

}