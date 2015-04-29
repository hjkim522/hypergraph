import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Simple hypergraph importer
 *
 * Number of nodes at the first line
 * Startable set at the second line
 * Hyperedge description (source -> target) from next line
 * (nodeID starts from 0)
 *
 * ex)
 * 7
 * 0,1,2 -> 3
 * 3,4 -> 5
 * 6 -> 2
 * 6 -> 4
 *
 * XXX: run in single transaction
 *
 * Created by Hyunjun on 2015-04-15.
 */
public class SimpleImporter {
    private String filename;
    private GraphDatabaseService graphDb;

    // SimpleImporter allows in-memory construction only
    // NOTE: If hypergraph size exceeds memory size then use unique index of neo4j
    private Node nodes[];

    public SimpleImporter(String filename) {
        this.filename = filename;
        this.graphDb = Application.getGraphDatabase();
    }

    public void run() {
        System.out.println("SimpleImporter START " + filename);

        try (FileReader fr = new FileReader(filename)) {
            BufferedReader br = new BufferedReader(fr);

            importNodes(Integer.valueOf(br.readLine()));
            importStartable(br.readLine());

            String s;
            while ((s = br.readLine()) != null) {
                importHyperedge(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("SimpleImporter DONE");
    }

    // insert n nodes
    private void importNodes(int n) {
        nodes = new Node[n];
        try (Transaction tx = graphDb.beginTx()) {
            for (int i = 0; i < n; i++) {
                Node node = graphDb.createNode(Const.LABEL_NODE);
                node.setProperty(Const.PROP_UNIQUE, i);
                nodes[i] = node;
            }
            tx.success();
        }
    }

    // mark startable nodes
    private void importStartable(String s) {
        String nodeSeq[] = s.split(",");
        try (Transaction tx = graphDb.beginTx()) {
            for (String node : nodeSeq) {
                int idx = Integer.valueOf(node);
                nodes[idx].addLabel(Const.LABEL_STARTABLE);
            }
            tx.success();
        }
    }

    // parse and insert hyperedges
    private void importHyperedge(String s) {
        // parse (source set -> target)
        String nodeStr[] = s.split(" -> ");
        String sourceStr[] = nodeStr[0].split(",");
        String targetStr = nodeStr[1];

        Hyperedge hyperedge = new Hyperedge();

        for (String source : sourceStr) {
            int sourceIdx = Integer.valueOf(source);
            hyperedge.addSource(nodes[sourceIdx]);
        }

        int targetIdx = Integer.valueOf(targetStr);
        hyperedge.setTarget(nodes[targetIdx]);

        try (Transaction tx = graphDb.beginTx()) {
            hyperedge.save(graphDb);
            tx.success();
        }
    }
}
