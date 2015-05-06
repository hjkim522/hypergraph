package hypergraph.traversal;

import hypergraph.Application;
import hypergraph.common.Const;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created by Hyunjun on 2015-04-17.
 */
public class HypergraphTraversal {
    private GraphDatabaseService graphDb;
    private Set<Long> visited; // visited nodes, in-memory
    private HypergraphTraversalCallback callback;

    public HypergraphTraversal() {
        this(node -> {});
    }

    public HypergraphTraversal(HypergraphTraversalCallback callback) {
        this.graphDb = Application.getGraphDatabase();
        this.visited = new HashSet<Long>();
        this.callback = callback;
    }

    public void traverse(Set<Node> start) {
        Queue<Node> queue = new LinkedList<Node>();

        for (Node s : start) {
            setVisited(s);
            queue.add(s);
            callback.onVisit(s);
        }

        while (!queue.isEmpty()) {
            // dequeue a normal node (one of source nodes)
            Node v = queue.poll();

            // get connected hyperedges
            Iterable<Relationship> rels = v.getRelationships(Direction.OUTGOING, Const.REL_FROM_SOURCE);
            for (Relationship rel : rels) {
                // pseudo hypernode - XXX: considering Node.getHyperedges()
                Node h = rel.getEndNode();
                if (isVisited(h))
                    continue;
                else if (!isEnabled(h))
                    continue;
                else
                    setVisited(h);

                // get single target node
                Node t = h.getSingleRelationship(Const.REL_TO_TARGET, Direction.OUTGOING).getEndNode();
                if (!isVisited(t)) {
                    setVisited(t);
                    queue.add(t);
                    callback.onVisit(t);
                }
            }
        }
    }

    private void setVisited(Node node) {
        visited.add(node.getId());
    }

    private boolean isVisited(Node node) {
        return visited.contains(node.getId());
    }

    private boolean isEnabled(Node hypernode) {
        Iterable<Relationship> rels = hypernode.getRelationships(Direction.INCOMING, Const.REL_FROM_SOURCE);
        for (Relationship rel : rels) {
            if (!isVisited(rel.getStartNode())) {
                return false;
            }
        }
        return true;
    }
}