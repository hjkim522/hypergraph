import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hyunun on 2015-04-17.
 */
public class MinimalSourceSet {
    private Set<Set<Long>> mss;

    public MinimalSourceSet() {
        mss = new HashSet<>();
    }

    public void addSourceSet(Set<Long> sourceSet) {
        // check minimality of sourceSet
        for (Set<Long> s : mss) {
            if (sourceSet.containsAll(s)) {
                return;
            }
        }

        // remove all superset of sourceSet
        for (Set<Long> s : mss) {
            if (s.containsAll(sourceSet)) {
                mss.remove(s);
            }
        }
    }

    public void union(MinimalSourceSet other) {
        for (Set<Long> s : other.mss) {
            addSourceSet(s);
        }
    }

    public void cartesian(MinimalSourceSet other) {
        
    }
}
