package hsb.game.util.imageconcat;

import hsb.game.util.Config;
import hsb.game.util.util.OpencvUtil;
import hsb.game.util.util.RobotUtil;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.List;
import java.util.*;

import static hsb.game.util.dll.CaptureDLL.captureOnce;
import static hsb.game.util.imageconcat.ImageStitching.stitchHorizonImages;

/**
 * @author 胡帅博
 * @date 2023/10/5 18:48
 * <p>
 * 获取高分辨率的大地图
 */
public class GetBigMap {


    public static void main(String[] args) throws Throwable {
        OpencvUtil.init();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getMapOnce(null,null);
    }


    public static record BigMapConcatResult( Mat bigMap, int[] hIndex,int[] vIndex){

    }


    public static BigMapConcatResult getMapOnce(int[] hIndex,int[] vIndex) throws Throwable {

        RobotUtil.openGameMap();

        MatOfInt parameters = new MatOfInt(Imgcodecs.IMWRITE_PNG_COMPRESSION, 0);

        //最小化地图
        resetMap();

        //把鼠标移动到左上角
        resetMousePosition();

        //调整地图放大级别
        enlargeMap(9);

        //然后把地图定位到左上角，准备开始截图
        positionMapToLeftTop();

        //保存每一行合并后的图片
        List<Mat> rowConcatImg = new ArrayList<>();

        List<List<Mat>> allMapImg = new ArrayList<>(9);
        List<int[]> allMapIndex = new ArrayList<>(9);

        //[605, 1210, 1815, 2420, 3025, 3630, 4235, 4400]
        //[605, 1210, 1815, 2420, 3025, 3630, 4235, 4400]
        //[605, 1210, 1815, 2420, 3025, 3630, 4235, 4400]
        int[] ints = null;
        for (int i = 0; i < captureCount; i++) {
            List<Mat> mats1 = captureAndConcatRow();

            int[] concatIndex = ImageStitching.calHorizonImagesConcatIndex(mats1);


            System.out.println(Arrays.toString(concatIndex));
            allMapIndex.add(concatIndex);
            allMapImg.add(mats1);

            if (concatIndex[0] == 0 && concatIndex[1] == 0 && concatIndex[2] == 0) {
                System.out.println("没有合并，截图错误，结束");
                for (List<Mat> mats : allMapImg) {
                    mats.forEach(Mat::release);
                }
                //这次出错了,下一次
                return null;
            }

            // Imgcodecs.imwrite(STR."hebing\{i}.png", mat);


            //Imgcodecs.imwrite(STR."temp/\{i}.png", mat,parameters);
            //把地图移动到最左侧
            positionMapToLeft();

            //往下移动一次
            verticalDragFromBottomToTopMove();

            adjustMouseY();
        }

        RobotUtil.esc();
        System.out.println("正在合并地图...");
        //收集到所有地图后，通过中位数，来确定实际索引
        System.out.println("使用众数统计多次索引");

        if (hIndex==null){
            hIndex = correctIndex(allMapIndex);
        }


        System.out.println(Arrays.toString(hIndex));
        for (List<Mat> mats1 : allMapImg) {
            Mat mat = stitchHorizonImages(hIndex, mats1);
            rowConcatImg.add(mat);
        }


        if (vIndex==null){
            vIndex = ImageStitching.calVerticalImagesConcatIndex(rowConcatImg, 2000);
        }

        System.out.println(Arrays.toString(vIndex));
        Mat mat = ImageStitching.stitchVerticalImages(vIndex, rowConcatImg);
        // 创建保存参数
        Imgcodecs.imwrite("fegew.png", mat, parameters);



        rowConcatImg.forEach(Mat::release);
        for (List<Mat> mats : allMapImg) {
            mats.forEach(Mat::release);
        }
        System.out.println("地图合并完成");

        return new BigMapConcatResult(mat,hIndex,vIndex);

    }

