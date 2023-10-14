package hsb.game.util.util;

import org.opencv.core.Point;

import java.util.List;

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
