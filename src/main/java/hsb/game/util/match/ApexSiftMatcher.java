package hsb.game.util.match;

import hsb.game.util.util.OpencvUtil;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.Feature2D;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.SIFT;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;

/**
 * @author 胡帅博
 * @date 2023/7/21 16:53
 * <p>
 * <p>
 * 图片 匹配 SIFT
 * <p>
 * 您的目的是找到一种通用的方法,在大图中检测小图,而不需要训练过程。这里我给出一些建议:
 * <p>
 * 利用模板匹配法 - 将小图作为模板,在大图上滑动,计算相似度,找到最匹配的区域。
 * 基于互信息的匹配 - 计算小图和大图窗口间的互信息,相似区域互信息高。
 * 平移不变特征匹配 - 使用SIFT等提取特征,找到两图共同的特征点集,从而匹配区域。
 * 相对特征匹配 - 不仅匹配特征点,还匹配两点间的相对位置关系,加强匹配的准确性。
 * 形状配准法 - 利用小图的轮廓信息,配准大图中相似形状的区域。
 * 影响区域生成 - 从小图生成影响区域,作为权重模板进行大图匹配。
 * 多尺度匹配 - 考虑小图与大图间尺度变化,在不同尺度进行特征提取和匹配。
 * 这些方法都不需要训练,能够直接针对输入的小图和大图进行匹配,避免了训练过程。可以根据实际需求选择合适的方法或结合使用。关键是设计出能够表征小图内容的显著特征。
 */
public class ApexSiftMatcher {


    Feature2D bigMapDetector = null;

    Feature2D minMapDetector = null;

    BFMatcher matcher = null;


    boolean knnFilter = true;

    boolean showResult = false;

    int statisticLimit = 500;
    //记录小图和大图的历史配对,这里只保留和均值误差小的

    double xScale = 0;
    double yScale = 0;

    int statisticCount = 0;

    //根据minHistoryPoints和bigHistoryPoints统计缩放矩阵，统计出来后，后续只需要计算平移
    // double[] scaleMat = null;
    double[] scaleMat = null;
    // double[] scaleMat = new double[]{4.214561089684368, 0.0, 0, 0.0, 4.2162836697811485, 0, 0.0, 0.0, 1.0};   //世界尽头
    //double[] scaleMat = new double[]{4.200286223577545, 0.0, 0.0, 0.0, 4.205595444867763, 0.0, 0.0, 0.0, 1.0}; //奥林匹斯
    //double[] scaleMat = new double[]{4.603066208792305, 0.0, 0, 0.0, 4.609888440774374, 0, 0.0, 0.0, 1.0}; //诸王峡谷

    static {
        OpencvUtil.init();
    }

    private final static Map<String, double[]> scaleMap = Map.of(
            "诸王峡谷", new double[]{4.603066208792305, 0.0, 0, 0.0, 4.609888440774374, 0, 0.0, 0.0, 1.0}
            , "世界尽头", new double[]{4.214561089684368, 0.0, 0, 0.0, 4.2162836697811485, 0, 0.0, 0.0, 1.0}
            , "奥林匹斯", new double[]{4.200286223577545, 0.0, 0.0, 0.0, 4.205595444867763, 0.0, 0.0, 0.0, 1.0}
    );

    public void changeScaleMat(String mapName) {
        scaleMat = scaleMap.get(mapName);
    }


    public ApexSiftMatcher() {
        // SIFT检测和描述
        // detector = SIFT.create(0, 3, 0.03, 5);

        // bigMapDetector = SIFT.create(0, 5, 0.02, 3, 2.0);
        //  minMapDetector = bigMapDetector;

        //大地图的特征提取
        bigMapDetector = SIFT.create(0, 2, 0.02, 3, 2);

        //小地图的特征提取  小地图大概是大地图缩放4-5倍
        minMapDetector = SIFT.create(0, 4, 0.02, 3, 2);


        //监测点的匹配
        matcher = new BFMatcher();
    }


    public static class DetectedFeature {
        public MatOfKeyPoint imgPoints = null;
        public Mat imgDescriptors = null;

