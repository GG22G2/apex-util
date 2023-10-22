package hsb.game.util.help;

import hsb.game.util.GameProcess;
import hsb.game.util.pathfinding.Finder;
import hsb.game.util.pathfinding.astar_java.Astar;
import hsb.game.util.pathfinding.astar_java.AstarPosVo;
import hsb.game.util.pathfinding.jps.JPSFinder;
import hsb.game.util.pathfinding.map.MapData;
import hsb.game.util.pathfinding.map.Node;
import hsb.game.util.util.MathUtils;
import hsb.game.util.util.OpencvUtil;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;

import static hsb.game.util.GameProcess.analysisMove;
import static hsb.game.util.help.RoadInfoHelper.*;

/**
 * @author 胡帅博
 * @date 2023/10/9 22:07
 * <p>
 * 根据可行走区域 生成路径
 */
public class RoadHelper {


    public static void main(String[] args) {
        OpencvUtil.init();
        //Mat road = Imgcodecs.imread("bigMapTemplate/alps_road.png", Imgcodecs.IMREAD_GRAYSCALE);
        byte[] bytes = RoadInfoHelper.readMapRoadInfo("世界尽头");


        //1587  2045
        //1587 2046
        List<Node> blockPoint = List.of(new Node(1587, 2045), new Node(1587, 2046));


        byte[] bytes2 = roadInPaint(bytes, RoadInfoHelper.roadInfoWidth, RoadInfoHelper.roadInfoHeight);

        for (Node node : blockPoint) {
            int index = node.x + node.y * roadInfoWidth;
            bytes2[index] = MapData.OBSTACLE;
        }

        MapData mapData = new MapData(bytes2);
        Finder finder = new JPSFinder(mapData);


        //List<Node> paths = finder.findPath(new Node(939, 1357), new Node(1160, 1403));
        //List<Node> paths = finder.findPath(new Node(2588, 3833), new Node(2626, 3833));

        //System.out.println(finder.findPath(new Node(939, 1357), new Node(1160, 1403)));
        //  System.out.println(finder.findPath(new Node(2588, 3833), new Node(2618, 3833)));

        //todo findPath生成的点可能不是原图中的，而是腐蚀后的点，这种情况在拐杖处很容导致撞墙，索引
        //如果一个点不在原始的路径上，则删除，重新查早
        List<Node> path = finder.findPath(new Node(1587, 2039), new Node(1860, 2045));


//        for (Node node : path) {
//            int index = node.x + node.y * RoadInfoHelper.roadInfoWidth;
//            if (bytes[index] != RoadInfoHelper.ALLOW) {
//                //这个点在本来路径上没有, 判断一下,如果这两个点忽略了一个拐角，则需要填补上
//
//            }
//        }


        System.out.println(path);

        List<Point> list = path.stream().map(x -> new Point(x.x, x.y)).toList();

        List<Point> smoothPath = smoothPath(list,bytes,roadInfoWidth);


        pathPointAdjust(smoothPath, bytes, roadInfoWidth, roadInfoHeight);
        pathPointAdjust(smoothPath, bytes, roadInfoWidth, roadInfoHeight);
        pathPointAdjust(smoothPath, bytes, roadInfoWidth, roadInfoHeight);

        System.out.println(list.size());
        System.out.println(smoothPath.size());

        //List<Node> paths = finder.findPath(new Node(939, 1357), new Node(1160, 1403));
        //List<Node> paths = finder.findPath(new Node(1628, 490), new Node(2833, 3709));
        //System.out.println(paths);

        drawPath(list, bytes, RoadInfoHelper.roadInfoWidth, RoadInfoHelper.roadInfoHeight, "t1.png");
        drawPath(smoothPath, bytes, RoadInfoHelper.roadInfoWidth, RoadInfoHelper.roadInfoHeight, "t2.png");
        //  Mat mat = drawPath(smoothPath, RoadInfoHelper.roadInfoWidth, RoadInfoHelper.roadInfoHeight);
        //  Imgcodecs.imwrite("bigMapTemplate/" + "t3.png", mat);
//        for (int i = 0; i < smoothPath.size() - 1; i++) {
//            Point first = smoothPath.get(i);
//            Point second = smoothPath.get(i + 1);
//            System.out.println(MathUtils.pointDistance(first, second));
//        }
    }


