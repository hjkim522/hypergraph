package hypergraph;

import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import hypergraph.data.Importer;
import hypergraph.data.KeggImporter;
import hypergraph.mss.*;
import hypergraph.util.Log;
import hypergraph.util.Measure;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

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
//        keggQuery();
    }

    private static void keggImport() {
        Runnable runnable = () -> {
//            Importer importer = new KeggImporter();//false, true, 20);
//            importer.run();

            MinimalSourceSetBuilder builder = new PartitionBuilder(1024);
            builder.run();
        };

//        execute("kegg-import", "db/kegg", true, runnable);
        execute("kegg-import", "db/kegg", false, runnable);
    }

    private static void keggQuery() {
        Runnable runnable = () -> {
            Measure measure = new Measure("Query MSS");
            ResourceIterator<Node> nodes = graphDb.findNodes(Const.LABEL_NODE);

            while (nodes.hasNext()) {
                Node node = nodes.next();

                // query 1%
                if (Math.random() < 0.01)
                    continue;

                measure.start();
                MinimalSourceSetFinder finder = new NaiveFinder();
                MinimalSourceSet mss = finder.find(node);
                measure.end();
            }
            measure.printStatistic();
        };

        executeTx("kegg-query", "db/kegg", false, runnable);
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
