package hypergraph.mss;

import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import hypergraph.util.Log;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hyunjun on 2015-05-12.
 */
public class PartitionFinder implements MinimalSourceSetFinder {
    private static GraphDatabaseService graphDb;
    private Set<Long> reconstructed;

    public PartitionFinder() {
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
}
