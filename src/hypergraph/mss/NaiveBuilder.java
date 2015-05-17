package hypergraph.mss;

import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import hypergraph.util.Log;
import hypergraph.util.Measure;
import org.neo4j.graphdb.*;

import java.util.*;

/**
 * Simple in-memory builder
 * No optimization applied
 *
 * Created by Hyunjun on 2015-04-17.
 */
public class NaiveBuilder implements MinimalSourceSetBuilder {
    private static GraphDatabaseService graphDb;
    protected Map<Long, MinimalSourceSet> mssMap;
    private Set<Long> visited;
    private Set<Long> computed;

    // decomposition parameter
    private int maxMSS;

    // statistic - XXX: separate as a module
    private int statDecomposed;
    private int totalComputation;
    private int queueLen;

    public NaiveBuilder() {
        this(512);
    }

    public NaiveBuilder(int maxMSS) {
        this.maxMSS = maxMSS;
        graphDb = HypergraphDatabase.getGraphDatabase();
        mssMap = new HashMap<>();
        visited = new HashSet<>();
        computed = new HashSet<>();
    }

    public void run() {
        // measure building time
        long t = System.currentTimeMillis();
        statDecomposed = 0;
        totalComputation = 0;
        queueLen = 0;

        Log.info("MSS builder maxMSS = " + maxMSS);

        try (Transaction tx = graphDb.beginTx()) {
            // find all startable nodes
            Set<Node> start = new HashSet<>();
            ResourceIterator<Node> nodeIter = graphDb.findNodes(Const.LABEL_STARTABLE);
            while (nodeIter.hasNext()) {
                Node v = nodeIter.next();
                start.add(v);
            }

            // compute with startable nodes
            compute(start);

            // write computed mms into database
            save();
            tx.success();
        }

        Log.info("Build MSSIndex complete (" + (System.currentTimeMillis() - t) + " ms)");
        Log.info("Decomposed MSS " + statDecomposed);
        Log.info("totalComputation " + totalComputation);
        Log.info("queueLen " + queueLen);
    }

    private void save() {
        Measure measureSize = new Measure("MSS size");
        Measure measureCardinality = new Measure("MSS cardinality");
        for (Map.Entry<Long, MinimalSourceSet> entry : mssMap.entrySet()) {
            Long id = entry.getKey();
            MinimalSourceSet mss = entry.getValue();
            Node node = graphDb.getNodeById(id);
            node.setProperty(Const.PROP_MSS, mss.toString());

            Log.debug("MSS(" + id + ") = " + mss.toString());
            measureSize.addData(mss.size());
            measureCardinality.addData(mss.cardinality());
        }
        measureSize.printStatistic();
        measureCardinality.printStatistic();
    }

    private void printQueue(Queue<Node> queue) {
        String str = "";
        for (Node n : queue) {
            str += getComputationRate(n) + ":" + n.getId() + ", ";
        }
        Log.debug(str);
    }

    protected void compute(Set<Node> start) {
        PriorityQueue<Node> queue = new PriorityQueue<Node>(new Comparator<Node>() {
            @Override
            public int compare(Node n1, Node n2) {
                return getComputationRate(n1) - getComputationRate(n2);
            }
        });

        // enqueue start nodes
        for (Node s : start) {
            setVisited(s);
            queue.add(s);
            MinimalSourceSet mss = getMinimalSourceSet(s);
            mss.addSourceSetOfSingleNode(s.getId());
        }

        while (!queue.isEmpty()) {
            // dequeue a normal node (one of source nodes)
            printQueue(queue);
            Node s = queue.poll();
            Log.debug("node " + s.getId());
            queueLen++;

            if (getComputationRate(s) != 0) {
                Log.warn("nonzero rate " + getComputationRate(s));
            }

            // get forward star
            Iterable<Relationship> fromSources = s.getRelationships(Direction.OUTGOING, Const.REL_FROM_SOURCE);
            for (Relationship fromSource : fromSources) {
                // get pseudo hypernode and check enabled
                Node h = fromSource.getEndNode();

                // skip if already computed and not modified
                if (isComputed(h))
                    continue;

                // check enabled (all source visited)
                if (!isEnabled(h))
                    continue;

                // mark given hyperedge as computed
                setComputed(h);

                // calculate mss of hyperedge
                MinimalSourceSet mssHyperedge = computeMinimalSourceSet(h);
                mssMap.put(h.getId(), mssHyperedge); //XXX: for test

                // get target node
                // Node t = h.getSingleRelationship(Const.REL_TO_TARGET, Direction.OUTGOING).getEndNode();
                // modified to support multiple target nodes
                Iterable<Relationship> toTargets = h.getRelationships(Direction.OUTGOING, Const.REL_TO_TARGET);
                for (Relationship toTarget : toTargets) {
                    Node t = toTarget.getEndNode();

                    setVisited(t);
                    Log.debug("add target " + t.getId());

                    // calculate and update mss
                    MinimalSourceSet mssTarget = getMinimalSourceSet(t);
                    totalComputation++;

                    boolean modified = mssTarget.addAll(mssHyperedge);
                    if (modified) {
                        if (queue.contains(t)) {
                            queue.remove(t);
                            Log.debug("already contains node " + t.getId());
                        }
                        queue.add(t);
                        unsetComputed(t);
                    }
                }
            }
        }
    }

    private MinimalSourceSet getMinimalSourceSet(Node node) {
        MinimalSourceSet mss = mssMap.get(node.getId());
        if (mss != null)
            return mss;

        // allocate new mss
        mss = new MinimalSourceSet();
//        if (node.hasLabel(Const.LABEL_STARTABLE))
//            mss.addSourceSetOfSingleNode(node.getId());

        mssMap.put(node.getId(), mss);
        return mss;
    }

    private MinimalSourceSet computeMinimalSourceSet(Node hypernode) {
        // check already decomposed
        if (hypernode.hasProperty("decomposed")) {
            return new MinimalSourceSet(hypernode.getId());
        }

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
            if (mss.cardinality() > maxMSS) {
                hypernode.setProperty("decomposed", true);
                statDecomposed++;
                return new MinimalSourceSet(hypernode.getId());
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

    //TODO: how to handle changes
    private int getComputationRate(Node node) {
        int countComputed = 0;
        int countTotal = 0;

        // get incoming hyperedges
        Iterable<Relationship> rels = node.getRelationships(Direction.INCOMING, Const.REL_TO_TARGET);
        for (Relationship rel : rels) {
            Node h = rel.getStartNode();
            if (isComputed(h))
                countComputed++;
            countTotal++;
        }

        return countTotal - countComputed;
    }

    private void setComputed(Node node) {
        computed.add(node.getId());
    }

    private boolean isComputed(Node node) {
        return computed.contains(node.getId());
    }

    private void unsetComputed(Node node) {
        Iterable<Relationship> rels = node.getRelationships(Direction.OUTGOING, Const.REL_FROM_SOURCE);
        for (Relationship rel : rels) {
            Node h = rel.getEndNode();
            computed.remove(h.getId());
        }
    }
}
