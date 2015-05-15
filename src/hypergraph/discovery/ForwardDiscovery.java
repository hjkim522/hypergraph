package hypergraph.discovery;

import hypergraph.traversal.HypergraphTraversal;
import org.neo4j.graphdb.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hyunun on 2015-05-14.
 */
public class ForwardDiscovery {
    Set<Long> find(Node t) {
        return null;
    }

    boolean isReachable(Set<Node> source, Set<Node> target) {
        Set<Long> check = new HashSet<>();
        for (Node t : target)
            check.add(t.getId());

        HypergraphTraversal traversal = new HypergraphTraversal(node -> {
            if (check.contains(node.getId())) {
                check.remove(node.getId());
            }
        });
        traversal.traverse(source);

        return check.isEmpty();
    }
}