    /**
     * @param mapDate 最原始的道路信息， 使用的是RoadInfoHelper.ALLOW  RoadInfoHelper.FORBID来表示道路的行走和阻挡
     */
    public static List<Point> generateMovePath(byte[] mapDate, int width, int height, Point startPoint, Point end, int length, int method) {

        int x = (int) startPoint.x;
        int y = (int) startPoint.y;
        int index = x + y * width;
        //如果起始点不可行走，那么就让起点走位length长度区域都可行走
        if (mapDate[index] != ALLOW) {
            byte[] curGameRoad = Arrays.copyOf(mapDate, mapDate.length);
            setRoadAllow(curGameRoad, (int) startPoint.x, (int) startPoint.y, width, length);
            mapDate = curGameRoad;
        }
        List<Point> path = null;
        if (method == 1) {
            path = generateMovePathUseAstar2(mapDate, width, height, startPoint, end, length);
        } else {
            path = generateMovePathUseJps(mapDate, width, height, startPoint, end, length);
        }

        //todo 点重定位，对于交叉口的点，因为大面积位置都可以走，算法生产的路径点可能是靠近边缘的，这时候可以往交叉口中心稍微靠近写，


        //路径平滑
        if (path != null) {
            path = smoothPath(path,mapDate,width);
            pathPointAdjust(path, mapDate, width, height);
            pathPointAdjust(path, mapDate, width, height);
        }


        return path;
    }

    private static void pathPointAdjust(List<Point> points, byte[] mapDate, int width, int height) {
    /*
        点位优化， 将每个点都尽可放置在路中间,这在一些狭小区域能避免碰撞
        还有一个问题就是，平滑后的线段，可能某两点的连线上的点大部分都不是可行走区域

         *大部分情况下，路网走过的地方都是保证两侧都有很宽的距离，即使偏移一点也问题不大，但是仍然有少部分狭窄地区，这时候一个微小幅度的转型调整可能
         *
         *
         * 对于任意两点A连线上的点，    并且C是B的后续点
         *   ： 如果连线上的点经过阻塞位置
         *           统计在道路上的点数量，不在道路上的点数量,
         *           首先尝试移动点位置，分别沿A->B方向和C->B方向的后续三个点当作新的B点做尝试,三个点中第一次遇到不在道路上的点时，仍可以计算，但是后续点丢弃
         *           如果新位置经过的阻塞点更少，则使用新的B点
         *
         * */

        if (points.size() < 3) {
            return;
        }

        for (int i = 1; i < (points.size() - 1); i++) {
            Point A = points.get(i - 1);
            Point B = points.get(i);
            Point C = points.get(i + 1);

            Point bestB = findBestB(A, B, C, mapDate, width);

            if (bestB != B) {
                //
                // System.out.println("找到一个更合适的B点");
                points.set(i, bestB);
            }
        }
    }

    private static Point findBestB(Point A, Point B, Point C, byte[] mapDate, int width) {
        double[] fc = calYI_YUAN_YI_CI(A, B);
        double[] fc2 = calYI_YUAN_YI_CI(C, B);
        //四个候选点
        List<Point> bPoints = List.of(
                B
                , MathUtils.nextPoint(A, B, fc[0], fc[1], 1)
                , MathUtils.nextPoint(A, B, fc[0], fc[1], 2)
                , MathUtils.nextPoint(A, B, fc[0], fc[1], 3)

                , MathUtils.nextPoint(C, B, fc2[0], fc2[1], 1)
                , MathUtils.nextPoint(C, B, fc2[0], fc2[1], 2)
                , MathUtils.nextPoint(C, B, fc2[0], fc2[1], 3)
        );


        Point newB = null;

        int minBlockCount = Integer.MAX_VALUE;

        for (int i = 0; i < bPoints.size(); i++) {
            Point b = bPoints.get(i);

            LineBlockPoint t1 = blockCountInLine(A, b, mapDate, width);
            LineBlockPoint t2 = blockCountInLine(C, b, mapDate, width);

            //int totalPointCount = t1.totalPointCount() + t2.totalPointCount();
            int blockPointCount = t1.blockPointCount() + t2.blockPointCount();

            if (blockPointCount < minBlockCount) {
                newB = b;
                minBlockCount = blockPointCount;
            }

            if (minBlockCount == 0) {
                //遇到block为0的点就直接采纳
                break;
            }
        }

        return newB;
    }

