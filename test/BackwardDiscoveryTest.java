import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import hypergraph.data.Importer;
import hypergraph.data.SimpleImporter;
import hypergraph.mss.*;
import hypergraph.util.Log;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.*;

/**
 * Created by Hyunun on 2015-05-14.
 */
public class BackwardDiscoveryTest {
    @Test
    public void testIndexedBackwardDiscovery() throws Exception {
    }

    @Test
    public void testBare() throws Exception {
        GraphDatabaseService graphDb = HypergraphDatabase.open("db/kegg");

        try (Transaction tx = graphDb.beginTx()) {
            Node t = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, "cpd:C05345");

            String mssStr = (String) t.getProperty(Const.PROP_MSS);
            MinimalSourceSet mss = MinimalSourceSet.valueOf(mssStr);
            Log.debug(mssStr);
            Log.debug("size " + mss.size());
            Log.debug("card " + mss.cardinality());
        }

        HypergraphDatabase.close();
    }
}
