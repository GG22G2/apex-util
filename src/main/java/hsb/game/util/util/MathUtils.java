package hsb.game.util.util;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

import static hsb.game.util.help.RoadInfoHelper.calYI_YUAN_YI_CI;

/**
 * @author 胡帅博
 * @date 2023/10/10 16:18
 */
public class MathUtils {


    //多个点中到斜线的距离最小的那个
    public static Point minClosePointToLine(List<Point> points, double a, double b) {
        Point minClosePoint = null;
        double minDistance = Double.MAX_VALUE;
        for (Point point : points) {
            double x = point.x;
            double y = point.y;
            //点到直线的距离
            double distance = Math.abs(a * x - y + b) / Math.sqrt(a * a + 1);
            if (minDistance > distance) {
                minClosePoint = point;
                minDistance = distance;
            }
        }
        return minClosePoint;
    }

    //多个点中到垂线的距离最小的那个
    public static Point minClosePointToLine(List<Point> points, double x) {
        Point minClosePoint = null;
        double minDistance = Double.MAX_VALUE;
        for (Point point : points) {
            //点到直线的距离
            double distance = Math.abs(point.x - x);
            if (minDistance > distance) {
                minClosePoint = point;
                minDistance = distance;
            }
        }
        return minClosePoint;
    }

    // 点到直线的距离
    public static double pointToLineDistance(Point point, double a, double b) {
        double x = point.x;
        double y = point.y;
        //点到直线的距离
        double distance = Math.abs(a * x - y + b) / Math.sqrt(a * a + 1);
        return distance;
    }

    //颠倒垂直线段的距离
    public static double pointToLineDistance(Point point, double x) {
        return Math.abs(point.x - x);
    }

    public static double pointDistance(Point point1, Point point2) {
        return Math.sqrt(Math.pow(point1.x - point2.x, 2) + Math.pow(point1.y - point2.y, 2));
    }


    /**
     * 获取一条直线的垂线
     * <p>
     * 两条垂直线段的斜率为-1
     */
    public static double[] getLineVerticalLine(double a, double b, Point point) {
        if (Math.abs(a) == Double.MAX_VALUE) {
            //原线段是垂线
            return new double[]{0, point.y};
        } else if (a == 0) {
            //原线段是平行线
            return new double[]{Double.MAX_VALUE, point.x};
        } else {
            //y=a2 * x+b
            //b=y-a2 * x
            double a2 = -1.0 / a;
            double b2 = point.y - a2 * point.x;
            return new double[]{a2, b2};
        }
    }

    //求解一元一次方程，输入x
    public static double getFunY(double a, double b, double x) {
        return a * x + b;
    }

    //求解一元一次方程，输入y
    public static double getFunX(double a, double b, double y) {
        // y = ax+b
        //y-b=ax
        //x = (y-b)/a
        if (Math.abs(a) == Double.MAX_VALUE) {
            //这时候 b就是x轴坐标，做过处理了
            return b;
        } else {
            return (y - b) / a;
        }
    }


    //从start往end的下一个点
    public static Point nextPoint(Point startPoint, Point endPoint, double a, double b, int step) {
        Point nextPoint = null;
        double x, y;
        if (Math.abs(a) > 1) {
            //斜率大于1，到完全垂直的处理
            if (endPoint.y > startPoint.y) {
                // step是正值
                step = Math.abs(step);
            } else {
                step = -Math.abs(step);
            }
            y = endPoint.y + step;
            x = Math.abs(a) == Double.MAX_VALUE ? endPoint.x : ((y - b) / a);
        } else {
            if (endPoint.x > startPoint.x) {
                step = Math.abs(step);
            } else {
                step = -Math.abs(step);
            }
            x = endPoint.x + step;
            y = (a * x + b);
        }
        nextPoint = new Point(Math.round(x), Math.round(y));
        return nextPoint;
    }

    public static List<Point> linePoints(Point startPoint, Point endPoint) {
        double[] fc = calYI_YUAN_YI_CI(startPoint, endPoint);
        return linePoints(startPoint,endPoint,fc[0],fc[1]);
    }


    //计算给定的起点和终点之间的点
    public static List<Point> linePoints(Point startPoint, Point endPoint, double a, double b) {
        List<Point> pointList = new ArrayList<>();
        pointList.add(startPoint);
        if (Math.abs(a) > 1) {
            //斜率大于1，到完全垂直的处理
            int step = 1;
            int count = (int) Math.abs(endPoint.y - startPoint.y)-1;
            double startY;
            if (endPoint.y > startPoint.y) {
                startY = startPoint.y;
            } else {
                startY =  endPoint.y;
            }
            while (count > 0) {
                count--;
                double y = startY + step;
                double x = Math.abs(a) == Double.MAX_VALUE ? endPoint.x : ((y - b) / a);
                pointList.add(new Point(Math.round(x),y));
                startY = y;
            }
        } else {
            int step = 1;
            int count = (int) Math.abs(endPoint.x -  startPoint.x)-1;
            double startX;
            if (endPoint.x > startPoint.x) {
                startX = (int) (startPoint.x);
            } else {
                startX = (int) (endPoint.x);
            }
            while (count > 0) {
                count--;
                double x = startX + step;
                double y = (a * x + b);
                pointList.add(new Point(x,Math.round(y)));
                startX = x;
            }
        }
        pointList.add(endPoint);
        return pointList;
    }


    public record XStatisticss(int count, double averageXb) {

    }

    public static void main(String[] args) {
        List<Double> numbers = List.of(2.0, 2.1, 5., 7., 8., 2., 1.9, 2.01);
        double suitableAverage = findSuitableAverage(numbers, 2);
        System.out.println(suitableAverage);
    }

    //从一组应该相同，但是存在误差的数据中找出最可能正确的值
    public static double findSuitableAverage(List<Double> numbers, double errorLimit) {
        int maxSimilarCount = 0;
        XStatisticss t = null;
        //先找一个合理的均值
        for (int i = 0; i < numbers.size(); i++) {
            Double x = numbers.get(i);
            //这里改成找最集中的区域
            XStatisticss homographyTranslation = findHomographyTranslation(numbers, x, errorLimit);
            if (homographyTranslation.count > maxSimilarCount) {
                maxSimilarCount = homographyTranslation.count;
                t = homographyTranslation;
            }
        }
        return t.averageXb;
    }


    public static XStatisticss findHomographyTranslation(List<Double> numbers, double averageXb, double xLimit) {

        double totalXb = 0;

        int count = 0;
        for (int i = 0; i < numbers.size(); i++) {
            double xb = numbers.get(i);
            //这里改成找最集中的区域
            if (Math.abs(averageXb - xb) < xLimit) {
                totalXb += xb;
                count++;
            }
        }
        if (count > 0) {
            return new XStatisticss(count, totalXb / count);
        } else {
            return new XStatisticss(0, 0);
        }

    }


}
