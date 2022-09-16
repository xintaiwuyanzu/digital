package com.foxit.img;

public class imageWhiteDis {

    //识别图中的特定颜色，速度太慢
    public static void main(String[] args) {
       /* try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            String sourcePath = "";
            Mat source = Highgui.imread(sourcePath, Highgui.CV_LOAD_IMAGE_COLOR);
            Mat destination = new Mat(source.rows(), source.cols(), source.type());
            Imgproc.cvtColor(source, source, Imgproc.COLOR_BGR2HSV);
            double min = 0;
            double max = 20;
            Core.inRange(source, new Scalar(min, 90, 90), new Scalar(max, 255, 255), destination);
            byte[] imgByte =covertMat2Byte1(destination);

        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

 /*   public static byte[] covertMat2Byte1(Mat mat) throws IOException {
        long time1 = new Date().getTime();
        MatOfByte mob = new MatOfByte();
        //  Highgui.imencode(".jpg", mat, mob);
        long time3 = new Date().getTime();
        // System.out.println(mat.total() * mat.channels());
        System.out.println("Mat转byte[] 耗时=" + (time3 - time1));
        return mob.toArray();
    }

    public void cs() {
        //导入dll
        String relativelyPath = System.getProperty("user.dir");
        System.load(relativelyPath + "\\opencv_java340-x64.dll");

        //扫描图片地址
        Mat mat = Imgcodecs.imread("ate.png");
        //扫描后的图片
        Mat hsv_image = new Mat(mat.size(), mat.type());
        Mat mat2 = new Mat(mat.size(), mat.type());
        //将图片的格式转为HSV模式,原来为RGB
        Imgproc.cvtColor(mat, hsv_image, Imgproc.COLOR_BGR2HSV);
        // 色调（H），饱和度（S），明度（V)
        // 下面就可以进行颜色的提取了
        int num = 0;
        int num_rows = hsv_image.rows();
        int num_col = hsv_image.cols();
        for (int i = 0; i < num_rows; i++) {
            for (int j = 0; j < num_col; j++) {
                System.out.println("i:" + i + "  j:" + j);
                // 获取每个像素
                double[] clone = hsv_image.get(i, j).clone();
                double hun = clone[0]; // HSV hun

                if ((hun >= 0 && hun <= 10) || (hun >= 156 && hun <= 180)) {
                    if (clone[1] >= 43 && clone[1] <= 255) {
                        if (clone[2] >= 46 && clone[2] <= 255) {
                            // 红色范围,全部设置为黑色,
                            clone[0] = 0;
                            clone[1] = 0;
                            clone[2] = 255;
                            num++;
                            mat2.put(i, j, clone);
                        }
                    }
                }
            }
        }
        System.out.println(num);
        String filename = "E:/gray.png";
        //保存图像到Result目录中
        Imgcodecs.imwrite(filename, mat2);
    }*/

}
