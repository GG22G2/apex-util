package hsb.game.util.demo;

import hsb.game.util.util.OpencvUtil;
import hsb.game.util.help.RoadInfoHelper;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * @author 胡帅博
 * @date 2023/10/10 10:47
 *
 * 两张图里的可行走区域合并
 *
 */
public class yongyici {
    public static void main(String[] args) {
        OpencvUtil.init();
        Mat newRoad = Imgcodecs.imread("bigMapTemplate/sjjt_road.png", Imgcodecs.IMREAD_GRAYSCALE);
        Mat oldRoad = Imgcodecs.imread("bigMapTemplate/sjjt_road_1.png", Imgcodecs.IMREAD_GRAYSCALE);


        byte[] oldRoadBytes = new byte[newRoad.width() * newRoad.height()];
        byte[] newRoadBytes = new byte[newRoad.width() * newRoad.height()];
        oldRoad.get(0, 0, oldRoadBytes);
        newRoad.get(0, 0, newRoadBytes);


        for (int i = 0; i < newRoadBytes.length; i++) {
            if (oldRoadBytes[i] == RoadInfoHelper.FORBID) {
                //禁止
                newRoadBytes[i] = RoadInfoHelper.FORBID;
            } else if (oldRoadBytes[i] == RoadInfoHelper.ALLOW && newRoadBytes[i] != RoadInfoHelper.FORBID) {
                newRoadBytes[i] = RoadInfoHelper.ALLOW;
            }
        }
        newRoad.put(0,0,newRoadBytes);
        Imgcodecs.imwrite("bigMapTemplate/sjjt_road_2.png", newRoad);
    }
}
