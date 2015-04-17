import org.neo4j.graphdb.*;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created by Hyunjun on 2015-04-16.
 */
public class MSSBuilder {
    private GraphDatabaseService graphDb;
    private Set<Long> visited;
    private Set<Long> modified;

    public MSSBuilder() {
        graphDb = Application.getGraphDatabase();
    }

    public void build() {
        // start from startables
        try (Transaction tx = graphDb.beginTx()) {
            ResourceIterator<Node> iter = graphDb.findNodes(Const.LABEL_STARTABLE);
            //Node v = iter.next();
            iter.close();
        }
    }

    //XXX: copy of HypergraphTraversal.traverse
    private void build(Set<Node> start) {
        Queue<Node> queue = new LinkedList<Node>();

        for (Node s : start) {
            setVisited(s);
            queue.add(s);
        }

        while (!queue.isEmpty()) {
            // dequeue a normal node (one of source nodes)
            Node v = queue.poll();

            // get connected hyperedges
            Iterable<Relationship> rels = v.getRelationships(Direction.OUTGOING, Const.REL_FROM_SOURCE);
            for (Relationship rel : rels) {
                // pseudo hypernode
                Node h = rel.getEndNode();
                if (!isModified(h)) //XXX: ¾ÈµÉµí...
                    continue;
                if (!isEnabled(h))
                    continue;
//                else
//                    setVisited(h);
// use is updated marks oo

                // TODO:
                // compute mss for h
                // update for t
                // if mss of t is changed then add in the queue

                // get single target node
                Node t = h.getSingleRelationship(Const.REL_TO_TARGET, Direction.OUTGOING).getEndNode();
                if (!isVisited(t)) {
                    setVisited(t);
                    queue.add(t);
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

    private void setModified(Node node) {
        modified.add(node.getId());
    }

    private boolean isModified(Node node) {
        return modified.contains(node.getId());
    }

}
