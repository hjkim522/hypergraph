package hypergraph.mss;

import hypergraph.util.Log;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
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

    public MinimalSourceSet(MinimalSourceSet other) {
        this();
        for (Set<Long> sourceSet : other.mss) {
            mss.add(sourceSet);
        }
    }

    public MinimalSourceSet(Set<Long> sourceSet) {
        this();
        mss.add(sourceSet);
    }

    public MinimalSourceSet(Long nodeId) {
        this();
        addSourceSetOfSingleNode(nodeId);
    }

    // testing purpose
    public MinimalSourceSet(Number... nodeIds) {
        this();
        Set<Long> sourceSet = new HashSet<>();
        for (Number id : nodeIds) {
            sourceSet.add(id.longValue());
        }
        mss.add(sourceSet);
    }

    public Set<Set<Long>> getSourceSets() {
        return mss;
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
        Iterator<Set<Long>> iter = mss.iterator();
        while (iter.hasNext()) {
            Set<Long> s = iter.next();
            if (s.containsAll(sourceSet)) {
                iter.remove();
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

    public boolean addSourceSetOfSingleNode(Long nodeId) {
        Set<Long> sourceSet = new HashSet<>();
        sourceSet.add(nodeId);
        return addSourceSet(sourceSet);
    }

    public void removeContains(Long nodeId) {
        Iterator<Set<Long>> iter = mss.iterator();
        while (iter.hasNext()) {
            Set<Long> s = iter.next();
            if (s.contains(nodeId)) {
                //if (s.size() != 1) Log.error("not 1");
                iter.remove();
                //break;
            }
        }
    }

    public MinimalSourceSet union(MinimalSourceSet other) {
        MinimalSourceSet result = new MinimalSourceSet();
        result.addAll(this); //XXX: do deep copy
        result.addAll(other);
        return result;
    }

    public MinimalSourceSet cartesian(MinimalSourceSet other) {
        MinimalSourceSet result = new MinimalSourceSet();

        if (this.cardinality() == 0 || other.cardinality() == 0)
            return result;

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

    public int size() {
        int sum = 0;
        for (Set<Long> s : mss) {
            sum += s.size();
        }
        return sum;
    }

    public int cardinality() {
        return mss.size();
    }

    @Override
    public String toString() {
        String str = "";
        for (Set<Long> s : mss) {
            for (Long id : s) {
                str += id + ",";
            }
            str += "/";
        }
        return str;
    }

    public static MinimalSourceSet valueOf(String str) {
        MinimalSourceSet mss = new MinimalSourceSet();

        String[] setSeq = str.split("/");
        for (String setStr : setSeq) {
            Set<Long> s = new HashSet<>();

            String[] nodeSeq = setStr.split(",");
            for (String nodeStr : nodeSeq) {
                s.add(Long.valueOf(nodeStr));
            }
            mss.mss.add(s);
        }
        return mss;
    }

    @Override
    public boolean equals(Object other) {
        MinimalSourceSet otherMss = (MinimalSourceSet) other;
        if (this.cardinality() != otherMss.cardinality())
            return false;

        for (Set<Long> s : mss) {
            if (!otherMss.contains(s)) {
                return false;
            }
        }

        return true;
    }

    public boolean contains(Set<Long> sourceSet) {
        for (Set<Long> s : mss) {
            if (s.containsAll(sourceSet) && sourceSet.containsAll(s)) {
                return true;
            }
        }
        return false;
    }

    // for testing purpose
    public static Set<Long> createSourceSet(Number... ids) {
        Set<Long> sourceSet = new HashSet<>();
        for (Number id : ids) {
            sourceSet.add(id.longValue());
        }
        return sourceSet;
    }
}
