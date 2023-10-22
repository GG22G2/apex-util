package hsb.game.util.demo;

import hsb.game.util.match.ApexSiftMatcher;
import hsb.game.util.util.OpencvUtil;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;
import java.util.List;

/**
 * @author 胡帅博
 * @date 2023/10/14 23:43
 */
public class SaveSiftDescriptor {
    public static void main(String[] args) {
        OpencvUtil.init();
        ApexSiftMatcher apexSiftMatcher = new ApexSiftMatcher();

        //Mat largeImg = Imgcodecs.imread("G:\\kaifa_environment\\code\\java\\apex-util\\bigMapTemplate\\zwxg.png");
        Mat largeImg = Imgcodecs.imread("G:\\kaifa_environment\\code\\java\\apex-util\\bigMapTemplate\\alps.png");

        long startNanos_30_23 = System.nanoTime();
        ApexSiftMatcher.DetectedFeature largeMapFeature = apexSiftMatcher.featureExtreact(largeImg);
        long endNanos_30_25 = System.nanoTime();
        System.out.println((endNanos_30_25 - startNanos_30_23) / 1000000.0);
        Mat imgDescriptors = largeMapFeature.imgDescriptors;

        float[] pixels = new float[imgDescriptors.width() * imgDescriptors.height() * imgDescriptors.channels()];
        imgDescriptors.get(0, 0, pixels);
        Imgcodecs.imwrite("data_feature.bmp", imgDescriptors);

        Mat t = Imgcodecs.imread("data_feature.bmp", Imgcodecs.IMREAD_UNCHANGED);
        byte[] p = new byte[t.width() * t.height() * t.channels()];
        t.get(0, 0, p);
        float[] pixels2 = new float[imgDescriptors.width() * imgDescriptors.height()];
        for (int i = 0; i < p.length; i++) {
            int a = p[i] & 0xff;
            pixels2[i] = a;
        }

        Mat t2 = new Mat(imgDescriptors.height(),imgDescriptors.width(), CvType.CV_32FC1);
        t2.put(0,0,pixels2);

        Mat dst = new Mat();
        Core.compare(t2,imgDescriptors,dst,Core.CMP_NE);
        dst.get(0,0,p);

        // 如果两个 Mat 相同，则 diff 所有元素都为零
        if (Core.countNonZero(dst) == 0) {
            System.out.println("两个 Mat 相同！");
        } else {
            System.out.println("两个 Mat 不相同！");
        }


        //保存特征点
        MatOfKeyPoint imgPoints = largeMapFeature.imgPoints;

        KeyPoint[] array = imgPoints.toArray();

        List<Point> list = Arrays.stream(array).map(x -> x.pt).toList();

        double[] d1 = new double[list.size()*3];

        for(int i = 0; i < list.size(); i++) {
            int index = i*3;
            Point point = list.get(i);
            d1[index] = point.x;
            d1[index+1] = point.y;
            d1[index+2] = 0;
        }


        Mat pointMat = new Mat(list.size(),1,CvType.CV_64FC3);
        pointMat.put(0,0,d1);


        Imgcodecs.imwrite("data_point.bmp", pointMat);

      //  Mat t = Imgcodecs.imread("data.bmp", Imgcodecs.IMREAD_UNCHANGED);




    }
}
