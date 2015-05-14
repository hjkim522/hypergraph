import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import hypergraph.data.Importer;
import hypergraph.data.SimpleImporter;
import hypergraph.discovery.BackwardDiscovery;
import hypergraph.discovery.IndexedBackwardDiscovery;
import hypergraph.mss.*;
import hypergraph.util.Log;
import org.junit.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by Hyunun on 2015-05-14.
 */
public class BackwardDiscoveryTest {
    GraphDatabaseService graphDb;

    @Before
    public void openDatabase() {
        graphDb = HypergraphDatabase.open("db/kegg");
    }

    @After
    public void closeDatabase() {
        HypergraphDatabase.close();
    }

    @Test
    public void testIndexedBackwardDiscovery() throws Exception {
        try (Transaction tx = graphDb.beginTx()) {
            Node t = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, "hsa:5161");

            NaiveFinder finder = new NaiveFinder();
            MinimalSourceSet mss = finder.find(t);

            // MSS to node set
            for (Set<Long> source : mss.getSourceSets()) {
                System.out.print("{");
                for (Long sid : source) {
                    Node s = graphDb.getNodeById(sid);
                    String name = (String) s.getProperty(Const.PROP_UNIQUE);
                    System.out.print(name + ", ");
                }
                System.out.println("}");
            }
        }
    }

    @Test
    public void testWithoutReconstruction() throws Exception {
        try (Transaction tx = graphDb.beginTx()) {
            Node t = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, "cpd:C05345");

            String mssStr = (String) t.getProperty(Const.PROP_MSS);
            MinimalSourceSet mss = MinimalSourceSet.valueOf(mssStr);
            Log.debug(mssStr);
            Log.debug("size " + mss.size());
            Log.debug("card " + mss.cardinality());
        }
    }
}
