package hypergraph.data;

import hypergraph.common.Const;
import hypergraph.common.Hyperedge;
import hypergraph.common.HypergraphDatabase;
import hypergraph.util.Log;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

/**
 * Import CODA data
 * Tab separated
 * ex) CE00000001 D000050 Acanthocytes Acanthocyte
 * ex) "left(GE02054564 <<bd:BD00000567><ce:CE00000535>>, i(GE02064094 <<bd:BD00000567><ce:CE00000535>>))"
 *      RE00000162
 *      right(GE02054441 <<ts:TS00000021><ce:CE00000326>>) -r [EndoNet] -t [1]
 *
 * Created by Hyunjun on 2015-05-18.
 */
public class CodaImporter implements Importer {

    private class CodaEntity {
        String id;
        String originId;
        String name;
        String synonym;
        public CodaEntity(String s) {
            String[] token = s.split("\t");
            id = token[0];
            originId = token[1];
            name = token[2];
            synonym = token[3];
        }
        public String toString() {
            return id + "(" + name + ") " + synonym;
        }
    }

    private class CodaSide {
        String id;
        String[] tags;
        public CodaSide(String s) {
            String data[] = s.split(" ");
            id = data[0];
            tags = data[1].split("<>"); //TODO:complex
        }
    }

    private class CodaRelationship {

    }

    private GraphDatabaseService graphDb;
    private Set<String> entities;
    private int countEntity;
    private int countRelationship;

    public CodaImporter() {
        graphDb = HypergraphDatabase.getGraphDatabase();
        entities = new HashSet<>();
        countEntity = 0;
        countRelationship = 0;
    }

    @Override
    public void run() {
        try (Transaction tx = graphDb.beginTx()) {
            importEntityFile(new File("input/coda/BISL_Ontology/gene(GE)_HomoSapiens.txt"));
            tx.success();
        }
//        importEntityFile(new File("input/coda/BISL_Ontology/"));
//        importEntityFile(new File("input/coda/"));

//        importRelationshipFile(new File("input/coda/Backbone_KEGG_Reactions.txt"));
//        importRelationshipFile(new File("input/coda/Backbone_KEGG_Relations.txt"));

        Log.info("CodaImporter DONE");
        Log.info("countEntity: " + countEntity);
        Log.info("countRelationship: " + countRelationship);
    }

    private void importEntityFile(File file) {
        try (FileReader fr = new FileReader(file)) {
            BufferedReader br = new BufferedReader(fr);
            String s = s = br.readLine(); // skip first line
            while ((s = br.readLine()) != null) {
                importEntity(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void importEntity(String row) {
        CodaEntity entity = new CodaEntity(row);
        Log.debug(entity.toString());

        Node node = graphDb.createNode(Const.LABEL_NODE);
        node.setProperty(Const.PROP_UNIQUE, entity.id);
        node.setProperty("name", entity.name);
        node.setProperty("synonym", entity.synonym);

        countEntity++;
    }

    private void importRelationshipFile(File file) {
        try (FileReader fr = new FileReader(file)) {
            BufferedReader br = new BufferedReader(fr);
            String s;
            while ((s = br.readLine()) != null) {
                importRelationship(s);
                countRelationship++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse
     * "left(GE02054564 <<bd:BD00000567><ce:CE00000535>>, i(GE02064094 <<bd:BD00000567><ce:CE00000535>>))"
     * RE00000162	right(GE02054441 <<ts:TS00000021><ce:CE00000326>>)	-r [EndoNet]	-t [1]
     */
    private void importRelationship(String row) {
        String[] data = row.split("\t");
        String left = data[0];
        String rel = data[1];
        String right = data[2];

        // remove double quote
        if (left.startsWith("\"")) left = left.substring(1, left.length() - 1);
        if (right.startsWith("\"")) right = right.substring(1, right.length() - 1);

        // remove left() and right()
        left = left.substring(5, left.length() - 1);
        right = right.substring(6, right.length() - 1);

        Log.debug(left);
        Log.debug(right);

        Set<CodaSide> source = parseSides(left);
        Set<CodaSide> target = parseSides(right);

        if (source == null || target == null)
            return;

        for (CodaSide s : source) {
            entities.add(s.id);
        }

        for (CodaSide t : target) {
            entities.add(t.id);
        }

        //Hyperedge h = new Hyperedge();

    }

    private Set<CodaSide> parseSides(String s) {
        //TODO: handle complex
        if (s.startsWith("complex"))
            return null;

        Set<CodaSide> result = new HashSet<>();
        String[] entries = s.split(",");
        for (String entry : entries) {
            if (entry.startsWith("i("))
                continue;
            else if (entry.startsWith("a("))
                entry = entry.substring(3, entry.length() - 1);

            Log.debug(entry);
            CodaSide codaRelSide = new CodaSide(entry);
            result.add(codaRelSide);
        }
        return result;
    }

}
