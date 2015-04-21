import org.neo4j.graphdb.*;

import java.util.*;
import java.util.concurrent.SynchronousQueue;

/**
 * Simple in-memory builder
 * No optimization applied
 *
 * Created by Hyunjun on 2015-04-17.
 */
public class MinimalSourceSetBuilder {
    private static GraphDatabaseService graphDb;
    private Map<Long, MinimalSourceSet> mssMap;
    private Set<Long> visited;
    private Set<Long> calculated; // vote for halt
    private Map<Long, Integer> calculatedMap;

    public MinimalSourceSetBuilder() {
        graphDb = Application.getGraphDatabase();
        mssMap = new HashMap<>();
        visited = new HashSet<>();
        calculated = new HashSet<>();
        calculatedMap = new HashMap<>();
    }

    public void run() {
        // measure building time
        long t = System.currentTimeMillis();

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
            tx.success();
        }

        System.out.println("Build MSSIndex complete (" + (System.currentTimeMillis() - t) + " ms)");
    }

    private void flush() {
        for (Map.Entry<Long, MinimalSourceSet> entry : mssMap.entrySet()) {
            Long id = entry.getKey();
            MinimalSourceSet mss = entry.getValue();
            Node node = graphDb.getNodeById(id);
            node.setProperty(Const.PROP_MSS, mss.toString());
        }
    }

    private void printQueue(Queue<Node> queue) {
        for (Node n : queue) {
            System.out.print(getCalculationRate(n) + ":" + n.getId() + ", ");
        }
        System.out.println();
    }

    //XXX: copy of HypergraphTraversal.traverse()
    private void build(Set<Node> start) {
        //Queue<Node> queue = new LinkedList<Node>();
        Queue<Node> queue = new PriorityQueue<>(new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                Node n1 = (Node) o1;
                Node n2 = (Node) o2;
                //return n1.getDegree(Direction.INCOMING) - n2.getDegree(Direction.INCOMING);
                return getCalculationRate(n1) - getCalculationRate(n2);
            }
        });

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
            Node s = queue.poll();
            System.out.println("Build for node " + s.getId());

            //TODO: remove duplicated computation

            // get connected hyperedges
            Iterable<Relationship> rels = s.getRelationships(Direction.OUTGOING, Const.REL_FROM_SOURCE);
            for (Relationship rel : rels) {
                // get pseudo hypernode and check enabled
                Node h = rel.getEndNode();
                if (isCalculated(h))
                    continue;
                if (!isEnabled(h))
                    continue;

                // calculate mss of hyperedge
                MinimalSourceSet mssHyperedge = computeMinimalSourceSet(h);
                setCalculated(h);

                // update targets mss
                Node t = h.getSingleRelationship(Const.REL_TO_TARGET, Direction.OUTGOING).getEndNode();
                setVisited(t);
                calculatedMap.put(t.getId(), calculatedMap.getOrDefault(t.getId(), 0) + 1);

                MinimalSourceSet mssTarget = getMinimalSourceSet(t);
                boolean modified = mssTarget.addAll(mssHyperedge);
                if (modified) {
                    queue.add(t);
                    //printQueue(queue);
                    unsetCalculated(t);
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
                mss = mss.cartesian(getMinimalSourceSet(s));
            }

            // decomposition
            if (mss.size() > 64) { // temporal threshold
                Set<Long> sourceSet = new HashSet<>();
                sourceSet.add(hypernode.getId());
                mss = new MinimalSourceSet();
                mss.addSourceSet(sourceSet);
                return mss;
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

    private int getCalculationRate(Node node) {
        int calculated = calculatedMap.getOrDefault(node.getId(), 0);
        int total = node.getDegree(Const.REL_TO_TARGET);
        return total - calculated;
    }

    private void setCalculated(Node node) {
        calculated.add(node.getId());
    }

    private boolean isCalculated(Node node) {
        return calculated.contains(node.getId());
    }

    private void unsetCalculated(Node node) {
        Iterable<Relationship> rels = node.getRelationships(Direction.OUTGOING, Const.REL_FROM_SOURCE);
        for (Relationship rel : rels) {
            Node h = rel.getEndNode();
            calculated.remove(h.getId());
        }
    }
}
