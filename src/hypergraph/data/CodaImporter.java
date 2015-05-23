package hypergraph.data;

import hypergraph.common.Const;
import hypergraph.common.Hyperedge;
import hypergraph.common.HypergraphDatabase;
import hypergraph.util.Log;
import org.neo4j.graphdb.*;

import javax.sound.sampled.Line;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Import CODA data
 * Tab separated
 * ex) CE00000001 D000050 Acanthocytes Acanthocyte
 * ex) left(GE02054564 <<bd:BD00000567><ce:CE00000535>>, GE02064094 <<bd:BD00000567><ce:CE00000535>>
 *     RE00000162
 *     right(GE02054441 <<ts:TS00000021><ce:CE00000326>>) -r [EndoNet] -t [1]
 *
 * Created by Hyunjun on 2015-05-18.
 */
public class CodaImporter implements Importer {
    private class CodaRule {
        //TODO:
    }

    private GraphDatabaseService graphDb;
    private int countEntity;
    private int countRelationship;

    private Map<String, String> relationMap;
    private Map<String, String> geneMap;

    public CodaImporter() {
        graphDb = HypergraphDatabase.getGraphDatabase();
        countEntity = 0;
        countRelationship = 0;
    }

    @Override
    public void run() {
        relationMap = constructMap(new File("input/coda/BISL_Ontology/relation(RE).txt"), 1);
//        geneMap = constructMap(new File("input/coda/BISL_Ontology/gene(GE)_HomoSapiens.txt"), 2);

        importRuleFile(new File("input/coda/FinalNetwork/CODA2_Gene_Disease_Network.txt"));
        importRuleFile(new File("input/coda/FinalNetwork/CODA2_Inter_Cell_Network.txt"));
//        importRuleFile(new File("input/coda/FinalNetwork/CODA2_Intra_Cell_Network.txt"));
        importRuleFile(new File("input/coda/FinalNetwork/kegg.txt"));
        importDrugAndInteraction(new File("input/coda/drug_target_interaction_alldrugs.txt"));

        Log.info("CodaImporter DONE");
        Log.info("countEntity: " + countEntity);
        Log.info("countRelationship: " + countRelationship);
    }

    private Map<String, String> constructMap(File file, int nameColumn) {
        Map<String, String> result = new HashMap<>();
        try (FileReader fr = new FileReader(file)) {
            BufferedReader br = new BufferedReader(fr);
            String s = br.readLine(); // skip first line
            while ((s = br.readLine()) != null) {
                if (s.startsWith("<DB END>")) break;
                String[] token = s.split("\t");
                result.put(token[0], token[nameColumn]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void importRuleFile(File file) {
        try (FileReader fr = new FileReader(file)) {
            BufferedReader br = new BufferedReader(fr);
            while (importRules(br));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean importRules(BufferedReader br) throws IOException {
        final int lineMax = 50000;
        int lines = 0;
        try (Transaction tx = graphDb.beginTx()) {
            String s;
            while (lines < lineMax) {
                s = br.readLine();
                if (s == null) return false;
                importRule(s);
                lines++;
            }
            tx.success();
        }
        return true;
    }

    private void importRule(String row) {
        String[] data = row.split("\t");
        String left = data[0];
        String rel = data[1];
        String right = data[2];
        String mode = (data.length > 3) ? data[3] : "";

        if (rel.length() < 10)
            rel = rel.replaceFirst("RE", "RE0");
        if (rel.startsWith("RE00000111")) // XXX: exclude inhibit
            return;
        if (mode.startsWith("-i")) //XXX: exclude inhibit
            return;

        // remove left() and right()
        left = left.substring(5, left.length() - 1);
        right = right.substring(6, right.length() - 1);

        // parse each side
        Set<Node> leftSide = parseSide(left);
        Set<Node> rightSide = parseSide(right);

        if (leftSide == null || rightSide == null)
            return;

        // handle -a and -i
        // -a [GE02056143 <BD00000567><CE00000535>]
        if (mode.startsWith("-a")) {
            mode = mode.substring(4, mode.length() - 1);
            leftSide.add(findOrCreate(mode));
        }

        Hyperedge h = new Hyperedge(leftSide, rightSide);
        if (!isDuplicated(h)) {
            h.save(graphDb);
            if (h.getHypernode() != null) {
                h.getHypernode().setProperty("type", rel);
                h.getHypernode().setProperty("name", relationMap.get(rel));
                countRelationship++;
            }
        }
    }

    private Set<Node> parseSide(String s) {
        Set<Node> result = new HashSet<>();

        //TODO: handle complex
        // left(complex(GE02056012,GE02056659) <BD00000828><CE00000083>)
        if (s.startsWith("complex"))
            return null;

        String[] entries = s.split(",");
        for (String entry : entries) {
            if (entry.startsWith(" "))
                entry = entry.substring(1, entry.length());
            if (entry.startsWith("<"))
                continue;
            result.add(findOrCreate(entry));
        }
        return result;
    }

    private Node findOrCreate(String entry) {
        Node node = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, entry);
        if (node == null) {
            node = graphDb.createNode(Const.LABEL_NODE);
            node.setProperty(Const.PROP_UNIQUE, entry);

            Label label = getLabel(entry);
            if (label != null) node.addLabel(label);

            countEntity++;
        }
        return node;
    }

    private boolean isDuplicated(Hyperedge hyperedge) {
        Set<Hyperedge> hyperedges = Hyperedge.getHyperedgesFrom(hyperedge.getSource());
        for (Hyperedge e : hyperedges) {
            if (e.getTarget().containsAll(hyperedge.getTarget())) {
                return true;
            }
        }
        return false;
    }

    private Label getLabel(String entry) {
        if (entry.startsWith("GE")) return DynamicLabel.label("Gene");
        else if (entry.startsWith("MB")) return DynamicLabel.label("Metabolite");
        else if (entry.startsWith("DS")) return DynamicLabel.label("Disease");
        return null;
    }

    private void importDrugAndInteraction(File file) {
        try (Transaction tx = graphDb.beginTx()) {
            try (FileReader fr = new FileReader(file)) {
                BufferedReader br = new BufferedReader(fr);
                String s = br.readLine(); // skip line
                while ((s = br.readLine()) != null) {
                    importDrug(s);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            tx.success();
        }
    }

    private void importDrug(String s) {
        String[] data = s.split("\t");
        String name = data[0];
        String up = data[1];
        String down = data[2];

        Node node = graphDb.createNode(Const.LABEL_NODE);
        node.addLabel(Const.LABEL_STARTABLE);
        node.addLabel(DynamicLabel.label("Drug"));
        node.setProperty(Const.PROP_UNIQUE, name);
        node.setProperty("name", name);

        if (!up.startsWith("null"))
            importDrugInteraction(node, up);
        if (!down.startsWith("null"))
            importDrugInteraction(node, down);
    }

    private void importDrugInteraction(Node node, String targetSeq) {
        Set<String> targetIds = new HashSet<>();
        String[] targets = targetSeq.split("\\|");
        for (String target : targets) {
            String id = target.split(" ")[0];
            targetIds.add(id);
        }

        Hyperedge h = new Hyperedge();
        h.addSource(node);

        for (String target : targetIds) {
            Node t = graphDb.findNode(Const.LABEL_NODE, Const.PROP_UNIQUE, target);
            if (t == null) continue;
            h.addTarget(t);
        }

        h.save(graphDb);
    }
}
