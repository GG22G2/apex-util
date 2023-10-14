package hsb.game.util.dll;

import hsb.game.util.Config;
import hsb.game.util.help.DllFunctionFindHelper;

import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

/**
 * @author
 * @date 2022/2/12 21:44
 */
public class MouseDLL {
    static {
        loadDLL();

        mouseMove = DllFunctionFindHelper.getFuncOfVoid("mouseMove", JAVA_INT, JAVA_INT);
        mouseLeftPress = DllFunctionFindHelper.getFuncOfVoid("mouseLeftPress");
        mouseLeftRelease = DllFunctionFindHelper.getFuncOfVoid("mouseLeftRelease");
        listener_mouse_move = DllFunctionFindHelper.getFuncOfVoid("listenerMouseMove");

        get_absolute_move = DllFunctionFindHelper.getFuncOf("getAbsoluteMove", ADDRESS);

    }

    private static void loadDLL() {
        System.load(Config.MOUSE_HELP_DLL_PATH);
    }

    public static MethodHandle mouseMove;
    public static MethodHandle mouseLeftPress;
    public static MethodHandle mouseLeftRelease;
    public static MethodHandle listener_mouse_move;
    public static MethodHandle get_absolute_move;

    public static void moveXDegree(double degree, double scale)   {
        try {
            int x = (int) (degree * scale);
            MouseDLL.mouseMove.invoke(x, 0);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public static void moveYDegree(double degree, double scale)   {
        try {
            int y = (int) (degree * scale);
            MouseDLL.mouseMove.invoke(0, y);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


}
