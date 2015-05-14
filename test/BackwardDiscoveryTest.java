import hypergraph.common.Const;
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
 * Created by Hyunun on 2015-05-14.
 */
public class BackwardDiscoveryTest {
    @Test
    void testIndexedBackwardDiscovery() throws Exception {
        GraphDatabaseService graphDb = HypergraphDatabase.open("db/kegg");

        try (Transaction tx = graphDb.beginTx()) {
            Node t = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, "cpd:C00103");

            NaiveFinder finder = new NaiveFinder();
            MinimalSourceSet mss = finder.find(t);


        }

        HypergraphDatabase.close();
    }
}
