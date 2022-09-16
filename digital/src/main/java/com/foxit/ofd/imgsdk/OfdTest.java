package com.foxit.ofd.imgsdk;

import com.alibaba.fastjson.JSONObject;
import com.dr.digital.ofd.service.impl.OfdConversion;
import com.dr.digital.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;


/**
 * @brief SDK
 */
public class OfdTest {
    private static Logger logger = LoggerFactory.getLogger(OfdTest.class);
    private String _serial_path = "./key";

    private String _serial = "XXXXX-XXXXX-XXXXX-XXXXX-XXXXX";

    /**
     * @brief 通用接口测试
     */
    public void img2ofd_test() {
        String imgfile = new File("./testcase/00001.jpg").getAbsolutePath();
        long hPackage = OfdPackage.create("./testcase/00001_std.ofd");
        logger.info("test 111 ...hPackage=" + hPackage);
        long hDocument = OfdPackage.addDocument(hPackage);
        logger.info("test 222 ...");
        long hPage = OfdDocument.addPage(hDocument);
        logger.info("test 333 ...");
        OfdPage.setSize(hPage, 0, 0, 210, 297);
        logger.info("test 444 ...");
        long handler = OfdOcr.Start(imgfile);
        int ret = OfdOcr.Continue(handler, hPage, 0, 0, 210, 297);
        logger.info("OfdOcr.Image ret = " + ret);
        int nWidth = OfdOcr.Getimgwh(handler, 0);
        int nHeight = OfdOcr.Getimgwh(handler, 1);
        logger.info("OfdOcr.Image nWidth = " + nWidth);
        logger.info("OfdOcr.Image nHeight = " + nHeight);
        OfdOcr.End(handler);
        OfdPackage.save(hPackage);
        OfdPackage.close(hPackage);
    }

    public void img2ofd_langchao(String srcPath, String destPath) {
        File dir = new File(srcPath);
        if (!dir.exists()) {
            logger.info("源目录不存在！");
            return;
        }
        File[] files = dir.listFiles();
        int fileSize = files.length / 2;
        for (File file : files) {
            String p = file.getAbsolutePath();
            logger.info(p);
        }
        long hPackage = OfdPackage.create(destPath);//生成ofd文件路径
        logger.info("img2ofd_langchao ...hPackage=" + hPackage);
        long hDocument = OfdPackage.addDocument(hPackage);
        logger.info("img2ofd_langchao ...");
        for (int i = 0; i < fileSize; i++) {
            long hPage = OfdDocument.addPage(hDocument);
            logger.info("img2ofd_langchao ...");
            OfdPage.setSize(hPage, 0, 0, 210, 297);
            logger.info("img2ofd_langchao ...");
            String imgfile = new File(srcPath + File.separator + FileUtil.getNewFileName((i + 1)) + ".jpg").getAbsolutePath();
            logger.info(imgfile);
            String txtfile = new File(srcPath + File.separator + FileUtil.getNewFileName((i + 1)) + ".txt").getAbsolutePath();
            try {
                int ret = OfdOcr.CreatePageWithOcrData(hPage, new String(imgfile.getBytes(), "UTF-8"), new String(txtfile.getBytes(), "UTF-8"));
                logger.info("CreatePageWithOcrData = " + ret);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        OfdPackage.save(hPackage);
        OfdPackage.close(hPackage);
    }

    /**
     * @brief 定制接口测试
     */
    public void ocr_test() {
        String imgfile = new File("./testcase/00001.jpg").getAbsolutePath();
        String tiffile = new File("./testcase/aa.tif").getAbsolutePath();
        int ret = 0;
        JSONObject joption = new JSONObject();
        JSONObject pages = new JSONObject();
        pages.put("start", 1);
        pages.put("count", 1);
        joption.put("pages", pages);
        String option = joption.toJSONString();

        ret = OfdOcr.OcrOutput(tiffile, tiffile + ".txt", option);
        logger.info("**********OfdOcr.OcrOutput tif2txt ret = " + ret);
        ret = OfdOcr.OcrOutput(imgfile, imgfile + ".txt", null);
        logger.info("**********OfdOcr.OcrOutput img2txt ret = " + ret);
        ret = OfdOcr.OcrOutput(imgfile, imgfile + ".ofd", null);
        logger.info("**********OfdOcr.OcrOutput img2ofd ret = " + ret);
        ret = OfdOcr.OcrOutput(imgfile, imgfile + ".pdf", null);
        logger.info("**********OfdOcr.OcrOutput img2pdf ret = " + ret);

        //结构化数据输出
        ret = OfdOcr.OcrOutput(imgfile, imgfile + ".json", null);
        logger.info("**********OfdOcr.OcrOutput img2json ret = " + ret);

        String data = OfdOcr.OcrOutStream(imgfile, null);
        logger.info("**********OfdOcr.OcrOutput OcrOutStream data =[" + new String(data.getBytes(StandardCharsets.UTF_8)) + "]");
    }

    public int init() {
        int ret = OfdBase.init(_serial, _serial_path);
        logger.info("test init ret = " + ret);
        ret = OfdBase.setLibraryPath(System.getProperty("java.library.path"));
        logger.info("setLibraryPath ret = " + ret);
        return ret;
    }

    public void destroy() {
        OfdBase.destroy();
    }

    /**
     * @brief Main
     */
    public static void main(String[] args) {
        logger.info("main begin ...");
        OfdConversion test = new OfdConversion();
        int ret = test.init();
        test.imgOfdLangChAo("D:\\shuzihua\\split\\0032\\0032-2005-永久-0084\\001\\", "D:\\shuzihua\\ofd\\0032\\0032-2005-永久-0084\\正文\\0032-2005-永久-0084.ofd");
        test.destroy();
    }

    /**
     * @brief Load library.fofdimgsdk64.dll
     */
    static {
        String libName = "";
        String arch = System.getProperty("os.arch");
        logger.info(arch);
        if (arch.contains("64")) {
            libName = "fofdimgsdk64";
        } else {
            libName = "fofdimgsdk32";
        }
        System.loadLibrary(libName);
    }

}
