package hypergraph.mss;

import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import hypergraph.util.Log;
import hypergraph.util.Measure;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import java.util.*;

/**
 * Created by Hyunjun on 2015-07-23.
 */
public class RecursiveBuilder implements MinimalSourceSetBuilder {
    private static GraphDatabaseService graphDb;
    protected Map<Long, MinimalSourceSet> mssMap;
    private Set<Long> visited;
    private Set<Long> computed;

    // decomposition parameter
    private int maxMSS;

    // statistic
    private int statDecomposed;
    private int totalComputation;
    private int queueLen;

    public RecursiveBuilder() {
        this(512);
    }

    public RecursiveBuilder(int maxMSS) {
        this.maxMSS = maxMSS;
        graphDb = HypergraphDatabase.getGraphDatabase();
        mssMap = new HashMap<>();
        visited = new HashSet<>();
        computed = new HashSet<>();
    }

    @Override
    public void run() {
        // measure building time
        long t = System.currentTimeMillis();
        statDecomposed = 0;
        totalComputation = 0;
        queueLen = 0;

        Log.info("MSS builder maxMSS = " + maxMSS);

        try (Transaction tx = graphDb.beginTx()) {
            // find all startable nodes
            Set<Node> start = new HashSet<>();
            ResourceIterator<Node> nodeIter = graphDb.findNodes(Const.LABEL_STARTABLE);
            while (nodeIter.hasNext()) {
                Node v = nodeIter.next();
                start.add(v);
            }

            // compute with startable nodes
            //compute(start);

            // write computed mms into database
//            save();
//            tx.success();
        }

        saveTx();

        Log.info("Build MSSIndex complete (" + (System.currentTimeMillis() - t) + " ms)");
        Log.info("Decomposed MSS " + statDecomposed);
        Log.info("totalComputation " + totalComputation);
        Log.info("queueLen " + queueLen);
    }

    private void saveTx() {
        Measure measure = new Measure("MSS size");
        Iterator<Map.Entry<Long, MinimalSourceSet>> iter = mssMap.entrySet().iterator();
        //while (saveTxHelper(iter, measure));
        measure.printStatistic();
    }


}
