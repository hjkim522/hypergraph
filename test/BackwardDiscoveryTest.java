import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import hypergraph.data.Importer;
import hypergraph.data.KeggImporter;
import hypergraph.data.SimpleImporter;
import hypergraph.discovery.BackwardDiscovery;
import hypergraph.discovery.IndexedBackwardDiscovery;
import hypergraph.discovery.MixedBackwardDiscovery;
import hypergraph.discovery.NaiveBackwardDiscovery;
import hypergraph.mss.*;
import hypergraph.traversal.BackwardTraversal;
import hypergraph.util.Log;
import org.junit.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by Hyunjun on 2015-05-14.
 */
public class BackwardDiscoveryTest {
    static GraphDatabaseService graphDb;
    static String targetName = "88";//"hsa:5161";

    @BeforeClass
    public static void openDatabase() {
//        graphDb = HypergraphDatabase.open("db/kegg");

        graphDb = HypergraphDatabase.init("db/test");
        SimpleImporter importer = new SimpleImporter("input/hypergraph.txt");
        importer.run();
        NaiveBuilder builder = new NaiveBuilder();
        builder.run();
    }

    @AfterClass
    public static void closeDatabase() {
        HypergraphDatabase.close();
    }

    @Test
    public void testIndexedBackwardDiscovery() throws Exception {
        try (Transaction tx = graphDb.beginTx()) {
            Node t = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, targetName);

            BackwardDiscovery discovery = new IndexedBackwardDiscovery();
            MinimalSourceSet mss = discovery.findMinimal(t);
            printNodes(mss);
        }
    }

    @Test
    public void testNaiveBackwardDiscovery() throws Exception {
        try (Transaction tx = graphDb.beginTx()) {
            Node t = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, targetName);

            BackwardDiscovery discovery = new NaiveBackwardDiscovery();
            MinimalSourceSet mss = discovery.findMinimal(t);
            printNodes(mss);
        }
    }

    @Test
    public void testMixedBackwardDiscovery() throws Exception {
        try (Transaction tx = graphDb.beginTx()) {
            Node t = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, targetName);

            BackwardDiscovery discovery = new MixedBackwardDiscovery();
            MinimalSourceSet mss = discovery.findMinimal(t);
            printNodes(mss);
        }
    }

    private void printNodes(MinimalSourceSet mss) {
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
