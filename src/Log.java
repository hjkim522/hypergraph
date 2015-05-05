import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Simple logger
 * File writer added for experiments
 *
 * Created by Hyunjun on 2015-05-01.
 */
public class Log {
    public static final int ERROR = 0x1;
    public static final int WARN = 0x2;
    public static final int INFO = 0x4;
    public static final int DEBUG = 0x8;
    public static final int ALL = ERROR | WARN | INFO | DEBUG;

    private static int consoleLevel = ALL; // ERROR | INFO;
    private static int fileLevel = ERROR | INFO;

    // log file writers
    private static FileWriter fw = null;
    private static BufferedWriter out = null;

    public static void fileOpen(String logFileName) {
        try {
            new File("log").mkdir();
            fw = new FileWriter("log/" + logFileName);
            out = new BufferedWriter(fw);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void fileClose() {
        try {
            out.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        out = null;
        fw = null;
    }

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
        if ((consoleLevel & mask) == mask) {
            System.out.println(str);
        }

        if ((fileLevel & mask) == mask && out != null) {
            try {
                out.write(str);
                out.newLine();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
