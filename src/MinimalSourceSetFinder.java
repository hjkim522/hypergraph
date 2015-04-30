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

    //XXX: considering non recursive
    private void reconstruct(MinimalSourceSet mss) {
        for (Set<Long> s : mss.getMSS()) {
            for (Long nodeId : s) {
                Node node = graphDb.getNodeById(nodeId);

                // if decomposed
                if (node.hasLabel(Const.LABEL_HYPERNODE)) {
                    Log.info("need recon at " + nodeId);
                    MinimalSourceSet reconstructed = computeMinimalSourceSet(node);
                    //TODO:
                }
            }
        }
    }

    //XXX: copy from builder
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
