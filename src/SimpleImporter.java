import org.neo4j.graphdb.DynamicLabel;
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
 *
 * ex)
 * 7
 * 1,2,3 -> 4
 * 4,5 -> 6
 * 7 -> 3
 * 7 -> 5
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
    }

    // insert n nodes
    private void importNodes(int n) {
        nodes = new Node[n + 1]; // nodes[0] = null
        try (Transaction tx = graphDb.beginTx()) {
            for (int i = 1; i <= n; i++) {
                Node node = graphDb.createNode(DynamicLabel.label("Node"));
                node.setProperty("name", i);
                nodes[i] = node;
            }
            tx.success();
        }
    }

    // mark startable nodes
    private void importStartable(String s) {
        //TODO: add labels
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
