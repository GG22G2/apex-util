package hsb.game.util.demo;

import hsb.game.util.match.ApexSiftMatcher;
import hsb.game.util.util.OpencvUtil;
import hsb.game.util.help.RoadInfoHelper;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * @author 胡帅博
 * @date 2023/10/5 12:32
 * <p>
 * 根据小地图确定任务位置
 * <p>
 * <p>
 * 人在小地图的正中心，确定小地图在大地图中的位置，也就确定了人在大地图中的位置
 * <p>
 * 也可在直接根据人物的箭头直接从大地图找人，但是根据现在这种更不容易出错
 */
public class BigMapLocation {
    public static void main(String[] args) {
        OpencvUtil.init();
        ApexSiftMatcher apexSiftMatcher = new ApexSiftMatcher();


        // Mat t = Imgcodecs.imread("G:\\kaifa_environment\\code\\java\\apex-util\\error\\1697103229727_1383.360802527385.png");
        //   Mat minImg = t.submat(Config.minMapTop, Config.minMapBottom, Config.minMapLeft, Config.minMapRight);
        Mat minImg = Imgcodecs.imread("G:\\kaifa_environment\\code\\java\\apex-util\\minMap.bmp");

        Mat largeImg = Imgcodecs.imread("G:\\kaifa_environment\\code\\java\\apex-util\\bigMapTemplate\\zwxg.png");


        //Mat largeMat2 = new Mat();
        //Imgproc.resize(largeImg, largeMat2, new Size(largeImg.width() * 0.5, largeImg.height() * 0.5));

        ApexSiftMatcher.DetectedFeature largeMapFeature = apexSiftMatcher.featureExtreact(largeImg);


        //因为只需要缩放和平移， 缩放矩阵, 并且缩放是定制，也就是说这个仿射矩阵只有平移部分不确定

        //现在只需要第一测量出来准确的缩放系数，
        // 再想办法确定放射矩阵，就确定了x和y的平移了

        // 2322 2474
        apexSiftMatcher.initBigMap(largeMapFeature);
        apexSiftMatcher.changeScaleMat("世界尽头");
        Mat mask = new Mat();
        for (int i = 0; i < 10000; i++) {
            //  Rect rect = matchRect(new Point(2056, 1675), 500, 0.03);
            //Point[] match = apexSiftMatcher.match(minImg, largeMapFeature, largeImg, rect); //new Rect(1400, 1000, 1200, 1200)

            Point[] match = apexSiftMatcher.match(minImg, largeMapFeature, largeImg, new Rect(1400, 1500, 1500, 1500));

           // System.out.println(match[0]);
        }
        //{1409.408203125, 2468.097900390625}
        // apexSiftMatcher.setPossibleXYAverage(1400,2418);
        //  Point[] match = apexSiftMatcher.match(minImg, largeMapFeature, largeImg, new Rect(900, 1900, 1000, 1000));//new Rect(1000, 2000, 1200, 1200)
        //   apexSiftMatcher.setPossibleXYAverage(-1,-1);
        //   Point point1 = match[0];
        //   Point point2 = match[2];


        //  System.out.println(match[0]);

    }


    /**
     * @param useTime 两次定位间隔
     * @param speed   平居速度的极限 单位毫秒   0.03应该是极限
     */
    public static Rect matchRect(Point lastCenter, long useTime, double speed) {
        if (lastCenter == null) {
            return null;
        }
        double scale = 5.0;
        int baseWidth = 180;

        double v = useTime * speed;

        double width = (baseWidth + 4 * v) * scale;
        //限制最大2000 在大还不如直接全图匹配
        //

        if (width > 2300) {
            // width = (int) Math.min(2300, width);
            return null;
        }

        double r = width / 2.0;

        double x1 = lastCenter.x;

        double x = lastCenter.x - r;
        double y = lastCenter.y - r;


        int left = (int) Math.max(0, x);
        int top = (int) Math.max(0, y);

        int right = (int) Math.min(RoadInfoHelper.roadInfoWidth, x + width);
        int bottom = (int) Math.min(RoadInfoHelper.roadInfoHeight, y + width);

        return new Rect(left, top, right - left, bottom - top);
    }


}
