package hypergraph.traversal;

import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created by Hyunjun on 2015-05-15.
 */
public class BackwardTraversal {
    private Set<Long> visited; // visited nodes, in-memory
    private HypergraphTraversalCallback callback;

    public BackwardTraversal() {
        this(node -> {});
    }

    public BackwardTraversal(HypergraphTraversalCallback callback) {
        this.visited = new HashSet<Long>();
        this.callback = callback;
    }

    public void traverse(Set<Node> target) {
        Queue<Node> queue = new LinkedList<Node>();

        for (Node t : target) {
            setVisited(t);
            queue.add(t);
            //callback.onVisit(t);
        }

        while (!queue.isEmpty()) {
            // dequeue a normal node (one of source nodes)
            Node v = queue.poll();
            callback.onVisit(v);

            // get connected hyperedges, backward star
            Iterable<Relationship> toTargets = v.getRelationships(Direction.INCOMING, Const.REL_TO_TARGET);
            for (Relationship toTarget : toTargets) {
                // get pseudo hypernode
                Node h = toTarget.getStartNode();
                if (isVisited(h))
                    continue;
                setVisited(h);

                // get all source nodes
                Iterable<Relationship> fromSources = h.getRelationships(Direction.INCOMING, Const.REL_FROM_SOURCE);
                for (Relationship fromSource : fromSources) {
                    Node s = fromSource.getStartNode();
                    if (!isVisited(s)) {
                        setVisited(s);
                        queue.add(s);
//                        callback.onVisit(s);
                    }
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
}