    public record LineBlockPoint(int totalPointCount, int blockPointCount) {
    }

    private static LineBlockPoint blockCountInLine(Point A, Point B, byte[] mapDate, int pitch) {
        List<Point> points = MathUtils.linePoints(A, B);
        int total = points.size();
        int blockCount = 0;
        for (Point point : points) {
            int index = (int) (point.x + point.y * pitch);
            if (mapDate[index] == FORBID || mapDate[index] == DEFAULT) {
                blockCount++;
            }
        }
        return new LineBlockPoint(total, blockCount);
    }


    private static List<Point> generateMovePathUseJps(byte[] mapDate, int width, int height, Point startPoint, Point end, int length) {
        //初始化道路数据，和jsp寻路算法
        byte[] road = RoadHelper.roadInPaint(mapDate, width, height);

        MapData mapData = new MapData(road);
        Finder finder = new JPSFinder(mapData);
        List<Node> path = finder.findPath(new Node((int) startPoint.x, (int) startPoint.y), new Node((int) end.x, (int) end.y));
        if (path != null) {
            return path.stream().map(x -> new Point(x.x, x.y)).toList();
        }
        return null;
    }


    private static List<Point> generateMovePathUseAstar2(byte[] mapDate, int width, int height, Point startPoint, Point end, int length) {
        Astar astar = new Astar();
        int row = height;
        int col = width;

        byte[][] mapdata = new byte[row][col];
        for (int i = 0; i < col; i++) {
            for (int j = 0; j < row; j++) {
                int index = j * col + i;
                if (mapDate[index] == RoadInfoHelper.ALLOW) {
                    //可行走
                    mapdata[j][i] = 0;
                } else {
                    //不可行走
                    mapdata[j][i] = 1;
                }
            }
        }

        ArrayList<AstarPosVo> points = astar.find(mapdata, row, col, (int) startPoint.x, (int) startPoint.y, (int) end.x, (int) end.y, Math.max(width, height) + 10);
        if (points != null) {
            return points.stream().map(x -> new Point(x.x, x.y)).toList();
        }
        return null;
    }

    public static List<Point> smoothPath(List<Point> list) {
        return smoothPath(list, null, 0);
    }

