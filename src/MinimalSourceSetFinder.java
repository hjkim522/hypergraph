import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

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
        if (!target.hasProperty(Const.PROP_MSS))
            return new MinimalSourceSet();

        //TODO: reconstruction
        String mssStr = (String) target.getProperty(Const.PROP_MSS);
        return MinimalSourceSet.valueOf(mssStr);
    }
}
