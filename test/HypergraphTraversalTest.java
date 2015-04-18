import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by Hyunjun on 2015-04-19.
 */
public class HypergraphTraversalTest {

    @Test
    public void testTraverse() throws Exception {
//        GraphDatabaseService graphDb =  new GraphDatabaseFactory().newEmbeddedDatabase("testdb");
//
//        // XXX: importer to db... o......
//        //SimpleImporter importer = new SimpleImporter("sample.txt");
//        //importer.run();
//
//        try (Transaction tx = graphDb.beginTx()) {
//            Set<Node> start = new HashSet<Node>();
//            start.add(graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, 0));
//            start.add(graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, 1));
//            start.add(graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, 2));
//            start.add(graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, 3));
//
//            HypergraphTraversal traversal = new HypergraphTraversal(node -> {System.out.println(node.getId());});
//            traversal.traverse(start);
//        }
//
//        graphDb.shutdown();
    }
}