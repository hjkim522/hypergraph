package hypergraph.data;

import hypergraph.common.Const;
import hypergraph.common.Hyperedge;
import hypergraph.util.Log;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hyunun on 2015-05-07.
 */
public class KeggStatistic {
    Set<String> enrtySet;

    public KeggStatistic() {
        enrtySet = new HashSet<>();
    }

    public void run() {
        File[] files = new File("input/kegg").listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".xml")) {
                handleFile(file);
            }
        }

        Log.debug("enties: " + enrtySet.size());
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

        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            insertEntry(entry);
        }

        for (int i = 0; i < reactions.getLength(); i++) {
            Element reaction = (Element) reactions.item(i);
            insertReaction(reaction);
        }
    }

    private void insertEntry(Element entry) {
        String name = entry.getAttribute("name");
        enrtySet.add(name);
    }

    private void insertReaction(Element reaction) {
//        NodeList substrates = reaction.getElementsByTagName("substrate");
//        NodeList products = reaction.getElementsByTagName("product");
//
//        Set<Node> sources = constructNodeSet(substrates);
//        Set<Node> targets = constructNodeSet(products);
//
//        Log.debug("insertReaction " + reaction.getAttribute("id"));
//
//        // insert hyperedges
//        for (Node t : targets) {
//            Hyperedge hyperedge = new Hyperedge(sources, t);
//        }
    }

//    private Set<Node> constructNodeSet(NodeList list) {
//        Set<Node> set = new HashSet<Node>();
//        for (int i = 0; i < list.getLength(); i++) {
//            Element elem = (Element) list.item(i);
//            String name = elem.getAttribute("name");
//            Log.debug(name);
//
//            Node node = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, name);
//            if (node == null) {
//                Log.warn("null node");
//                continue;
//            } else {
//                set.add(node);
//            }
//        }
//        return set;
//    }
}
