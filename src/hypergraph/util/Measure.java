package hypergraph.util;

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
    private long dt;

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
        dt = System.currentTimeMillis() - start;
        addData(dt);
    }

    public long getRecentMeasureTime() {
        return dt;
    }

    public void printStatistic() {
        LongSummaryStatistics stat = list.stream().mapToLong((x)->x).summaryStatistics();
        Log.info("hypergraph.util.Measure - " + name);
        Log.info(" average : " + stat.getAverage());
        Log.info(" max : " + stat.getMax());
        Log.info(" sum : " + stat.getSum());
        Log.info(" count : " + stat.getCount());
    }
}
