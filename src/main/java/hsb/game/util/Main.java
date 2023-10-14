package hsb.game.util;

import hsb.game.util.dll.CaptureDLL;
import hsb.game.util.help.MemoryAccessHelper;
import hsb.game.util.util.OpencvUtil;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;

/**
 * @author 胡帅博
 * @date 2023/10/5 0:31
 */
public class Main {
    public static void main(String[] args) throws Throwable {
        OpencvUtil.init();



        String windowName = "Apex Legends";
        String windowClassName = "Respawn001";
        Arena arena = Arena.ofShared();

        MemorySegment w1 = arena.allocateUtf8String(windowName);
        MemorySegment w2 = arena.allocateUtf8String(windowClassName);

        CaptureDLL.SetProcessDPIAwareProxy.invoke();
        MemorySegment h1 = (MemorySegment) CaptureDLL.findWindowByTitle.invoke(w2, w1);

        System.out.println(h1.address());


        for (int i = 0; i < 1; i++) {

            MemorySegment captureRes = (MemorySegment) CaptureDLL.screenshotWindow.invoke(h1);

            ByteBuffer imageBuffer = MemoryAccessHelper.asSegment(captureRes, Config.curWidth *  Config.curHeight * 3).asByteBuffer();
            Mat bgrImg = new Mat( Config.curHeight,  Config.curWidth, CvType.CV_8UC3, imageBuffer);

            Imgcodecs.imwrite("template.bmp",bgrImg);



           // Imgproc.resize(bgrImg, bgrImg, new Size(width *0.5, height *0.5));

            //Imgproc.cvtColor(bgrImg, bgrImg, Imgproc.COLOR_RGBA2BGR);  //CSGO

         //   HighGui.imshow("12", bgrImg);
        //    HighGui.waitKey(1);

        }


    }
}
