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
        reconstruct(mss);
        return mss;
    }

    private MinimalSourceSet getMinimalSourceSet(Node target) {
        if (!target.hasProperty(Const.PROP_MSS))
            return new MinimalSourceSet();
        return MinimalSourceSet.valueOf((String) target.getProperty(Const.PROP_MSS));
    }

    //XXX: considering non recursive version
    private void reconstruct(MinimalSourceSet mss) {
        for (Set<Long> s : mss.getMSS()) {
            for (Long nodeId : s) {
                Node v = graphDb.getNodeById(nodeId);

                // if decomposed
                if (v.hasLabel(Const.LABEL_HYPERNODE)) {
                    Log.info("need recon at " + nodeId + " set size " + s.size());
                    MinimalSourceSet recon = computeMinimalSourceSet(v);
                    //TODO: remove s from mss

                    //TODO: add cartesian of (s-v) * recon(v)

                    //but how to avoid both simultaneous modification
                    //as using iter
                    //근데 이게 cartesian 일까? ㅇ set 이 크면 cartesian 이 맞네 ...
                }
            }
        }
    }

    private void createMinimalSourceSet(Set<Long> s) {

    }

    //XXX: 아 이 인덱스를 계산할때 1번 하고 2홉에 또하고 3홉에 또하고 이러면 되는건가
    //계산 시간이 남을때 packing하는걸로?

    private boolean needReconstruction(Set<Long> s) {
        for (Long nodeId : s) {
            Node v = graphDb.getNodeById(nodeId);
            if (v.hasLabel(Const.LABEL_HYPERNODE)) {
                return true;
            }
        }
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