    public static List<Point> smoothPath(List<Point> list, byte[] mapDate, int pitch) {

        List<Point> smoothPath = new ArrayList<>();
        int startIndex = 0;
        //首先是合并一段连续的路径点，这一段点集合的左右摆动幅度小于某个值则合并
        while (true) {
            Point first = list.get(startIndex);
            smoothPath.add(first);

            if (startIndex == list.size() - 1) {
                break;
            } else if (startIndex == list.size() - 2) {
                smoothPath.add(list.get(startIndex + 1));
                break;
            }
            int index = startIndex;
            //如果还有至少两个点则进入这里
            for (int i = index + 2; i < list.size(); i++) {
                Point nextPoint = list.get(i);
                //GameProcess.MoveInfo moveInfo = analysisMove(1, 1, first, nextPoint);

                double[] fc = calYI_YUAN_YI_CI(first, nextPoint);
                double a = fc[0];
                double b = fc[1];
                boolean vLine = first.x == nextPoint.x;
                int endIndex = -1;

                //遍历中间点,判断是否有大于某个阈值的
                for (int j = index + 1; j < i; j++) {
                    Point innerPoint = list.get(startIndex);
                    double dis = vLine ? MathUtils.pointToLineDistance(innerPoint, first.x) : MathUtils.pointToLineDistance(innerPoint, a, b);

                    int blockPointCount = 0;
                    int totalPointCount = 1;
                    if (mapDate != null) {
                        LineBlockPoint lineBlockPoint = blockCountInLine(first, nextPoint, mapDate, pitch);
                        blockPointCount = lineBlockPoint.blockPointCount;
                        totalPointCount = lineBlockPoint.totalPointCount;
                    }
                    /*
                     *           新路径上的点的连线上经过的阻塞点数量大于某个阈值，这一段AB的长度也大于某个阈值，则需要在A和B的中心点A2
                     *
                     * */

                    boolean blockMany = totalPointCount > 9 && (blockPointCount*1.0/totalPointCount) >0.4;

                    // 点到线距离大于0.6 或者连线上超过10个点是不可行走的，则都不允许修复
                    if (dis > 0.6 || blockMany) {
                        endIndex = i - 1;
                        break;
                    }


                }

                if (endIndex == -1) {
                    //当前拟合线段没问题，则继续往后拟合
                    startIndex = i;
                } else {
                    startIndex = endIndex;
                    break;
                }
            }
        }


        //统计关键点数量
        List<Point> keyPoints = new LinkedList<>();

        Set<Point> keyPointSet = new HashSet<>(smoothPath.size());


        //这一段是识别方向改变的点，上下左右四个方法，任意一个改变就认为改变了
        //方向改变幅度大于15°的点，多个两点距离超过50的点
        //把这些点记作关键点，也就是这些点一定要被走到
        int preDx = 0;
        int preDy = 0;
        Point prePoint = smoothPath.get(0);
        Point second = smoothPath.get(1);
        GameProcess.MoveInfo preMoveInfo = analysisMove(1, 1, prePoint, second);
        if (second.x > prePoint.x) preDx = 1;
        else if (second.x < prePoint.x) preDx = -1;
        if (second.y > prePoint.y) preDy = 1;
        else if (second.y < prePoint.y) preDy = -1;
        keyPoints.add(prePoint);
        for (int i = 1; i < smoothPath.size(); i++) {
            Point curPoint = smoothPath.get(i);
            int dx = 0;
            int dy = 0;

            if (curPoint.x > prePoint.x) dx = 1;
            else if (curPoint.x < prePoint.x) dx = -1;
            if (curPoint.y > prePoint.y) dy = 1;
            else if (curPoint.y < prePoint.y) dy = -1;


            GameProcess.MoveInfo moveInfo = analysisMove(1, 1, prePoint, curPoint);

            double abs = Math.abs(moveInfo.calDirect() - preMoveInfo.calDirect());
            double distance = MathUtils.pointDistance(curPoint, keyPoints.get(keyPoints.size() - 1));

            if (dx != preDx || dy != preDy) {
                //方向改变
                //  System.out.println("方向改变");
                keyPoints.add(prePoint);
                keyPointSet.add(prePoint);
            } else if (abs > 15 || distance > 50) {
                keyPoints.add(prePoint);
            }

            prePoint = curPoint;
            preDx = dx;
            preDy = dy;
            preMoveInfo = moveInfo;
        }

        //System.out.println("拐点数量：" + keyPoints.size());


        /* 最后消除一条直线上的抖动
         * 有一些路径，基本是一条直线，但是可能左走一下5单位，右走5单位，来回在一个小幅度的变相中行走，但是整体上却是往一个方向移动的。
         *
         * 这种不停的方向变得还很容易影星行走的准确率
         * */

        Set<Point> ignorePoint = new HashSet<>(keyPoints.size());
        while (true) {
            double minDistance = Double.MAX_VALUE;
            int minDistanceIndex = -1;
            for (int i = 0; i < keyPoints.size() - 1; i++) {
                if (ignorePoint.contains(keyPoints.get(i))) {
                    continue;
                }
                double v = MathUtils.pointDistance(keyPoints.get(i), keyPoints.get(i + 1));

                if (v < minDistance) {
                    minDistance = v;
                    minDistanceIndex = i;
                }
            }

            if (minDistance < 7) {
                //一次移动过小，取中间点代替
                Point pre = keyPoints.get(minDistanceIndex);
                Point next = keyPoints.get(minDistanceIndex + 1);

                if (keyPointSet.contains(pre) && keyPointSet.contains(next)) {
                    ignorePoint.add(pre);
                } else if (keyPointSet.contains(pre)) {
                    keyPoints.remove(next);
                } else if (keyPointSet.contains(next)) {
                    keyPoints.remove(pre);
                } else {
                    //取两者终点
                    double x = (pre.x + next.x) / 2.0;
                    double y = (pre.y + next.y) / 2.0;
                    Point p = new Point(x, y);

                    keyPoints.set(minDistanceIndex, p);
                    //这里要添加，再删除，不然这个索引就乱了
                    keyPoints.remove(pre);
                    keyPoints.remove(next);
                }
            } else {
                break;
            }
        }


        return keyPoints;
    }


