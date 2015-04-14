import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

// http://neo4j.com/docs/stable/tutorials-java-embedded-hello-world.html

public class Application {
    private static GraphDatabaseService graphDb = null;

    public static GraphDatabaseService getGraphDatabase() {
        return graphDb;
    }

    public static void main(String[] args) {
        Importer importer = new Importer("sample.txt");
        importer.run();
    }

    public static void _main(String[] args) {
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase("db/graphDb");
        registerShutdownHook(graphDb);
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

    private static void deleteDatabase() {
        //TODO:
    }

    // This only needs to be done once
    private static void createIndex() {
        IndexDefinition indexDefinition;
        try (Transaction tx = graphDb.beginTx())
        {
            Schema schema = graphDb.schema();
            indexDefinition = schema.indexFor(DynamicLabel.label("Node"))
                    .on("uniqueId")
                    .create();
            tx.success();
        }
        //schema.awaitIndexOnline( indexDefinition, 10, TimeUnit.SECONDS ); ?????
    }
}
