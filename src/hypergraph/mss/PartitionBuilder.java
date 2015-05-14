package hypergraph.mss;

import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import hypergraph.util.Log;
import hypergraph.util.Measure;
import org.neo4j.cypher.internal.compiler.v1_9.commands.expressions.Min;
import org.neo4j.graphdb.*;

import java.util.*;

/**
 * Simple in-memory builder
 * No optimization applied
 *
 * Created by Hyunjun on 2015-04-17.
 */
public class PartitionBuilder implements MinimalSourceSetBuilder {
    private static GraphDatabaseService graphDb;
    private Map<Long, MinimalSourceSet> mssMap;
    private Set<Long> visited;
    private Set<Long> computed;

    private int countDecomposed;

    // for partition
    private int currentMSSSize;
    private int currentPartition;
    private Map<Long, Integer> partition;

    public PartitionBuilder() {
        graphDb = HypergraphDatabase.getGraphDatabase();
        mssMap = new HashMap<>();
        visited = new HashSet<>();
        computed = new HashSet<>();
        partition = new HashMap<>();
    }

    public void run() {
        // measure building time
        long t = System.currentTimeMillis();
        countDecomposed = 0;
        currentMSSSize = 0;
        currentPartition = 1;

        Log.info("MSS builder with partition");

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
        Log.info("Decomposed MSS " + countDecomposed);
    }

    private void save() {
        Measure measureSize = new Measure("MSS size");
        Measure measureCardinality = new Measure("MSS cardinality");
        for (Map.Entry<Long, MinimalSourceSet> entry : mssMap.entrySet()) {
            Long id = entry.getKey();
            MinimalSourceSet mss = entry.getValue();
            Node node = graphDb.getNodeById(id);

            if (node.hasProperty(Const.PROP_MSS))
                continue;

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

    private void compute(Set<Node> start) {
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
            MinimalSourceSet mss = new MinimalSourceSet(s.getId());
            setMinimalSourceSet(s, mss);
        }

        while (!queue.isEmpty()) {
            // dequeue a normal node (one of source nodes)
            Node s = queue.poll();
            Log.debug("node " + s.getId());

            // get forward star
            Iterable<Relationship> fromSources = s.getRelationships(Direction.OUTGOING, Const.REL_FROM_SOURCE);
            for (Relationship fromSource : fromSources) {
                // get pseudo hypernode and check enabled
                Node h = fromSource.getEndNode();

                if (isComputed(h)) // skip if already computed and not modified
                    continue;
                if (!isEnabled(h)) // check enabled (all source visited)
                    continue;

                // get target nodes
                Iterable<Relationship> toTargets = h.getRelationships(Direction.OUTGOING, Const.REL_TO_TARGET);
                for (Relationship toTarget : toTargets) {
                    Node t = toTarget.getEndNode();

                    // 1. target is visited and same partition
                    // 2. target is visited and different partition
                    // 3. target is not visited
                    if (isVisited(t)) {
                        if (t.hasProperty("decomposed"))
                            continue;

                        if (partition.getOrDefault(t.getId(), 0) == currentPartition) {
                            MinimalSourceSet mssTarget = getMinimalSourceSet(t);
                            MinimalSourceSet mssHyperedge = computeMinimalSourceSet(h);
                            setComputed(h);

                            int prevSize = mssTarget.size();

                            boolean modified = mssTarget.addAll(mssHyperedge);
                            if (modified) {
                                if (queue.contains(t)) {
                                    queue.remove(t);
                                }
                                queue.add(t);
                                unsetComputed(t);
                                currentMSSSize = currentMSSSize - prevSize + mssTarget.size();
                            }

                            // early decomposition
                            if (mssTarget.size() > 1024) {
                                setMinimalSourceSet(t, new MinimalSourceSet(t.getId()));
                                setDecomposed(t);
                            }
                        }
                        else {
                            setDecomposed(t);
                        }
                    }
                    else {
                        setVisited(t);
                        MinimalSourceSet mssHyperedge = computeMinimalSourceSet(h);
                        setComputed(h);
                        setMinimalSourceSet(t, mssHyperedge);
                        queue.add(t);

                        // set partition to t
                        //currentMSSSize++;
                        currentMSSSize += mssHyperedge.size();
                        partition.put(t.getId(), currentPartition);
                    }
                }
            }

            // partition
            if (currentMSSSize > 1024) {
                currentMSSSize = 0;
                currentPartition++;

                Log.debug("currentPartition " + currentPartition);

                // flush mss
                for (Map.Entry<Long, MinimalSourceSet> entry : mssMap.entrySet()) {
                    Long id = entry.getKey();
                    MinimalSourceSet mss = entry.getValue();
                    Node node = graphDb.getNodeById(id);
                    if (node.hasProperty(Const.PROP_MSS))
                        continue;
                    node.setProperty(Const.PROP_MSS, mss.toString());
                    Log.debug("MSS(" + id + ") = " + mss.toString());
                }

                mssMap = new HashMap<>();
            }
        }
    }

    //XXX: separate with decomposition
    private MinimalSourceSet getMinimalSourceSet(Node node) {
        MinimalSourceSet mss = mssMap.get(node.getId());
        if (mss != null)
            return mss;
        mss = new MinimalSourceSet();
        mss.addSourceSetOfSingleNode(node.getId());
        mssMap.put(node.getId(), mss);
        setDecomposed(node);
        return mss;
    }

    private void setMinimalSourceSet(Node node, MinimalSourceSet mss) {
        mssMap.put(node.getId(), mss);
    }

    private MinimalSourceSet computeMinimalSourceSet(Node hypernode) {
        Log.debug("computeMinimalSourceSet of hypernode " + hypernode.getId());
        MinimalSourceSet mss = null;
        Iterable<Relationship> rels = hypernode.getRelationships(Direction.INCOMING, Const.REL_FROM_SOURCE);
        for (Relationship rel : rels) {
            Node s = rel.getStartNode();
            if (mss == null) {
                mss = getMinimalSourceSet(s);
            } else {
                mss = mss.cartesian(getMinimalSourceSet(s));
            }
            Log.debug(mss.cardinality() + " - " + mss.toString());
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

    private void setDecomposed(Node node) {
        if (!node.hasProperty("decomposed")) {
            node.setProperty("decomposed", true);
            countDecomposed++;
        }
    }
}
