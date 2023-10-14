package hsb.game.util.imageconcat;

import hsb.game.util.util.OpencvUtil;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 胡帅博
 * @date 2023/10/5 21:18
 *
 * 多张图合并工具
 *
 */
public class ImageStitching {
    private static final String OUTPUT_FILE = "output.png";

    public static void main(String[] args) {
        // 加载OpenCV库
        OpencvUtil.init();

        // 读取截图
        List<Mat> screenshots = loadScreenshots();

        int[] ints = calVerticalImagesConcatIndex(screenshots,2000);

        Mat stitchedImage = stitchVerticalImages(ints, screenshots);
        // 拼接截图


        MatOfInt parameters = new MatOfInt(Imgcodecs.IMWRITE_PNG_COMPRESSION,1);

        // 保存拼接后的截图
        Imgcodecs.imwrite(OUTPUT_FILE, stitchedImage,parameters);

        // 显示拼接后的截图
        //  HighGui.imshow("Stitched Image", stitchedImage);
        //   HighGui.waitKey();
    }

    private static List<Mat> loadScreenshots() {
        List<Mat> screenshots = new ArrayList<>();

        // 读取并存储每张截图
        screenshots.add(Imgcodecs.imread("temp/0.png"));
        screenshots.add(Imgcodecs.imread("temp/1.png"));
        screenshots.add(Imgcodecs.imread("temp/2.png"));
        screenshots.add(Imgcodecs.imread("temp/3.png"));
        screenshots.add(Imgcodecs.imread("temp/4.png"));
        screenshots.add(Imgcodecs.imread("temp/5.png"));
        screenshots.add(Imgcodecs.imread("temp/6.png"));
        screenshots.add(Imgcodecs.imread("temp/7.png"));
        screenshots.add(Imgcodecs.imread("temp/8.png"));
//        screenshots.add(Imgcodecs.imread("temp/0.bmp"));
//        screenshots.add(Imgcodecs.imread("temp/1.bmp"));
//        screenshots.add(Imgcodecs.imread("temp/2.bmp"));
//        screenshots.add(Imgcodecs.imread("temp/3.bmp"));
//        screenshots.add(Imgcodecs.imread("temp/4.bmp"));
//        screenshots.add(Imgcodecs.imread("temp/5.bmp"));
//        screenshots.add(Imgcodecs.imread("temp/6.bmp"));
//        screenshots.add(Imgcodecs.imread("temp/7.bmp"));
        // screenshots.add(Imgcodecs.imread("temp/8.bmp"));
        // 添加更多截图...

        return screenshots;
    }




    public static Mat stitchVerticalImages(int[] concatIndex, List<Mat> images) {
        int lastConcatIndex = concatIndex[concatIndex.length - 1];
        Mat temp = images.get(0);
        int concatImgWidth = temp.width();
        int concatImgHeight = lastConcatIndex + temp.height();
        Mat resultMat = new Mat(concatImgHeight, concatImgWidth, CvType.CV_8UC3);


        int bigCopyYOffset = 0;
        for (int i = 0; i < concatIndex.length; i++) {

            temp = images.get(i);

            // 定义源Mat中要复制的矩形区域
            Rect sourceRect = new Rect(0, 0, concatImgWidth, concatIndex[i] - bigCopyYOffset);

            // 定义目标Mat中粘贴的位置
            Point destinationPoint = new Point(0, bigCopyYOffset);

            // 将源Mat的指定矩形区域复制到目标Mat的指定位置
            Mat submat = temp.submat(sourceRect); // 获取源Mat中指定区域的子Mat
            submat.copyTo(resultMat.colRange((int) destinationPoint.x, (int) destinationPoint.x + submat.cols())
                    .rowRange((int) destinationPoint.y, (int) destinationPoint.y + submat.rows()));

            bigCopyYOffset = concatIndex[i];
        }


        Point destinationPoint = new Point(0, bigCopyYOffset);
        temp = images.get(images.size() - 1);
        temp.copyTo(resultMat.colRange((int) destinationPoint.x, (int) destinationPoint.x + temp.cols())
                .rowRange((int) destinationPoint.y, (int) destinationPoint.y + temp.rows()));

        return resultMat;
    }


