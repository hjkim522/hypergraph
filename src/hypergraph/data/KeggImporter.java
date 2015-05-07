package hypergraph.data;

import hypergraph.Application;
import hypergraph.common.Const;
import hypergraph.common.Hyperedge;
import hypergraph.common.HypergraphDatabase;
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
import java.util.*;

/**
 * Created by hyunjun on 2015. 5. 7..
 */
public class KeggImporter {
    private GraphDatabaseService graphDb;
    private int countFile;
    private int countEntry;
    private int countRelations;
    private int countReactions;

    public KeggImporter() {
        graphDb = HypergraphDatabase.getGraphDatabase();
        countFile = 0;
        countEntry = 0;
        countRelations = 0;
        countReactions = 0;
    }

    public void run() {
        File[] files = new File("input/kegg").listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".xml")) {
                handleFile(file);
                countFile++;
            }
        }

        markStartables();

        // TODO: handler undefined, check is same node or unmained anonemous node

        Log.info("countFile : " + countFile);
        Log.info("countEntry : " + countEntry);
        Log.info("countRelations : " + countRelations);
        Log.info("countReactions : " + countReactions);
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
        NodeList relations = root.getElementsByTagName("relation");
        NodeList reactions = root.getElementsByTagName("reaction");

        Map<String, String> entryMap = new HashMap<>();

        // insert entries
        try (Transaction tx = graphDb.beginTx()) {
            for (int i = 0; i < entries.getLength(); i++) {
                Element entry = (Element) entries.item(i);
                insertEntry(entryMap, entry);
            }
            tx.success();
        }

        // insert relations
        try (Transaction tx = graphDb.beginTx()) {
            for (int i = 0; i < relations.getLength(); i++) {
                Element relation = (Element) relations.item(i);
                insertRelation(entryMap, relation);
            }
            tx.success();
        }

        // insert reactions
        try (Transaction tx = graphDb.beginTx()) {
            for (int i = 0; i < reactions.getLength(); i++) {
                Element reaction = (Element) reactions.item(i);
                insertReaction(entryMap, reaction);
            }
            tx.success();
        }
    }

    private void insertEntry(Map<String, String> entryMap, Element entry) {
        String nameSeq = entry.getAttribute("name");
        String type = entry.getAttribute("type");
        String id = entry.getAttribute("id");

        Log.debug("insertEntry " + nameSeq);
        entryMap.put(id, nameSeq);

        //TODO: handle undefined
        //TODO: handle group.... fuck

        String[] names = nameSeq.split(" ");
        for (String name : names) {
            // insert into map
            assert entryMap.containsKey(id);

            // insert node
            Node node = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, name);
            if (node == null) {
                node = graphDb.createNode(Const.LABEL_NODE);
                node.setProperty(Const.PROP_UNIQUE, name);
                node.setProperty("type", type);
                countEntry++;
            }
        }
    }

    private void insertRelation(Map<String, String> entryMap, Element relation) {
        String entry1 = relation.getAttribute("entry1");
        String entry2 = relation.getAttribute("entry2");
        String name1 = entryMap.get(entry1);
        String name2 = entryMap.get(entry2);

        // TODO: handle activate and inhibit.... fuck
        NodeList subtypes = relation.getElementsByTagName("subtype");

        Set<Node> sources = constructNodeSet(name1);
        Set<Node> targets = constructNodeSet(name2);

        // insert hyperedges
        for (Node t : targets) {
            Hyperedge hyperedge = new Hyperedge(sources, t);
            hyperedge.save(graphDb);
            countRelations++;
        }
    }

    private Set<Node> constructNodeSet(String nameSeq) {
        Set<Node> set = new HashSet<Node>();
        String[] names = nameSeq.split(" ");
        for (String name : names) {
            Node node = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, name);
            if (node == null) {
                Log.warn("null node2");
                continue;
            } else {
                set.add(node);
            }
        }
        return set;
    }

    private void insertReaction(Map<String, String> entryMap, Element reaction) {
        NodeList substrates = reaction.getElementsByTagName("substrate");
        NodeList products = reaction.getElementsByTagName("product");

        Set<Node> sources = constructNodeSet(substrates);
        Set<Node> targets = constructNodeSet(products);

        Log.debug("insertReaction " + reaction.getAttribute("id"));

        // insert hyperedges
        for (Node t : targets) {
            Hyperedge hyperedge = new Hyperedge(sources, t);
            hyperedge.save(graphDb);
            countReactions++;
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

    private void markStartables() {
        try (Transaction tx = graphDb.beginTx()) {
            int numStartable = 0;

            Random random = new Random(0);
            ResourceIterator<Node> iter = graphDb.findNodes(Const.LABEL_NODE);

            while (iter.hasNext()) {
                Node n = iter.next();

                if (n.getProperty("type").equals("compound")) {
                    n.addLabel(Const.LABEL_STARTABLE);
                    numStartable++;
                }
            }

            Log.info("numStartable : " + numStartable);
            tx.success();
        }
    }
}
