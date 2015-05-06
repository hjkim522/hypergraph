package hypergraph.discovery;

import org.neo4j.graphdb.Node;

import java.util.Set;

/**
 * Created by Hyunjun on 2015-05-06.
 */
public interface BackwardDiscovery {
    public Set<Long> find(Node t);
}
