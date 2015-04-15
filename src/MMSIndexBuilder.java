import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;

/**
 * Created by Hyunjun on 2015-04-16.
 */
public class MMSIndexBuilder {

    private GraphDatabaseService graphDb;

    public MMSIndexBuilder() {
        graphDb = Application.getGraphDatabase();
    }

    public void build() {

        // Start from startables
        ResourceIterator<Node> iter = graphDb.findNodes(DynamicLabel.label("startable"));

        Node v = iter.next();

        iter.close();
    }


}
