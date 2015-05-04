import java.util.LinkedList;
import java.util.List;
import java.util.LongSummaryStatistics;

/**
 * Simple measure
 *
 * Created by Hyunjun on 2015-05-04.
 */
public class Measure {
    private String name;
    private List<Long> list;
    private long start;

    public Measure(String name) {
        this.name = name;
        this.list = new LinkedList<>();
    }

    public void addData(long data) {
        list.add(data);
    }

    public void start() {
        start = System.currentTimeMillis();
    }

    public void end() {
        long dt = System.currentTimeMillis() - start;
        addData(dt);
        Log.info(name + " : " + dt + " ms");
    }

    public void printStatistic() {
        LongSummaryStatistics stat = list.stream().mapToLong((x)->x).summaryStatistics();
        Log.info("Measure - " + name);
        Log.info(" average : " + stat.getAverage());
        Log.info(" max : " + stat.getMax());
    }
}
