import hypergraph.common.HypergraphDatabase;
import hypergraph.data.Importer;
import hypergraph.data.SimpleImporter;
import hypergraph.mss.*;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.*;

/**
 * Created by Hyunjun on 2015-05-12.
 */
public class IntegrationTest {

    @Test
    public void testSimple() throws Exception {
        GraphDatabaseService graphDb = HypergraphDatabase.init("db/test");
        Importer importer = new SimpleImporter("input/example-1.txt");
        MinimalSourceSetBuilder builder = new NaiveBuilder();
        MinimalSourceSetFinder finder = new NaiveFinder();

        importer.run();
        builder.run();

        try (Transaction tx = graphDb.beginTx()) {
            Node t = graphDb.findNode(hypergraph.common.Const.LABEL_NODE, hypergraph.common.Const.PROP_UNIQUE, 5);
            MinimalSourceSet mss = finder.find(t);
            assertTrue(mss.equals(new MinimalSourceSet(0, 1, 6)));
        }

        HypergraphDatabase.close();
    }

    @Test
    public void testKegg() throws Exception {

    }



//    private void testFind(GraphDatabaseService graphDb, MinimalSourceSetFinder finder) throws Exception {
//        try (Transaction tx = graphDb.beginTx()) {
//            Node t = graphDb.findNode(hypergraph.common.Const.LABEL_NODE, hypergraph.common.Const.PROP_UNIQUE, 5);
//            MinimalSourceSet mss = finder.find(t);
//            assertTrue(mss.equals(new MinimalSourceSet(0, 1, 6)));
//        }
//    }
}