        public DetectedFeature(MatOfKeyPoint imgPoints, Mat imgDescriptors) {
            this.imgPoints = imgPoints;
            this.imgDescriptors = imgDescriptors;
        }


        public void release() {
            imgPoints.release();
            imgDescriptors.release();
        }

    }


    public Point[] match(Mat minImg, Mat largeImg) {
        DetectedFeature minImgFeature = featureExtreact(minImg, minMapDetector);
        Point[] point = match(minImgFeature, minImg, largeImg, null);
        minImgFeature.release();
        return point;
    }

    public void showKeyPoints(Mat img) {
        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        bigMapDetector.detect(img, keyPoints);
        KeyPoint[] list = keyPoints.toArray();
        for (KeyPoint keyPoint : list) {
            Point pt = keyPoint.pt;
            Imgproc.circle(img, pt, 1, new Scalar(0, 255, 0), 1);
        }
        HighGui.imshow("特征点结果", img);
        HighGui.waitKey(1);
    }


    public DetectedFeature featureExtreact(Mat img) {
        return featureExtreact(img, bigMapDetector);
    }

    public DetectedFeature featureExtreact(Mat img, Feature2D detector) {
        Mat mask = new Mat();
        MatOfKeyPoint minImgPoints = new MatOfKeyPoint();
        Mat ImgDescriptors = new Mat();
        detector.detectAndCompute(img, mask, minImgPoints, ImgDescriptors);
        mask.release();
        return new DetectedFeature(minImgPoints, ImgDescriptors);
    }

    /**
     * 如果是做找图之类的功能，那么小图应该提前英国特侦提取步骤，所有小图接受特征作为入参
     */
    public Point[] match(Mat minImg, DetectedFeature largeImgFeature, Mat largeImg, Rect mask) {
        long startNanos_8_151 = System.nanoTime();
        DetectedFeature minImgFeature = featureExtreact(minImg, minMapDetector);
        long endNanos_8_153 = System.nanoTime();
       // System.out.println((endNanos_8_153 - startNanos_8_151) / 1000000.0);
        Point[] result = match(minImgFeature, minImg, largeImgFeature, largeImg, mask);
        minImgFeature.release();
        return result;
    }


    /**
     * 如果是做找图之类的功能，那么小图应该提前英国特侦提取步骤，所有小图接受特征作为入参
     */
    public Point[] match(DetectedFeature minImgFeature, Mat minImg, Mat largeImg, Rect mask) {
        DetectedFeature largeImgFeature = featureExtreact(largeImg);
        Point[] result = match(minImgFeature, minImg, largeImgFeature, largeImg, mask);
        largeImgFeature.release();
        return result;

    }


    float[] bigDescriptorsArr;

    public void initBigMap(DetectedFeature largeImgFeature) {
        Mat largeImgDescriptors = largeImgFeature.imgDescriptors;
        bigDescriptorsArr = new float[largeImgDescriptors.width() * largeImgDescriptors.height() * largeImgDescriptors.channels()];
        largeImgDescriptors.get(0, 0, bigDescriptorsArr);
    }

    double possibleXAverage = -1;
    double possibleYAverage = -1;

    public void setPossibleXYAverage(double x, double y) {
        possibleXAverage = x;
        possibleYAverage = y;
    }

