package hypergraph.mss;

import hypergraph.common.Const;
import org.neo4j.graphdb.Node;

/**
 * No reconstruction needed
 * Created by Hyunjun on 2015-05-21.
 */
public class NaiveFinder implements MinimalSourceSetFinder {
    @Override
    public MinimalSourceSet find(Node target) {
        String mssStr = (String) target.getProperty(Const.PROP_MSS);
        return MinimalSourceSet.valueOf(mssStr);
    }
}
