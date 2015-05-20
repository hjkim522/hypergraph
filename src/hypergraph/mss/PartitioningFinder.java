package hypergraph.mss;

import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import hypergraph.util.Log;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hyunjun on 2015-05-12.
 */
public class PartitioningFinder implements MinimalSourceSetFinder {
    private static GraphDatabaseService graphDb;
    private Set<Long> reconstructed;

    public PartitioningFinder() {
        graphDb = HypergraphDatabase.getGraphDatabase();
        reconstructed = new HashSet<>();
    }

    @Override
    public MinimalSourceSet find(Node target) {
        MinimalSourceSet mss = getMinimalSourceSet(target);

        // Naive implementation
        long decomposedId = needReconstruction(mss);
        while (decomposedId > 0) {
            mss = reconstruct(mss, decomposedId);
            decomposedId = needReconstruction(mss);
        }
        return mss;
    }

    private MinimalSourceSet getMinimalSourceSet(Node target) {
        if (!target.hasProperty(Const.PROP_MSS))
            return new MinimalSourceSet();
        return MinimalSourceSet.valueOf((String) target.getProperty(Const.PROP_MSS));
    }

    private MinimalSourceSet reconstruct(MinimalSourceSet mss, long decomposedId) {
        /*
        A = {a, gf, h}
        mss = {Abc, Abe, ADe}
        mss = A cartesian {bc, be, De} = A * B
         */
        MinimalSourceSet mss1 = new MinimalSourceSet(); // B = mss containing decomposedId
        MinimalSourceSet mss2 = new MinimalSourceSet(); // others (does not need to be join with A)

        for (Set<Long> s : mss.getSourceSets()) {
            if (s.contains(decomposedId) && s.size() == 1) {
                mss2 = mss;
                mss2.removeContains(decomposedId);
                break;
            }
            else if (s.contains(decomposedId)) {
                Set<Long> t = new HashSet<>(s);
                t.remove(decomposedId);
                mss1.getSourceSets().add(t);
            } else {
                mss2.getSourceSets().add(s);
            }
        }

        Node d = graphDb.getNodeById(decomposedId);
        MinimalSourceSet mss3 = getMinimalSourceSet(d); // A in example
//        MinimalSourceSet mss3 = computeMinimalSourceSetOfNode(d); // A in example

        if (mss1.cardinality() != 0) {
            mss2.addAll(mss3.cartesian(mss1));
        }
        else {
            mss2.addAll(mss3);
        }

        reconstructed.add(decomposedId);

        return mss2;
    }

    private long needReconstruction(MinimalSourceSet mss) {
        Log.debug("call needReconstruction of mss(" + mss.cardinality() + "):");
        Log.debug(mss.toString());

        for (Set<Long> s : mss.getSourceSets()) {
            for (Long nodeId : s) {
                Node v = graphDb.getNodeById(nodeId);
                if (v.hasProperty("decomposed") && !reconstructed.contains(nodeId)) {
                    Log.debug("needs recon at " + nodeId);
                    Log.debug("of " + s);
                    return nodeId;
                }
            }
        }
        Log.debug("reconstructed!");
        return -1;
    }

    private MinimalSourceSet computeMinimalSourceSetOfNode(Node node) {
        MinimalSourceSet mss = null;
        Iterable<Relationship> rels = node.getRelationships(Direction.INCOMING, Const.REL_TO_TARGET);
        for (Relationship rel : rels) {
            Node h = rel.getStartNode();
            if (mss == null) {
                mss = computeMinimalSourceSetOfHypernode(h);
            } else {
                mss.addAll(computeMinimalSourceSetOfHypernode(h));
            }
        }
        return mss;
    }

    private MinimalSourceSet computeMinimalSourceSetOfHypernode(Node hypernode) {
        MinimalSourceSet mss = null;
        Iterable<Relationship> rels = hypernode.getRelationships(Direction.INCOMING, Const.REL_FROM_SOURCE);
        for (Relationship rel : rels) {
            Node s = rel.getStartNode();
            if (mss == null) {
                mss = getMinimalSourceSet(s);
            } else {
                mss = mss.cartesian(getMinimalSourceSet(s));
            }
        }
        Log.debug("computeMinimalSourceSetOfHypernode " + hypernode.getId());
        Log.debug("hypernode mss len = " + mss.cardinality());
        Log.debug(mss.toString());

        return mss;
    }
}
