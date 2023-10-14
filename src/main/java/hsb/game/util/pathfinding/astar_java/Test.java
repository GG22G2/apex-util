package hsb.game.util.pathfinding.astar_java;

import hsb.game.util.util.OpencvUtil;
import hsb.game.util.help.RoadInfoHelper;
import hsb.game.util.help.RoadHelper;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 胡帅博
 * @date 2023/10/14 20:44
 */
public class Test {
    public static void main(String[] args) {
        OpencvUtil.init();
        Astar astar = new Astar();
        /**
         * 	boolean isBlock(int x, int y) {
         * 		return (y >= ROW || x >= COL) || (mapdt[y][x] & 0x1) != 0;
         *        }
         * */
        byte[] bytes = RoadInfoHelper.readMapRoadInfo("世界尽头");

        int row = RoadInfoHelper.roadInfoHeight;
        int col = RoadInfoHelper.roadInfoWidth;

        byte[][] mapdata = new byte[row][col];

        for (int i = 0; i < col; i++) {
            for (int j = 0; j < row; j++) {
                int index = j *col+ i;
                if (bytes[index]==RoadInfoHelper.ALLOW){
                    //可行走
                    mapdata[j][i] = 0;
                }else {
                    //不可行走
                    mapdata[j][i] = 1;
                }
            }
        }


        ArrayList<AstarPosVo> astarPosVos = astar.find(mapdata, row, col, 1502, 556, 1840, 2921, 4500);
      // ArrayList<AstarPosVo> astarPosVos = astar.find(mapdata, row, col, 2882, 2126, 2815, 1940, 1000);
        System.out.println(astarPosVos.size());


//        List<Point> list = astarPosVos.stream().map(x -> new Point(x.x, x.y)).toList();
//        System.out.println(list);
//        List<Point> smoothPath = smoothPath(list);
//        System.out.println(smoothPath.size());
//        GenerateRoad.drawPath(list,RoadInfoHelper.roadInfoWidth,RoadInfoHelper.roadInfoHeight,"t4.png");


        for(int i = 0; i < 100; i++) {
            long startNanos_53_65 = System.nanoTime();
            List<Point> points = RoadHelper.generateMovePath(bytes, col, row, new Point(1502, 556), new Point(1840, 2921), 20, 1);
            long endNanos_53_67 = System.nanoTime();
            System.out.println((endNanos_53_67 - startNanos_53_65) / 1000000.0);
            System.out.println(points.size());

        }
//        List<Point> points = GenerateRoad.generateMovePath(bytes, col, row, new Point(1502, 556), new Point(1840, 2921), 20, 1);
//        System.out.println(points.size());


    }
}
