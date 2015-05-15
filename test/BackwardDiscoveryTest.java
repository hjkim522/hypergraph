import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import hypergraph.data.Importer;
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

import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by Hyunjun on 2015-05-14.
 */
public class BackwardDiscoveryTest {

    @Test
    public void testIndexedBackwardDiscovery() throws Exception {
        GraphDatabaseService graphDb = HypergraphDatabase.open("db/kegg");
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
        HypergraphDatabase.close();
    }

    @Test
    public void testNaiveBackwardDiscovery() throws Exception {
        GraphDatabaseService graphDb = HypergraphDatabase.init("db/hypergraph");
        SimpleImporter importer = new SimpleImporter("input/naive-backward-discovery-test.txt");
//        SimpleImporter importer = new SimpleImporter("input/example-2.txt");
        importer.run();

        try (Transaction tx = graphDb.beginTx()) {
            Node t = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, 0);

            NaiveBackwardDiscovery discovery = new NaiveBackwardDiscovery();
            Set<Long> source = discovery.find(t);

            // source to print
//            System.out.print("{");
//            for (Long sid : source) {
//                Node s = graphDb.getNodeById(sid);
//                int name = (int) s.getProperty(Const.PROP_UNIQUE);
//                System.out.print(name + ", ");
//            }
//            System.out.println("}");
        }

        HypergraphDatabase.close();
    }

    @Test
    public void testNaiveWithBackawrdTraversal() throws Exception {
        GraphDatabaseService graphDb = HypergraphDatabase.init("db/hypergraph");
        SimpleImporter importer = new SimpleImporter("input/naive-backward-discovery-test.txt");
        importer.run();

        try (Transaction tx = graphDb.beginTx()) {
            Node t = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, 5);
            BackwardTraversal bt = new BackwardTraversal();
        }

        HypergraphDatabase.close();
    }

}
