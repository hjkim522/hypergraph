package hypergraph.discovery;

import hypergraph.mss.*;
import org.neo4j.graphdb.Node;

import java.util.Set;

/**
 * Created by Hyunjun on 2015-05-06.
 */
public class IndexedBackwardDiscovery implements BackwardDiscovery {
    
    @Override
    public MinimalSourceSet findMinimal(Set<Node> target) {
        MinimalSourceSet result = null;

        for (Node t : target) {
            MinimalSourceSetFinder finder = new FastDecompositionFinder();
            MinimalSourceSet mss = finder.find(t);
            if (result == null) result = mss;
            else result = result.cartesian(mss);
        }

        return result;
    }
}
