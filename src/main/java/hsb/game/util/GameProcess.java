package hsb.game.util;

import hsb.game.util.demo.BigMapLocation;
import hsb.game.util.dll.CaptureDLL;
import hsb.game.util.dll.MouseDLL;
import hsb.game.util.help.RoadHelper;
import hsb.game.util.help.RoadInfoHelper;
import hsb.game.util.help.WindowHelper;
import hsb.game.util.imageconcat.GetBigMap;
import hsb.game.util.match.ApexSiftMatcher;
import hsb.game.util.paddleocrutil.wrapper.PaddleOcr;
import hsb.game.util.process.MoveController;
import hsb.game.util.process.RandomMoveController;
import hsb.game.util.util.MathUtils;
import hsb.game.util.util.OpencvUtil;
import hsb.game.util.util.RobotUtil;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.event.KeyEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static hsb.game.util.Config.resolutionX;
import static hsb.game.util.Config.resolutionY;
import static hsb.game.util.dll.CaptureDLL.apexWindowRect;
import static hsb.game.util.help.RoadInfoHelper.*;

/**
 * @author 胡帅博
 * @date 2023/10/6 11:06
 * <p>
 * 游戏流程
 */
public class GameProcess {
    public static void main(String[] args) {
        OpencvUtil.init();
        GameProcess gameProcess = new GameProcess();

        for (int i = 0; i < 10000; i++) {
           //  gameProcess.playGameOnce(RECORD_ROAD); //记录模式
            gameProcess.playGameOnce(NAVIGATE); //导航模式
          //  gameProcess.playGameOnce(RANDOM_MOVE);
        }

       // gameProcess.recordHumanMove("诸王峡谷");
       // gameProcess.navigationMove("奥林匹斯");

        //gameProcess.getBigMat("世界尽头");
        //gameProcess.randomMove2();
        //gameProcess.xDirect(230, Config.xAngeleScale / 2);
        //奥林匹斯
        // gameProcess.prepareLargeMat("世界尽头");
        //  gameProcess.saveOnce();

        // gameProcess.prepareLargeMat("诸王峡谷");
        //   gameProcess.endGame(false);


        // gameProcess.showPosition();

        //  gameProcess.recordHumanMove(0);
        //gameProcess.randomMove(1000);


        // gameProcess.xDirect(184);

//        for (int i = 20; i < 100; i += 4) {
//            gameProcess.xDirect(i);
//            RobotUtil.sleep(1);
//        }
        WindowHelper.close(); //关闭所有窗口

    }

    public final static Map<String, String> mapNameMap = Map.of(
            "奥林匹斯", "alps.png"
            , "诸王峡谷", "zwxg.png"
            , "世界尽头", "sjjt.png"
    );


    ApexSiftMatcher apexSiftMatcher = new ApexSiftMatcher();
    private String curMapName;

    private Mat bigMap;
    //地图对应的路况信息
    private byte[] roadInfo;

    MoveController moveController = new MoveController(this);
    RandomMoveController randomMoveController = null;
    private ApexSiftMatcher.DetectedFeature bigMatFeature;


    public static final int RECORD_ROAD = 1;

    public static final int RANDOM_MOVE = 2;

    public static final int NAVIGATE = 3;


    public GameProcess(){
        randomMoveController = new RandomMoveController(this,moveController);
    }

