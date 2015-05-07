package hypergraph.common;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.io.fs.FileUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by Hyunjun on 2015-05-07.
 */
public class HypergraphDatabase {
    private static GraphDatabaseService graphDb = null;
    private static Thread hook = null;

    public static GraphDatabaseService getGraphDatabase() {
        return graphDb;
    }

    public static void init(String path) {
        delete(path);
        open(path);
        createIndex();
    }

    public static void open(String path) {
        if (graphDb == null) {
            graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(path);
            registerShutdownHook(graphDb);
        }
    }

    public static void close() {
        removeShutdownHook();
        graphDb.shutdown();
        graphDb = null;
    }

    private static void delete(String path) {
        try {
            FileUtils.deleteRecursively(new File(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
}
