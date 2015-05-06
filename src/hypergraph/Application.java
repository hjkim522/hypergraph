package hypergraph;

import hypergraph.common.Const;
import hypergraph.data.KeggImporter;
import hypergraph.discovery.IndexedBackwardDiscovery;
import hypergraph.discovery.NaiveBackwardDiscovery;
import hypergraph.mss.MinimalSourceSet;
import hypergraph.mss.MinimalSourceSetBuilder;
import hypergraph.mss.MinimalSourceSetFinder;
import hypergraph.util.Log;
import hypergraph.util.Measure;
import hypergraph.data.SimpleImporter;
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
 * Main hypergraph.Application
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
        importKegg();
    }

    private static void importKegg() {
        commandInitDB("db/kegg");
        KeggImporter importer = new KeggImporter();
        importer.run();
        commandShutdownDB();
    }

    private static void experiment(String dataSet) {
        if (!dataSet.isEmpty()) {
            dataSet = "-" + dataSet;
        }

        Log.fileOpen("log" + dataSet + ".txt");

        commandInitDB("db/hypergraph" + dataSet);
        commandImportGraph("input/hypergraph" + dataSet + ".txt");

        commandOpenDB("db/hypergraph" + dataSet);
        commandBuildMSS();
//        commandQueryMSS();
//        commandBackwardDiscovery();
        commandShutdownDB();

        Log.fileClose();
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
        final int maxMSS = 512;
        MinimalSourceSetBuilder builder = new MinimalSourceSetBuilder(maxMSS);
        builder.run();
    }

    private static void commandShutdownDB() {
        removeShutdownHook();
        graphDb.shutdown();
        graphDb = null;
    }

    private static void commandQueryMSS() {
        Random random = new Random(0);
        Set<Long> targets = new HashSet<>();
        Measure measure = new Measure("Query MSS");

        try (Transaction tx = graphDb.beginTx()) {
            // get number of nodes from meta node
            Node meta = graphDb.findNodes(Const.LABEL_META).next();
            int numNodes = (int) meta.getProperty(Const.PROP_COUNT);
            int numQuery = (int) (numNodes * 0.05);

            // select random target nodes
            while (targets.size() < numQuery) {
                targets.add((long) random.nextInt(numNodes));
            }

            targets.add((long) numNodes - 1); // test last node

            Log.info("Querying " + targets.size() + " nodes");

            for (Long nodeId : targets) {
                Node target = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, nodeId);

                measure.start();
                MinimalSourceSetFinder finder = new MinimalSourceSetFinder();
                MinimalSourceSet mss = finder.find(target);
                measure.end();
            }
        }

        measure.printStatistic();
    }

    private static void commandBackwardDiscovery() {
        try (Transaction tx = graphDb.beginTx()) {
            // get number of nodes from meta node
            Node meta = graphDb.findNodes(Const.LABEL_META).next();
            int numNodes = (int) meta.getProperty(Const.PROP_COUNT);

            Node t = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, numNodes - 2);
            NaiveBackwardDiscovery naiveBackwardDiscovery = new NaiveBackwardDiscovery();
            IndexedBackwardDiscovery indexedBackwardDiscovery = new IndexedBackwardDiscovery();

            Set<Long> r1 = naiveBackwardDiscovery.find(t);
            Set<Long> r2 = indexedBackwardDiscovery.find(t);

            // compare result
            Log.info("backward discovery results");
            Log.info("naive: " + r1.toString());
            Log.info("index: " + r2.toString());
        }
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
