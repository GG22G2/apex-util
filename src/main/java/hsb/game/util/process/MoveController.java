package hsb.game.util.process;

import hsb.game.util.GameProcess;
import hsb.game.util.util.MathUtils;
import hsb.game.util.util.RobotUtil;
import org.opencv.core.Point;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static hsb.game.util.GameProcess.analysisMove;

/**
 * @author 胡帅博
 * @date 2023/10/12 21:37
 */
public class MoveController {

    GameProcess gameProcess;

    public MoveController() {

    }

    public MoveController(GameProcess gameProcess) {
        this.gameProcess = gameProcess;
    }

    public static void main(String[] args) {
        MoveController controller = new MoveController();

        controller.shiftForward();
        RobotUtil.sleep(3000);
        controller.stop();
    }

    boolean shift = false;

    boolean pressW = false;

    //一直加速走
    public void shiftForward() {
        shift = true;
        RobotUtil.keyPress(KeyEvent.VK_SHIFT);
        RobotUtil.sleep(1);
        forward();
    }

    //加速走time毫秒
    public void shiftForward(int time) {
        shiftForward();
        RobotUtil.sleep(time);
        stop();
    }

    //向前一直走
    public void forward() {
        //System.out.println("前进");
        pressW = true;
        RobotUtil.keyPress(KeyEvent.VK_W);
        RobotUtil.sleep(1);
    }

    //向前走time毫秒
    public void forward(int time) {
        //System.out.println("前进");
        RobotUtil.keyLongPressOnce(KeyEvent.VK_W, time);
    }

    //停止移动
    public void stop() {
        //System.out.println("停止");
        if (pressW) {
            RobotUtil.keyRelease(KeyEvent.VK_W);
        }
        if (shift) {
            RobotUtil.keyRelease(KeyEvent.VK_SHIFT);
        }
        pressW = shift = false;
        RobotUtil.sleep(10);
    }

    //跳伞
    public void launch() {
        RobotUtil.keyLongPressOnce(KeyEvent.VK_E, 500);
    }


    public static record HistoryPos(long time, double x, double y) {
    }

    public final static int MOVE_BLOCK = 0;  //遇到障碍物
    public final static int MOVE_SUCCESS = 1; //到达目的地

    public final static int MOVE_THROUGH = 2;  //走过了，越走越远

    public int moveToPoint(Point nextPoint) {
        List<HistoryPos> historyPos = new ArrayList<>(300);

        //移动到point
        long curTime = System.currentTimeMillis();
        Point curPoint = gameProcess.calCurPosition();

        //展示当前信息

        historyPos.add(new HistoryPos(curTime, curPoint.x, curPoint.y));

        GameProcess.MoveInfo moveInfo = analysisMove(1, 1, curPoint, nextPoint);


        double distance = moveInfo.distance();

        double calDirect = moveInfo.calDirect();

        System.out.println("设置朝向为：" + calDirect);

        //调整方向
        gameProcess.xDirect(calDirect);
        RobotUtil.sleep(50);
        shiftForward();
//        if (distance > 6) {
//            shiftForward();
//        } else {
//            forward();
//        }

        while (true) {
            curTime = System.currentTimeMillis();
            Point point = gameProcess.calCurPosition();
            if (point == null) {
                RobotUtil.sleep(50);
                continue;
            }
            historyPos.add(new HistoryPos(curTime, point.x, point.y));

            GameProcess.MoveInfo temp = analysisMove(1, 1, point, nextPoint);

            //System.out.println(STR. "距离下一个点还有:\{ temp.distance }" );

            if (temp.distance() < 4) {
                stop();
                //走慢一点
                if (slowMove(nextPoint, historyPos)) {
                    break;
                }else {
                    return MOVE_BLOCK;
                }

            }

            if (isMoveStop(historyPos, nextPoint)) {
                System.out.println(STR."可能遇到障碍物了");
                //先往上一个节点走，然后标记这个位置是障碍物，然后重新生成路径
                return MOVE_BLOCK;
            }

            if (isMoveGood(historyPos, nextPoint, 0.5)) {
                System.out.println(STR."可能走过了");
                return MOVE_THROUGH;
            }


            RobotUtil.sleep(200);
        }

        stop();
        historyPos.clear();
        return MOVE_SUCCESS;
    }


    public boolean slowMove(Point endPoint, List<HistoryPos> historyPos) {
        long curTime = System.currentTimeMillis();
        Point point = gameProcess.calCurPosition();
        if (point == null) {
            forward(20);
            point = gameProcess.calCurPosition();
        }
        if (point==null){
            return false;
        }
        historyPos.add(new HistoryPos(curTime, point.x, point.y));
        double v = MathUtils.pointDistance(endPoint, point);

        double v1 = v * 60;
        forward((int) v1);
        while (true) {
            curTime = System.currentTimeMillis();
            point = gameProcess.calCurPosition();

            historyPos.add(new HistoryPos(curTime, point.x, point.y));
            v = MathUtils.pointDistance(endPoint, point);
            if (v < 2) {
                System.out.println(STR. "接近点\{ endPoint }，剩余距离：\{ v }" );
                return true;
            }

            if (isMoveStop(historyPos,endPoint)){
                System.out.println(STR. "点\{ point }，可能是障碍物" );
                return false;
            }

            if (isMoveGood(historyPos, endPoint, 0.3)) {
                //走反了
                return false;
            }

            forward(100);
            System.out.println(STR. "慢速移动，距离：\{ v }" );
        }
    }


    //判断是不是走过来
    private boolean isMoveGood(List<HistoryPos> historyPosList, Point targetPoint, double t) {
        int errorCount = 0;
        int start = Math.max(0, historyPosList.size() - 4);
        for (int i = start; i < historyPosList.size() - 1; i++) {
            HistoryPos historyPos = historyPosList.get(i);
            HistoryPos historyPos2 = historyPosList.get(i + 1);
            double v = MathUtils.pointDistance(new Point(historyPos.x(), historyPos.y()), targetPoint);
            double v2 = MathUtils.pointDistance(new Point(historyPos2.x(), historyPos2.y()), targetPoint);
            if (v2 >= v && Math.abs(v2 - v) > t) {
                errorCount++;
            }
        }

        return errorCount > 2;
    }

    private boolean isMoveStop(List<HistoryPos> historyPosList, Point targetPoint) {
        int errorCount = 0;
        int start = Math.max(0, historyPosList.size() - 5);
        for (int i = start; i < historyPosList.size() - 1; i++) {
            HistoryPos historyPos = historyPosList.get(i);
            HistoryPos historyPos2 = historyPosList.get(i + 1);
            double v = MathUtils.pointDistance(new Point(historyPos.x(), historyPos.y()), targetPoint);
            double v2 = MathUtils.pointDistance(new Point(historyPos2.x(), historyPos2.y()), targetPoint);
            if (Math.abs(v2 - v) < 0.3) {
                errorCount++;
            }
        }
        return errorCount > 3;
    }

}
