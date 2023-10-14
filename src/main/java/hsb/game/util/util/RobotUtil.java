package hsb.game.util.util;

import hsb.game.util.dll.MouseDLL;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * @author 胡帅博
 * @date 2023/10/5 17:27
 */
public class RobotUtil {
    public static Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }


    //缩小地图
    public static void zoomDownMap() {
        robot.mouseWheel(1);
    }


    //放大地图
    public static void zoomUpMap() {
        robot.mouseWheel(-1);
    }

    //鼠标相对移动
    public static void mouseRelativeMove(int x, int y) {
        //todo 这个方法，目前只用来生成大地图
     //   robot.mouseMove(x,y);


        try {
            MouseDLL.mouseMove.invoke(x,y);
        } catch (Throwable e) {
            e.printStackTrace();
        }


    }

    public static void mouseAbsoluteMove(int x, int y, double dpiScale) {
        robot.mouseMove((int) (x*dpiScale), (int) (y*dpiScale));
    }

    public static void mouseClick(int x,int y,double dpiScale) {
        mouseAbsoluteMove(x,y,dpiScale);
        sleep();
        robot.mousePress(InputEvent.BUTTON1_MASK);
        sleep();
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        sleep();
    }

    public static void mouseDrag(int key,int x,int y) {
        sleep(20);
        robot.mousePress(key);
        sleep(30);

        RobotUtil.mouseRelativeMove(x,y);

        sleep(30);
        robot.mouseRelease(key);
        sleep(5);
    }
    public static void sleep() {
        robot.delay(50);
    }
    public static void sleep(int delay) {
        robot.delay(delay);
    }
    public static void openGameMap() {
        keyPressOnce(KeyEvent.VK_M);
    }

    public static void esc() {
        keyPressOnce(KeyEvent.VK_ESCAPE);
    }


    public static void keyPress(int key) {
        // 模拟按下键盘上的A键
        robot.keyPress(key);
    }
    public static void keyRelease(int key) {
        robot.keyRelease(key);
    }


    public static void keyPressOnce(int key) {
        // 模拟按下键盘上的A键
        robot.keyPress(key);
        robot.delay(20);
        // 模拟释放A键
        robot.keyRelease(key);
        robot.delay(40);
    }

    public static void keyLongPressOnce(int key,int duration) {
        // 模拟按下键盘上的A键
        robot.keyPress(key);
        robot.delay(duration);
        // 模拟释放A键
        robot.keyRelease(key);
        robot.delay(40);
    }
}
