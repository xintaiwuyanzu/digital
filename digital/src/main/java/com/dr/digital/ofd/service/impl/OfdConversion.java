package com.dr.digital.ofd.service.impl;

import com.dr.digital.util.FileUtil;
import com.foxit.ofd.imgsdk.*;

import java.io.File;
import java.io.UnsupportedEncodingException;

public class OfdConversion {
    private String _serial_path = "./key";

    private String _serial = "XXXXX-XXXXX-XXXXX-XXXXX-XXXXX";

    public int init() {
        int ret = OfdBase.init(_serial, _serial_path);
        System.out.println("返回的ret的值************************************************："+ret);
        if (ret == 0){
            ret = OfdBase.setLibraryPath(System.getProperty("java.library.path"));
            System.out.println("ret=0之后返回的ret************************************************："+ret);

        }
        return ret;
    }

    /**
     * 根据地址进行转换ofd
     *
     * @param srcPath
     * @param destPath
     */
    public void imgOfdLangChAo(String srcPath, String destPath) {
        File dir = new File(srcPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File[] files = dir.listFiles();
        if (files.length > 0) {
            int fileSize = files.length / 2;
            long hPackage = OfdPackage.create(destPath);//生成ofd文件路径
            long hDocument = OfdPackage.addDocument(hPackage);
            for (int i = 0; i < fileSize; i++) {
                long hPage = OfdDocument.addPage(hDocument);
                OfdPage.setSize(hPage, 0, 0, 210, 297);
                String imgFile = new File(srcPath + File.separator + FileUtil.getNewFileName((i + 1)) + ".jpg").getAbsolutePath();
                String txtFile = new File(srcPath + File.separator + FileUtil.getNewFileName((i + 1)) + ".txt").getAbsolutePath();
                try {
                    int ret = OfdOcr.CreatePageWithOcrData(hPage, new String(imgFile.getBytes(), "UTF-8"), new String(txtFile.getBytes(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            OfdPackage.save(hPackage);
            OfdPackage.close(hPackage);
        }
    }

    public void destroy() {
        OfdBase.destroy();
    }

    /**
     * @brief Load library.fofdimgsdk64.dll
     */
    static {
        try{
            String libName = "";
            String arch = System.getProperty("os.arch");
            if (arch.contains("64")) {
                libName = "fofdimgsdk64";
            } else {
                libName = "fofdimgsdk32";
            }
            System.loadLibrary(libName);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}