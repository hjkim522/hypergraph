package hypergraph.discovery;

import hypergraph.mss.MinimalSourceSet;
import org.neo4j.graphdb.Node;

import java.util.Set;

/**
 * Created by Hyunjun on 2015-05-17.
 */
public class MixedBackwardDiscovery implements BackwardDiscovery {

    @Override
    public MinimalSourceSet findMinimal(Set<Node> target) {
        return null;
    }
}
