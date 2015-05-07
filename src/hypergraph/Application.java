package hypergraph;

import com.sun.corba.se.impl.naming.cosnaming.TransientNameServer;
import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import hypergraph.data.KeggImporter;
import hypergraph.data.KeggStatistic;
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
    public static void main(String[] args) {
        keggTest();
    }

    private static void keggTest() {
        HypergraphDatabase.open("db/kegg");
        GraphDatabaseService graphDb = HypergraphDatabase.getGraphDatabase();

        try (Transaction tx = graphDb.beginTx()) {
            Node node = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, "hsa:36");

            MinimalSourceSetFinder finder = new MinimalSourceSetFinder();
            MinimalSourceSet mss = finder.find(node);
        }

        HypergraphDatabase.close();
    }

    private static void keggImport() {
        Log.init("log-kegg.txt");
        HypergraphDatabase.init("db/kegg");
        HypergraphDatabase.open("db/kegg");

        KeggImporter importer = new KeggImporter();
        importer.run();

        MinimalSourceSetBuilder builder = new MinimalSourceSetBuilder();
        builder.run();

        HypergraphDatabase.close();
        Log.close();
    }

    private static void experiment(String dataSet) {
        if (!dataSet.isEmpty()) {
            dataSet = "-" + dataSet;
        }

        Log.init("log" + dataSet + ".txt");
        HypergraphDatabase.init("db/hypergraph" + dataSet);

        SimpleImporter importer = new SimpleImporter("input/hypergraph" + dataSet + ".txt");
        importer.run();

        MinimalSourceSetBuilder builder = new MinimalSourceSetBuilder();
        builder.run();

        HypergraphDatabase.close();
        Log.close();
    }

    private static void query() {
        GraphDatabaseService graphDb = HypergraphDatabase.getGraphDatabase();
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

    private static void backwardDiscovery() {
        GraphDatabaseService graphDb = HypergraphDatabase.getGraphDatabase();
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

}
