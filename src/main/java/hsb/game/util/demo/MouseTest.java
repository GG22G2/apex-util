package hsb.game.util.demo;

import hsb.game.util.dll.CaptureDLL;
import hsb.game.util.dll.MouseDLL;

import java.lang.foreign.MemorySegment;

/**
 * @author 胡帅博
 * @date 2023/10/7 1:50
 */
public class MouseTest {
    public static void main(String[] args) throws Throwable {



        MouseDLL.mouseMove.invoke(4072,0);

        MemorySegment apexHwnd = CaptureDLL.getApexHwnd();




    }
}
