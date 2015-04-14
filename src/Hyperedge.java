import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.util.Set;

/**
 * Created by Hyunjun on 2015-04-15.
 */
public class Hyperedge {
    Set<Node> source;
    Node target;

    private static enum NodeLabel implements Label {
        HyperNode
    }

    //XXX: or use singleton
    public void save(GraphDatabaseService graphDb) { //XXX: may use interface
        // create a pseudo hypernode
        Node hypernode = graphDb.createNode(NodeLabel.HyperNode);

        // connecting edges between source set and target
        for (Node s : source) {
            s.createRelationshipTo(hypernode, null);
        }

        hypernode.createRelationshipTo(target, null);
    }
}
