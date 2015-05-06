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
    public Set<Long> find(Node t) {
        MinimalSourceSetFinder finder = new MinimalSourceSetFinder();
        MinimalSourceSet mss = finder.find(t);

        Set<Long> minimum = null;
        int minimumCardinality = 0;

        for (Set<Long> s : mss.getMSS()) {
            if (minimum == null || minimumCardinality > s.size()) {
                minimum = s;
                minimumCardinality = s.size();
            }
        }

        return minimum;
    }
}