    /**
     * 将道路信息修正后返回
     */
    public static byte[] roadInPaint(byte[] oldRoadBytes, int width, int height) {
        byte[] newRoadBytes = new byte[width * height * 3]; // 腐蚀操作必须要是三通道图像
        Mat newRoad = new Mat(height, width, CvType.CV_8UC3);

        for (int i = 0; i < oldRoadBytes.length; i++) {
            if (oldRoadBytes[i] == RoadInfoHelper.ALLOW) {
                int index = i * 3;
                newRoadBytes[index] = 0;
                newRoadBytes[index + 1] = (byte) 255;
                newRoadBytes[index + 2] = 0;
            }
        }
        newRoad.put(0, 0, newRoadBytes);


        // 定义腐蚀核的大小
        // Size kernelSize = new Size(3, 3); // 可根据需要调整
        // 进行腐蚀操作
        // Imgproc.dilate(newRoad, newRoad, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, kernelSize));
        //腐蚀
        //Imgproc.erode(newRoad, newRoad, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3)));

        newRoad.get(0, 0, newRoadBytes);

        byte[] map2 = new byte[width * height];

        //将图像中所有障碍物点设置为不可走
        for (int i = 0, j = 1; i < oldRoadBytes.length; i++, j += 3) {
            byte oldValue = oldRoadBytes[i];
            byte newValue = newRoadBytes[j];
            if (oldValue == RoadInfoHelper.FORBID) {
                map2[i] = MapData.OBSTACLE;
            } else if (newValue == RoadInfoHelper.ALLOW) {
                map2[i] = MapData.ACCESS;
            }
        }
        newRoad.release();
        return map2;
    }


