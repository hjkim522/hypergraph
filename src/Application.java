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
    private static Thread hook = null;

    public static GraphDatabaseService getGraphDatabase() {
        return graphDb;
    }

    public static void main(String[] args) {
        experiment("100");
        experiment("1000");
    }

    private static void experiment(String dataSet) {
        Log.init("log-" + dataSet + ".txt");

        commandInitDB("db/hypergraph-" + dataSet);
        commandImportGraph("input/hypergraph-" + dataSet + ".txt");

        commandOpenDB("db/hypergraph-" + dataSet);
        commandBuildMSS();
        commandQueryMSS();
        commandShutdownDB();

        Log.close();
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
        removeShutdownHook();
        graphDb.shutdown();
        graphDb = null;
    }

    //TODO: have to query worst case
    private static void commandQueryMSS() {
        final int numQuery = 10;
        Random random = new Random();
        Set<Long> targets = new HashSet<>();
        Measure measure = new Measure("Query MSS");

        try (Transaction tx = graphDb.beginTx()) {
            // get number of nodes from meta node
            Node meta = graphDb.findNodes(Const.LABEL_META).next();
            int numNodes = (int) meta.getProperty(Const.PROP_COUNT);

            // select random target nodes
            //XXX: temporal test
//            while (targets.size() < numQuery) {
//                targets.add((long) random.nextInt(numNodes));
//            }
            // test last node
            targets.add((long) numNodes - 1);

            for (Long nodeId : targets) {
                Node target = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, nodeId);
//                Log.info("Query for node " + nodeId);

                measure.start();
                MinimalSourceSetFinder finder = new MinimalSourceSetFinder();
                MinimalSourceSet mss = finder.find(target);
//                Log.info(mss.toString());
//                Log.info("time : " + measure.getRecentMeasureTime() + " ms");
                measure.end();
            }
        }

        measure.printStatistic();
    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(hook = new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }

    private static void removeShutdownHook() {
        Runtime.getRuntime().removeShutdownHook(hook);
        hook = null;
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
