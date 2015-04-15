import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.shell.util.json.JSONParser;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by Hyunjun on 2015-04-15.
 */
public class SimpleImporter { //XXX: rename to SimpleImporter
    private String filename;
    private GraphDatabaseService graphDb;

    //XXX: temporal....
    //XXX: needs index!
    private Node nodes[] = new Node[10];

    public SimpleImporter(String filename) {
        this.filename = filename;
        this.graphDb = Application.getGraphDatabase();
    }

    public void run() {
        try (FileReader fr = new FileReader(filename)) {
            BufferedReader br = new BufferedReader(fr);
            String s = br.readLine();

            importNodes(Integer.valueOf(s));

            while ((s = br.readLine()) != null) {
                // skip empty lines and comments - XXX: temporal impl
                if (s.length() == 0 || s.startsWith("#"))
                    continue;

                importHyperedge(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // insert n nodes
    private void importNodes(int n) {
        try (Transaction tx = graphDb.beginTx()) {
            for (int i = 1; i <= n; i++) {
                Node node = graphDb.createNode(DynamicLabel.label("Node"));
                node.setProperty("name", i);
                nodes[i] = node;
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
