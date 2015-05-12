package hypergraph.mss;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hyujnun on 2015-05-12.
 */
public class Partition {
    private int id; // partition id
    private Set<Long> startables;
    private int mssSize;

    public Partition(int id) {
        this.id = id;
        this.startables = new HashSet<>();
        this.mssSize = 0;
    }

    public void addStartable(Long s) {
        startables.add(s);
    }


}
