import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hyunjun on 2015-05-04.
 */
public class Statistic<T> {
    private List<T> list;

    public Statistic() {
        list = new LinkedList<T>();
    }

    public void addData(T data) {
        list.add(data);
    }

    public double getAverage() {
        //list.stream().reduce();
        return 0;
    }

    public T getMax() {
        return null;
    }

    public T getMin() {
        return null;
    }
}
