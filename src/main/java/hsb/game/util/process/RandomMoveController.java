package hsb.game.util.process;

import hsb.game.util.GameProcess;
import hsb.game.util.help.RoadInfoHelper;
import hsb.game.util.util.RobotUtil;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static hsb.game.util.GameProcess.analysisMove;
import static hsb.game.util.process.MoveController.*;

/**
 * @author 胡帅博
 * @date 2023/10/16 12:18
 * <p>
 * 随机移动探图
 * <p>
 * 每一次设定一个A和B点 先从A到B 在从B到A
 * <p>
 * 如果从A走不到B,说明有障碍，如果从B走不到A，说明A和B之间有落差
 * <p>
 * 如果A走到B，接近B但是没有走到，说明A和B之间有阻挡，导致偏移了
 */
public class RandomMoveController {

    MoveController moveController = null;
    GameProcess gameProcess;
    byte[] roadInfo;
    int width = RoadInfoHelper.roadInfoWidth;
    int height = RoadInfoHelper.roadInfoHeight;

    Random random= ThreadLocalRandom.current();

    public RandomMoveController(GameProcess gameProcess,MoveController moveController) {
        this.moveController = moveController;
        this.gameProcess = gameProcess;
    }


    public void randomMove() {
        int xOffset = random.nextInt(80) - 40;
        int yOffset = random.nextInt(80) - 40;


        for(int i = 0; i < 100; i++) {
            Point start = gameProcess.calCurPosition();


            Point end = new Point(start.x+xOffset,start.y+yOffset);

            List<MoveController.HistoryPos> historyPos = new ArrayList<>(300);
            int status = moveToPoint(end,historyPos);

            if(status==MOVE_BLOCK){
                xOffset = random.nextInt(80) - 40;
                yOffset = random.nextInt(80) - 40;
            }

            RobotUtil.sleep(2000);
        }
    }


    public int moveToPoint(Point nextPoint,  List<MoveController.HistoryPos> historyPos) {
        //移动到point
        long curTime = System.currentTimeMillis();
        Point curPoint = gameProcess.calCurPosition();

        //展示当前信息

        historyPos.add(new MoveController.HistoryPos(curTime, curPoint.x, curPoint.y));

        GameProcess.MoveInfo moveInfo = analysisMove(1, 1, curPoint, nextPoint);

        double calDirect = moveInfo.calDirect();

        System.out.println("设置朝向为：" + calDirect);

        //调整方向
        gameProcess.xDirect(calDirect);
        RobotUtil.sleep(50);
        moveController.shiftForward();
        RobotUtil.sleep(5);
        int status = MOVE_SUCCESS;
        while (true) {
            curTime = System.currentTimeMillis();
            Point point = gameProcess.calCurPosition();
            if (point == null) {
                RobotUtil.sleep(50);
                continue;
            }
            historyPos.add(new MoveController.HistoryPos(curTime, point.x, point.y));

            GameProcess.MoveInfo temp = analysisMove(1, 1, point, nextPoint);

            System.out.println(STR. "距离下一个点还有:\{ temp.distance() }" );

            //todo 如果遇到障碍物，那么行进路线就会有一定的偏移,所有需要能够调整角度,但是角度调整不能太早，太早可能会导致撞上其他障碍物


            if (temp.distance() < 1) {
                status = MOVE_SUCCESS;
                break;
            }

            if (moveController.isMoveStop(historyPos, nextPoint,0.06)) {
                System.out.println(STR."可能遇到障碍物了");
                //先往上一个节点走，然后标记这个位置是障碍物，然后重新生成路径
                status = MOVE_BLOCK;
                break;
            }

            if (moveController.isMoveGood(historyPos, nextPoint, 0.3)) {
                System.out.println(STR."可能走偏了");
                status = MOVE_THROUGH;
                break;
            }
            RobotUtil.sleep(10);
        }
        moveController.stop();
        return status;
    }






}
