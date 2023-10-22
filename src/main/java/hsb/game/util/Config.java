package hsb.game.util;

import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.List;
import java.util.Map;

/**
 * @author 胡帅博
 * @date 2023/10/5 0:29
 */
public class Config {

    //降落点选择
    public static final Map<String, List<Point>> mapLandPints = Map.of(
            "诸王峡谷", List.of(new Point(1100, 2433),new Point(1968, 3383),new Point(2666, 1798),new Point(3534, 2525))
            //, "世界尽头", List.of(new Point(1817, 2534), new Point(2888, 2130))
          //  , "世界尽头", List.of(new Point(2888, 2130),new Point(1973, 1619),new Point(2649, 3367),new Point(1050, 727),new Point(3130, 3561))
        //   , "世界尽头", List.of(new Point(1973, 1619),new Point(2649, 3367),new Point(1050, 727),new Point(3130, 3561))
         //   , "世界尽头", List.of(new Point(1616, 1741))
            , "世界尽头", List.of(new Point(1507, 1828))

            // ,"奥林匹斯",List.of(new Point(1200, 1600),new Point(2340, 2175))
            , "奥林匹斯", List.of(new Point(2413, 2116))
    );

    public static final String CAPTURE_DLL_PATH = "G:\\kaifa_environment\\code\\clion\\wgc-demo\\cmake-build-debug\\cap.dll";
    final static public String MOUSE_HELP_DLL_PATH = "G:\\kaifa_environment\\code\\C\\mouseHelp\\x64\\Release\\mouseHelp.dll";
    public static final double DPI_SCALE = 1 / 1.25;


    //开发脚本时候，使用的宽高
    public static final int designWidth = 1600;
    public static final int designHeight = 900;

    //当前运行的apex使用的宽高
    public static final int curWidth = 1280;
    public static final int curHeight = 720;


    //1°对应的x偏移 ,这个是根据水平方向移动360需要的x轴移动计算出来的,鼠标灵敏度是4.02
    public static final double xAngeleScale = 4072.0 / 360.0;   //11.311;

    //这个是开启 +cl_showpos 1 启动项后，根据输出调出来的，鼠标灵敏度是4.02
    public static final double yAngeleScale = 11.325;

    //脚本制作是在1600*900分辨率下，也就是16:9的分辨率下开发，如果使用其他分辨率
    public static final double resolutionX = curWidth / (designWidth * 1.0);

    public static final double resolutionY = curHeight / (designHeight * 1.0);


    //小地图    1280 * 720分辨率下的小地图坐标
    public static final int minMapLeft = 33; //
    public static final int minMapTop = 39;  //

    public static final int minMapRight = 192+1;  //

    public static final int minMapBottom = 186+1;   //


    //大地图
    public static final int bigMapLeft = 301;
    public static final int bigMapTop = 0;

    public static final int bigMapRight = 978;

    public static final int bigMapBottom = 676;


    //视角方向文字描述
    public static final int angleLeft = (int) (760 * resolutionX)+18;
    public static final int angleTop = (int) (77 * resolutionY);

    public static final int angleRight = (int) (802 * resolutionX)+18;

    public static final int angleBottom = (int) (96 * resolutionY);


    //地图名称

    public static final int mapNameLeft = (int) (30 * resolutionX)+18;
    public static final int mapNameTop = (int) (720 * resolutionY);

    public static final int mapNameRight = (int) (100 * resolutionX)+18;

    public static final int mapNameBottom = (int) (738 * resolutionY);


    //准备状态
    public static final int prepareLeft = (int) (125 * resolutionX)+18;
    public static final int prepareTop = (int) (790 * resolutionY);

    public static final int prepareRight = (int) (215 * resolutionX)+18;

    public static final int prepareBottom = (int) (828 * resolutionY);


    public static final int launchLeft = (int) (772 * resolutionX)+18;
    public static final int launchTop = (int) (752 * resolutionY);

    public static final int launchRight = (int) (824 * resolutionX)+18;

    public static final int launchBottom = (int) (779 * resolutionY);

   // public static final Rect launchRect = getRect(438,16,491,43);

    //一局游戏结束后的第一个页面 有观战 死亡回放  总结 这三种文字的页面
    public static final Rect gameOverRect = getRect(438+18,16,491+18,43);

    //比赛总结
    public static final Rect gameSummaryRect = getRect(540+18,30,710+18,78);

    //59 -> 73
    public static final Rect posRect = getRect(43,60,275,73);
    public static final Rect angle2Rect = getRect(43,74,190,87);
    public static final Rect speedRect = getRect(43,87,104,100);

    public static Rect getRect(int left,int top, int right,int bottom ) {
        int x = left;
        int y = top;
        int width = right - left;
        int height = bottom - top;
        return new Rect(x, y, width, height);
    }


}
