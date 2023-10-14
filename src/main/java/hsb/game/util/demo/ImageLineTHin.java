package hsb.game.util.demo;

/**
 * @author 胡帅博
 * @date 2023/10/10 11:50
 */
public class ImageLineTHin {

   // private final int backGround =

    /**
     * 单通道  传递过来的图片，0是前景点  -1(255)是背景
     */
    public static void ZhangSuen_Thin(byte[] src, int width, int height) {

        int step = width;
        boolean ifEnd = false;
        int p1, p2, p3, p4, p5, p6, p7, p8; //八邻域
        int[] flag = new int[src.length]; //用于标记待删除的点
        int flagLimit = 0;


        int heightLimit = height - 1;
        int widthLimit = width - 1;
        int cishu=0;
        while (cishu<30) {
            cishu++;
            for (int i = 1; i < heightLimit; ++i) {
                for (int j = 1, rowStartIndex = i * step; j < widthLimit; ++j) {
                    int curIndex = rowStartIndex + j;
                    int p = src[curIndex];
                    if (p == -1) continue; //如果不是前景点,跳过

                    p7 = src[curIndex - 1];
                    p3 = src[curIndex + 1];

                    /***
                     *
                     * p8  p1  p2
                     * p7  p0  p3
                     * p6  p5  p4
                     *
                     */
                    int curIndex0 = curIndex - step;
                    p8 = src[curIndex0 - 1];
                    p1 = src[curIndex0];
                    p2 = src[curIndex0 + 1];


                    //判断八邻域像素点的值(要考虑边界的情况),若为前景点(白色255),则为1;反之为0
                    int curIndex2 = curIndex + step;
                    p6 = src[curIndex2 - 1];
                    p5 = src[curIndex2];
                    p4 = src[curIndex2 + 1];


                    //前景像素点的个数要大于等于2小于等于6  也就是说背景点个数大于等于2而且小于等于6
                    if ((p1 + p2 + p3 + p4 + p5 + p6 + p7 + p8) <= -2 && (p1 + p2 + p3 + p4 + p5 + p6 + p7 + p8) >= -6) //条件1
                    {
                        //条件2的计数
                        int count = 0;

                        //判断是否是 背景->前景
                        if (p1 == -1 && p2 == 0) ++count;
                        if (p2 == -1 && p3 == 0) ++count;
                        if (p3 == -1 && p4 == 0) ++count;
                        if (p4 == -1 && p5 == 0) ++count;
                        if (p5 == -1 && p6 == 0) ++count;
                        if (p6 == -1 && p7 == 0) ++count;
                        if (p7 == -1 && p8 == 0) ++count;
                        if (p8 == -1 && p1 == 0) ++count;

                        //这样写虽然减少了if分支，但是经测试，速度反而慢了
                        //int count2 = (p1 - p2) & 0x02 + (p2 - p3) & 0x02 + (p3 - p4) & 0x02 + (p4 - p5) & 0x02 + (p5 - p6) & 0x02 + (p6 - p7) & 0x02 + (p7 - p8) & 0x02 + (p8 - p1) & 0x02;

                        //p1 p3 p5中存在背景点  及值为-1的点
                        if (count == 1 && (p1 | p3 | p5) == -1 && (p3 | p5 | p7) == -1) { //条件2、3、4
                            flag[flagLimit++] = curIndex;//将当前像素添加到待删除数组中
                        }
                    }
                }
            }


            ifEnd = flagLimit > 0 ? true : false;

            //将标记的点删除
            for (int j = 0; j < flagLimit; j++) {
                src[flag[j]] = -1;
            }
            flagLimit = 0;  //清空待删除数组

            for (int i = 1; i < heightLimit; ++i) {
                for (int j = 1; j < widthLimit; ++j) {

                    int curIndex = i * step + j;
                    int p = src[curIndex];
                    if (p == -1) continue; //如果不是前景点,跳过

                    p7 = src[curIndex - 1];
                    p3 = src[curIndex + 1];

                    int curIndex0 = curIndex - step;
                    p8 = src[curIndex0 - 1];
                    p1 = src[curIndex0];
                    p2 = src[curIndex0 + 1];

                    //判断八邻域像素点的值(要考虑边界的情况),若为前景点(白色255),则为1;反之为0
                    int curIndex2 = curIndex + step;
                    p6 = src[curIndex2 - 1];
                    p5 = src[curIndex2];
                    p4 = src[curIndex2 + 1];


                    if ((p1 + p2 + p3 + p4 + p5 + p6 + p7 + p8) <= -2 && (p1 + p2 + p3 + p4 + p5 + p6 + p7 + p8) >= -6) {
                        int count = 0;
                        if (p1 == -1 && p2 == 0) ++count;
                        if (p2 == -1 && p3 == 0) ++count;
                        if (p3 == -1 && p4 == 0) ++count;
                        if (p4 == -1 && p5 == 0) ++count;
                        if (p5 == -1 && p6 == 0) ++count;
                        if (p6 == -1 && p7 == 0) ++count;
                        if (p7 == -1 && p8 == 0) ++count;
                        if (p8 == -1 && p1 == 0) ++count;

                        if (count == 1 && (p1 | p3 | p7) == -1 && (p1 | p5 | p7) == -1) {
                            flag[flagLimit++] = curIndex;//将当前像素添加到待删除数组中
                        }
                    }
                }
            }
            //将标记的点删除

            ifEnd = flagLimit > 0 ? true : false;

            //将标记的点删除
            for (int j = 0; j < flagLimit; j++) {
                src[flag[j]] = -1;
            }
            flagLimit = 0;  //清空待删除数组

            if (!ifEnd) break; //若没有可以删除的像素点，则退出循环
        }
    }
}
