/**
 * Simple logger
 *
 * Created by Hyunjun on 2015-05-01.
 */
public class Log {
    public static final int ERROR = 0x1;
    public static final int WARN = 0x2;
    public static final int INFO = 0x4;
    public static final int DEBUG = 0x8;
    public static final int ALL = ERROR | WARN | INFO | DEBUG;

    private static int level = ALL;

    public static void error(String str) {
        println(ERROR, str);
    }

    public static void warn(String str) {
        println(WARN, str);
    }

    public static void info(String str) {
        println(INFO, str);
    }

    public static void debug(String str) {
        println(DEBUG, str);
    }

    private static void println(int mask, String str) {
        if ((level & mask) == mask)
            System.out.println(str);
    }
}
