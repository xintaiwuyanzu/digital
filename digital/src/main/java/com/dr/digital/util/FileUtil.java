package com.dr.digital.util;

import org.springframework.util.ClassUtils;

import java.io.*;
import java.util.*;

public class FileUtil {
    public static Map<String, File> dirAllStrArr = new HashMap<>();

    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    /**
     * 获取文件大小
     *
     * @param size
     * @return
     */
    public static String getPrintSize(long size) {
        // 如果字节数少于1024，则直接以B为单位，否则先除于1024，后3位因太少无意义
        if (size < 1024) {
            return String.valueOf(size) + "B";
        } else {
            size = size / 1024;
        }
        // 如果原字节数除于1024之后，少于1024，则可以直接以KB作为单位
        // 因为还没有到达要使用另一个单位的时候
        // 接下去以此类推
        if (size < 1024) {
            return String.valueOf(size) + "KB";
        } else {
            size = size / 1024;
        }
        if (size < 1024) {
            // 因为如果以MB为单位的话，要保留最后1位小数，
            // 因此，把此数乘以100之后再取余
            size = size * 100;
            return String.valueOf((size / 100)) + "." + String.valueOf((size % 100)) + "MB";
        } else {
            // 否则如果要以GB为单位的，先除于1024再作同样的处理
            size = size * 100 / 1024;
            return String.valueOf((size / 100)) + "." + String.valueOf((size % 100)) + "GB";
        }
    }

    /**
     * 删除目录
     *
     * @param folder
     * @return
     */
    public static void deleteFolder(File folder) throws Exception {
        if (!folder.exists()) {
        }
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    //递归直到目录下没有文件
                    deleteFolder(file);
                } else {
                    //删除
                    file.delete();
                }
            }
        }
        //删除
        folder.delete();
    }

    /**
     * 遍历文件夹找到所有的 tif、jpg
     *
     * @param fileDir
     * @return
     */
    public static Map<String, File> findtif(String fileDir) {
        File dirFile = new File(fileDir);
        if (dirFile.exists()) {
            File files[] = dirFile.listFiles();
            for (File file : files) {
                // 如果遇到文件夹则递归调用。
                if (file.isDirectory()) {
                    findtif(file.getAbsolutePath());
                } else {
                    // 如果遇到文件夹则放入数组
                    if (dirFile.getPath().endsWith(File.separator)) {
                        dirAllStrArr.put(dirFile.getPath(), new File(file.getName()));
                    } else {
                        dirAllStrArr.put(file.getPath(), new File(file.getName()));
                    }
                }
            }
        }
        return dirAllStrArr;
    }

    /**
     * 获取文件输出流
     *
     * @return
     */
    public static void downLoadFile(String path) {
        //设置文件路径
        File file = new File(path);
        if (file.exists()) {
            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                File descFile = new File("D:" + File.separator + "shuzihua" + File.separator + "files", path.substring(path.lastIndexOf(File.separator) + 1));
                // 校验文件夹目录是否存在，不存在就创建一个目录
                if (!descFile.getParentFile().exists()) {
                    descFile.getParentFile().mkdirs();
                }
                OutputStream os = new FileOutputStream(descFile);
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
                bis.close();
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static String getRootPath() {
        String path = ClassUtils.getDefaultClassLoader().getResource("").getPath().substring(1).replace("/target/classes/", "");
        String rootPath = path.substring(0, path.lastIndexOf("/"));
        return rootPath;
    }

    /**
     * 更换原文名称的位数
     *
     * @param num
     * @return
     */
    public static String getNewFileName(int num) {
        String photoName = "";
        if (num < 10) {
            photoName = "00" + num;
        } else if (num >= 10 && num < 100) {
            photoName = "0" + num;
        } else {
            photoName = "" + num;
        }
        return photoName;
    }

    /**
     * 对文件件进行名称排序
     *
     * @param file
     * @return
     */
    public static List<File> orderByName(File file) {
        List<File> files = Arrays.asList(file.listFiles());
        //比较器。
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile()) {
                    return -1;
                }
                if (o1.isFile() && o2.isDirectory()) {
                    return 1;
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
        return files;
    }

}
