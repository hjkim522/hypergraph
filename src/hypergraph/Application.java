package hypergraph;

import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
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
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Main hypergraph.Application
 *
 * Initialize Neo4j as follows
 * http://neo4j.com/docs/stable/tutorials-java-embedded-hello-world.html
 *
 * Created by Hyunjun on 2015-04-15.
 */
public class Application {
    private static GraphDatabaseService graphDb;

    public static void main(String[] args) {

//        keggImport();

        keggQuery();


//        MinimalSourceSetBuilder builder = new MinimalSourceSetBuilder(100000);
//        builder.run();
//    });execute("kegg-test", "db/kegg", false, () -> {


//        executeTx("kegg", "db/kegg", false, () -> {
//            Node node = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, "cpd:C00011");
//            Log.debug("node id " + node.getId());
//            MinimalSourceSetFinder finder = new MinimalSourceSetFinder();
//            MinimalSourceSet mss = finder.find(node);
//        });

    }

    private static void execute(String log, String db, boolean init, Runnable runnable) {
        // Initialize log
        Log.init(log);

        // Init and open hypergraph database
        if (init) HypergraphDatabase.init(db);
        graphDb = HypergraphDatabase.open(db);

        runnable.run();

        // close
        HypergraphDatabase.close();
        Log.close();
    }

    private static void executeTx(String log, String db, boolean init, Runnable runnable) {
        execute(log, db, init, ()->{
            try (Transaction tx = graphDb.beginTx()) {
                runnable.run();
            }
        });
    }

    private static void keggImport() {
        Runnable runnable = () -> {
            KeggImporter importer = new KeggImporter();
            importer.run();

            MinimalSourceSetBuilder builder = new MinimalSourceSetBuilder(512);
            builder.run();
        };

        execute("kegg-import", "db/kegg", true, runnable);
    }


    private static void keggQuery() {
        Runnable runnable = () -> {
            Measure measure = new Measure("Query MSS");
            ResourceIterator<Node> nodes = graphDb.findNodes(Const.LABEL_NODE);

            while (nodes.hasNext()) {
                Node node = nodes.next();

                // query 1%
                if (Math.random() < 0.99)
                    continue;

                measure.start();
                MinimalSourceSetFinder finder = new MinimalSourceSetFinder();
                MinimalSourceSet mss = finder.find(node);
                measure.end();
            }
            measure.printStatistic();
        };

        executeTx("kegg-query.txt", "db/kegg", false, runnable);
    }

    // Deprecated below
//    private static void experiment(String dataSet) {
//        if (!dataSet.isEmpty()) {
//            dataSet = "-" + dataSet;
//        }
//
//        Log.init("log" + dataSet + ".txt");
//        HypergraphDatabase.init("db/hypergraph" + dataSet);
//
//        SimpleImporter importer = new SimpleImporter("input/hypergraph" + dataSet + ".txt");
//        importer.run();
//
//        MinimalSourceSetBuilder builder = new MinimalSourceSetBuilder();
//        builder.run();
//
//        HypergraphDatabase.close();
//        Log.close();
//    }
//
//    private static void query() {
//        GraphDatabaseService graphDb = HypergraphDatabase.getGraphDatabase();
//        Random random = new Random(0);
//        Set<Long> targets = new HashSet<>();
//        Measure measure = new Measure("Query MSS");
//
//        try (Transaction tx = graphDb.beginTx()) {
//            // get number of nodes from meta node
//            Node meta = graphDb.findNodes(Const.LABEL_META).next();
//            int numNodes = (int) meta.getProperty(Const.PROP_COUNT);
//            int numQuery = (int) (numNodes * 0.05);
//
//            // select random target nodes
//            while (targets.size() < numQuery) {
//                targets.add((long) random.nextInt(numNodes));
//            }
//
//            targets.add((long) numNodes - 1); // test last node
//
//            Log.info("Querying " + targets.size() + " nodes");
//
//            for (Long nodeId : targets) {
//                Node target = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, nodeId);
//
//                measure.start();
//                MinimalSourceSetFinder finder = new MinimalSourceSetFinder();
//                MinimalSourceSet mss = finder.find(target);
//                measure.end();
//            }
//        }
//
//        measure.printStatistic();
//    }
//
//    private static void backwardDiscovery() {
//        GraphDatabaseService graphDb = HypergraphDatabase.getGraphDatabase();
//        try (Transaction tx = graphDb.beginTx()) {
//            // get number of nodes from meta node
//            Node meta = graphDb.findNodes(Const.LABEL_META).next();
//            int numNodes = (int) meta.getProperty(Const.PROP_COUNT);
//
//            Node t = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, numNodes - 2);
//            NaiveBackwardDiscovery naiveBackwardDiscovery = new NaiveBackwardDiscovery();
//            IndexedBackwardDiscovery indexedBackwardDiscovery = new IndexedBackwardDiscovery();
//
//            Set<Long> r1 = naiveBackwardDiscovery.find(t);
//            Set<Long> r2 = indexedBackwardDiscovery.find(t);
//
//            // compare result
//            Log.info("backward discovery results");
//            Log.info("naive: " + r1.toString());
//            Log.info("index: " + r2.toString());
//        }
//    }

}
