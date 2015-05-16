package hypergraph.discovery;

import hypergraph.mss.MinimalSourceSet;
import org.neo4j.graphdb.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hyunjun on 2015-05-06.
 */
public interface BackwardDiscovery {
    MinimalSourceSet findMinimal(Set<Node> target);

    default Set<Long> findMinimum(Set<Node> target) {
        MinimalSourceSet mss = findMinimal(target);
        Set<Long> minimum = null;
        int minimumCardinality = 0;

        for (Set<Long> s : mss.getSourceSets()) {
            if (minimum == null || minimumCardinality > s.size()) {
                minimum = s;
                minimumCardinality = s.size();
            }
        }

        if (minimum == null) {
            return new HashSet<>();
        }

        return minimum;
    }

    default MinimalSourceSet findMinimal(Node t) {
        Set<Node> target = new HashSet<>();
        target.add(t);
        return findMinimal(target);
    }

    default Set<Long> findMinimum(Node t) {
        Set<Node> target = new HashSet<>();
        target.add(t);
        return findMinimum(target);
    }
}
