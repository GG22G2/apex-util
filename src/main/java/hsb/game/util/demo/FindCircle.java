package hsb.game.util.demo;

import hsb.game.util.Config;
import hsb.game.util.util.OpencvUtil;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * @author 胡帅博
 * @date 2023/10/14 0:00
 */
public class FindCircle {
    public static void main(String[] args) {
        OpencvUtil.init();

       // Mat template = Imgcodecs.imread("1697215344860.png");
        Mat template = Imgcodecs.imread("1697215697213.png");
        Mat img = template.submat(Config.bigMapTop, Config.bigMapBottom, Config.bigMapLeft, Config.bigMapRight);

        Mat clone = img.clone();

        // Imgcodecs.imwrite("f.bmp",img);
        byte[] pixels = new byte[img.height() * img.width() * img.channels()];

        //如果一个点某个通道低于70，或者三个通道差距过大，或者通道颜色大于200 都可以删除

        img.get(0,0,pixels);
        for (int i = 0; i < pixels.length; i += 3) {
            int b = pixels[i] & 0xff;
            int g = pixels[i + 1] & 0xff;
            int r = pixels[i + 2] & 0xff;
            boolean delete = false;
            if (b < 70 || g < 70 || r < 70) {
                delete = true;
            } else if (b > 150 || g > 150 || r > 150  ) {
                delete = true;
            } else if (Math.abs(b - g) > 60 || Math.abs(b - r) > 60) {
                delete = true;
            }

            if (delete) {
                pixels[i] = 0;
                pixels[i + 1] = 0;
                pixels[i + 2] = 0;
            }
        }

        img.put(0,0,pixels);


        // 转为灰度图
        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);

        // 图片平滑
        Imgproc.medianBlur(gray, gray, 3);
        Imgcodecs.imwrite("gray.bmp", gray);
        // HoughCircles函数查找圆形
        Mat circles = new Mat();

      //  Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1, 1000, 150, 20, 160, 170);
        Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1, 1000, 150, 20, 92, 100);
        // 在原图上绘制圆形
        for (int i = 0; i < circles.cols(); i++) {
            double[] vCircle = circles.get(0, i);
            Point center = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
            int radius = (int) Math.round(vCircle[2]);
            System.out.println(center + "," + radius);
          ///  Imgproc.circle(img, center, radius, new Scalar(0, 255, 0), 2);

            //Imgproc.circle(img, center, 2, new Scalar(0, 255, 0), 2);
            Imgproc.circle(img, center, radius, new Scalar(0, 255, 0), -1);


            //Core.bitwise_and(img, mask, circleMask);
          // RingFinder.filterPixels(clone, vCircle);

           Imgcodecs.imwrite("gegeeg.jpg", clone);

        }

        // 保存包含圆形的图片
        Imgcodecs.imwrite("output_image.jpg", img);

    }
}
