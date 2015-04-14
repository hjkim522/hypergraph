import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hyunjun on 2015-04-15.
 */
public class Hyperedge {
    private static final Label label = DynamicLabel.label("Hypernode");
    private Set<Node> source;
    private Node target;

    public Hyperedge() {
        source = new HashSet<Node>();
        target = null;
    }

    public void addSource(Node node) {
        source.add(node);
    }

    public void setTarget(Node node) {
        target = node;
    }

    public void save(GraphDatabaseService graphDb) {
        // create a pseudo hypernode
        Node hypernode = graphDb.createNode(label);

        // create edges from source set to hypernode
        for (Node s : source) {
            s.createRelationshipTo(hypernode, null);
        }

        // create an edge from hypernode to target
        hypernode.createRelationshipTo(target, null);
    }
}
