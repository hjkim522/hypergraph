import java.util.HashSet;
import java.util.Set;

/**
 * Naive implementation of minimal source set
 * No optimization applied
 *
 * Created by Hyunjun on 2015-04-17.
 */
public class MinimalSourceSet {
    private Set<Set<Long>> mss;

    public MinimalSourceSet() {
        mss = new HashSet<>();
    }

    /**
     * Add a source set to MSS
     * Check minimality constraing
     * @param sourceSet source set to add
     * @return true if modified
     */
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

    public boolean addAll(MinimalSourceSet other) {
        boolean modified = false;
        for (Set<Long> s : other.mss) {
            modified = modified | addSourceSet(s);
        }
        return modified;
    }

    public void union(MinimalSourceSet other) { //XXX: define as binary operator
        for (Set<Long> s : other.mss) {
            addSourceSet(s);
        }
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

    @Override
    public String toString() {
        String str = "";
        for (Set<Long> s : mss) {
            for (Long id : s) {
                str += id + ",";
            }
            str += ";";
        }
        return str;
    }

    public static MinimalSourceSet valueOf(String str) {
        return null;
    }

    // for testing purpose
    public boolean contains(Set<Long> sourceSet) {
        for (Set<Long> s : mss) {
            if (s.containsAll(sourceSet) && sourceSet.containsAll(s)) {
                return true;
            }
        }
        return false;
    }


}
