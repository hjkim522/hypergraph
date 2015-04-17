import org.neo4j.graphdb.*;

import java.util.*;

/**
 * Simple in-memory builder
 * No optimization applied
 *
 * Created by Hyunjun on 2015-04-17.
 */
public class MinimalSourceSetBuilder {
    private static GraphDatabaseService graphDb;
    private Set<Long> visited;
    private Map<Long, MinimalSourceSet> mssMap;

    public MinimalSourceSetBuilder() {
        graphDb = Application.getGraphDatabase();
        visited = new HashSet<>();
        mssMap = new HashMap<>();
    }

    public void run() {
        try (Transaction tx = graphDb.beginTx()) {
            // find all startable nodes
            Set<Node> start = new HashSet<>();
            ResourceIterator<Node> nodeIter = graphDb.findNodes(Const.LABEL_STARTABLE);
            while (nodeIter.hasNext()) {
                Node v = nodeIter.next();
                start.add(v);
            }

            // build with startable nodes
            build(start);

            // write computed mms into database
            flush();
        }
    }

    private void flush() {

    }

    //XXX: copy of HypergraphTraversal.traverse()
    private void build(Set<Node> start) {
        Queue<Node> queue = new LinkedList<Node>();

        for (Node s : start) {
            setVisited(s);
            queue.add(s);

            Set<Long> sourceSet = new HashSet<>();
            sourceSet.add(s.getId());

            MinimalSourceSet mss = getMinimalSourceSet(s);
            mss.addSourceSet(sourceSet);
        }

        while (!queue.isEmpty()) {
            // dequeue a normal node (one of source nodes)
            Node v = queue.poll();

            // get connected hyperedges
            Iterable<Relationship> rels = v.getRelationships(Direction.OUTGOING, Const.REL_FROM_SOURCE);
            for (Relationship rel : rels) {
                // pseudo hypernode - XXX: considering Node.getHyperedges()
                Node h = rel.getEndNode();
                if (!isEnabled(h))
                    continue;

                //TODO: check modified

                // calculate mss of hyperedge
                MinimalSourceSet mssHyperedge = computeMinimalSourceSet(h);

                // update targets mss
                Node t = h.getSingleRelationship(Const.REL_TO_TARGET, Direction.OUTGOING).getEndNode();
                setVisited(t);

                MinimalSourceSet mssTarget = getMinimalSourceSet(t);
                boolean modified = mssTarget.addAll(mssHyperedge);
                if (modified) {
                    queue.add(t);
                }
            }
        }
    }

    private MinimalSourceSet getMinimalSourceSet(Node node) {
        MinimalSourceSet mss = mssMap.get(node.getId());
        if (mss != null)
            return mss;
        mss = new MinimalSourceSet();
        mssMap.put(node.getId(), mss);
        return mss;
    }

    private MinimalSourceSet computeMinimalSourceSet(Node hypernode) {
        MinimalSourceSet mss = null;
        Iterable<Relationship> rels = hypernode.getRelationships(Direction.INCOMING, Const.REL_FROM_SOURCE);
        for (Relationship rel : rels) {
            Node s = rel.getStartNode();
            if (mss == null) {
                mss = getMinimalSourceSet(s);
            } else {
                mss.cartesian(getMinimalSourceSet(s));
            }
        }
        return mss;
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
