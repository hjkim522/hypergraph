package hypergraph.discovery;

import hypergraph.common.HypergraphDatabase;
import hypergraph.mss.MinimalSourceSet;
import hypergraph.mss.MinimalSourceSetFinder;
import hypergraph.mss.NaiveFinder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hyunjun on 2015-05-06.
 */
public class IndexedBackwardDiscovery implements BackwardDiscovery {
    
    @Override
    public MinimalSourceSet findMinimal(Set<Node> target) {
        MinimalSourceSet result = null;

        for (Node t : target) {
            MinimalSourceSetFinder finder = new NaiveFinder();
            MinimalSourceSet mss = finder.find(t);
            if (result == null) result = mss;
            else result = result.cartesian(mss);
        }

        return result;
    }
}
