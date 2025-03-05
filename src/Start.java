import net.minecraft.client.main.Main;
import ru.InfinityGuard;
import java.util.Arrays;
import ru.ProtectedInfinity;

@InfinityGuard
public class Start
{
    public static void main(String[] args)
    {
        ProtectedInfinity.process();
        String assets = System.getenv().containsKey("assetDirectory") ? System.getenv("assetDirectory") : "assets";
        Main.main(concat(new String[] {"--version", "mcp", "--accessToken", "0", "--assetsDir", assets, "--assetIndex", "1.16", "--userProperties", "{}"}, args));
    }

    public static <T> T[] concat(T[] first, T[] second)
    {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
