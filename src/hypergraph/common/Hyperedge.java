package hypergraph.common;

import org.neo4j.graphdb.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hyunjun on 2015-04-15.
 */
public class Hyperedge {
    private Set<Node> source;
    private Node target;
    private Node hypernode;

    public Hyperedge() {
        source = new HashSet<Node>();
        target = null;
        hypernode = null;
    }

    public Hyperedge(Set<Node> s, Node t) {
        source = s;
        target = t;
        hypernode = null;
    }

    public void addSource(Node node) {
        source.add(node);
    }

    public void setTarget(Node node) {
        target = node;
    }

    public void save(GraphDatabaseService graphDb) {
        // create a pseudo hypernode
        hypernode = graphDb.createNode(Const.LABEL_HYPERNODE);

        // create edges from source set to hypernode
        for (Node s : source) {
            s.createRelationshipTo(hypernode, Const.REL_FROM_SOURCE);
        }

        // create an edge from hypernode to target
        hypernode.createRelationshipTo(target, Const.REL_TO_TARGET);
    }
}
