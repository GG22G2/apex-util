package hsb.game.util.demo;

import hsb.game.util.util.OpencvUtil;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

/**
 * @author 胡帅博
 * @date 2023/10/8 15:05
 */
public class OpticalFlowExample {
    public static void main(String[] args) {
        OpencvUtil.init();
        // 加载图片并转换为Mat对象
        Mat previousFrame = Imgcodecs.imread("G:\\kaifa_environment\\code\\java\\apex-util\\1696750104469.png",Imgcodecs.IMREAD_GRAYSCALE);
        Mat currentFrame = Imgcodecs.imread("G:\\kaifa_environment\\code\\java\\apex-util\\1696750114249.png",Imgcodecs.IMREAD_GRAYSCALE);

        // 计算光流
        Mat flow = new Mat(previousFrame.size(), CvType.CV_32FC2);
        Video.calcOpticalFlowFarneback(previousFrame, currentFrame, flow, 0.5, 2, 15, 3, 5, 1.2, 0);

        // 处理光流结果（例如，可视化、提取特征等）

        // 可视化光流
        Mat flowVisualization = new Mat(previousFrame.size(), CvType.CV_8UC3);

        for (int y = 0; y < flowVisualization.rows(); y += 5) {
            for (int x = 0; x < flowVisualization.cols(); x += 5) {
                // 获取光流向量
                Point flowVector = new Point(flow.get(y, x));

                // 绘制箭头或线段
                Point startPoint = new Point(x, y);
                Point endPoint = new Point(x + flowVector.x, y + flowVector.y);
                Imgproc.arrowedLine(flowVisualization, startPoint, endPoint,
                        new Scalar(0, 0, 255), 1);
            }
        }

        HighGui.imshow("12",flowVisualization);

        HighGui.waitKey(1);


        // 释放资源
        previousFrame.release();
        currentFrame.release();
        flow.release();
    }
}