    //垂直方向的合并索引
    public static int[] calVerticalImagesConcatIndex(List<Mat> images,int limit) {
        int imgSize = images.size();
        int[] concatIndex = new int[imgSize - 1];
        int lastConcatIndex = 0;

        int compareWidth = Math.min(limit,images.get(0).width());

        for (int c = 0; c < imgSize - 1; c++) {

            // 计算相同位置
            Mat mat1 = images.get(c);
            Mat mat2 = images.get(c + 1);
            int size = mat2.width() * mat2.height() * mat2.channels();
            byte[] p1 = new byte[size];
            byte[] p2 = new byte[size];

            mat1.get(0, 0, p1);
            mat2.get(0, 0, p2);

            byte[] firstRow = new byte[mat2.channels() * compareWidth];

            int pitch = mat1.width() * mat1.channels();

            for (int i = 0; i < compareWidth; i += 3) {
                firstRow[i] = p2[i];
                firstRow[i + 1] = p2[i + 1];
                firstRow[i + 2] = p2[i + 2];
            }

            int maxEq = -1;
            int maxEqIndex = 0;
            for (int i = mat1.height() - 1; i >= 0; i--) {
                int eqCount = 0;
                for (int j = 0, index = 0; j < compareWidth; j++) {
                    int matIndex = i * pitch + j * 3;
                    boolean eq1 = p1[matIndex + 0] == firstRow[index++];
                    boolean eq2 = p1[matIndex + 1] == firstRow[index++];
                    boolean eq3 = p1[matIndex + 2] == firstRow[index++];
                    if (eq1 && eq2 && eq3) {
                        eqCount++;
                    }
                }
                if (eqCount > maxEq) {
                    maxEq = eqCount;
                    maxEqIndex = i;
                }

            }
            //System.out.println(maxEqIndex);
            concatIndex[c] = lastConcatIndex + maxEqIndex;
            lastConcatIndex = concatIndex[c];
        }
        return concatIndex;
    }


    public static int[] calHorizonImagesConcatIndex(List<Mat> images) {
        int imgSize = images.size();
        int[] concatIndex = new int[imgSize - 1];
        int lastConcatIndex = 0;
        for (int c = 0; c < imgSize - 1; c++) {

            // 计算相同位置
            Mat mat1 = images.get(c);
            Mat mat2 = images.get(c + 1);
            int size = mat2.width() * mat2.height() * mat2.channels();
            byte[] p1 = new byte[size];
            byte[] p2 = new byte[size];

            mat1.get(0, 0, p1);
            mat2.get(0, 0, p2);

            byte[] firstCol = new byte[mat2.channels() * mat2.height()];

            int pitch = mat1.width() * mat1.channels();

            for (int i = 0, index = 0; i < firstCol.length; i += 3, index++) {
                int t = index * pitch;
                firstCol[i] = p2[t];
                firstCol[i + 1] = p2[t + 1];
                firstCol[i + 2] = p2[t + 2];
            }

            int maxEq = -1;
            int maxEqIndex = 0;
            for (int i = mat1.width() - 1; i >= 0; i--) {
                int eqCount = 0;
                for (int j = 0, index = 0; j < mat1.height(); j++) {
                    int matIndex = j * pitch + 3 * i;
                    boolean eq1 = p1[matIndex + 0] == firstCol[index++];
                    boolean eq2 = p1[matIndex + 1] == firstCol[index++];
                    boolean eq3 = p1[matIndex + 2] == firstCol[index++];
                    if (eq1 && eq2 && eq3) {
                        eqCount++;
                    }
                }
                if (eqCount > maxEq) {
                    maxEq = eqCount;
                    maxEqIndex = i;
                }

            }

            concatIndex[c] = lastConcatIndex + maxEqIndex;
            lastConcatIndex = concatIndex[c];
        }
        return concatIndex;
    }

    //水平方向的连续图片合并成一张大图
    public static Mat stitchHorizonImages(int[] concatIndex, List<Mat> images) {

        int lastConcatIndex = concatIndex[concatIndex.length - 1];
        Mat temp = images.get(0);
        int concatImgWidth = lastConcatIndex + temp.width();
        int concatImgHeight = temp.height();
        Mat resultMat = new Mat(concatImgHeight, concatImgWidth, CvType.CV_8UC3);


        int bigCopyXOffset = 0;
        for (int i = 0; i < concatIndex.length; i++) {

            temp = images.get(i);

            // 定义源Mat中要复制的矩形区域
            Rect sourceRect = new Rect(0, 0, concatIndex[i] - bigCopyXOffset, concatImgHeight);

            // 定义目标Mat中粘贴的位置
            Point destinationPoint = new Point(bigCopyXOffset, 0); // 将复制的区域粘贴到 (200, 100) 的位置

            // 将源Mat的指定矩形区域复制到目标Mat的指定位置
            Mat submat = temp.submat(sourceRect); // 获取源Mat中指定区域的子Mat
            submat.copyTo(resultMat.colRange((int) destinationPoint.x, (int) destinationPoint.x + submat.cols())
                    .rowRange((int) destinationPoint.y, (int) destinationPoint.y + submat.rows()));

            bigCopyXOffset = concatIndex[i];
        }


        Point destinationPoint = new Point(bigCopyXOffset, 0); // 将复制的区域粘贴到 (200, 100) 的位置
        temp = images.get(images.size() - 1);
        temp.copyTo(resultMat.colRange((int) destinationPoint.x, (int) destinationPoint.x + temp.cols())
                .rowRange((int) destinationPoint.y, (int) destinationPoint.y + temp.rows()));


        return resultMat;
    }


}