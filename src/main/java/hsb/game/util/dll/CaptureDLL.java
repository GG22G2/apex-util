package hsb.game.util.dll;


import hsb.game.util.Config;
import hsb.game.util.help.DllFunctionFindHelper;
import hsb.game.util.help.MemoryAccessHelper;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.util.Arrays;


/**
 * @author
 * @date 2022/2/3 17:15
 */
public class CaptureDLL {

    static {
        loadDLL();

        screenshotWindow = DllFunctionFindHelper.getFuncOf("screenshotWindow", ValueLayout.ADDRESS, ValueLayout.ADDRESS);

        findWindowByTitle = DllFunctionFindHelper.getFuncOf("findWindowByTitle", ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS);

        SetProcessDPIAwareProxy = DllFunctionFindHelper.getFuncOfVoid("SetProcessDPIAwareProxy");

        SetForegroundWindowProxy = DllFunctionFindHelper.getFuncOfVoid("SetForegroundWindowProxy",ValueLayout.ADDRESS);

        findWindowRECT = DllFunctionFindHelper.getFuncOf("findWindowRECT", ValueLayout.ADDRESS, ValueLayout.ADDRESS);




    }

    private static void loadDLL() {
        System.load(Config.CAPTURE_DLL_PATH);
    }


    public static MethodHandle screenshotWindow;
    public static MethodHandle findWindowByTitle;

    public static MethodHandle SetProcessDPIAwareProxy;

    public static MethodHandle SetForegroundWindowProxy;
    public static MethodHandle findWindowRECT;


    public static MemorySegment apexHwnd;




    public static MemorySegment getApexWindowHwnd() throws Throwable {


        String windowName = "Apex Legends";
        String windowClassName = "Respawn001";
        Arena arena = Arena.ofShared();

        MemorySegment w1 = arena.allocateUtf8String(windowName);
        MemorySegment w2 = arena.allocateUtf8String(windowClassName);

        CaptureDLL.SetProcessDPIAwareProxy.invoke();
        MemorySegment hwnd = (MemorySegment) CaptureDLL.findWindowByTitle.invoke(w2, w1);

        return hwnd;
    }

    public static void foregroundApexWindow(){
        try {
            MemorySegment apexHwnd1 = getApexHwnd();
            CaptureDLL.SetForegroundWindowProxy.invoke(apexHwnd1);
            Thread.sleep(1000);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public static MemorySegment getApexHwnd(){
        if (apexHwnd == null) {
            try {
                apexHwnd = getApexWindowHwnd();
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }

        }
        return  apexHwnd;
    }

    //todo 以之问题， 截图的时候，通过也就是调用screenshotWindow的时候，内部会调用GetClientRect,估计这个不是准确的
    //todo 导致最左侧17个像素(1280*720分辨率下)的内容跑到了图片最右侧， 然后现在所有取图操作都是基于这个错误的情况下开发的，
    //todo 如果要修正这个错误，记得把所有坐标往左平移17像素
    public static Mat captureOnce() {
        try {
            apexHwnd = getApexHwnd();
            MemorySegment captureRes = (MemorySegment) CaptureDLL.screenshotWindow.invoke(apexHwnd);
            ByteBuffer imageBuffer = MemoryAccessHelper.asSegment(captureRes, Config.curWidth* Config.curHeight * 3).asByteBuffer();
            Mat bgrImg = new Mat( Config.curHeight, Config.curWidth, CvType.CV_8UC3, imageBuffer); // bgrImg不用管
            Mat result = bgrImg.clone();
            bgrImg.release();
            return result;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int[] apexWindowRect() {
        try {
            apexHwnd = getApexHwnd();
            MemorySegment segment = (MemorySegment) findWindowRECT.invoke(apexHwnd);

            MemorySegment segment1 = MemoryAccessHelper.asSegment(segment, 16);
            int[] array = segment1.toArray(ValueLayout.JAVA_INT);
            //有的窗口可能包含阴阳之类的，导致实际宽高多了一点，
            //又因为dpi缩放，为了后续代码方便，这里直接按照0.8处理了
            array[0]= (int) ((array[0]+11));
            array[1]= (int) ((array[1]+40));
//            array[2]= (int) (array[2]*0.8);
//            array[3]= (int) (array[3]*0.8);
            System.out.println(Arrays.toString(array));
            return array;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;

    }
}