    public Point[] match(DetectedFeature minImgFeature, Mat minImg, DetectedFeature largeImgFeature, Mat largeImg, Rect mask) {


        MatOfKeyPoint minImgPoints = minImgFeature.imgPoints;
        Mat minImgDescriptors = minImgFeature.imgDescriptors;

        //提取特征

        MatOfKeyPoint largeImgPoints = largeImgFeature.imgPoints;
        Mat largeImgDescriptors = largeImgFeature.imgDescriptors;

        List<KeyPoint> keypoints1List = minImgPoints.toList();
        List<KeyPoint> keypoints2List = largeImgPoints.toList();
        boolean needReleaseDescriptors = false;
        if (mask != null && bigDescriptorsArr != null) {
            //指定区域匹配，重新构造 largeImgDescriptors 和 keypoints2List
            int left = mask.x;
            int right = mask.x + mask.width;
            int top = mask.y;
            int bottom = mask.y + mask.height;

            List<KeyPoint> keypoints2List2 = new ArrayList<>(keypoints2List.size());
            int[] keyPointIndex = new int[keypoints2List.size()];
            int keyPointIndexPos = 0;
            for (int i = 0; i < keypoints2List.size(); i++) {
                KeyPoint keyPoint = keypoints2List.get(i);
                Point point = keyPoint.pt;
                if (point.x >= left && point.x <= right && point.y >= top && point.y <= bottom) {
                    keypoints2List2.add(keyPoint);
                    keyPointIndex[keyPointIndexPos++] = i;
                }
            }

            float[] t2 = new float[largeImgDescriptors.width() * keyPointIndexPos * largeImgDescriptors.channels()];
            Mat largeImgDescriptors2 = new Mat(keyPointIndexPos, 128, CvType.CV_32FC1);

            for (int i = 0; i < keyPointIndexPos; i++) {
                int index = keyPointIndex[i] * 128;
                System.arraycopy(bigDescriptorsArr, index, t2, i * 128, 128);
            }
            largeImgDescriptors2.put(0, 0, t2);

            keypoints2List = keypoints2List2;
            largeImgDescriptors = largeImgDescriptors2;
            needReleaseDescriptors = true;
        }


        List<DMatch> goodMatches = new ArrayList<>();
        if (knnFilter) {
            List<MatOfDMatch> matches = new ArrayList<>();
            //k=2的参考是假设第一个是正确值，第二个是错误值，这样第一个的distance应该明显低于第二个。不然的话这这一组匹配的置信度偏低，
            //这里要结合下面的0.75过滤条件

            matcher.knnMatch(minImgDescriptors, largeImgDescriptors, matches, 2);

            for (int i = 0; i < matches.size(); i++) {
                MatOfDMatch matOfDMatch = matches.get(i);
                if (matches.get(i).rows() > 1) {
                    DMatch[] dmatches = matOfDMatch.toArray();
                    if (dmatches[0].distance < 0.75 * dmatches[1].distance) {
                        goodMatches.add(dmatches[0]);
                    }
                }
                matOfDMatch.release();
            }
            //点不够做仿射变换，那就按照最小的距离点，按照矩阵映射来做
            goodMatches.sort((t1, t2) -> Float.compare(t1.distance, t2.distance));
            Set<Integer> queryIds = new HashSet<Integer>(goodMatches.size());
            Set<Integer> trainIds = new HashSet<Integer>(goodMatches.size());


            List<DMatch> goodMatches2 = new ArrayList<>();
            for (DMatch goodMatch : goodMatches) {
                int queryId = goodMatch.queryIdx;
                int trainId = goodMatch.trainIdx;
                if (!queryIds.contains(queryId) && !trainIds.contains(trainId)) {
                    goodMatches2.add(goodMatch);
                    queryIds.add(queryId);
                    trainIds.add(trainId);
                }
            }
            goodMatches = goodMatches2;

        } else {
            MatOfDMatch matches = new MatOfDMatch();
            matcher.match(minImgDescriptors, largeImgDescriptors, matches);
            goodMatches = matches.toList();
            matches.release();
        }


        List<Point> points1 = new ArrayList<>();
        List<Point> points2 = new ArrayList<>();
        for (DMatch m : goodMatches) {
            points1.add(keypoints1List.get(m.queryIdx).pt);
            points2.add(keypoints2List.get(m.trainIdx).pt);
        }
        MatOfPoint2f points1Mat = new MatOfPoint2f();
        points1Mat.fromList(points1);

        MatOfPoint2f points2Mat = new MatOfPoint2f();
        points2Mat.fromList(points2);

        int size = goodMatches.size();

        Point[] result = null;

        //因为只有平移和缩放，所以可以按照一元一次方程来分别却x方向的缩放，平移系数， y方向的缩放，平移系数
        Mat homography = null;
        //todo 这个矩阵变换中 缩放是固定的，可以保存起来，这样后续即使只有一个配对点也可以计算出 平移系数

        if (scaleMat != null && size > 0) {
            double[] homography1 = findHomography(points1, points2);
            homography = new Mat(3, 3, CvType.CV_64FC1);
            homography.put(0, 0, homography1);

        } else if (size >= 4) {
            findHomography(points1, points2);
            //根据筛选出来的配对点，调用findHomography方法确定最优的仿射变换
            homography = Calib3d.findHomography(points1Mat, points2Mat, Calib3d.USAC_MAGSAC, 1);
        }


        if (homography != null && !homography.empty()) {
            if (Math.abs(Core.determinant(homography)) > 1e-6) {
                MatOfPoint2f borderPoints = new MatOfPoint2f(new Point(0, 0)
                        , new Point(minImg.width(), 0)
                        , new Point(minImg.width(), minImg.height())
                        , new Point(0, minImg.height()));
                MatOfPoint2f mappedPoints = new MatOfPoint2f();
                // 使用单应矩阵映射点
                Core.perspectiveTransform(borderPoints, mappedPoints, homography);
                result = mappedPoints.toArray();
                homography.release();
                borderPoints.release();
            }
        } else {
           // System.out.println(size);
        }


        if (result != null && showResult) {
            // 在img2上画出映射后的点
            Mat output = largeImg.clone();
            MatOfPoint matOfPoint = new MatOfPoint(result);
            List<MatOfPoint> matOfPoints = List.of(matOfPoint);
            Imgproc.polylines(output, matOfPoints, true, new Scalar(0, 255, 0));

            for (Point point : result) {
                Imgproc.circle(output, point, 1, new Scalar(0, 255, 0), 1);
            }

            // 显示结果
            Imgcodecs.imwrite("output.jpg", output);

            // 绘制优化后的匹配结果
            Mat matched = new Mat();
            MatOfDMatch goodMatchMat = new MatOfDMatch();
            goodMatchMat.fromList(goodMatches);
            Features2d.drawMatches(minImg, minImgPoints, largeImg, largeImgPoints, goodMatchMat, matched);

            Imgcodecs.imwrite("matched.jpg", matched);

            output.release();
            matOfPoint.release();
            matched.release();
            goodMatchMat.release();

        }

        if (needReleaseDescriptors) {
            largeImgDescriptors.release();
        }
        //释放内存
        // largeImgFeature.release();
        points1Mat.release();
        points2Mat.release();


        return result;
    }


