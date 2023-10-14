package hsb.game.util.demo;

import hsb.game.util.Config;
import hsb.game.util.util.OpencvUtil;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * @author 胡帅博
 * @date 2023/10/5 12:04
 *
 *
 * 提取小地图和大地图
 *
 *
 */
public class MinMapExtract {
    public static void main(String[] args) {
        OpencvUtil.init();
        Mat template =  Imgcodecs.imread("1697096296345.png");

        Mat submat = template.submat(Config.minMapTop, Config.minMapBottom, Config.minMapLeft, Config.minMapRight);

        Imgcodecs.imwrite("minMap.bmp",submat);


      //  Mat template2 =  Imgcodecs.imread("bigMap.bmp");

    //    Mat submat2 = template2.submat(Config.bigMapTop, Config.bigMapBottom, Config.bigMapLeft, Config.bigMapRight);

    //    Imgcodecs.imwrite("ft2.bmp",submat2);



    }
}
