package hypergraph.common;

import hypergraph.util.Log;
import org.neo4j.graphdb.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hyunjun on 2015-04-15.
 */
public class Hyperedge {
    private Set<Node> source;
    private Set<Node> target;
    private Node hypernode;

    public Hyperedge() {
        source = new HashSet<Node>();
        target = new HashSet<Node>();
        hypernode = null;
    }

    public Hyperedge(Set<Node> s, Set<Node> t) {
        source = s;
        target = t;
        hypernode = null;
    }

    @Deprecated
    public Hyperedge(Set<Node> s, Node t) {
        source = s;
        target = new HashSet<Node>();
        target.add(t);
        hypernode = null;
    }

    public void addSource(Node node) {
        source.add(node);
    }

    public void addTarget(Node node) {
        target.add(node);
    }

    @Deprecated
    public void setTarget(Node node) {
        addTarget(node);
    }

    public Node getHypernode() {
        return hypernode;
    }

    public void save(GraphDatabaseService graphDb) {
        // check error
        if (source == null || source.isEmpty()) {
            Log.debug("empty source");
            return;
        }
        if (target == null || target.isEmpty()) {
            Log.debug("empty target");
            return;
        }

        // create a pseudo hypernode
        hypernode = graphDb.createNode(Const.LABEL_HYPERNODE);

        // create edges from source set to hypernode
        for (Node s : source) {
            s.createRelationshipTo(hypernode, Const.REL_FROM_SOURCE);
        }

        // create an edge from hypernode to target
        for (Node t : target) {
            hypernode.createRelationshipTo(t, Const.REL_TO_TARGET);
        }
    }
}
