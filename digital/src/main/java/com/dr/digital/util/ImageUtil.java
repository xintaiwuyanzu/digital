package com.dr.digital.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ImageUtil {

    /**
     * <p>Title: thumbnailImageWindows</p>
     * <p>Description: 根据图片路径生成缩略图 </p>
     *
     * @param imagePath     原图片路径
     * @param w             缩略图宽
     * @param h             缩略图高
     * @param thumbnailPath 生成缩略图的路径
     * @param force         是否强制按照宽高生成缩略图(如果为false，则生成最佳比例缩略图)
     */
    public static void thumbnailImageWindows(String imagePath, int w, int h, String thumbnailPath, boolean force) {
        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            try {
                // ImageIO 支持的图片类型 : [BMP, bmp, jpg, JPG, wbmp, jpeg, png, PNG, JPEG, WBMP, GIF, gif]
                String types = Arrays.toString(ImageIO.getReaderFormatNames());
                String suffix = null;
                // 获取图片后缀
                if (imgFile.getName().indexOf(".") > -1) {
                    suffix = imgFile.getName().substring(imgFile.getName().lastIndexOf(".") + 1);
                }
                // 类型和图片后缀全部小写，然后判断后缀是否合法
                if (suffix == null || types.toLowerCase().indexOf(suffix.toLowerCase()) < 0) {
                    return;
                }
                Image img = ImageIO.read(imgFile);
                if (!force) {
                    // 根据原图与要求的缩略图比例，找到最合适的缩略图比例
                    int width = img.getWidth(null);
                    int height = img.getHeight(null);
                    if ((width * 1.0) / w < (height * 1.0) / h) {
                        if (width > w) {
                            h = Integer.parseInt(new java.text.DecimalFormat("0").format(height * w / (width * 1.0)));
                        }
                    } else {
                        if (height > h) {
                            w = Integer.parseInt(new java.text.DecimalFormat("0").format(width * h / (height * 1.0)));
                        }
                    }
                }
                BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics g = bi.getGraphics();
                g.drawImage(img, 0, 0, w, h, Color.LIGHT_GRAY, null);
                g.dispose();
                String p = imgFile.getPath();
                // 将图片保存在原目录并加上前缀
                File file = new File(thumbnailPath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                ImageIO.write(bi, suffix, new File(thumbnailPath + File.separator + imgFile.getName()));
            } catch (IOException e) {
                System.out.println("generate thumbnail image failed." + e);
            }
        } else {
            System.out.println("the image is not exist.");
        }
    }
}
