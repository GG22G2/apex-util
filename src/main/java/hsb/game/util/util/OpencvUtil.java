package hsb.game.util.util;

/**
 * @author 胡帅博
 * @date 2023/8/15 15:47
 */
public final class OpencvUtil {

    static {
        System.load("G:\\kaifa_environment\\opencv\\opencv_all_build\\java\\opencv452\\opencv_java452.dll");
    }

    public static void init(){
        // 调用init其实没啥用，但是调用init就一定能保证OpencvUtil中的静态代码块被执行，System.load
    }

}