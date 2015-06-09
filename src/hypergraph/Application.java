package hypergraph;

import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import hypergraph.data.*;
import hypergraph.discovery.*;
import hypergraph.mss.*;
import hypergraph.traversal.HypergraphTraversal;
import hypergraph.util.Log;
import hypergraph.util.Measure;
import org.neo4j.graphdb.*;

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
        syntheticImport();
        syntheticQuery();

//        HypergraphDatabase.delete("db/coda");
//        HypergraphDatabase.copy("db/coda-imported", "db/coda");
//        codaImport();
//        codaQuery();

//        executeTx("coda-query", "db/coda", false, () -> {
//            Set<Node> disease = new HashSet<Node>();
//            Node s = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, "Lepirudin");
//            HypergraphTraversal traversal = new HypergraphTraversal((v)->{
//                String name = (String) v.getProperty("name");
//                Log.debug("node: " + name);
//                if (v.hasLabel(DynamicLabel.label("Disease")))
//                    disease.add(v);
//            }, (v)->{
////                String name = "";
////                if (v.hasProperty("name"))
////                    name = (String) v.getProperty("name");
////                Log.debug("edge: " + name);
//            });
//            traversal.traverse(s);
//            Log.debug("disease");
//            printNames(disease);
//        });
    }

    private static void codaImport() {
        execute("coda-import", "db/coda", true, () -> {
            Importer importer = new CodaImporter();
            importer.run();

            MinimalSourceSetBuilder builder = new DecompositionBuilder(512);
            builder.run();
        });
    }

    private static void codaQuery() {
        executeTx("coda-query", "db/coda", false, () -> {
            Measure measure = new Measure("Query MSS");
            ResourceIterator<Node> nodes = graphDb.findNodes(Const.LABEL_NODE);

            while (nodes.hasNext()) {
                Node node = nodes.next();

                if (node.hasLabel(Const.LABEL_STARTABLE)) {
                    String name = (String) node.getProperty("name");
                    Log.debug("\nquery for node " + node.getId() + " " + name);

                    measure.start();
//                    MinimalSourceSetFinder finder = new DecompositionFinder();
//                    MinimalSourceSet mss = finder.find(node);
                    ForwardDiscovery discovery = new ForwardDiscovery();
                    Set<Node> result = discovery.find(node, (v) -> {
//                        Log.debug("meet " + v.getId());
                        return v.hasLabel(DynamicLabel.label("Disease"));
                    });
                    measure.end();
                    if (result.size() == 1)
                        printNames(result);
                    Log.debug("result " + result.size());
                }
            }
            measure.printStatistic();
        });
    }

    private static void keggImport() {
        execute("kegg-import", "db/kegg", true, () -> {
            Importer importer = new KeggImporter();
            importer.run();

//            MinimalSourceSetBuilder builder = new NaiveBuilder();
//            builder.run();
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
                    MinimalSourceSetFinder finder = new DecompositionFinder();
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

            MinimalSourceSetBuilder builder = new DecompositionBuilder(512);
            builder.run();
        });
    }

    private static void syntheticQueryForward() {
        executeTx("syn-query", "db/syn", false, () -> {
            Measure measure = new Measure("Forward Query MSS");
            ResourceIterator<Node> nodes = graphDb.findNodes(Const.LABEL_NODE);
            int count = 0;
            int max = 10;

            while (nodes.hasNext()) {
                Node node = nodes.next();
                String name = (String) node.getProperty(Const.PROP_UNIQUE);

                if (Math.random() < 0.2) {

                    measure.start();
                    ForwardDiscovery discovery = new ForwardDiscovery();
                    discovery.find(node, (v)->(true));
                    measure.end();

                    count++;
                    if (count > max)
                        break;
                }
            }
            measure.printStatistic();
        });
    }

    private static void syntheticQuery() {
        executeTx("syn-query", "db/syn", false, () -> {
            Measure measureIndexed = new Measure("Indexed Query MSS");
            Measure measureNaive = new Measure("Naive Query MSS");
            ResourceIterator<Node> nodes = graphDb.findNodes(Const.LABEL_NODE);
            int count = 0;
            int max = 25;
            int countErr = 0;

            while (nodes.hasNext()) {
                Node node = nodes.next();
                String name = (String) node.getProperty(Const.PROP_UNIQUE);

                if (Math.random() < 0.2) {
                    Log.info("Query for node " + node.getId() + " " + name);
                    Log.debug("Indexed query for node " + node.getId() + " " + name);
                    measureIndexed.start();
                    BackwardDiscovery indexedDiscovery = new IndexedBackwardDiscovery();
                    MinimalSourceSet mssIndexed = indexedDiscovery.findMinimal(node);
//                    DecompositionFinder finder = new DecompositionFinder();
//                    MinimalSourceSet mssIndexed = finder.findWithSampling(node);
                    measureIndexed.end();
                    printNames(mssIndexed);

//                    Log.debug("Naive query for node " + node.getId() + " " + name);
//                    measureNaive.start();
//                    BackwardDiscovery naiveDiscovery = new MixedBackwardDiscovery();
//                    Set<Node> targets = new HashSet<>();
//                    targets.add(node);
//                    MinimalSourceSet mssNaive = naiveDiscovery.findMinimal(targets);
//                    measureNaive.end();
//                    printNames(mssNaive);

//                    if (!mssIndexed.equals(mssNaive)) {
//                        Log.error("ERROR: MSS diff at " + node.getId() + " " + name);
//                        Log.error(mssIndexed.toString());
//                        Log.error("naive");
//                        Log.error(mssNaive.toString());
//                        countErr++;
//                    }
                    count++;
                    if (count > max)
                        break;
                }
            }
            measureIndexed.printStatistic();
//            measureNaive.printStatistic();
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
                if (!s.hasLabel(Const.LABEL_STARTABLE)) {
                    Log.error("ERROR: NOT A STARTABLE!!");
                }
            }
            System.out.println("}");
        }
    }

    private static void printNames(Set<Node> nodes) {
        for (Node v : nodes) {
            if (v.hasProperty(Const.PROP_UNIQUE)) {
                String name = (String) v.getProperty(Const.PROP_UNIQUE);
                Log.debug(name);
            }
            else if (v.hasProperty("name")) {
                String name = (String) v.getProperty("name");
                Log.debug(name);
            }
            else {
                Log.debug("node " + v.getId());
            }
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
