package hsb.game.util.demo;

/**
 * @author 胡帅博
 * @date 2023/10/8 15:39
 */

import hsb.game.util.util.OpencvUtil;
import org.opencv.core.*;
import org.opencv.features2d.SIFT;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.List;

public class SparseOpticalFlowVisualization {

    public static void main(String[] args) {
        OpencvUtil.init();
        Mat previousFrame = Imgcodecs.imread("G:\\kaifa_environment\\code\\java\\apex-util\\1696750104469.png");
        Mat currentFrame = Imgcodecs.imread("G:\\kaifa_environment\\code\\java\\apex-util\\1696750114249.png");

        showFlow(previousFrame,currentFrame);
    }


    public static double showFlow( Mat previousFrame, Mat currentFrame) {


        // 转换为灰度图像
        Mat previousGray = new Mat();
        Mat currentGray = new Mat();
        Imgproc.cvtColor(previousFrame, previousGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(currentFrame, currentGray, Imgproc.COLOR_BGR2GRAY);


        // 定义稀疏光流的位置点
        MatOfPoint2f previousPoints = new MatOfPoint2f();
        MatOfPoint2f currentPoints = new MatOfPoint2f();

        SIFT sift = SIFT.create(0, 6, 0.05, 3, 2.0);

        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        sift.detect(previousGray, keypoints);

        KeyPoint[] array = keypoints.toArray();


        // 在前一帧中设置初始位置点（用于追踪）
        // 这里选择手动指定一些位置点，你可以根据需求选择不同的位置点

        double w1 = previousGray.width() / 3.0;
        double h1 = previousGray.height() / 3.0;

        List<Point> pointsList = new ArrayList<>();
        for (KeyPoint point : array) {
            Point pt = point.pt;
            if (w1< pt.x &&  pt.x < (w1 * 2.0 ) && pt.y>h1 && pt.y < (h1*2.0) ) {
                pointsList.add(pt);
            }
        }

        if (pointsList.isEmpty()){

            for(int i = (int) w1; i < 2*w1; i+=50) {
                pointsList.add(new Point(i,300));
                pointsList.add(new Point(i,400));
                pointsList.add(new Point(i,500));
            }

        }



        previousPoints.fromList(pointsList);




        // 计算稀疏光流
        MatOfByte status = new MatOfByte();
        MatOfFloat err = new MatOfFloat();
        Video.calcOpticalFlowPyrLK(previousGray, currentGray, previousPoints, currentPoints, status, err);

        // 可视化光流
        // Mat flowVisualization = currentGray.clone();

        Mat flowVisualization = new Mat(currentGray.size(), currentGray.type(), new Scalar(25, 0, 0));
        double totalDistance = 0;
        int totalFlowCount  = 0;
        for (int i = 0; i < currentPoints.total(); i++) {
            // 获取光流位置点和状态
            Point previousPoint = new Point(previousPoints.get(i, 0));
            Point currentPoint = new Point(currentPoints.get(i, 0));
            double flowStatus = status.get(i, 0)[0];

            if (flowStatus == 1) {
                totalFlowCount++;
                double v = calDistance(previousPoint, currentPoint);
                totalDistance+=v;
                // 绘制箭头或线段（只绘制被成功追踪的位置点）
                Imgproc.arrowedLine(flowVisualization, previousPoint, currentPoint, new Scalar(0, 0, 255), 2);
            }
        }

        //HighGui.imshow("12", flowVisualization);
        //HighGui.waitKey(1);

        // 释放资源
        previousFrame.release();
        currentFrame.release();
        previousGray.release();
        currentGray.release();
        previousPoints.release();
        currentPoints.release();

        status.release();
        err.release();
        flowVisualization.release();
        return totalDistance/totalFlowCount;
    }


    public static double calDistance(Point p1,Point p2){
        double distance = Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
        return distance;
    }


}