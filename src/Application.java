import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

// http://neo4j.com/docs/stable/tutorials-java-embedded-hello-world.html

public class Application {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase("db/graphDb");
        registerShutdownHook(graphDb);
        try (Transaction tx = graphDb.beginTx()) {
            Node node = graphDb.createNode();
            node.setProperty("Hello", "world");
            Node node2 = graphDb.createNode();
            node2.setProperty("Hello", "world");
            tx.success();
        }

        graphDb.shutdown();
    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb)
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }
}
