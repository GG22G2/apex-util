package hsb.game.util.help;

import hsb.game.util.dll.CaptureDLL;
import hsb.game.util.util.RobotUtil;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 胡帅博
 * @date 2023/10/13 14:20
 */
public class WindowHelper {

    public final static Map<String,Boolean> windowInited = new HashMap<>();

   // public static boolean inited= false;

    private static void initMapKey(String name){
        windowInited.putIfAbsent(name, false);
    }

    public static int yOffset = 10;

    public final static int step = 350;

    private static void init(String name){
        Boolean inited = windowInited.get(name);
        if (inited==null) {
            windowInited.put(name,false);
            HighGui.namedWindow(name);
            HighGui.moveWindow(name, 10, yOffset);
            yOffset+=step;
        }
    }

    public static void show(Mat showMat){
        show(showMat,"default");
    }

    public synchronized static void show(Mat showMat,String name){
        init(name);
        boolean inited = windowInited.get(name);
        HighGui.imshow(name, showMat);
        HighGui.waitKey(1);
        if (!inited) {
            RobotUtil.sleep(300);
            CaptureDLL.foregroundApexWindow();
            RobotUtil.sleep(300);
            windowInited.put(name,true);
        }
    }


    public static void close(){
        HighGui.destroyAllWindows();
    }

}
