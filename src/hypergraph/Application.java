package hypergraph;

import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import hypergraph.data.Importer;
import hypergraph.data.KeggImporter;
import hypergraph.data.SimpleImporter;
import hypergraph.mss.*;
import hypergraph.util.Log;
import hypergraph.util.Measure;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import scala.collection.immutable.Stream;

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
        keggImport();

        executeTx("kegg-query", "db/kegg", false, () -> {
            Node t = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, "hsa:5161");

            NaiveFinder finder = new NaiveFinder();
            MinimalSourceSet mss = finder.find(t);

            // MSS to node set
            for (Set<Long> source : mss.getSourceSets()) {
                System.out.print("{");
                for (Long sid : source) {
                    Node s = graphDb.getNodeById(sid);
                    String name = (String) s.getProperty(Const.PROP_UNIQUE);
                    System.out.print(name + ", ");
                }
                System.out.println("}");
            }
        });

//        keggQuery();

//        execute("hypergraph-import", "db/hypergraph", false, () -> {

//            Importer importer = new SimpleImporter("input/hypergraph.txt");
//            importer.run();
//            MinimalSourceSetBuilder builder = new PartitionBuilder();
//            builder.run();
//        });
//
//        query("hypergraph-query", "db/hypergraph");
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
        query("kegg-query", "db/kegg");
    }

    private static void query(String log, String db) {
        executeTx(log, db, false, () -> {
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
