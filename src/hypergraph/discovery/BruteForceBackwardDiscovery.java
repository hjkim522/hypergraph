package hypergraph.discovery;

import hypergraph.common.Const;
import hypergraph.mss.MinimalSourceSet;
import hypergraph.traversal.BackwardTraversal;
import org.neo4j.graphdb.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hyunjun on 2015-05-17.
 */
public class BruteForceBackwardDiscovery implements BackwardDiscovery {

    @Override
    public MinimalSourceSet findMinimal(Set<Node> target) {

        Set<Node> sources = new HashSet<>();
        BackwardTraversal traversal = new BackwardTraversal(node -> {
            if (node.hasLabel(Const.LABEL_STARTABLE)) {
                sources.add(node);
            }
        });
        traversal.traverse(target);

        // Test all possible cases
        

        return null;
    }
}
