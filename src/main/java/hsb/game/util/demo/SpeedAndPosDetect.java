package hsb.game.util.demo;

import hsb.game.util.Config;
import hsb.game.util.GameProcess;
import hsb.game.util.util.OpencvUtil;
import hsb.game.util.util.RobotUtil;
import hsb.game.util.paddleocrutil.wrapper.PaddleOcr;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.List;

/**
 * @author 胡帅博
 * @date 2023/10/10 19:57
 *
 *
 * 这个作为备选方案，通过+cl_showpos 1 开启坐标和速度展示，这样可以直接通过文字识别确定位置
 * 但是文字识别有误差
 *
 */
public class SpeedAndPosDetect {


    public static void main(String[] args) {
        OpencvUtil.init();
//        Mat mat = Imgcodecs.imread("1696940799935.png");
//        Mat angleImg = mat.submat(Config.angle2Rect);
//        for(int i = 0; i < 1; i++) {
//            long startNanos_58_31 = System.nanoTime();
//            String test = detectOnce(mat);
//            long endNanos_58_33 = System.nanoTime();
//            System.out.println((endNanos_58_33 - startNanos_58_31) / 1000000.0);
//            System.out.println(test);
//            RobotUtil.sleep(200);
//        }
        test();
    }


    public static void test() {

        for (int i = 0; i < 1000000; i++) {
            Mat mat = GameProcess.captureOnce();
            String s = detectOnce(mat);
            System.out.println(s);
            mat.release();
            RobotUtil.sleep(1000);
        }
    }

    //todo 通过+cl_showpos 1 开启坐标和速度展示，这样可以直接通过文字识别确定位置

    /*
    * 0,0坐标对应的是地图中心，而且展示的格式必定是，数字.两位小数
    *
    * 这个识别不是很精确，但是可以通过规律来消除误差，比如格式是  (不固定位数数字)(小数点)(两位小数)
    *
    * 一个数的前后两秒钟的变化不会太大，否则可以认为识别错了
    * 一个数必定是一 小数点加两个数字结尾的
    *
    *
    * */
    public static String detectOnce(Mat mat) {
        Mat posImg = mat.submat(Config.posRect);
        Mat speedImg = mat.submat(Config.speedRect);
        Mat dst = new Mat();
        Core.hconcat(List.of(posImg, speedImg), dst);

        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_BGR2GRAY);

        // 创建输出图片的Mat对象


        // 设置阈值和输出的像素值
        double threshold = 135;
        double maxVal = 255;

        // 二值化处理
        Imgproc.threshold(dst, dst, threshold, maxVal, Imgproc.THRESH_BINARY);



       // Imgcodecs.imwrite("posText.png",dst);

        String detect = PaddleOcr.detect(dst);

        posImg.release();
        speedImg.release();
        dst.release();
        return detect;
    }


}
