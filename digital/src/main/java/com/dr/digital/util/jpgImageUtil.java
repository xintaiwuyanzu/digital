package com.dr.digital.util;

import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

public class jpgImageUtil {
    /**
     * 校验路径是否为文件
     *
     * @param path
     * @return check为通过
     */
    public static String check(String path) {
        if (StringUtils.isEmpty(path)) {
            return "路径为空";
        }
        File file = new File(path);
        if (!file.exists()) {
            return "文件为空";
        }
        if (file.isDirectory()) {
            return "解析为文件夹，不是文件";
        }
        if (!file.isFile()) {
            return "解析路径不是文件";
        }
        return "check";
    }

    /**
     * 图片色彩
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static String ImageRGB(String path) throws IOException {
        String check = check(path);
        if (!"check".equals(check)) {
            return check;
        }
        File file = new File(path);
        //图片地址
        BufferedImage src = ImageIO.read(file);
        //处理图片颜色测试。
        int height = src.getHeight();
        int width = src.getWidth();
        int[] rgb = new int[4];
        int o = 0;
        //返回是否是彩色和黑白颜色
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int pixel = src.getRGB(i, j);
                rgb[1] = (pixel & 0xff0000) >> 16;
                rgb[2] = (pixel & 0xff00) >> 8;
                rgb[3] = (pixel & 0xff);
                //如果像素点不相等的数量超过50个 就判断为彩色图片
                if (rgb[1] != rgb[2] && rgb[2] != rgb[3] && rgb[3] != rgb[1]) {
                    o += 1;
                    if (o >= 50) {
                        return "彩色";
                    }
                }
            }
        }
        return "黑白";
    }

    /**
     * 处理图片，设置图片DPI值
     *
     * @param dpi  dot per inch
     * @return
     * @throws IOException
     */

    public static byte[] process(BufferedImage image, int dpi) throws IOException {
        String formatName = "jpeg";
        for (Iterator iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext(); ) {
            ImageWriter writer = (ImageWriter) iw.next();

            ImageWriteParam writeParams = writer.getDefaultWriteParam();

            writeParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

            //调整图片质量

            writeParams.setCompressionQuality(1f);

            IIOMetadata data = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), writeParams);

            Element tree = (Element) data.getAsTree("javax_imageio_jpeg_image_1.0");
            Element jfif = (Element) tree.getElementsByTagName("app0JFIF").item(0);

            jfif.setAttribute("Xdensity", dpi + "");

            jfif.setAttribute("Ydensity", dpi + "");

            jfif.setAttribute("resUnits", "1"); // density is dots per inch

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            ImageOutputStream stream = null;

            try {
                stream = ImageIO.createImageOutputStream(out);

                writer.setOutput(stream);

                writer.write(data, new IIOImage(image, null, null), writeParams);

            } finally {
                stream.close();

            }

            return out.toByteArray();

        }
        return null;

    }

    /**
     * 图片分辨率
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static String resolution(String path) throws IOException {
        String check = check(path);
        if (!"check".equals(check)) {
            return check;
        }
        File file = new File(path);
        //图片地址
        BufferedImage src = ImageIO.read(file);
        //处理图片颜色测试。
        int height = src.getHeight();
        int width = src.getWidth();
        return height + "x" + width;
    }

    /**
     * 根据A4尺寸将文件的分辨率转换为dpi
     * @param filePower 分辨率
     * @return
     */
    public static String dpiA4Transformation(String filePower){
        String dpi = null;
        if (!StringUtils.isEmpty(filePower)){
            String[] power = filePower.split("x");
            Integer height = Integer.parseInt(power[0]);
            Integer width = Integer.parseInt(power[1]);
            //A4纸质的英寸为8.27x11.69
            int heightDpi = (int)Math.floor(height / 11.69);
            int widthDpi = (int)Math.floor(width / 8.27);
            dpi = heightDpi+","+widthDpi;
        }
        return dpi;
    }

    /**
     * 读取正常未加工文件的dpi
     * @param path
     * @return
     */
    public static int getDpi(String path) {
        File file = new File(path);
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            process(image,300);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        handleDpi(file, 300, 300);
        ImageInfo imageInfo = null;
        try {
            InputStream inputStream = new FileInputStream(file);
            String fileName = file.getName();
            imageInfo = Imaging.getImageInfo(inputStream, fileName);
            /*
            获取实际尺寸
            ImageInfo imageInfos = Imaging.getImageInfo(file);
            System.out.println(imageInfos.getPhysicalWidthInch());
            System.out.println(imageInfo.getPhysicalWidthInch());
            */
            return imageInfo.getPhysicalHeightDpi();
        } catch (ImageReadException | IOException e) {
            return 0;
        }
    }
}
