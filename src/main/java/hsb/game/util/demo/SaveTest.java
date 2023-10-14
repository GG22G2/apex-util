package hsb.game.util.demo;

import hsb.game.util.util.OpencvUtil;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author 胡帅博
 * @date 2023/10/6 21:14
 */
public class SaveTest {
    public static void main(String[] args) {
        OpencvUtil.init();

        Path bigMapTemplate = Path.of( "minMap.bmp");
        String path = bigMapTemplate.toAbsolutePath().toString();
        System.out.println("加载地图路径:" + path);


        boolean exists = Files.exists(bigMapTemplate);


        Mat bigMat = Imgcodecs.imread(path);

        System.out.println(bigMat);
        String prefix = "zwxg";
        int index = 1;
        Path savePath = Paths.get("temp", prefix, index + ".png").toAbsolutePath();
        if (!Files.exists(savePath.getParent())){
            try {
                Files.createDirectories(savePath.getParent());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Mat mapOnce = bigMat;
        MatOfInt parameters = new MatOfInt(Imgcodecs.IMWRITE_PNG_COMPRESSION, 0);
        boolean t = Imgcodecs.imwrite(savePath.toString(), mapOnce);
        System.out.println("保存结果"+t);
        parameters.release();
    }
}
