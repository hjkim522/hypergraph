package hypergraph.data;

import hypergraph.Application;
import hypergraph.util.Log;
import org.neo4j.graphdb.GraphDatabaseService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * Created by hyunjun on 2015. 5. 7..
 */
public class KeggImporter {
    private GraphDatabaseService graphDb;
    private int fileCount;

    public KeggImporter() {
        graphDb = Application.getGraphDatabase();
        fileCount = 0;
    }

    public void run() {
        File[] files = new File("input/kegg").listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".xml")) {
                handleFile(file);
                fileCount++;
            }

            //XXX: temporal exit
            if (fileCount > 5) {
                return;
            }
        }
    }

    private void handleFile(File file) {
        Log.debug("file " + file.getName());

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            handleDocument(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDocument(Document doc) {
        Element root = doc.getDocumentElement();
        NodeList entries = root.getElementsByTagName("entry");
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            String name = entry.getAttribute("name");
            String type = entry.getAttribute("type");
            String id = entry.getAttribute("id");
            
            Log.debug(name + " " + type);
        }
    }
}
