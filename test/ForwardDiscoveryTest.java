import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import hypergraph.data.Importer;
import hypergraph.data.SimpleImporter;
import hypergraph.mss.*;
import hypergraph.traversal.BackwardTraversal;
import hypergraph.traversal.HypergraphTraversal;
import hypergraph.util.Log;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by Hyunun on 2015-05-14.
 */
public class ForwardDiscoveryTest {

    @Test
    public void testForwardDiscovery() throws Exception {
        GraphDatabaseService graphDb = HypergraphDatabase.open("db/kegg");

        try (Transaction tx = graphDb.beginTx()) {
            Node s = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, "dr:D00944");

        }

        HypergraphDatabase.close();
    }

    @Test
    public void testForwardTraversal() throws Exception {
        GraphDatabaseService graphDb = HypergraphDatabase.open("db/kegg");

        try (Transaction tx = graphDb.beginTx()) {
            Node t = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, "dr:D00944");
            HypergraphTraversal traversal = new HypergraphTraversal(node -> {
                String name = (String) node.getProperty(Const.PROP_UNIQUE);
                Log.debug(name);
            });
            Set<Node> target = new HashSet<>();
            target.add(t);
            traversal.traverse(target);
        }

        HypergraphDatabase.close();
    }
}
