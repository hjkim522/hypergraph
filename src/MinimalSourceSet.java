import java.util.HashSet;
import java.util.Set;

/**
 * Naive implementation of minimal source set
 * No optimization applied
 *
 * Created by Hyunun on 2015-04-17.
 */
public class MinimalSourceSet {
    private Set<Set<Long>> mss;

    public MinimalSourceSet() {
        mss = new HashSet<>();
    }

    // return true if modified
    public boolean addSourceSet(Set<Long> sourceSet) {
        // check minimality of sourceSet
        for (Set<Long> s : mss) {
            if (sourceSet.containsAll(s)) {
                return false;
            }
        }

        // remove all superset of sourceSet
        for (Set<Long> s : mss) {
            if (s.containsAll(sourceSet)) {
                mss.remove(s);
            }
        }

        mss.add(sourceSet);
        return true;
    }

    // return true if modified
    public boolean union(MinimalSourceSet other) {
        boolean modified = false;
        for (Set<Long> s : other.mss) {
            modified = modified | addSourceSet(s);
        }
        return modified;
    }

    public void cartesian(MinimalSourceSet other) {
        MinimalSourceSet cartesian = new MinimalSourceSet();
        for (Set<Long> s1 : mss) {
            for (Set<Long> s2 : other.mss) {
                Set<Long> s = new HashSet<>();
                s.addAll(s1);
                s.addAll(s2);
                cartesian.addSourceSet(s);
            }
        }
        mss = cartesian.mss;
    }
}
