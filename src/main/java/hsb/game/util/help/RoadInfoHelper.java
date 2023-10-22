package hsb.game.util.help;

import hsb.game.util.GameProcess;
import hsb.game.util.util.MathUtils;
import hsb.game.util.util.OpencvUtil;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.opencv.imgcodecs.Imgcodecs.IMREAD_GRAYSCALE;

/**
 * @author 胡帅博
 * @date 2023/10/8 1:48
 *
 * 这个类是用来处理原始的路径信息的
 *
 */
public class RoadInfoHelper {
    public static Map<String, String> mapRoadMap = Map.of(
            "奥林匹斯", "alps_road.png"
            , "诸王峡谷", "zwxg_road.png"
            , "世界尽头", "sjjt_road.png"
    );
    public final static byte ALLOW = (byte) 255;
    public final static byte FORBID = 0b0;
    public final static byte DEFAULT = 127;


   public final static int roadInfoWidth = 4195;
    public final static int roadInfoHeight = 4198;


    public static void main(String[] args) {
        OpencvUtil.init();

        addRoadToMap("诸王峡谷");
        //Mat imread = Imgcodecs.imread("bigMapTemplate/alps.png");
        //   byte[] bytes = readMapRoadInfo("奥林匹斯");

//        bytes[1547]=ALLOW;
//        bytes[1147]=FORBID;

//        ThreadLocalRandom current = ThreadLocalRandom.current();
//        for(int i = 0; i < 100; i++) {
//            bytes[current.nextInt(10000)]=FORBID;
//        }
//
//        for(int i = 0; i < 100; i++) {
//            bytes[current.nextInt(10000)]=ALLOW;
//        }
        // showRoadInfo(bytes, 1272,1992,imread.width(),1000);
        //    saveMapRoadInfo(bytes, "奥林匹斯");


//        Mat imread2 = Imgcodecs.imread("bigMapTemplate/test.webp");
//        Imgproc.cvtColor(imread2, imread2, Imgproc.COLOR_BGR2GRAY);
//        byte[] byte2 = new byte[imread2.width()*imread2.height()];
//
//        imread2.get(0,0,byte2);
//
//        boolean equals = Arrays.equals(byte2, bytes);
//        System.out.println(equals);


    }


    public static void addRoadToMap(String mapName) {

        Mat map = Imgcodecs.imread(STR. "bigMapTemplate/\{ GameProcess.mapNameMap.get(mapName) }" );
        Mat road = Imgcodecs.imread(STR. "bigMapTemplate/\{ mapRoadMap.get(mapName) }" );

        byte[] p = new byte[3];
        byte[] pixels = new byte[3];
        for (int i = 0; i < map.height(); i++) {
            for (int j = 0; j < map.width(); j++) {
                road.get(i, j, pixels);

                // double[] doubles = map.get(i, j);
                if (pixels[0] == ALLOW) {
                    p[0] = (byte) 0;
                    p[1] = (byte) 255;
                    p[2] = (byte) 0;
                    map.put(i, j, p);
                } else if (pixels[0] == FORBID) {
                    p[0] = (byte) 0;
                    p[1] = (byte) 0;
                    p[2] = (byte) 255;
                    map.put(i, j, p);
                } else {
                    //   System.out.println(pixels[0]);
                }

            }
        }

        Imgcodecs.imwrite("bigMapTemplate/mapRoad.png", map);

    }


