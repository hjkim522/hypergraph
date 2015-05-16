package hypergraph;

import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import hypergraph.data.Importer;
import hypergraph.data.KeggImporter;
import hypergraph.data.SimpleImporter;
import hypergraph.discovery.ForwardDiscovery;
import hypergraph.discovery.NaiveBackwardDiscovery;
import hypergraph.mss.*;
import hypergraph.traversal.HypergraphTraversal;
import hypergraph.util.Log;
import hypergraph.util.Measure;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import scala.collection.immutable.Stream;

import java.util.HashSet;
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
//        keggQuery();

        syntheticImport();
        syntheticQuery();
    }

    private static void keggImport() {
        execute("kegg-import", "db/kegg", true, () -> {
            Importer importer = new KeggImporter();
            importer.run();

            MinimalSourceSetBuilder builder = new NaiveBuilder();
            builder.run();
        });
    }

    private static void keggQuery() {
        executeTx("kegg-query", "db/kegg", false, () -> {
            Measure measure = new Measure("Query MSS");
            ResourceIterator<Node> nodes = graphDb.findNodes(Const.LABEL_NODE);

            while (nodes.hasNext()) {
                Node node = nodes.next();

                String name = (String) node.getProperty(Const.PROP_UNIQUE);
                Log.debug(name);

                if (name.startsWith("hsa:")) {
                    Log.debug("query for node " + node.getId() + " " + name);

                    measure.start();
                    MinimalSourceSetFinder finder = new NaiveFinder();
                    MinimalSourceSet mss = finder.find(node);
                    measure.end();
                    printNames(mss);
                }
            }
            measure.printStatistic();
        });
    }

    private static void syntheticImport() {
        execute("syn-import", "db/syn", true, () -> {
            Importer importer = new SimpleImporter("input/hypergraph.txt");
            importer.run();

            MinimalSourceSetBuilder builder = new NaiveBuilder();
            builder.run();
        });
    }

    private static void syntheticQuery() {
        executeTx("syn-query", "db/syn", false, () -> {
            Measure measureIndexed = new Measure("Indexed Query MSS");
            Measure measureNaive = new Measure("Naive Query MSS");
            ResourceIterator<Node> nodes = graphDb.findNodes(Const.LABEL_NODE);
            int count = 0;
            int max = 20;
            int countErr = 0;

            while (nodes.hasNext()) {
                Node node = nodes.next();
                String name = (String) node.getProperty(Const.PROP_UNIQUE);

                if (Math.random() < 0.5) {//1) {
                    Log.debug("Indexed query for node " + node.getId() + " " + name);
                    measureIndexed.start();
                    MinimalSourceSetFinder finder = new NaiveFinder();
                    MinimalSourceSet mssIndexed = finder.find(node);
                    measureIndexed.end();
                    printNames(mssIndexed);

                    Log.debug("Naive query for node " + node.getId() + " " + name);
                    measureNaive.start();
                    NaiveBackwardDiscovery discovery = new NaiveBackwardDiscovery();
                    Set<Node> targets = new HashSet<>();
                    targets.add(node);
                    MinimalSourceSet mssNaive = discovery.findMinimal(targets);
                    measureNaive.end();
                    printNames(mssNaive);

                    if (!mssIndexed.equals(mssNaive)) {
                        Log.error("ERROR: MSS diff at " + node.getId() + " " + name);
                        Log.error(mssIndexed.toString());
                        Log.error("\nnaive");
                        Log.error(mssNaive.toString());
                        countErr++;
                    }
                    count++;
                    if (count > max)
                        break;
                }
            }
            measureIndexed.printStatistic();
            measureNaive.printStatistic();
            Log.info("error " + countErr);
        });
    }

    private static void checkMss(MinimalSourceSet mss, Node t) {
        Set<Node> target = new HashSet<>();
        target.add(t);

        for (Set<Long> source : mss.getSourceSets()) {
            Set<Node> sourceSet = new HashSet<>();
            for (Long s : source) {
                Node v = graphDb.getNodeById(s);
                sourceSet.add(v);
            }

            ForwardDiscovery discovery = new ForwardDiscovery();
            //discovery.isReachable(sourceSet, target);
        }
    }

    private static void printNames(MinimalSourceSet mss) {
        for (Set<Long> source : mss.getSourceSets()) {
            System.out.print("{");
            for (Long sid : source) {
                Node s = graphDb.getNodeById(sid);
                String name = (String) s.getProperty(Const.PROP_UNIQUE);
                System.out.print(name + ", ");
            }
            System.out.println("}");
        }
    }

    private static void execute(String log, String db, boolean init, Runnable runnable) {
        // Initialize log
        Log.init(log);

        // Init or open hypergraph database
        if (init) graphDb = HypergraphDatabase.init(db);
        else graphDb = HypergraphDatabase.open(db);

        runnable.run();

        // close
        HypergraphDatabase.close();
        Log.close();
    }

    private static void executeTx(String log, String db, boolean init, Runnable runnable) {
        execute(log, db, init, () -> {
            try (Transaction tx = graphDb.beginTx()) {
                runnable.run();
            }
        });
    }
}
