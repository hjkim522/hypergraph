package hypergraph.common;

import hypergraph.util.Log;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.ReadableIndex;

import javax.management.relation.Relation;
import java.util.HashSet;
import java.util.Iterator;
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

    public Set<Node> getSource() {
        return source;
    }

    public Set<Node> getTarget() {
        return target;
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

        // temporarily skip self edge
        Iterator<Node> iter = target.iterator();
        while (iter.hasNext()) {
            Node t = iter.next();
            if (source.contains(t)) {
                Log.info("self edge");
                iter.remove();
            }
        }
        if (target.isEmpty()) {
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

    public static Set<Hyperedge> getHyperedgesFrom(Set<Node> source) {
        Set<Node> hypernodes = null;

        for (Node s : source) {
            Set<Node> intermediate = new HashSet<>();

            // get forward star
            Iterable<Relationship> rels = s.getRelationships(Direction.OUTGOING, Const.REL_FROM_SOURCE);
            for (Relationship rel : rels) {
                // get pseudo hypernode and check enabled
                Node h = rel.getEndNode();

                // check source size
                if (source.size() == h.getDegree(Const.REL_FROM_SOURCE, Direction.INCOMING)) {
                    intermediate.add(h);
                }
            }

            // get intersection
            if (hypernodes == null) {
                hypernodes = intermediate;
            } else {
                hypernodes.retainAll(intermediate);
            }
        }

        // convert node to hyperedges
        Set<Hyperedge> result = new HashSet<>();

        for (Node h : hypernodes) {
            Hyperedge e = new Hyperedge();
            e.source = source;
            e.target = new HashSet<>();
            e.hypernode = h;

            // make target set
            Iterable<Relationship> rels = h.getRelationships(Direction.OUTGOING, Const.REL_TO_TARGET);
            for (Relationship rel : rels) {
                e.target.add(rel.getEndNode());
            }

            result.add(e);
        }

        return result;
    }
}
