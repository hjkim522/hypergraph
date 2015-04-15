import org.neo4j.graphdb.*;

import java.util.Set;

/**
 * Created by Hyunjun on 2015-04-16.
 */
public class MSSBuilder {

    private GraphDatabaseService graphDb;

    public MSSBuilder() {
        graphDb = Application.getGraphDatabase();
    }

    public void prepare() {
        // Reset all nodes attributes
        // visited = false
    }

    public void build() {
        // start from startables
        try (Transaction tx = graphDb.beginTx()) {
            ResourceIterator<Node> iter = graphDb.findNodes(DynamicLabel.label("startable"));
            //Node v = iter.next();
            iter.close();
        }
    }

    // XXX: temporal in memory build
    public Set<Node> step(Set<Node> T) {
        // find all incident and activated hypernode (i.e. hyperedge)

        // foreach hyperedge, compute mms

        // update targets mms and add target into modified set

        return null;
    }

}
