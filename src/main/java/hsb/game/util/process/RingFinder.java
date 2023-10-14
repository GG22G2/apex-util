package hsb.game.util.process;

import hsb.game.util.Config;
import hsb.game.util.util.OpencvUtil;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 胡帅博
 * @date 2023/10/14 1:04
 *
 * 找安全区的中心
 */
public class RingFinder {

    public static void main(String[] args) {
        OpencvUtil.init();
        RingFinder ringFinder = new RingFinder();
        Mat template = Imgcodecs.imread("1697215711093.png");
        Mat img = template.submat(Config.bigMapTop, Config.bigMapBottom, Config.bigMapLeft, Config.bigMapRight);

        double[] vCircle = ringFinder.findRing(img, 1);

        Point center = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
        int radius = (int) Math.round(vCircle[2]);
        Imgproc.circle(img, center, radius, new Scalar(0, 255, 0), 2);
        Imgproc.circle(img, center, 2, new Scalar(0, 255, 0), 2);
        // 保存包含圆形的图片
        Imgcodecs.imwrite("output_image.jpg", img);
    }

    //第一个安全区的半径区间
    public final int[] firstRingRadius = new int[]{160, 170};

    //第二个安全区的半径区间
    public final int[] secondRingRadius = new int[]{92, 100};

    List<double[]> ringInfoList = new ArrayList<>(10);

    int index = 0;

    public double[] findRing(Mat img, int index) {
        if (index > 0 && ringInfoList.size() >= index) {
            double[] vCircle = ringInfoList.get(index - 1);
            filterPixels(img, vCircle[0], vCircle[1], (int) Math.round(vCircle[2]));
        } else {
            filterPixels(img, 0, 0, 10000);
        }
        // Imgcodecs.imwrite("fefefecxcxc.jpg", img);
        double[] ringInfo = switch (index) {
            case 0 -> findRing(img, firstRingRadius);
            case 1 -> findRing(img, secondRingRadius);
            default -> null;
        };
        if (ringInfo != null && ringInfoList.size() == index) {
            ringInfoList.set(index,ringInfo);
            this.index++;
        }
        return ringInfo;
    }


    //这个应该是大地图区域
    public double[] findRing(Mat img) {
        return findRing(img, index);
    }


    public void reset() {
        index = 0;
        ringInfoList.clear();
    }

    private static void filterPixels(Mat img, double cx, double cy, int radius) {
        radius = radius + 2;
        // Calculate the squared radius
        double radiusSquared = radius * radius;
        int width = img.width();
        int height = img.height();

        byte[] pixels = new byte[img.height() * img.width() * img.channels()];

        //如果一个点 某个通道低于70，或者三个通道差距过大，或者通道颜色大于200 都可以删除

        img.get(0, 0, pixels);
        for (int i = 0; i < pixels.length; i += 3) {
            int x = (i / 3) % width;
            int y = (i / 3) / width;

            double distanceSquared = (x - cx) * (x - cx) + (y - cy) * (y - cy);

            int b = pixels[i] & 0xff;
            int g = pixels[i + 1] & 0xff;
            int r = pixels[i + 2] & 0xff;
            boolean delete = false;
            if (b < 70 || g < 70 || r < 70) {
                delete = true;
            } else if (b > 150 || g > 150 || r > 150) {
                delete = true;
            } else if (Math.abs(b - g) > 60 || Math.abs(b - r) > 60) {
                delete = true;
            } else if (distanceSquared > radiusSquared) {
                delete = true;
            }

            if (delete) {
                pixels[i] = 0;
                pixels[i + 1] = 0;
                pixels[i + 2] = 0;
            }
        }

        img.put(0, 0, pixels);

    }


    private double[] findRing(Mat img, int[] range) {


        // 转为灰度图
        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);

        // 图片平滑
        Imgproc.medianBlur(gray, gray, 3);

        Mat circles = new Mat();
        Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1, 1000, 150, 20, range[0], range[1]);
        double[] doubles = circles.get(0, 0);

        circles.release();
        gray.release();

        return doubles;
    }

}
