import org.neo4j.graphdb.*;

/**
 * Created by Hyunjun on 2015-04-15.
 */
public class RandomGenerator {

    private static int numNodes = 10;
    private static int numEdges = 10;
    private static int lenPath = 2;

    public void generate() {

    }

    private void generateNodes() {
        GraphDatabaseService graphDb = Application.getGraphDatabase();
        Label label = DynamicLabel.label("Generated");

        try (Transaction tx = graphDb.beginTx()) {
            for (int i = 0; i < numNodes; i++) {
                Node node = graphDb.createNode(label);
            }
            tx.success();
        }
    }

    private void generateHyperedges() {
        GraphDatabaseService graphDb = Application.getGraphDatabase();

        try (Transaction tx = graphDb.beginTx()) {
            for (int i = 0; i < numEdges; i++) {
                //TODO: randomly select source set and target node
            }
            tx.success();
        }
    }
}
