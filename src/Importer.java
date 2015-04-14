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
public class Importer { //XXX: rename to SimpleImporter
    private String filename;

    public Importer(String filename) {
        this.filename = filename;
    }

    public void run() {
        try (FileReader fr = new FileReader(filename)) {
            BufferedReader br = new BufferedReader(fr);
            String s = null;
            while ((s = br.readLine()) != null) {
                System.out.println(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addNodes() {
        GraphDatabaseService graphDb = Application.getGraphDatabase();
        try (Transaction tx = graphDb.beginTx()) {
            Node node = graphDb.createNode();
            node.setProperty("name", 1);
            tx.success();
        }
    }

    private void addHyperedges() {

    }

    private void addHyperedge() {

    }
}