    public void drawResult() {

    }


    public double[] findHomography(List<Point> minPoint, List<Point> bigPoint) {
        if (scaleMat != null) {
            return findHomographyTranslation(minPoint, bigPoint);
        }

        double[] homographyY = findHomographyY(minPoint, bigPoint);
        double[] homographyX = findHomographyX(minPoint, bigPoint);

        double[] result = new double[9];

        result[0] = homographyX[0];
        result[2] = homographyX[1];
        result[4] = homographyY[0];
        result[5] = homographyY[1];
        result[8] = 1;


        xScale += homographyX[0];
        yScale += homographyY[0];
        statisticCount++;

        //System.out.println(statisticCount);
        //然后这里处理均值
        if (statisticCount > statisticLimit) {
            double v = xScale / statisticCount;
            double v1 = yScale / statisticCount;
            scaleMat = new double[]{v, 0, 0, 0, v1, 0, 0, 0, 1};
        }
        return result;
    }

    public double[] findHomographyY(List<Point> minPoint, List<Point> bigPoint) {
        double aSum = 0, bSum = 0;
        int count = 0;
        for (int i = 0; i < minPoint.size() - 1; i++) {
            double x1 = minPoint.get(i).y;
            double y1 = bigPoint.get(i).y;

            double x2 = minPoint.get(i + 1).y;
            double y2 = bigPoint.get(i + 1).y;

            double a = (y2 - y1) / (x2 - x1);
            double b = y2 - a * x2;

            if (a > 0 && b >= 0) {
                if (a > 4.0 && a < 5) {
                    count++;
                    aSum += a;
                    bSum += b;
                }

            }
        }
        return new double[]{aSum / count, bSum / count};
    }


