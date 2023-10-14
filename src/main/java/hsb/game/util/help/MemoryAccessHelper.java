package hsb.game.util.help;

import java.lang.foreign.MemorySegment;

/**
 * @author 胡帅博
 * @date 2023/9/17 22:54
 */
public class MemoryAccessHelper {


    public static MemorySegment asSegment(MemorySegment zeroLengthSegment, int byteSize) {
        return   zeroLengthSegment.reinterpret(byteSize);
    }

}
