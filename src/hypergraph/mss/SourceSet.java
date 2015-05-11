package hypergraph.mss;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hyunjun on 2015-05-12.
 */
public class SourceSet {
    private Set<Long> set;

    public SourceSet() {
        set = new HashSet<>();
    }

    public SourceSet(Long s) {
        this();
        set.add(s);
    }

    public void addSource(Long s) {
        set.add(s);
    }

    @Override
    public String toString() {
        String str = "";
        for (Long s : set) {
            str += s + ",";
        }
        return str;
    }


}
