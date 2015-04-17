import org.neo4j.cypher.internal.compiler.v1_9.commands.expressions.Min;

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

    public MinimalSourceSet union(MinimalSourceSet other) {
        MinimalSourceSet result = new MinimalSourceSet();
        result.addAll(this); //XXX: do deep copy
        result.addAll(other);
        return result;
    }

    public MinimalSourceSet cartesian(MinimalSourceSet other) {
        MinimalSourceSet result = new MinimalSourceSet();
        for (Set<Long> s1 : mss) {
            for (Set<Long> s2 : other.mss) {
                Set<Long> s = new HashSet<>();
                s.addAll(s1);
                s.addAll(s2);
                result.addSourceSet(s);
            }
        }
        return result;
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
        //TODO:
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
