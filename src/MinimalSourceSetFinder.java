import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * Find all MSS recovering from decomposed MSS
 *
 * Created by Hyunjun on 2015-04-30.
 */
public class MinimalSourceSetFinder {
    private static GraphDatabaseService graphDb;

    public MinimalSourceSetFinder() {
        graphDb = Application.getGraphDatabase();
    }

    public MinimalSourceSet find(Node target) {
        MinimalSourceSet mss = getMinimalSourceSet(target);

        // Naive implementation
//        while (needReconstruction(mss)) {
//            Log.debug(mss.toString());
//            mss = reconstruct(mss);
//        }

//        Log.debug(msRs.toString());
//        needReconstruction(mss);
//
//        mss = reconstruct(mss);
//
//        Log.debug(mss.toString());
//        needReconstruction(mss);
        // 이거 cycle 에서 계속 돌고있는건가

        return mss;
    }

    private MinimalSourceSet getMinimalSourceSet(Node target) {
        if (!target.hasProperty(Const.PROP_MSS))
            return new MinimalSourceSet();
        return MinimalSourceSet.valueOf((String) target.getProperty(Const.PROP_MSS));
    }

    private MinimalSourceSet reconstruct(MinimalSourceSet mss) {
        MinimalSourceSet recon = new MinimalSourceSet(mss);

        for (Set<Long> s : mss.getMSS()) {
            for (Long nodeId : s) {
                Node v = graphDb.getNodeById(nodeId);

                // if decomposed
                if (v.hasLabel(Const.LABEL_HYPERNODE)) {
                    MinimalSourceSet mssV = computeMinimalSourceSet(v);

                    // get cartesian
                    if (s.size() > 1) {
                        Set<Long> sv = new HashSet<>(s);
                        sv.remove(nodeId);
                        MinimalSourceSet mssSv = new MinimalSourceSet(sv);
                        mssV = mssSv.cartesian(mssV);
                    }

                    // remove s and then add recon
                    recon.getMSS().remove(s); //XXX: 안될것같은데
                    recon.addAll(mssV);
                    return recon;
                }
            }
        }

        return null;
    }

    private boolean needReconstruction(MinimalSourceSet mss) {
        for (Set<Long> s : mss.getMSS()) {
            for (Long nodeId : s) {
                Node v = graphDb.getNodeById(nodeId);
                if (v.hasLabel(Const.LABEL_HYPERNODE)) {
                    Log.debug("needs recon at " + nodeId);
                    return true;
                }
            }
        }
        Log.debug("reconstructed!");
        return false;
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
        }
        return mss;
    }
}
