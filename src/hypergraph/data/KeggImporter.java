package hypergraph.data;

import hypergraph.Application;
import hypergraph.common.Const;
import hypergraph.common.Hyperedge;
import hypergraph.util.Log;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by hyunjun on 2015. 5. 7..
 */
public class KeggImporter {
    private GraphDatabaseService graphDb;
    private int countFile;
    private int countEntry;
    private int countRelations;

    public KeggImporter() {
        graphDb = Application.getGraphDatabase();
        countFile = 0;
        countEntry = 0;
        countRelations = 0;
    }

    public void run() {
        File[] files = new File("input/kegg").listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".xml")) {
                handleFile(file);
                countFile++;
            }
        }

        // TODO: handler undefined, check is same node or unmained anonemous node

        Log.info("countFile : " + countFile);
        Log.info("countEntry : " + countEntry);
        Log.info("countRelations : " + countRelations);
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
        NodeList reactions = root.getElementsByTagName("reaction");

        try (Transaction tx = graphDb.beginTx()) {
            for (int i = 0; i < entries.getLength(); i++) {
                Element entry = (Element) entries.item(i);
                insertEntry(entry);
            }
            tx.success();
        }

        try (Transaction tx = graphDb.beginTx()) {
            for (int i = 0; i < reactions.getLength(); i++) {
                Element reaction = (Element) reactions.item(i);
                insertReaction(reaction);
            }
            tx.success();
        }
    }

    private void insertEntry(Element entry) {
        String name = entry.getAttribute("name");
        String type = entry.getAttribute("type");

        Log.debug("insertEntry " + name);

        // insert node
        Node node = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, name);
        if (node == null) {
            node = graphDb.createNode(Const.LABEL_NODE);
            node.setProperty(Const.PROP_UNIQUE, name);
            countEntry++;
        }
    }

    private void insertReaction(Element reaction) {
        NodeList substrates = reaction.getElementsByTagName("substrate");
        NodeList products = reaction.getElementsByTagName("product");

        Set<Node> sources = constructNodeSet(substrates);
        Set<Node> targets = constructNodeSet(products);

        Log.debug("insertReaction " + reaction.getAttribute("id"));

        // insert hyperedges
        for (Node t : targets) {
            Hyperedge hyperedge = new Hyperedge(sources, t);
            hyperedge.save(graphDb);
            countRelations++;
        }
    }

    private Set<Node> constructNodeSet(NodeList list) {
        Set<Node> set = new HashSet<Node>();
        for (int i = 0; i < list.getLength(); i++) {
            Element elem = (Element) list.item(i);
            String name = elem.getAttribute("name");
            Log.debug(name);

            Node node = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, name);
            if (node == null) {
                Log.warn("null node");
                continue;
            } else {
                set.add(node);
            }
        }
        return set;
    }

    //XXX: temporal impl
    public void markStartables() {
        try (Transaction tx = graphDb.beginTx()) {
            int numStartable = 0;

            Random random = new Random(0);
            ResourceIterator<Node> iter = graphDb.findNodes(Const.LABEL_NODE);

            while (iter.hasNext()) {
//                Log.debug(random.nextInt(100) + " ");
                if (random.nextInt(100) > 90) {
                    Node n = iter.next();
                    n.addLabel(Const.LABEL_STARTABLE);
                    numStartable++;
                } else {
                    iter.next();
                }
            }

            Log.info("numStartable : " + numStartable);
            tx.success();
        }
    }
}
