package hsb.game.util.imageconcat;

import hsb.game.util.util.OpencvUtil;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * @author 胡帅博
 * @date 2023/10/6 1:22
 */
public class ImageRongHe {
    public static void main(String[] args) {
        //截图三次，每个点取三次中出现次数最多的那一次
        OpencvUtil.init();

        Mat mat1 = Imgcodecs.imread("temp/sjjt/1.png");
        Mat mat2 = Imgcodecs.imread("temp/sjjt/2.png");
        Mat mat3 = Imgcodecs.imread("temp/sjjt/3.png");

//        Mat mat1 = Imgcodecs.imread("mat1.png");
//        Mat mat2 = Imgcodecs.imread("mat2.png");
//        Mat mat3 = Imgcodecs.imread("mat3.png");


        int size = mat1.width() * mat1.height() * mat1.channels();

        byte[] p1 = new byte[size];
        byte[] p2 = new byte[size];
        byte[] p3 = new byte[size];
        byte[] p4 = new byte[size];


        mat1.get(0, 0, p1);
        mat2.get(0, 0, p2);
        mat3.get(0, 0, p3);

        for (int i = 0; i < size; i+=3) {
            int a = p1[i]&0Xff;
            int b = p2[i]&0Xff;
            int c = p3[i]&0Xff;
            byte[] p = null;

            if (a == b || a == c) {
                p =p1;
            } else {
                if (b!=c){
                    //如果是在圈里边，那么会一直闪烁，就会出现这种情况，判断一下那两个颜色最接近
                    //取三者中颜色最低的哪一个
                    if (a < b && a<c){
                        p = p1;
                    }else if(b < a && b<c){
                        p = p2;
                    }else {
                        p = p3;
                    }
                }else {
                    p=p2;
                }
            }
            p4[i] = p[i];
            p4[i+1] = p[i+1];
            p4[i+2] = p[i+2];
        }
        Mat mat4 = new Mat(mat1.height(), mat1.width(), CvType.CV_8UC3);

        mat4.put(0, 0, p4);
        mat1.release();
        mat2.release();
        mat3.release();


        Imgcodecs.imwrite("tf1.png", mat4);

    }
}
