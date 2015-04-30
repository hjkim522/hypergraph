import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

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
            for (Long node : s) {
                //TODO:
            }
        }
    }
}
