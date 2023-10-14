package hsb.game.util.paddleocrutil.wrapper;

import hsb.game.util.help.DllFunctionFindHelper;
import hsb.game.util.help.MemoryAccessHelper;
import hsb.game.util.util.OpencvUtil;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.file.Paths;

/**
 * @author
 * @date 2022/2/3 17:15
 * <p>
 * <p>
 * ppocr.dll中需要加载模型，其中使用的是
 * ".\\data\\ch_PP-OCRv3_rec_infer" 和  ".\\data\\ppocr_keys_v1.txt"
 * 因为加载的dll 好像相对路径就是项目跟路径也是就是G:\kaifa_environment\code\java\book-get-ocr\data\ch_PP-OCRv3_rec_infer
 */
public class PaddleOcr {
    static {
        String dir = "G:\\kaifa_environment\\code\\java\\book-get-ocr\\ppocr\\";
        File file = Paths.get(dir).toFile();
        File[] files = file.listFiles();
        for (File file1 : files) {
            if (file1.getName().toLowerCase().endsWith(".dll")) {
                System.load(file1.getAbsolutePath());
            }
        }

        ocr_detect = DllFunctionFindHelper.getFuncOf("ocr_detect", ValueLayout.ADDRESS, ValueLayout.ADDRESS);


        ocr_init = DllFunctionFindHelper.getFuncOfVoid("ocr_init");
        ocr_release = DllFunctionFindHelper.getFuncOfVoid("ocr_release");


    }

    public static void main(String[] args) throws Throwable {
        OpencvUtil.init();
        Mat img = Imgcodecs.imread("G:\\kaifa_environment\\code\\java\\apex-util\\template.bmp");

        //760 77
       // 802  96

        img=  img.submat(77,96,760,802);



        Thread.sleep(1000);
        for (int i = 0; i < 200; i++) {
            long startNanos_22_56 = System.nanoTime();
            String text =detect(img);
            long endNanos_22_58 = System.nanoTime();
            System.out.println((endNanos_22_58 - startNanos_22_56) / 1000000.0);
        }
    }



    public static String detect(Mat img){
        try {
            MemorySegment matAddress = MemorySegment.ofAddress(img.getNativeObjAddr());
            MemorySegment address = (MemorySegment) PaddleOcr.ocr_detect.invoke(matAddress);
            MemorySegment segment = MemoryAccessHelper.asSegment(address, 300);
            String text = segment.getUtf8String(0);
            return text;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }


    private static MethodHandle ocr_detect;

    private static MethodHandle ocr_init;
    private static MethodHandle ocr_release;


}
