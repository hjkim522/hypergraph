import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.io.fs.FileUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Main Application
 *
 * Initialize Neo4j as follows
 * http://neo4j.com/docs/stable/tutorials-java-embedded-hello-world.html
 *
 * Created by Hyunjun on 2015-04-15.
 */
public class Application {
    private static GraphDatabaseService graphDb = null;

    public static GraphDatabaseService getGraphDatabase() {
        return graphDb;
    }

    public static void main(String[] args) {
        // comment below 2 lines from the second execution
        commandInitDB("db/hypergraph");
        commandImportGraph("input/hypergraph.txt");

        commandOpenDB("db/hypergraph");
        commandBuildMSS();
        commandQueryMSS();
        commandShutdownDB();
    }

    private static void commandInitDB(String path) {
        deleteDatabase(path);
        commandOpenDB(path);
        createIndex();
    }

    private static void commandOpenDB(String path) {
        if (graphDb == null) { //XXX: for convenience
            graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(path);
            registerShutdownHook(graphDb);
        }
    }

    private static void commandImportGraph(String input) {
        SimpleImporter importer = new SimpleImporter(input);
        importer.run();
    }

    private static void commandBuildMSS() {
        MinimalSourceSetBuilder builder = new MinimalSourceSetBuilder();
        builder.run();
    }

    private static void commandShutdownDB() {
        graphDb.shutdown();
        graphDb = null;
    }

    private static void commandQueryMSS() {
        final int numQuery = 10;
        Random random = new Random();
        Set<Long> targets = new HashSet<>();

        try (Transaction tx = graphDb.beginTx()) {
            // get number of nodes from meta node
            Node meta = graphDb.findNodes(Const.LABEL_META).next();
            int numNodes = (int) meta.getProperty(Const.PROP_COUNT);

            // select random target nodes
            while (targets.size() < numQuery) {
                targets.add((long) random.nextInt(numNodes));
            }

            long total = 0;
            for (Long nodeId : targets) {
                Node target = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, nodeId);
                Log.info("Query for node " + nodeId);

                long t = System.currentTimeMillis();
                MinimalSourceSetFinder finder = new MinimalSourceSetFinder();
                MinimalSourceSet mss = finder.find(target);

                long dt = System.currentTimeMillis() - t;
                total += dt;
                Log.info("Query MSS " + dt + "ms");
                Log.info(mss.toString());
            }
            Log.info("Average query time : " + total + " ms");
        }
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

    private static void deleteDatabase(String path) {
        try {
            FileUtils.deleteRecursively(new File(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This only needs to be done once
    private static void createIndex() {
        IndexDefinition indexDefinition;
        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = graphDb.schema();
            indexDefinition = schema.indexFor(Const.LABEL_NODE)
                    .on(Const.PROP_UNIQUE)
                    .create();
            tx.success();
        }

        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = graphDb.schema();
            schema.awaitIndexOnline(indexDefinition, 10, TimeUnit.SECONDS);
        }
    }
}