    public static void main122(String[] args) {
        OpencvUtil.init();
        Mat map = Imgcodecs.imread("bigMapTemplate/alps.png");
        Mat road = Imgcodecs.imread("bigMapTemplate/alps_road.png", Imgcodecs.IMREAD_GRAYSCALE);

        Mat newRoad = new Mat(road.height(), road.width(), CvType.CV_8UC3);

        byte[] oldRoadBytes = new byte[road.width() * road.height()];
        byte[] newRoadBytes = new byte[road.width() * road.height() * 3]; // 腐蚀操作必须要是三通道图像

        road.get(0, 0, oldRoadBytes);


        for (int i = 0; i < oldRoadBytes.length; i++) {
            if (oldRoadBytes[i] == RoadInfoHelper.ALLOW) {
                int index = i * 3;
                newRoadBytes[index] = 0;
                newRoadBytes[index + 1] = (byte) 255;
                newRoadBytes[index + 2] = 0;
            }
        }

        newRoad.put(0, 0, newRoadBytes);


        // 定义腐蚀核的大小
        Size kernelSize = new Size(5, 5); // 可根据需要调整
        // 进行腐蚀操作
        Imgproc.dilate(newRoad, newRoad, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, kernelSize));
        //腐蚀
        Imgproc.erode(newRoad, newRoad, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3)));


        newRoad.get(0, 0, newRoadBytes);

        byte[] map2 = new byte[4195 * 4198];
        int width = road.width();
        int height = road.height();
        //将图像中所有障碍物点设置为不可走
        for (int i = 0, j = 1; i < oldRoadBytes.length; i++, j += 3) {
            byte oldValue = oldRoadBytes[i];
            byte newValue = newRoadBytes[j];

            if (oldValue == RoadInfoHelper.FORBID) {
                map2[i] = MapData.OBSTACLE;
                newRoadBytes[j] = (byte) 0; //背景色
                newRoadBytes[j + 1] = (byte) 255;
            } else {
                if (newValue == RoadInfoHelper.ALLOW) {
                    map2[i] = MapData.ACCESS;
                }
            }
        }


        MapData mapData = new MapData(map2);

        Finder finder = new JPSFinder(mapData);
        long startNanos_36_89 = System.nanoTime();
        for (int i = 0; i < 1; i++) {
            //List<Node> paths = finder.findPath(new Node(939, 1357), new Node(1160, 1403));
            //List<Node> paths = finder.findPath(new Node(2588, 3833), new Node(2626, 3833));
        }
        long endNanos_36_94 = System.nanoTime();
        System.out.println((endNanos_36_94 - startNanos_36_89) / 1000000.0);
        //   List<Node> paths = finder.findPath(new Node(939, 1357), new Node(1160, 1403));
        List<Node> paths = finder.findPath(new Node(1628, 490), new Node(2833, 3709));
        //findPath返回的可能是不连续的点击，虽然不连续，但是前后两个点是联通的，也就是说从这个点走到下一个点即可
        //todo findPath输出的点集合可能是紧贴可移动区域边缘的，尝试将点重新定位到线路中心
        // List<Node> paths = finder.findPath(new Node(2588, 3833), new Node(2626, 3833));
        System.out.println(paths);

        for (int i = 0; i < paths.size() - 1; i++) {
            Node node1 = paths.get(i);
            Node node2 = paths.get(i + 1);
            Imgproc.line(map, new Point(node1.x, node1.y), new Point(node2.x, node2.y), new Scalar(255, 0, 0), 1);
        }
        Imgcodecs.imwrite("bigMapTemplate/alps_path_show_1.png", map);

        map.get(0, 0, newRoadBytes);
        for (int i = 0, j = 0; i < map2.length; i++, j += 3) {
            if (map2[i] == MapData.ACCESS) {
                newRoadBytes[j] = 0;
                newRoadBytes[j + 1] = (byte) 255;
                newRoadBytes[j + 2] = 0;
            }
        }
        map.put(0, 0, newRoadBytes);

        for (int i = 0; i < paths.size() - 1; i++) {
            Node node1 = paths.get(i);
            Node node2 = paths.get(i + 1);
            Imgproc.line(map, new Point(node1.x, node1.y), new Point(node2.x, node2.y), new Scalar(255, 0, 0), 1);
        }

        //  Imgcodecs.imwrite("bigMapTemplate/alps_path_show_2.png", map);
    }


    public static void drawPath(List<Point> paths, int width, int height, String fileName) {
        drawPath(paths, null, width, height, fileName);
    }

    /**
     * mat是单通道图像
     */
    public static void drawPath(List<Point> paths, byte[] mat, int width, int height, String fileName) {
        Mat map = new Mat(height, width, CvType.CV_8UC3);
        if (mat != null) {
            Mat temp = new Mat(height, width, CvType.CV_8UC1);
            temp.put(0, 0, mat);
            Imgproc.cvtColor(temp, map, Imgproc.COLOR_GRAY2BGR);  //CSGO
            temp.release();
        }

        for (int i = 0; i < paths.size() - 1; i++) {
            Point node1 = paths.get(i);
            Point node2 = paths.get(i + 1);
            Imgproc.line(map, new Point(node1.x, node1.y), new Point(node2.x, node2.y), new Scalar(255, 0, 0), 1);
            Imgproc.circle(map, new Point(node1.x, node1.y), 1, new Scalar(210, 15, 155), 1);
        }
        if (fileName != null) {
            Imgcodecs.imwrite("bigMapTemplate/" + fileName, map);
        }
        map.release();
    }


    public static Mat drawPath(List<Point> paths, int width, int height) {
        Mat map = new Mat(height, width, CvType.CV_8UC3);
        for (int i = 0; i < paths.size() - 1; i++) {
            Point node1 = paths.get(i);
            Point node2 = paths.get(i + 1);
            Imgproc.line(map, new Point(node1.x, node1.y), new Point(node2.x, node2.y), new Scalar(0, 255, 0), 1);
            Imgproc.circle(map, new Point(node1.x, node1.y), 2, new Scalar(210, 15, 155), 1);
        }
        return map;
    }


    public static void showGenerateRoadInfo(Mat roadInfo, int cx, int cy, int length) {

        int startX = cx - length;
        int startY = cy - length;

        int endX = cx + length;
        int endY = cy + length;

        int width = endX - startX + 1;
        int height = endY - startY + 1;


        Rect showRect = new Rect(startX, startY, width, height);
        Mat showMat = roadInfo.submat(showRect);


        Imgproc.resize(showMat, showMat, new Size(301, 301));
        Imgproc.circle(showMat, new Point(150, 150), 3, new Scalar(210, 15, 203), 2);

        WindowHelper.show(showMat);

        showMat.release();
    }


}
