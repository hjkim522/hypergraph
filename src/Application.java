import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.io.fs.FileUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

// http://neo4j.com/docs/stable/tutorials-java-embedded-hello-world.html

public class Application {
    private static final String DB_PATH = "db/graphDb";

    private static GraphDatabaseService graphDb = null;

    public static GraphDatabaseService getGraphDatabase() {
        return graphDb;
    }

    //XXX: write test cases
    public static void main(String[] args) {
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
        registerShutdownHook(graphDb);

        try (Transaction tx = graphDb.beginTx()) {
            Set<Node> start = new HashSet<Node>();
            start.add(graphDb.findNode(DynamicLabel.label("Node"), "name", 1));
            start.add(graphDb.findNode(DynamicLabel.label("Node"), "name", 2));
            start.add(graphDb.findNode(DynamicLabel.label("Node"), "name", 3));

            HypergraphTraversal traversal = new HypergraphTraversal();
            traversal.traverse(start);
        }

        graphDb.shutdown();
    }

    public static void _main(String[] args) {
        deleteDatabase();
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
        registerShutdownHook(graphDb);
        createIndex();

        SimpleImporter importer = new SimpleImporter("sample.txt");
        importer.run();

        MSSBuilder builder = new MSSBuilder();
        builder.build();

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
        try {
            FileUtils.deleteRecursively(new File(DB_PATH));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This only needs to be done once
    private static void createIndex() {
        IndexDefinition indexDefinition;
        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = graphDb.schema();
            indexDefinition = schema.indexFor(DynamicLabel.label("Node"))
                    .on("name")
                    .create();

            tx.success();
        }

        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = graphDb.schema();
            schema.awaitIndexOnline(indexDefinition, 10, TimeUnit.SECONDS);
        }
    }
}