    //玩一次游戏
    public void playGameOnce(int playType) {
        boolean end = false;
        try {
            CaptureDLL.foregroundApexWindow();
            RobotUtil.sleep(500);
            //假设现在是在首页,也就是有准备按钮的页面

            String mapName = findMapName();
            if (mapName == null || mapName.isEmpty()) {
                System.out.println("没找到地图名称,请检查游戏当前页面");
                return;
            }
            System.out.println(STR. "地图:\{ mapName }" );
            //排队准备

            boolean success = prepare();


            if (!success) {
                System.out.println("未发现准备按钮,请检查游戏当前页面");
                return;
            }

            //等待连接游戏
            waitStart();

            //获取大地图
            boolean loadBigMatSuccess = getBigMat(mapName);
            if (!loadBigMatSuccess) {
                //加载大地图失败，这一句游戏用来抓取地图
            }

            //首先降落
            navigationLand();

            //等待可以跳伞
            //           success = waitLaunchActive();
//            跳伞
//            launch();

            if (loadBigMatSuccess) {
                System.out.println("开始游戏");
                //showPosition();
                RobotUtil.sleep(1000);

                if (playType == RECORD_ROAD) {
                    recordHumanMove(0);
                } else if (playType == RANDOM_MOVE) {
                    randomMove2();
                } else if (playType == NAVIGATE) {
                    navigationMove(1000 * 60 * 15);
                }

                endGame(true);
                end = true;
            } else {
                System.out.println("等待10s后,开始准备大地图");
                try {
                    Thread.sleep(1000 * 10);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //抓取地图
                prepareLargeMat(mapName);

                //结束这一局游戏

                endGame(true);
                end = true;
            }
            //结束时候需要保证也是在首页

            System.out.println("完成一次游戏");
        } catch (Exception e) {

            System.out.println("游戏异常结束" + e.getMessage());
        } finally {
            if (!end) {
                endGame(true);
            }

        }

    }


    public void showPosition() {

        //生成一张小地图
        //0.2167667212193128
        //0.21713998260377018

        apexSiftMatcher.initBigMap(bigMatFeature);
        long lastCapture = 0;
        Point lastPoint = calCurPosition();
        for (int i = 0; i < 30000; i++) {
            long time = System.currentTimeMillis();
            long useTime = time - lastCapture;
            long startNanos_55_196 = System.nanoTime();
            Point point = showPositionOnce(lastPoint, useTime + 20);
            long endNanos_55_198 = System.nanoTime();
            System.out.println((endNanos_55_198 - startNanos_55_196) / 1000000.0);
            lastCapture = time;
            lastPoint = point;
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void recordHumanMove(String mapName) {
        getBigMat(mapName);
        recordHumanMove(0);
    }

    //记录人自己都过的地方，这个模式下只记录可行走区域
    public void recordHumanMove(int count) {

        long curTime;
        int moveTime = 3;
        long lastTime = System.currentTimeMillis();
        Point lastPoint = calCurPosition();

        for (int i = 1; i < 10000; i++) {
            if (checkGameOver()) {
                System.out.println("游戏结束");
                break;
            }
            curTime = System.currentTimeMillis();
            Point curPoint = calCurPosition();
            if (curPoint != null) {
                MoveInfo moveInfo = analysisMove((curTime - lastTime) / 100.0, 1, lastPoint, curPoint);
                double averageSpeed = moveInfo.averageSpeed;
                System.out.println(STR. "用时:\{ curTime - lastTime },速度:\{ averageSpeed }" );

                if (averageSpeed > 8) {
                    System.out.println("速度过快，忽略采样");
                } else {
                    recordRoadInfo(roadInfo, moveInfo, roadInfoWidth, true);
                    showRoadInfo(roadInfo, (int) moveInfo.startPoint.x, (int) moveInfo.startPoint.y, roadInfoWidth, 300);
                    if (i % 60 == 0) {
                        saveMapRoadInfo(roadInfo, curMapName);
                    }
                    lastPoint = curPoint;
                    lastTime = curTime;
                }
                long useTime = System.currentTimeMillis() - curTime;
                if (useTime < moveTime * 100) {
                    RobotUtil.sleep((int) ((moveTime * 100) - useTime));
                }
            } else {
                RobotUtil.sleep(100);
            }
        }
        saveMapRoadInfo(roadInfo, curMapName);
    }


    /**
     * 导航降落,降落到设置好的降落点中最近的一个
     */
    public void navigationLand() {
        CaptureDLL.foregroundApexWindow();
        //假设降落点是(1100,2433)
        //诸王峡谷 (1100,2433)

        // Point[] landPoints = new Point[]{new Point(1100, 2433)}; // 诸王峡谷
        //Point[] landPoints = new Point[]{new Point(1817, 2534),new Point(2888, 2130)}; //世界尽头

        List<Point> landPoints = Config.mapLandPints.get(curMapName);
        waitLaunchActive();
        //先等远离边缘，边缘的定位效果不好
        RobotUtil.sleep(2000);
        //这里取四个点的斜率均值
        MoveInfo moveInfo1 = recordMove(1000);
        MoveInfo moveInfo2 = recordMove(1000);
        MoveInfo moveInfo3 = recordMove(1000);
        MoveInfo moveInfo4 = recordMove(1000);

        //统计平均值，排除误差，再统计平均值
        double a = 0;
        double b = 0;
        Point minClosePoint = null;
        if (Math.abs(moveInfo1.a) == Double.MAX_VALUE && Math.abs(moveInfo2.a) == Double.MAX_VALUE) {
            //垂直移动
            double x = (moveInfo1.startPoint.x + moveInfo2.startPoint.x + moveInfo3.startPoint.x + moveInfo3.startPoint.x) / 4.0;
            minClosePoint = MathUtils.minClosePointToLine(landPoints, x);

        } else {
            a = (moveInfo1.a + moveInfo2.a + moveInfo3.a + moveInfo4.a) / 4.0;
            b = (moveInfo1.b + moveInfo2.b + moveInfo3.b + moveInfo4.b) / 4.0;
            // 计算点到直线距离
            minClosePoint = MathUtils.minClosePointToLine(landPoints, a, b);
        }
        //确定降落点后等待最近跳伞位置，也就是后一次距离大于前一次时就是该跳伞了
        System.out.println(STR. "确定降落点：\{ minClosePoint } , 等待最接近时跳伞" );

        while (true) {
            MoveInfo temp = recordMove(1000);
            double d1 = MathUtils.pointDistance(temp.startPoint, minClosePoint);
            double d2 = MathUtils.pointDistance(temp.endPoint, minClosePoint);
            System.out.println(STR. "检测当前位置中...,与降落点间距离:\{ d2 }" );

            if (d2 > d1 || d2 < 300) {
                System.out.println(STR."开始跳伞");
                //每次开局默认水平视角是30°
                long landStartTime = System.currentTimeMillis(); //开始跳伞时间
                moveController.launch();
                RobotUtil.sleep(100);

                Point curPoint = calCurPosition();
                MoveInfo moveInfo = analysisMove(10, 0, curPoint, minClosePoint);
                double calDirect = moveInfo.calDirect;
                double distance = moveInfo.distance;
                System.out.println(STR. "确定飞行方向为:\{ calDirect }" );
                xDirect(calDirect, Config.xAngeleScale / 2);

                //按w开始往前飞
                moveController.forward();
                RobotUtil.sleep(1000);

                //等待非到指定区域
                boolean isFiy = true;
                double lastDistance = -1;
                int adjustDirectCount = 0;
                while (d2 > 300) {
                    curPoint = calCurPosition();
                    moveInfo = analysisMove(1, 0, curPoint, minClosePoint);
                    System.out.println(STR. "与目标距离:\{ moveInfo.distance }" );

                    if (lastDistance != -1) {
                        if (Math.abs(moveInfo.distance - lastDistance) < 1) {
                            //如果两次距离变动幅度过小，则可能是距离太远，还没飞到指定区域就已经降落了
                            System.out.println(STR. "可能提前降落了:\{ Math.abs(moveInfo.distance - lastDistance) }" );
                            isFiy = false;
                            break;
                        }

                        if (moveInfo.distance > lastDistance) {
                            //如果两次距离变动幅度过小，则可能是距离太远，还没飞到指定区域就已经降落了
                            System.out.println("飞过了，赶紧降落");
                            break;
                        }
                    }

                    //如果距离小于100格像素，则开始垂直往下飞
                    if (moveInfo.distance < 300) {
                        System.out.println("接近区域，开始降落");
                        break;
                    }
                    lastDistance = moveInfo.distance;

                    RobotUtil.sleep(400);

                    //三次调整机会
                    if (adjustDirectCount < 3) {
                        System.out.println(STR. "修正飞行方向为:\{ moveInfo.calDirect }" );
                        xDirect(moveInfo.calDirect, Config.xAngeleScale / 2);
                        adjustDirectCount++;
                    }

                }
                moveController.stop();
                moveController.forward();
                //如果此时还在天上飞，则开始垂直降落
                if (isFiy) {
                    curPoint = calCurPosition();
                    moveInfo = analysisMove(1, 0, curPoint, minClosePoint);
                    System.out.println(STR. "修正飞行方向为:\{ moveInfo.calDirect }" );
                    xDirect(moveInfo.calDirect, Config.xAngeleScale / 2);

                    //600 - 209   13秒    30mi/s
                    //水平30°视角下，每秒下落15.6米，这个下来是按w和不按w都是这个速度，地面高度是144米(不同地图可能不同？)
                    long curTime = System.currentTimeMillis();
                    double useTime = (curTime - landStartTime) / 1000.0;

                    double maxVSpeed = 30.0; //这个是垂直往下时候的落地速度
                    double landHeight = 144; //地面海拔高度
                    final double vSpeed = 15.6; //30°视角下，每秒的下落速度
                    double curHeight = 600 - (useTime * vSpeed);

                    //垂直速度公式   v = ta * adjustAngle + tc
                    //水平速度公式   v = tb * adjustAngle
                    double ta = -0.23;
                    double tc = 30;
                    double tb = 1.1;

                    double h = (curHeight - landHeight);
                    double w = moveInfo.distance;

                    //高和垂直速度的比值，  水平距离和水平速度的比值应该相同，这样可以算出来
                    double adjustAngle = ((w * tc) / tb) / (h - ((ta * w) / tb));
//                    if (adjustAngle > 30) {
//                        adjustAngle = 30;
//                    }

                    double calVSpeed = ta * adjustAngle + tc;
                    //double sleepTime = Math.ceil((curHeight - landHeight) / calVSpeed)+1;
                    int sleepTime = (int) ((moveInfo.distance) / (tb * adjustAngle));


                    sleepTime = Math.max(7, sleepTime - 2); //这里减少三秒，因为最后降落时候，速度会下来
                    System.out.println(STR. "调整度数:\{ 60 - adjustAngle },垂直速度:\{ calVSpeed },水平速度:\{ tb * adjustAngle }" );
                    adjustAngle = 60 - adjustAngle;

                    //调整视角
                    yDirect(adjustAngle);

                    System.out.println(STR. "当前预估高度:\{ curHeight },预估降落用时:\{ sleepTime },距离目标点距离:\{ moveInfo.distance }" );
                    /*
                     * 90°时，水平移动是每秒0米左右
                     * 80°时，水平移动是每秒11米左右
                     * 70°时，水平移动是每秒24米
                     * 60°时，水平移动是每秒33米
                     * */
                    for (int i = 0; i < sleepTime; i++) {
                        moveController.forward();
                        long t1 = System.currentTimeMillis();
                        //每秒钟一次调整速度的机会
                        curPoint = calCurPosition();
                        moveInfo = analysisMove(1, 0, curPoint, minClosePoint);
                        System.out.println(STR. "与目标距离:\{ moveInfo.distance }" );
                        long t2 = System.currentTimeMillis();
                        RobotUtil.sleep((int) (1000 - (t2 - t1)));
                    }
                    moveController.stop();
                    RobotUtil.sleep(6000);
                    yDirect(-(adjustAngle + 30));
                } else {
                    moveController.stop();
                }


                System.out.println("结束落地流程");
                break;
            }
        }

    }

    public void navigationMove(String mapNme) {
        getBigMat(mapNme);
        navigationMove(0);
    }

    /**
     * @param surviveTime 存货时间，单位毫秒
     */
    public void navigationMove(long surviveTime) {
        CaptureDLL.foregroundApexWindow();
        //诸王峡谷 (1100,2433)
        /*
         *
         *  流程：
         *     降落->寻找最近线路->持续检测安全区->导航到安全区->到固定时间时结束游戏。
         *
         * 人为设定几个固定的降落点，找空旷的位置作为降落点
         *    游戏开始后记录航行方向，构造线段，根据点到直线的距离确定用那个降落点。
         *
         * 降落位置尽可能固定下来,
         *    然后降落流程是先平行飞，当接近指定降落点后开始垂直降落
         *
         *
         * 人物落点可能不在线路上，先确定去最近线路方向:
         *                   人物周围8个方向，匹配最近的路径点的朝向,然后过去
         *
         * 根据线路开始移动:
         *   遍历当前位置和后续位置，统计斜率，如果斜率变化超过10则结束，或者下个点和下下个点 这两个点间距离超过5则结束。
         *         此时确定了开始点和结束点，并且人物在开始点,
         *               ：然后控制人物的朝向为结束点，开始移动
         *
         *   移动规则:
         *     如果移动距离大于某个阈值则shift移动，并持续检测位置（障碍物判断，游戏结束判断）
         *     当检测到接近终点时，取消shift，以每前进50毫秒(暂定),一停顿的频率移动，并持续检测位置（障碍物判断，游戏结束判断）
         *
         *   检测规则:
         *     当检查到游戏结束则停止导航
         *     当检测到移动停滞或过慢，则标记障碍物,并停止运行。 (也可以并尝试绕过，绕过同时记录移动轨迹，但是后续再说)
         *
         * */

        List<Point> path = generateMovePath(curMapName, calCurPosition(), null, roadInfo, roadInfoWidth, roadInfoHeight, 20);

        if (path != null) {
            System.out.println(STR. "规划路径:\{ path }" );
            navigationMove(path);
        } else {
            System.out.println("没有找到可行路线");
        }

    }

    public static List<Point> generateMovePath(String curMapName, Point startPoint, Point endPoint, byte[] roadInfo, int mapWidth, int mapHeight, int length) {
        //todo 暂时把终点设置成固定值，方便测速
        Point end = Map.of(
                //"奥林匹斯", new Node(2353, 2836)
                "奥林匹斯", new Point(2932, 2273)
                , "诸王峡谷", new Point(2941, 3048)
                // , "世界尽头", new Node(2815, 1940)
                // , "世界尽头", new Point(1536, 2373)
                , "世界尽头", new Point(1860, 2045)   //测速障碍物
        ).get(curMapName);

        return RoadHelper.generateMovePath(roadInfo, mapWidth, mapHeight, startPoint, end, length, 0);
    }

    /**
     * @param smoothPath 行走路径，默认第一个点就是当前人物所在位置
     */
    public void navigationMove(List<Point> smoothPath) {
        System.out.println("开始移动");
        AtomicBoolean navigateEnd = new AtomicBoolean(false);

        Mat mat = RoadHelper.drawPath(smoothPath, roadInfoWidth, roadInfoHeight);
        Thread.startVirtualThread(() -> {
            while (!navigateEnd.get()) {
                if (lastCenterPoint != null) {
                    RoadHelper.showGenerateRoadInfo(mat, (int) lastCenterPoint.x, (int) lastCenterPoint.y, 80);
                }
                RobotUtil.sleep(16);
            }
            System.out.println("展示结束");
        });

        List<Point> newPath = null;

        try {

            Point prePoint = smoothPath.get(0);
            for (int i = 0; i < smoothPath.size(); i++) {
                Point nextPoint = smoothPath.get(i);

                int status = moveController.moveToPoint(nextPoint);
                if (status == MoveController.MOVE_SUCCESS) {
                    prePoint = nextPoint;
                } else {
                    if(status == MoveController.MOVE_THROUGH){
                        status = moveController.moveToPoint(nextPoint);
                        if (status == MoveController.MOVE_SUCCESS){
                            continue;
                        }
                    }

                    Point curPoint = calCurPosition();

                    MoveInfo moveInfo2 = analysisMove(1, 1, prePoint, nextPoint);


                    double angleAdjust = 0;
                    //如果遇到障碍物了，则标记一下,更新地图
                    if (status == MoveController.MOVE_BLOCK) {

                        MoveInfo moveInfo = analysisMove(1, 1, curPoint, nextPoint);

                        double[] funParams = MathUtils.getLineVerticalLine(moveInfo.a(), moveInfo.b(), curPoint);
                        double a = funParams[0];
                        double b = funParams[1];

                        Point p1, p2;
                        int length = 5;
                        if (Math.abs(a) > 1) {
                            //y方向取上下各取2像素

                            double x1 = MathUtils.getFunX(a, b, curPoint.y + length);
                            double x2 = MathUtils.getFunX(a, b, curPoint.y - length);

                            p1 = new Point(x1, curPoint.y + length);
                            p2 = new Point(x2, curPoint.y - length);

                        } else {
                            //x方向左右各取2像素
                            double y1 = MathUtils.getFunY(a, b, curPoint.x + length);
                            double y2 = MathUtils.getFunY(a, b, curPoint.x - length);

                            p1 = new Point(curPoint.x + length, y1);
                            p2 = new Point(curPoint.x - length, y2);
                        }

                        MoveInfo moveInfo1 = analysisMove(1, 1, p1, p2);
                        System.out.println("标记障碍物");
                        recordRoadInfo(roadInfo, moveInfo1, roadInfoWidth, false);
                        RobotUtil.sleep(1000);

                        double needDirect = moveInfo.calDirect();
                        double realDirect = moveInfo2.calDirect();

                        //计算为了躲避障碍物，应该微调的视角幅度
                        angleAdjust = angle(realDirect, needDirect);

//                        if (angleAdjust<0){
//                            // 往逆时针方向 调整一次角度
//
//                        }else {
//                            //顺时针方向调整一次
//
//                        }

                    }
                    //这里尝试调整角度后重试， 先后退，然后把视角调整到姐
                    // Point centerPoint = new Point((prePoint.x + curPoint.x) / 2.0, (prePoint.y + curPoint.y) / 2.0);
                    System.out.println("往回走...");
                    //往反方向走，走到当前位置和上一个点的中心，
                    int moveResult = moveController.moveToPoint(prePoint);

                    if (moveResult != MoveController.MOVE_BLOCK) {

                        if (Math.abs(angleAdjust) > 0) {
                            //往其他方向稍微偏移一点
                            adjustDirect(moveInfo2.calDirect(), angleAdjust);
                            moveController.forward(60);
                        }
                        RobotUtil.sleep(100);
                        Point startPoint = calCurPosition();
                        Point endPoint = smoothPath.getLast();

                        System.out.println("重新规划路径");
                        newPath = generateMovePath(curMapName, startPoint, endPoint, roadInfo, roadInfoWidth, roadInfoHeight, 10);
                        if (newPath == null) {
                            System.out.println("没有找到可行路径");
                        }
                        break;
                    } else {
                        System.out.println("往回走失败");
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            navigateEnd.set(true);
        }


        if (newPath != null) {
            System.out.println("还有路径待进行，开始按照新轨迹移动");
            navigationMove(newPath);
        }
    }

    public void randomMove2( ) {
        randomMoveController.randomMove();
    }
    //随机行走
    public void randomMove(int count) {
        adjustHorView();

        int pitch = bigMap.width();

        ThreadLocalRandom current = ThreadLocalRandom.current();
        double moveDirection = current.nextInt(360);

        //int moveDirection = 10;

        //获取道路文件
        //从指定位置加载地图对应的道路信息
        moveDirection = adjustDirect(moveDirection, 0);

        for (int i = 0; i < count; i++) {
            //大概是100毫秒走1像素

            if (i > 0 && i % 6 == 0) {
                if (checkGameOver()) {
                    break;
                }
                //调整一次方向，随机的

            }
            if (i > 0 && i % 20 == 0) {
                if (checkGameOver()) {
                    break;
                }
                System.out.println("保存一次状态");
                RoadInfoHelper.saveMapRoadInfo(roadInfo, curMapName);
                moveDirection = adjustDirect(moveDirection, current.nextInt(60) - 30);
            }

            int moveTime = 20;

            //todo 如哦缩圈或者小地图变红，   或者在圈边缘有白光闪烁都是降低识别的准确度
            MoveInfo moveInfo = personMove(moveTime, moveDirection, true);
            if (moveInfo == null) {
                continue;
            }


            double averageSpeed = moveInfo.averageSpeed;
            double averageSpeed2 = averageSpeed / Math.cos(Math.toRadians(moveInfo.angle));
            //100毫秒位移:0.2009410038514366,夹角:49.770535503910764
            System.out.println(STR. "100毫秒位移:\{ averageSpeed },夹角:\{ moveInfo.angle() }" );

            if (averageSpeed > 1.9 || averageSpeed2 > 1.9) {
                moveInfo.release();
                System.out.println("移动速度过快，忽略");
                //数据出错了
                continue;
            }


            //如果是直线跑，夹角应该在10以内

            if (moveInfo.angle > 90 && moveInfo.averageSpeed > 0.5) {
                //可以考虑是定位出错了
                System.out.println("定位出错");
                moveInfo.release();
                continue;
            }


            if (moveInfo.angle > 25 && moveInfo.angle < 70 && averageSpeed2 > 0.5 && averageSpeed2 < 1.1) {

                recordRoadInfo(roadInfo, moveInfo, pitch, false);
                System.out.println("遇到障碍物了，可能在贴墙走");
            } else if (averageSpeed < 0.9) {
                //碰撞检测
                double newDirect = barrierDetect(roadInfo, moveDirection, moveInfo);

                if (newDirect == moveDirection) {
                    recordRoadInfo(roadInfo, moveInfo, pitch, true);
                }
                moveDirection = newDirect;
            } else {
                recordRoadInfo(roadInfo, moveInfo, pitch, true);
            }
            moveInfo.release();

        }

        if (!checkGameOver()) {
            RoadInfoHelper.saveMapRoadInfo(roadInfo, curMapName);
        }
    }


    //www
    private double barrierDetect(byte[] roadInfo, double curDirect, MoveInfo lastMoveInfo) {


//        if (lastMoveInfo.angle() > 20 && lastMoveInfo.angle < 45 && lastMoveInfo.averageSpeed < 1.1 && lastMoveInfo.averageSpeed > 0.8) {
//            System.out.println("遇到障碍物了，可能在贴墙走");
//        }

        //todo 已知问题， 红圈过来后 识别率急速下降
        int pitch = bigMap.width();
        int moveTime = 3;

        while (true) {
            MoveInfo moveInfo = personMove(moveTime, curDirect, false);
            if (moveInfo == null) {
                return curDirect;
            }
            Point startPoint = moveInfo.startPoint;
            Point endPoint = moveInfo.endPoint;
            double distance = moveInfo.distance;
            double averageSpeed = (distance / moveTime);
            System.out.println(STR. "障碍物检测:移动速度距离\{ averageSpeed },夹角:\{ moveInfo.angle() }" );

            if (moveInfo.averageSpeed > 1.5) {
                //异常数据
                moveInfo.release();
                continue;
            }


            //double flowDistance = SparseOpticalFlowVisualization.showFlow(moveInfo.startFrame, moveInfo.endFrame);
            moveInfo.release();

            if (averageSpeed < 0.5) {
                recordRoadInfo(roadInfo, moveInfo, pitch, false);
                //todo 如果每次调整都是同一个方向，那么人物可能会被困在一个地方反复碰壁
                curDirect = adjustDirect(curDirect, 30);
            } else if (moveInfo.angle > 80) {
                continue;
            } else {
                if (averageSpeed < 1.9) {
                    recordRoadInfo(roadInfo, moveInfo, pitch, true);
                    RoadInfoHelper.showRoadInfo(roadInfo, (int) startPoint.x, (int) startPoint.y, pitch, 300);
                }
                //视野回调，用来多触发碰撞
                curDirect = adjustDirect(curDirect, -15);
                return curDirect;
            }
        }
    }


    /**
     * @param a         y=ax+b
     * @param b         y=ax+b
     * @param calDirect direct是根据startPoint和endPoint估算出来的方向
     * @param angle     angle是和实际朝向的夹角
     */
    public static record MoveInfo(Point startPoint, Point endPoint, double distance, Mat startFrame, Mat endFrame,
                                  double a, double b, double calDirect, double angle, double averageSpeed) {

        public void release() {
            startFrame.release();
            endFrame.release();
        }

    }

    /**
     * moveTime 移动此时， 单位100毫秒
     */
    public static MoveInfo analysisMove(double moveTime, double realMoveDirect, Point startPoint, Point endPoint) {
        if (startPoint == null || endPoint == null) {
            return null;
        }
        double distance = Math.sqrt(Math.pow(startPoint.x - endPoint.x, 2) + Math.pow(startPoint.y - endPoint.y, 2));

        double[] fc = calYI_YUAN_YI_CI(startPoint, endPoint);

        double baseDegree = 0;
        if (startPoint.x == endPoint.x) {
            baseDegree = startPoint.y > endPoint.y ? 0 : 180;
        } else if (startPoint.x > endPoint.x) {
            baseDegree = 270 + Math.toDegrees(Math.atan(fc[0]));
        } else {
            baseDegree = 90 + Math.toDegrees(Math.atan(fc[0]));
        }
        //当前朝向和实际移动方向之间的夹角
        double angle = Math.abs(baseDegree - realMoveDirect);

        if (angle > 180) {
            angle = 360 - angle;
        }
        double averageSpeed = distance / moveTime;
        return new MoveInfo(startPoint, endPoint, distance, null, null, fc[0], fc[1], baseDegree, angle, averageSpeed);
    }


    private MoveInfo recordMove(int moveTime) {
        Mat mat = captureOnce();
        Point startPoint = calCurPosition(mat);
        RobotUtil.sleep(moveTime);
        Mat mat2 = captureOnce();
        Point endPoint = calCurPosition(mat2);
        if (startPoint == null || endPoint == null) {
            return null;
        }
        double distance = Math.sqrt(Math.pow(startPoint.x - endPoint.x, 2) + Math.pow(startPoint.y - endPoint.y, 2));
        double averageSpeed = distance / moveTime;
        //y=ax+b   f[0]=a  f[1]=b

        double[] fc = calYI_YUAN_YI_CI(startPoint, endPoint);
        mat.release();
        mat2.release();
        return new MoveInfo(startPoint, endPoint, distance, null, null, fc[0], fc[1], 0, 0, averageSpeed);
    }

    private MoveInfo personMove(int moveTime, double realMoveDirect, boolean shift) {
        Mat mat = captureOnce();
        Point startPoint = calCurPosition(mat);

        if (shift) {
            moveController.shiftForward(100 * moveTime);
        } else {
            moveController.forward(100 * moveTime);
        }

        RobotUtil.sleep(50);
        Mat mat2 = captureOnce();
        Point endPoint = calCurPosition(mat2);
        if (startPoint == null || endPoint == null) {
            return null;
        }
        double distance = Math.sqrt(Math.pow(startPoint.x - endPoint.x, 2) + Math.pow(startPoint.y - endPoint.y, 2));

        //y=ax+b   f[0]=a  f[1]=b
        double[] fc = calYI_YUAN_YI_CI(startPoint, endPoint);

        double baseDegree = 0;
        if (startPoint.x == endPoint.x) {
            baseDegree = startPoint.y > endPoint.y ? 0 : 180;
        } else if (startPoint.x > endPoint.x) {
            baseDegree = 270 + Math.toDegrees(Math.atan(fc[0]));
        } else {
            baseDegree = 90 + Math.toDegrees(Math.atan(fc[0]));
        }
        //当前朝向和实际移动方向之间的夹角
        double angle = Math.abs(baseDegree - realMoveDirect);

        if (angle > 180) {
            angle = 360 - angle;
        }
        double averageSpeed = distance / moveTime;
        return new MoveInfo(startPoint, endPoint, distance, mat, mat2, fc[0], fc[1], baseDegree, angle, averageSpeed);
    }


    //调整游戏中时的视野尽可能水平
    public void adjustHorView() {
        try {
            MouseDLL.mouseMove.invoke(0, 4000);
            Thread.sleep(100);
            MouseDLL.mouseMove.invoke(0, -1040);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void yDirect(double angle) {
        MouseDLL.moveYDegree(angle, Config.yAngeleScale);
    }


    public void xDirect(double direction) {
        xDirect(direction, Config.xAngeleScale);
    }


    private double lastDirect = -1;
    private int directAdjustCount = 0;

    public void xDirect(double direction, double angeleScale) {
        //转动视角选择方向
        //可能文字识别失败，所以失败的话，往旁边转动一下

        double curDirect = -1;
        int retryCount =0;
        while (retryCount < 100) {
            retryCount++;
            if (lastDirect != -1) {
                curDirect = lastDirect;
            } else {
                curDirect = getAngleNumber();
            }

            if (curDirect > 360 || curDirect == -1) {
                MouseDLL.moveXDegree(5, angeleScale);
                RobotUtil.sleep(100);
                continue;
            }

            double c = direction - curDirect;

            //最多转动180°
            if (c > 180) {
                c = c - 361;
            }
            if (c < -180) {
                c = c + 361;
            }

            //验证一下识别准确率,通过四次视角转动来实现调整
            double step = c / 4.0;
            double start = curDirect;
            int errorCount = 0;
            double angleNumber = -1;
            for (int i = 0; i < 4; i++) {
                MouseDLL.moveXDegree(step, angeleScale);
                start = updateDirect(start, step);
                RobotUtil.sleep(30);
                angleNumber = getAngleNumber();
                //  System.out.println(STR. "估计朝向:\{ start },识别到的朝向:\{ angleNumber }" );
                if (Math.abs(angle(start, angleNumber)) > 2) {
                    errorCount++;
                }
            }


            //System.out.println(errorCount);
            if (errorCount > 2) {
                //认为这是一次失败的的识别
                MouseDLL.moveXDegree(5, angeleScale);
                RobotUtil.sleep(100);
                lastDirect = -1;
            } else {
                if (Math.abs(start - angleNumber) < 2) {
                    lastDirect = angleNumber;
                    //System.out.println("角度复用");
                } else {
                    System.out.println("偏差过大，重新识别水平角度");
                    lastDirect = -1;
                }
                break;
            }

            // System.out.println(STR. "识别朝向:\{ curDirect },目标朝向:\{ direction },需要转动角度;\{ c }" );
        }
    }


    private double adjustDirect(double old, double offset) {
        double t = old + offset;
        if (t > 360) {
            t = t % 360;
        }
        if (t < 0) {
            t = t + 360;
        }
        xDirect(t);
        return t;
    }

    private double updateDirect(double old, double offset) {
        double t = old + offset;
        if (t > 360) {
            t = t % 360;
        }
        if (t < 0) {
            t = t + 360;
        }
        return t;
    }

    private double angle(double targetAngle, double fromAngle) {
        double c = targetAngle - fromAngle;
        //最多转动180°
        if (c > 180) {
            c = c - 361;
        }
        if (c < -180) {
            c = c + 361;
        }
        return c;
    }


    /**
     * @param useTime 两次截图间隔差 单位毫秒
     */
    //展示位置
    public Point showPositionOnce(Point lastPoint, long useTime) {
        Mat bgrImg = captureOnce();

        Mat minMap = getMinMap(bgrImg);

        //把小图中间区域删除掉
        Rect roi = new Rect(70, 60, 20, 25);
        Imgproc.rectangle(minMap, roi.tl(), roi.br(), new Scalar(0), -1);

        if (lastPoint != null && useTime < 3000) {
            apexSiftMatcher.setPossibleXYAverage(lastPoint.x, lastPoint.y);
        }

        Rect matchRect = BigMapLocation.matchRect(lastPoint, useTime, 0.03);
        apexSiftMatcher.setPossibleXYAverage(-1, -1);

        Point[] result = apexSiftMatcher.match(minMap, bigMatFeature, bigMap, matchRect);
        Point centerPoint = null;
        if (result != null && result.length > 0) {
            Mat output = null;
            try {
                Point point1 = result[0];
                Point point2 = result[2];

                double cx = (result[0].x + result[2].x) / 2.0;
                double cy = (result[0].y + result[2].y) / 2.0;
                centerPoint = new Point(cx, cy);

                System.out.println(STR. "x:\{ cx },y:\{ cy }" );

                if (lastPoint != null) {
                    double d = MathUtils.pointDistance(lastPoint, centerPoint);
                    //偏移量过大
                    if (d > 10) {
                        // Imgcodecs.imwrite(STR. "error/\{ System.currentTimeMillis() }_\{ d }.png" , bgrImg);
                        centerPoint = null;
                    }
                }
                int length = 130;
                Rect rect = new Rect((int) (cx - length), (int) (cy - length), length * 2 + 1, length * 2 + 1);
                output = bigMap.submat(rect).clone();

                Imgproc.circle(output, new Point(length + 1, length + 1), 3, new Scalar(255, 0, 0), 1);

                HighGui.imshow("定位", output);
                HighGui.waitKey(1);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (output != null) {
                    output.release();
                }
            }
        } else {
            System.out.println("没有找到区域");
            //Imgcodecs.imwrite(STR. "error/\{ System.currentTimeMillis() }_no.png" , bgrImg);
        }

        bgrImg.release();
        minMap.release();
        return centerPoint;
    }

    public Point calCurPosition() {
        for (int i = 0; i < 3; i++) {
            Mat bgrImg = captureOnce();
            Point point = calCurPosition(bgrImg);
            bgrImg.release();
            if (point != null) {
                return point;
            }
        }
        return null;
    }


    private long lastCaptureTime = 0;
    private Point lastCenterPoint = null;


    //计算当前位置
    public Point calCurPosition(Mat bgrImg) {
        Mat minMap = getMinMap(bgrImg);

        //把小图中间区域删除掉
        Rect roi = new Rect(70, 60, 20, 25);
        Imgproc.rectangle(minMap, roi.tl(), roi.br(), new Scalar(0), -1);

        long time = System.currentTimeMillis();
        long useTime = time - lastCaptureTime;
        lastCaptureTime = time;
        Rect matchRect = BigMapLocation.matchRect(lastCenterPoint, useTime, 0.03);

        Point[] result = apexSiftMatcher.match(minMap, bigMatFeature, bigMap, matchRect);

        Mat output = null;
        try {
            if (result != null && result.length > 0) {
                Point point1 = result[0];
                Point point2 = result[2];

                //   int cx = (int) Math.round((result[0].x + result[2].x) / 2.0);
                // int cy = (int) Math.round((result[0].y + result[2].y) / 2.0);

                double cx = (result[0].x + result[2].x) / 2.0;
                double cy = (result[0].y + result[2].y) / 2.0;

                Point point = new Point(cx, cy);
                lastCenterPoint = point;
                return point;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                output.release();
            }
            minMap.release();
        }
        return null;
    }


    //准备
    public boolean prepare() {
        System.out.println("准备");

        String status = getRectText(Config.prepareTop, Config.prepareBottom, Config.prepareLeft, Config.prepareRight);
        System.out.println(STR. "游戏状态:\{ status }" );
        int[] rect = apexWindowRect();
        if (rect == null) {
            return false;
        }
        if ("准备".equals(status)) {
            int x = rect[0];
            int y = rect[1];

            RobotUtil.mouseClick(x + Config.prepareLeft, y + Config.prepareTop, Config.DPI_SCALE);
        } else if ("取消".equals(status)) {
            System.out.println("游戏已经是准备状态");
            return true;
        } else {
            System.out.println("识别错误:" + status);
            return false;
        }
        return true;
    }

    //游戏准备后，等待一句游戏开始
    public boolean waitStart() {
        System.out.println("等待连接游戏中...");
        int count = 300;
        //每秒检测一次，总共等待一百次
        for (int i = 0; i < count; i++) {
            String text = getRectText(Config.prepareTop, Config.prepareBottom, Config.prepareLeft, Config.prepareRight);

            if (!"取消".equals(text) && !"准备".equals(text)) {
                return true;
            }

            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    //等待跳伞可用
    public boolean waitLaunchActive() {
        System.out.println("等待跳伞发射可用中...");
        int count = 400;
        //每秒检测一次，总共等待一百次

        for (int i = 0; i < count; i++) {
            boolean success = waitLaunchOnce();
            if (success) {
                return true;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }


    public boolean waitLaunchOnce() {
        String text = getRectText(Config.launchTop, Config.launchBottom, Config.launchLeft, Config.launchRight);

        if ("发射".equals(text)) {
            return true;
        }

        return false;
    }

    //发射
    public void launch() {
        System.out.println("跳伞");

        RobotUtil.sleep(4000 + ThreadLocalRandom.current().nextInt(1000 * 10));

        moveController.launch();

        moveController.forward(1000);
        RobotUtil.mouseRelativeMove(0, 700);
        moveController.forward(1000 * 13);

        //落地后等待一会，万一被摔死了
        RobotUtil.sleep(4000);
    }


    public boolean getBigMat(String mapName) {
        String mapFileName = mapNameMap.get(mapName);

        //如果地图没有变，则复用
        if (mapName.equals(curMapName)) {
            return true;
        }


        //如果地图变了,则释放老地图
        if (curMapName != null) {
            //销毁上传一次的地图
            releaseLastBigMat();
            curMapName = null;
            roadInfo = null;
        }


        //找大地图模板
        Path bigMapTemplate = Path.of("bigMapTemplate", mapFileName);
        String path = bigMapTemplate.toAbsolutePath().toString();
        System.out.println("加载地图路径:" + path);


        boolean exists = Files.exists(bigMapTemplate);
        if (exists) {
            //返回地图
            bigMap = Imgcodecs.imread(path);
            //todo 大地图4000*4000 一次特征提取就得6秒，但是可以把提取出来的存到磁盘上，占用的空间也比地图小
            bigMatFeature = apexSiftMatcher.featureExtreact(bigMap);
            curMapName = mapName;
            roadInfo = RoadInfoHelper.readMapRoadInfo(mapName);
            apexSiftMatcher.changeScaleMat(mapName);
            return true;
        } else {
            //还没有大地图，需要制作
            return false;
        }
    }

    private void releaseLastBigMat() {
        if (bigMap != null) {
            bigMap.release();
            bigMap = null;
        }
        if (bigMatFeature != null) {
            bigMatFeature.release();
            bigMatFeature = null;
        }

    }

    public boolean checkGameOver() {
        String text = getRectText(Config.gameOverRect);
        if ("观战".equals(text)) {
            return true;
        }
        return false;
    }

    //结束游戏，可以是主动退出，也可以是死亡后退出
    public void endGame(boolean force) {
        //结束游戏要保证退回到首页
        boolean gameOver = false;
        //首先等待游戏结束
        if (!force) {
            System.out.println("等待游戏结束中...");
            //等待游戏结束
            while (true) {
                gameOver = checkGameOver();
                if (gameOver) {
                    break;
                } else {
                    RobotUtil.sleep(1000);
                }
            }
        } else {
            gameOver = checkGameOver();
        }

        System.out.println("开始退出游戏...");
        //考虑主动结束，死亡结束， 掉线结束，结束时候卡住等情况
        int[] rect = apexWindowRect();

//如果现在没结束 按esc ， 如果已经结束列 按space
        if (gameOver) {
            RobotUtil.keyPressOnce(KeyEvent.VK_SPACE);
        } else {
            RobotUtil.esc();
        }

        RobotUtil.sleep(1000);
        // String text = getRectText(Config.gameOverTop, Config.gameOverBottom, Config.gameOverLeft, Config.gameOverRight);

        //773  521
        RobotUtil.mouseClick((int) (rect[0] + 773 * resolutionX + 18), (int) (rect[1] + 521 * resolutionY + 18), Config.DPI_SCALE);
        RobotUtil.sleep(1000);
        RobotUtil.mouseClick((int) (rect[0] + 753 * resolutionX + 18), (int) (rect[1] + 581 * resolutionX + 18), Config.DPI_SCALE);

        //等待比赛总结，网络电信
        //这里可能网络卡住
        RobotUtil.sleep(2000);

        boolean ok = false;
        for (int i = 0; i < 30; i++) {
            String rectText = getRectText(Config.gameSummaryRect);
            if ("比赛总结".equals(rectText)) {
                ok = true;
                break;
            }
            RobotUtil.sleep(1000);
        }
        RobotUtil.sleep(1000);
        if (!ok) {
            System.out.println("检测出错");
            //等待重连页面出现,
            //todo 检测文字:找不到群游戏服务器 出现后点击继续

            //todo 点击有复活字样页面的  继续按钮

            //todo 等待

            //todo 重连上后继续结束流程

        }

        for (int i = 0; i < 5; i++) {
            RobotUtil.keyPressOnce(KeyEvent.VK_SPACE);
            RobotUtil.sleep(1000);
        }
    }

    //生成大地图
    public void prepareLargeMat(String mapName) {
        //一共需要提取三次，然后三次的结果合并就是一张大地图

        //因为大地图上可能包含有白色光圈，人物的箭头，航道线，所以通过三种图片合并来消除这些东西

        String fileName = mapNameMap.get(mapName);

        String prefix = fileName.split("\\.")[0];

        int index = 1;
        Path path1 = Paths.get("temp", prefix, "1.png");
        Path path2 = Paths.get("temp", prefix, "2.png");
        Path path3 = Paths.get("temp", prefix, "3.png");

        if (Files.exists(path1)) {
            index++;
        }
        if (Files.exists(path2)) {
            index++;
        }
        if (Files.exists(path3)) {
            //可以合并地图了
            System.out.println("合并地图");
            Path savePath = Paths.get("bigMapTemplate", fileName);
            if (Files.exists(savePath)) {
                index++;  //index等于4
            }
        } else {

            Path savePath = Paths.get("temp", prefix, index + ".png").toAbsolutePath();
            if (!Files.exists(savePath.getParent())) {
                try {
                    Files.createDirectories(savePath.getParent());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                //尝试加载索引
                Path indexPath = Paths.get("temp", prefix, "index.txt").toAbsolutePath();

                int[] hIndex = null;
                int[] vIndex = null;
                if (Files.exists(indexPath)) {
                    int[][] ints = readIndex(indexPath);
                    hIndex = ints[0];
                    vIndex = ints[1];
                }


                GetBigMap.BigMapConcatResult result = GetBigMap.getMapOnce(hIndex, vIndex);


                if (result != null && result.bigMap() != null) {
                    Mat mapOnce = result.bigMap();
                    MatOfInt parameters = new MatOfInt(Imgcodecs.IMWRITE_PNG_COMPRESSION, 0);
                    boolean t = Imgcodecs.imwrite(savePath.toString(), mapOnce, parameters);
                    System.out.println("保存结果" + t);
                    parameters.release();
                    mapOnce.release();


                    //保存索引
                    if (!Files.exists(indexPath)) {
                        writeIndex(indexPath, result.hIndex(), result.vIndex());
                    }
                }


            } catch (Throwable e) {
                e.printStackTrace();
            }

            //保存成功后，判断是否满足合并条件
            // index++;
        }


        if (index == 3) {
            //合并三张大地图
            Path savePath = Paths.get("bigMapTemplate", fileName);

            Mat mat1 = Imgcodecs.imread(path1.toAbsolutePath().toString());
            Mat mat2 = Imgcodecs.imread(path2.toAbsolutePath().toString());
            Mat mat3 = Imgcodecs.imread(path3.toAbsolutePath().toString());

            Mat mat = GetBigMap.concatMap(mat1, mat2, mat3);

            mat1.release();
            mat2.release();
            mat3.release();

            Imgcodecs.imwrite(savePath.toString().toString(), mat);
        }

    }


    private static int[][] readIndex(Path indexPath) {
        int[][] result = new int[2][];
        if (!Files.exists(indexPath)) {
            result[0] = null;
            result[1] = null;
        } else {
            try {
                List<String> lines = Files.readAllLines(indexPath);
                int[] hIndex = Arrays.stream(lines.get(0).split(",")).mapToInt(x -> Integer.valueOf(x)).toArray();
                int[] vIndex = Arrays.stream(lines.get(1).split(",")).mapToInt(x -> Integer.valueOf(x)).toArray();
                result[0] = hIndex;
                result[1] = vIndex;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private static void writeIndex(Path indexPath, int[] hIndex, int[] vIndex) {
        try {
            List<String> lines = new ArrayList<>(2);
            lines.add(Arrays.stream(hIndex).mapToObj(String::valueOf).collect(Collectors.joining(",")));
            lines.add(Arrays.stream(vIndex).mapToObj(String::valueOf).collect(Collectors.joining(",")));
            Files.write(indexPath, lines);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Mat captureOnce() {
        return CaptureDLL.captureOnce();
    }


    public static void saveOnce() {
        Mat mat = captureOnce();
        saveOnce(mat);
        mat.release();
    }

    public static void saveOnce(Mat mat) {
        Imgcodecs.imwrite(STR. "\{ System.currentTimeMillis() }.png" , mat);
    }

    //获取地图名称
    public String findMapName() {
        Mat mat = captureOnce();
        Mat mapNameMat = getMapNameMat(mat);

        String detect = PaddleOcr.detect(mapNameMat);

        mapNameMat.release();
        return detect;
    }


    public String getRectText(int rowStart, int rowEnd, int colStart, int colEnd) {
        Mat mat = captureOnce();
        Mat rectMat = mat.submat(rowStart, rowEnd, colStart, colEnd).clone();
        String text = PaddleOcr.detect(rectMat);
        rectMat.release();
        mat.release();
        return text;
    }

    public String getRectText(Rect rect) {
        Mat mat = captureOnce();
        Mat rectMat = mat.submat(rect).clone();
        String text = PaddleOcr.detect(rectMat);
        rectMat.release();
        mat.release();
        return text;
    }

    public Mat getMapNameMat(Mat mat) {
        return mat.submat(Config.mapNameTop, Config.mapNameBottom, Config.mapNameLeft, Config.mapNameRight).clone();
    }


    //判断是否是准备还是取消
    public Mat getPrepareStatusMat(Mat mat) {
        return mat.submat(Config.prepareTop, Config.prepareBottom, Config.prepareLeft, Config.prepareRight).clone();
    }

    //判断是否可以落地的图片
    public Mat getLaunchMat(Mat mat) {
        return mat.submat(Config.launchTop, Config.launchBottom, Config.launchLeft, Config.launchRight).clone();
    }

    //获取小地图
    public static Mat getMinMap(Mat mat) {
        return mat.submat(Config.minMapTop, Config.minMapBottom, Config.minMapLeft, Config.minMapRight).clone();
    }

    //获取视角方向
    public static Mat getAngle(Mat mat) {
        return mat.submat(Config.angleTop, Config.angleBottom, Config.angleLeft, Config.angleRight).clone();
    }

    public static double getAngleNumber() {
        Mat temp = captureOnce();
        Mat angleImg = getAngle(temp);
        try {
            String angleStr = PaddleOcr.detect(angleImg);
            angleStr = angleStr.replaceAll("\\D", "");
            return Double.parseDouble(angleStr);
        } catch (Exception _) {

        } finally {
            temp.release();
            angleImg.release();
        }
        return -1;
    }
}
