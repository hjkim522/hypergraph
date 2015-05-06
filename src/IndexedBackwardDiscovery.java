import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.util.Set;

/**
 * Created by Hyunjun on 2015-05-06.
 */
public class IndexedBackwardDiscovery implements BackwardDiscovery {
    private GraphDatabaseService graphDb;

    public IndexedBackwardDiscovery() {
        graphDb = Application.getGraphDatabase();
    }

    @Override
    public Set<Node> find(Node t) {
        return null;
    }
}