    public static int[] correctIndex(List<int[]> allIndex) {


        int length = allIndex.get(0).length;
        int[] result = new int[length];
        for (int i = 0; i < length; i++) {
            int[] statistic = new int[allIndex.size()];
            int t = 0;

            for (int[] index : allIndex) {
                statistic[t++] = index[i];
            }
            int zs = getZS(statistic);
            result[i] = zs;

        }
        return result;
    }

    private static int getZS(int[] arr) {
        // 使用 Map 记录每个元素出现的次数
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        for (int num : arr) {
            frequencyMap.put(num, frequencyMap.getOrDefault(num, 0) + 1);
        }

        int maxFrequency = 0;
        int mode = -1;
        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
            int num = entry.getKey();
            int frequency = entry.getValue();
            if (frequency > maxFrequency) {
                maxFrequency = frequency;
                mode = num;
            }
        }
        return mode;
    }


    public static List<Mat> captureAndConcatRow() {
        List<Mat> rowImg = new ArrayList<>(10);
        for (int i = 0; i < captureCount; i++) {
            //截图
            Mat temp = captureFromMap();

            rowImg.add(temp);

            //当前水平方向从左往右移动
            horizonDragFromRightToLeftMove();
        }

        return rowImg;
    }


    //截图三次，每个点取三次中出现次数最多的那一次
    private static Mat captureFromMap() {
        RobotUtil.sleep(100);
        Mat temp = captureOnce();
        Mat mat1 = temp.submat(Config.bigMapTop, Config.bigMapBottom, Config.bigMapLeft, Config.bigMapRight).clone();
        //Imgcodecs.imwrite("mat1.png", mat1);
        temp.release();
        RobotUtil.mouseRelativeMove((int) (dragXOnceMove*0.3), (int) (dragYOnceMove*0.3));
        RobotUtil.sleep(100);

        temp = captureOnce();
        Mat mat2 = temp.submat(Config.bigMapTop, Config.bigMapBottom, Config.bigMapLeft, Config.bigMapRight).clone();
        //Imgcodecs.imwrite("mat2.png", mat2);
        temp.release();
        RobotUtil.mouseRelativeMove((int) (dragXOnceMove*0.3), (int) (dragYOnceMove*0.3));
        RobotUtil.sleep(100);
        temp = captureOnce();
        Mat mat3 = temp.submat(Config.bigMapTop, Config.bigMapBottom, Config.bigMapLeft, Config.bigMapRight).clone();
        // Imgcodecs.imwrite("mat3.png", mat3);
        temp.release();
        RobotUtil.mouseRelativeMove(-(int) (dragXOnceMove*0.6), -(int) (dragYOnceMove*0.6));
        RobotUtil.sleep(100);
        temp.release();

        Mat mat4 = concatMap(mat1, mat2, mat3);
        mat1.release();
        mat2.release();
        mat3.release();

        return mat4;

    }


    public static Mat concatMap(Mat mat1, Mat mat2, Mat mat3) {

        int size = mat1.width() * mat1.height() * mat1.channels();

        byte[] p1 = new byte[size];
        byte[] p2 = new byte[size];
        byte[] p3 = new byte[size];
        byte[] p4 = new byte[size];


        mat1.get(0, 0, p1);
        mat2.get(0, 0, p2);
        mat3.get(0, 0, p3);

        for (int i = 0; i < size; i += 3) {
            int a = p1[i] & 0Xff;
            int b = p2[i] & 0Xff;
            int c = p3[i] & 0Xff;
            byte[] p = null;

            if (a == b || a == c) {
                p = p1;
            } else {
                if (b != c) {
                    //如果是在圈里边，那么会一直闪烁，就会出现这种情况，判断一下那两个颜色最接近
                    //取三者中颜色最低的哪一个
                    if (a < b && a < c) {
                        p = p1;
                    } else if (b < a && b < c) {
                        p = p2;
                    } else {
                        p = p3;
                    }
                } else {
                    p = p2;
                }
            }
            p4[i] = p[i];
            p4[i + 1] = p[i + 1];
            p4[i + 2] = p[i + 2];
        }
        Mat mat4 = new Mat(mat1.height(), mat1.width(), CvType.CV_8UC3);

        mat4.put(0, 0, p4);
        return mat4;
    }

    //虽然这里的移动距离是按照地图宽高， 但是实际测速发现还是会小一点
    public static int dragXOnceMove = (int) ((Config.bigMapRight - Config.bigMapLeft) );

    public static int dragYOnceMove = (int) ((Config.bigMapBottom - Config.bigMapTop) );


    public static int captureCount = 9;

    //小地图相当于缩放了9级
    public static void resetMap() {
        for (int i = 0; i < 15; i++) {
            RobotUtil.zoomDownMap();
            try {
                Thread.sleep(30);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //把鼠标充值到左上角
    public static void resetMousePosition() {
        for(int i = 0; i < 7; i++) {
            RobotUtil.mouseRelativeMove(-Config.bigMapLeft, -300);
            RobotUtil.sleep(1);
        }

        RobotUtil.sleep();
        RobotUtil.mouseRelativeMove(100, 100);
        RobotUtil.sleep();
    }


    //所以鼠标操作完都要把鼠标归为

    //把地图定位到左上角
    public static void positionMapToLeftTop() {
        for (int i = 0; i < 3; i++) {
            RobotUtil.mouseDrag(InputEvent.BUTTON3_MASK, dragXOnceMove, dragYOnceMove);
            RobotUtil.sleep();
            RobotUtil.mouseRelativeMove(-dragXOnceMove, -dragYOnceMove);
            RobotUtil.sleep();
        }
        resetMousePosition();
    }

    //把地图移动到最左侧
    public static void positionMapToLeft() {
        for (int i = 0; i < 10; i++) {
            RobotUtil.mouseDrag(InputEvent.BUTTON3_MASK, dragXOnceMove, 0);
            RobotUtil.sleep(20);
            RobotUtil.mouseRelativeMove(-dragXOnceMove, 0);
            RobotUtil.sleep(20);
        }

        for (int i = 0; i < 3; i++) {
            RobotUtil.mouseDrag(InputEvent.BUTTON3_MASK, Config.bigMapLeft, 0);
            RobotUtil.sleep(20);
            RobotUtil.mouseRelativeMove(-Config.bigMapLeft, 0);
            RobotUtil.sleep(20);
        }

        RobotUtil.mouseRelativeMove(-Config.bigMapLeft, 0);

        RobotUtil.sleep(20);
        RobotUtil.mouseRelativeMove(100, 0);
        RobotUtil.sleep(20);
    }

    public static void adjustMouseY() {
        RobotUtil.mouseRelativeMove(0, 10);
        RobotUtil.sleep();
    }

    public static void horizonDragFromLeftToRightMove() {
        RobotUtil.mouseDrag(Event.META_MASK, dragXOnceMove, 0);
        RobotUtil.sleep();
        RobotUtil.mouseRelativeMove(-dragXOnceMove, 0);
        RobotUtil.sleep();
    }


    public static void horizonDragFromRightToLeftMove() {
        RobotUtil.mouseRelativeMove(dragXOnceMove, 0);
        RobotUtil.sleep(30);
        RobotUtil.mouseDrag(Event.META_MASK, -dragXOnceMove, 0);
        RobotUtil.sleep(30);
    }


    public static void verticalDragFromBottomToTopMove() {
        RobotUtil.sleep(100);
        RobotUtil.mouseRelativeMove(0, dragYOnceMove);
        RobotUtil.sleep(100);
        RobotUtil.mouseDrag(Event.META_MASK, 0, -dragYOnceMove);
        RobotUtil.sleep(100);
    }


    //放大地图
    public static void enlargeMap(int level) {
        for (int i = 0; i < level; i++) {
            RobotUtil.zoomUpMap();
            try {
                Thread.sleep(30);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