    public static byte[] readMapRoadInfo(String mapName) {
        String roadName = mapRoadMap.get(mapName);
        Path roadInfoFile = Paths.get("bigMapTemplate", roadName);

        //每个点的状态, 可行走，不可行走，未知,所以暂定用一个字节表示
        byte[] roadInfo = null;
        if (Files.exists(roadInfoFile)) {
            //从磁盘加载
            try {
                Mat roadImg = Imgcodecs.imread(roadInfoFile.toAbsolutePath().toString(), IMREAD_GRAYSCALE);
                // Imgproc.cvtColor(roadImg, roadImg, Imgproc.COLOR_BGR2GRAY);
                roadInfo = new byte[roadImg.width() * roadImg.height()];
                roadImg.get(0, 0, roadInfo);
                roadImg.release();
                //todo 关于webp倒是是不是100%无损还不确定，所以每一次都需都校验一下,等以后确实不出问题了再删掉

                for (int i = 0; i < roadInfo.length; i++) {
                    byte b = roadInfo[i];
                    if (b == DEFAULT || b == ALLOW || b == FORBID) {
                        // System.out.println(b);
                    } else {
                        System.out.println((0Xff & b));
                        //throw new RuntimeException("压缩格式不是无损，图片中出现了位置标识:" + (0Xff & b));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //创建一个新的
            roadInfo = new byte[roadInfoWidth * roadInfoHeight];
            Arrays.fill(roadInfo, DEFAULT);
        }
        return roadInfo;
    }


    public static void saveMapRoadInfo(byte[] info, String mapName) {
        String roadName = mapRoadMap.get(mapName);

        Path roadInfoFile = Paths.get("bigMapTemplate", roadName);

        Mat mat = new Mat(roadInfoHeight, roadInfoWidth, CvType.CV_8UC1);
        mat.put(0, 0, info);

//        MatOfInt parameters = new MatOfInt(Imgcodecs.IMWRITE_WEBP_QUALITY, 100);
//        Imgcodecs.imwrite(roadInfoFile.toAbsolutePath().toString(), mat, parameters);
//        parameters.release();
//        mat.release();


        MatOfInt parameters = new MatOfInt(Imgcodecs.IMWRITE_PNG_COMPRESSION, 1);
        Imgcodecs.imwrite(roadInfoFile.toAbsolutePath().toString(), mat, parameters);
        parameters.release();
        mat.release();
    }

    public static byte getPointRoadInfo(byte[] info, int x, int y, int pitch) {
        int index = x + y * pitch;
        return info[index];
    }

    public static void setPointRoadInfo(byte[] info, int x, int y, int pitch, boolean allow) {
        try {
            int index = x + y * pitch;
            byte pointRoadInfo = getPointRoadInfo(info, x, y, pitch);
            if (pointRoadInfo == FORBID) {
                //对应检测到禁止的点，不允许覆盖，可能这里是上下两册之类的，
                return;
            }

            byte v = 0;
            if (allow) {
                v = ALLOW;
                info[index] = v;
            } else {
                v = FORBID;
                //如果遇到禁止，怎么这个点周围的8个点也设置为禁止

                index = index - pitch - 1;
                int pitch2 = pitch << 1;
                info[index] = v;
                info[index + 1] = v;
                info[index + 2] = v;
                info[index + pitch] = v;
                info[index + pitch + 1] = v;
                info[index + pitch + 2] = v;
                info[index + pitch2] = v;
                info[index + pitch2 + 1] = v;
                info[index + pitch2 + 2] = v;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void recordRoadInfo(byte[] roadInfo, GameProcess.MoveInfo moveInfo, int pitch, boolean allow) {
        Point startPoint = moveInfo.startPoint();
        Point endPoint = moveInfo.endPoint();

        // System.out.println(STR. "       \{ Math.round(startPoint.x) },\{ Math.round(startPoint.y) }" );

        //当前朝向和实际移动方向之间的夹角

        // System.out.println(STR. "夹角:\{angle},识别朝向:\{ moveDirect },估算角度:\{ moveInfo.calDirect() }" );

        double a = moveInfo.a();
        double b = moveInfo.b();

        if (Math.abs(a) > 1) {
            //斜率大于1，到完全垂直的处理
            int step = 1;
            int count = 0;
            int startY;
            if (endPoint.y > startPoint.y) {
                count = (int) Math.abs(Math.ceil(endPoint.y) - ((int) (startPoint.y))) + 1;
                startY = (int) (startPoint.y);
            }
            else {
                count = (int) Math.abs(((int) (endPoint.y)) - Math.ceil(startPoint.y)) + 1;
                startY = (int) (endPoint.y);
            }
            while (count > 0) {
                count--;
                int y = startY + step;
                double x = Math.abs(a) == Double.MAX_VALUE ? endPoint.x : ((y - b) / a);
                //System.out.println(STR. "       \{ x },\{ y }" );
                //这里对x轴向上和向下取整
                setPointRoadInfo(roadInfo, (int) x, y, pitch, allow);
                setPointRoadInfo(roadInfo, (int) Math.ceil(x), y, pitch, allow);
                startY = y;
            }
        }
        else {
            int step = 1;
            int count = 0;
            int startX;
            if (endPoint.x > startPoint.x) {
                count = (int) Math.abs(Math.ceil(endPoint.x) - ((int) (startPoint.x))) + 1;
                startX = (int) (startPoint.x);
            } else {
                count = (int) Math.abs(((int) (endPoint.x)) - Math.ceil(startPoint.x)) + 1;
                startX = (int) (endPoint.x);
            }
            while (count > 0) {
                count--;
                int nextX = startX + step;
                double nextY = (a * nextX + b);
                //    System.out.println(STR. "       \{ nextX },\{ Math.round(nextY) }" );
                //这里y轴向上和向下取整
                setPointRoadInfo(roadInfo, nextX, (int) nextY, pitch, allow);
                setPointRoadInfo(roadInfo, nextX, (int) Math.ceil(nextY), pitch, allow);
                startX = nextX;
            }
        }
        //  System.out.println(STR. "       \{ Math.round(endPoint.x) },\{ Math.round(endPoint.y) }" );
        // setPointRoadInfo(roadInfo, (int) startPoint.x, (int) startPoint.y, pitch, allow);
        // setPointRoadInfo(roadInfo, (int) endPoint.x, (int) endPoint.y, pitch, allow);
       // showRoadInfo(roadInfo, (int) startPoint.x, (int) startPoint.y, pitch, 300);
    }


    public static void showRoadInfo(byte[] roadInfo, int cx, int cy, int pitch, int length) {

        int startX = cx - length;
        int startY = cy - length;

        int endX = cx + length;
        int endY = cy + length;

        int width = endX - startX + 1;
        int height = endY - startY + 1;

        Mat showMat = new Mat(height, width, CvType.CV_8UC3);

        byte[] pixels = new byte[width * height * 3];


        for (int i = startX, x = 0; i <= endX; i++, x++) {
            for (int j = startY, y = 0; j <= endY; j++, y++) {
                byte info = getPointRoadInfo(roadInfo, i, j, pitch);

                int p = 255;
                int index = y * width * 3 + x * 3;

                if (info == ALLOW) {
                    //绿色
                    // pixels[index] = 1;
                    pixels[index + 1] = (byte) 255;
                    // pixels[index+2] = 1;
                } else if (info == FORBID) {
                    //红色
                    pixels[index + 2] = (byte) 255;
                }

            }
        }


        showMat.put(0, 0, pixels);

        Imgproc.resize(showMat, showMat, new Size(301, 301));

        Imgproc.circle(showMat, new Point(150, 150), 3, new Scalar(210, 15, 203), 2);

        WindowHelper.show(showMat,"road");

        showMat.release();
    }

    /**
     * @param radius 是正方形圈数
     */
    public static void setRoadAllow(byte[] roadInfo, int cx, int cy, int pitch, int radius) {
        //在指定范围内找最近的可行走点
        cx = cx - radius;
        cy = cy - radius;

        int length = 1 + 2 * radius;
        int start = cx + cy * pitch; //开始行首位索引

        for (int i = 0; i < length; i++) {
            int index = start + (i * pitch);
            for (int j = 0; j < length; j += 1) {
                if (roadInfo[index + j] != FORBID) {
                    roadInfo[index + j] = ALLOW;
                }
            }
        }
    }

    public static int findRoadInLine(byte[] roadInfo, int start, int end) {
        for (int i = start; i < end; i++) {
            if (roadInfo[i] == ALLOW) {
                return i;
            }
        }
        return -1;
    }


    //计算一元一次方程
    //
    public static double[] calYI_YUAN_YI_CI(Point start, Point end) {
        double a = 0, b = 0;

        if (start.y == end.y) {
            //垂直
            a = 0;
            b = start.y;
        } else if (start.x == end.x) {
            a = end.y > start.y ? Double.MAX_VALUE : -Double.MAX_VALUE;
            b = 0;
            //水平
        } else {
            a = (end.y - start.y) / (end.x - start.x);
            b = end.y - a * end.x;
        }

        return new double[]{a, b};
    }


}
