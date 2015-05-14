package hypergraph.discovery;

import hypergraph.mss.MinimalSourceSet;
import org.neo4j.graphdb.Node;

import java.util.Set;

/**
 * Created by Hyunjun on 2015-05-06.
 */
public interface BackwardDiscovery {
    Set<Long> find(Node t);
}
