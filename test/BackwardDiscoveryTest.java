import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import hypergraph.data.Importer;
import hypergraph.data.KeggImporter;
import hypergraph.data.SimpleImporter;
import hypergraph.discovery.BackwardDiscovery;
import hypergraph.discovery.IndexedBackwardDiscovery;
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
    public void testNaiveBackwardDiscovery() throws Exception {
        try (Transaction tx = graphDb.beginTx()) {
            Node t = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, targetName);

            NaiveBackwardDiscovery discovery = new NaiveBackwardDiscovery();
            //Set<Long> source = discovery.find(t);

            Set<Node> targets = new HashSet<>();
            targets.add(t);
            MinimalSourceSet mss = discovery.findOpt(targets);

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

//    @Test
//    public void testNaiveWithBackawrdTraversal() throws Exception {
//        try (Transaction tx = graphDb.beginTx()) {
//            Node t = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, targetName);
//            BackwardTraversal bt = new BackwardTraversal(node -> {
//                if (node.hasLabel(Const.LABEL_STARTABLE)) {
//                    String name = (String) node.getProperty(Const.PROP_UNIQUE);
//                    Log.debug(name);
//                }
//            });
//            Set<Node> target = new HashSet<>();
//            target.add(t);
//            bt.traverse(target);
//        }
//    }
}
