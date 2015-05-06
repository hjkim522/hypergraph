import org.neo4j.graphdb.Node;

import java.util.Set;

/**
 * Created by Hyunun on 2015-05-06.
 */
public interface BackwardDiscovery {
    public Set<Node> find(Node t);
}
