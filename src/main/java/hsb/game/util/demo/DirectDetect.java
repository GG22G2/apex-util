package hsb.game.util.demo;

import hsb.game.util.util.OpencvUtil;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 胡帅博
 * @date 2023/10/5 13:36
 * <p>
 * 方向识别
 */
public class DirectDetect {
    public static void main(String[] args) {
        OpencvUtil.init();

        Mat minImg = Imgcodecs.imread("G:\\kaifa_environment\\code\\java\\apex-util\\minMap.bmp");


        Mat allowImg = minImg.submat(92 - 11, 92 + 11, 99 - 11, 99 + 11);



//        Imgcodecs.imwrite("allow.bmp", allowImg);
//
//
//        Imgproc.cvtColor(allowImg, allowImg, Imgproc.COLOR_RGB2GRAY);
//        Imgcodecs.imwrite("allow3.bmp", allowImg);
//        Imgproc.adaptiveThreshold(allowImg, allowImg, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 3, 0);
//
//        Imgcodecs.imwrite("allow2.bmp", allowImg);



        // 灰度化处理
        Mat gray = new Mat();
        Imgproc.cvtColor(allowImg, gray, Imgproc.COLOR_BGR2GRAY);


        Mat t = new Mat();
       // Imgproc.adaptiveThreshold(gray, t, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 5);



        Imgproc.threshold(gray,t,100,255, Imgproc.THRESH_BINARY);

        Imgcodecs.imwrite("t.bmp", t);


        // 进行边缘检测
        Mat edges = new Mat();
        Imgproc.Canny(t, edges, 10, 30);



      //   寻找轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // 筛选出符合条件的轮廓

        for (MatOfPoint contour : contours) {
            // 计算轮廓周长
            double perimeter = Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
          //  System.out.println(perimeter);
            // 过滤轮廓周长太小的情况
            if (perimeter > 10) {
                // 使用近似多边形方法对轮廓进行简化
                MatOfPoint2f approxCurve = new MatOfPoint2f();
                Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), approxCurve, 0.03 * perimeter, true);
                System.out.println(approxCurve.total() );
                // 如果轮廓近似为 7 个点，就认为是箭头
            //    if (approxCurve.total() == 4) {
                    Point[] array = approxCurve.toArray();


                    for (Point point : array) {
                        Imgproc.circle(allowImg, point, 0, new Scalar(255, 0, 0), -1);
                    }
               // Imgproc.boxPoints();
                   // Imgproc.circle(allowImg, new Point(8,16), 1, new Scalar(255, 0, 0), -1);


                 //   break;
               // }
            }
        }



      //  Imgproc.circle(allowImg, topPoint, 1, new Scalar(0, 0, 255), 1);



    //     显示最终结果
       Imgcodecs.imwrite("arrow_direction.jpg", allowImg);



    }



   // public double



}
