package hsb.game.util.help;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;

/**
 * @author 胡帅博
 * @date 2023/9/17 21:45
 */
public class DllFunctionFindHelper {

    static Linker linker = Linker.nativeLinker();
    static SymbolLookup stdlib = SymbolLookup.loaderLookup();

    /**
     * 加载有返回值的函数
     *
     * */
    public static MethodHandle getFuncOf(String methodName, MemoryLayout returnLayout, MemoryLayout... argLayouts) {
        return linker.downcallHandle(
                stdlib.find(methodName).get(),
                FunctionDescriptor.of(returnLayout, argLayouts)
        );
    }

    /**
     * 加载无返回值的函数
     * */
    public static MethodHandle getFuncOfVoid(String methodName, MemoryLayout... argLayouts) {
        return linker.downcallHandle(
                stdlib.find(methodName).get(),
                FunctionDescriptor.ofVoid(argLayouts)
        );
    }

}