    //计算点的仿射变换，假设只有平移和缩放
    public double[] findHomographyX(List<Point> minPoint, List<Point> bigPoint) {
        double aSum = 0, bSum = 0;
        int count = 0;
        for (int i = 0; i < minPoint.size() - 1; i++) {
            double x1 = minPoint.get(i).x;
            double y1 = bigPoint.get(i).x;

            double x2 = minPoint.get(i + 1).x;
            double y2 = bigPoint.get(i + 1).x;

            double a = (y2 - y1) / (x2 - x1);
            double b = y2 - a * x2;
            if (a > 0 && b >= 0 && a > 4.0 && a < 5.0) {
                count++;
                aSum += a;
                bSum += b;
            }
        }
        return new double[]{aSum / count, bSum / count};
    }


    //平移变换
    public double[] findHomographyTranslation(List<Point> minPoint, List<Point> bigPoint) {
        XYTrans t0 = null;
        XYTrans average = null;
        //对于连续移动场景，possibleXAverage和possibleYAverage是上一次识别的均值，虽然和这一次的均值会有误差，但是一般都在几十这个范围内
        if (possibleXAverage != -1 && possibleYAverage != -1) {
            //这里设只100一般就是假设10秒内的移动幅度
            average = findHomographyTranslation(minPoint, bigPoint, possibleXAverage, possibleYAverage, 80, 80);
        } else {
            // 不然的话就从这组数据中求均值
            average = findSuitableAverage(minPoint, bigPoint);
        }

        t0 = findHomographyTranslation(minPoint, bigPoint, average.averageXb, average.averageYb, 40, 40);
        //System.out.println(STR. "均值:\{ t5.averageXb },\{ t5.averageYb }" );
        double[] result = new double[]{scaleMat[0], 0, t0.averageXb, 0, scaleMat[4], t0.averageYb, 0, 0, 1};
        //删除误差点
        return result;
    }

    public record XYTrans(double totalXb, double totalYb, int count, double averageXb, double averageYb) {

    }

    public XYTrans findHomographyTranslation(List<Point> minPoint, List<Point> bigPoint, double averageXb, double averageYb, double xLimit, double yLimit) {


        double xa = scaleMat[0];
        double ya = scaleMat[4];
        double totalXb = 0;
        double totalYb = 0;
        int count = 0;
        for (int i = 0; i < minPoint.size(); i++) {
            double x1 = minPoint.get(i).x;
            double x2 = bigPoint.get(i).x;
            double xb = x2 - xa * x1;  //x轴平移量
            double y1 = minPoint.get(i).y;
            double y2 = bigPoint.get(i).y;
            double yb = y2 - ya * y1;  //x轴平移量

            //这里改成找最集中的区域

            if (Math.abs(averageXb - xb) < xLimit && Math.abs(averageYb - yb) < yLimit) {
                totalXb += xb;
                totalYb += yb;
                count++;
            }
            // System.out.println(STR. "\{ xb },\{ yb }" );
        }
        if (count > 0) {
            return new XYTrans(totalXb, totalYb, count, totalXb / count, totalYb / count);
        } else {
            return new XYTrans(0, 0, 0, 0, 0);
        }

    }


    public XYTrans findSuitableAverage(List<Point> minPoint, List<Point> bigPoint) {
        //因为识别是连续的，两次失败间，可能移动间距离一般都是几十以内，所以可以直接拿上一次x和y来使用
        //todo 如果点对过少，可可能不好发现
        double xa = scaleMat[0];
        double ya = scaleMat[4];

        int maxSimilarCount = 0;

        XYTrans t = null;
        //先找一个合理的均值
        for (int i = 0; i < minPoint.size(); i++) {
            double x1 = minPoint.get(i).x;
            double x2 = bigPoint.get(i).x;
            double xb = x2 - xa * x1;  //x轴平移量
            double y1 = minPoint.get(i).y;
            double y2 = bigPoint.get(i).y;
            double yb = y2 - ya * y1;  //x轴平移量

            //这里改成找最集中的区域
            XYTrans homographyTranslation = findHomographyTranslation(minPoint, bigPoint, xb, yb, 20, 20);
            if (homographyTranslation.count > maxSimilarCount) {
                maxSimilarCount = homographyTranslation.count;
                t = homographyTranslation;
            }
        }
        return t;
    }

}