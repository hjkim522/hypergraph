package hypergraph.data;

import hypergraph.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

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

    @Override
    public void run() {
        importRelationshipFile(new File("input/coda/inter_Cell_network.txt"));
    }

    private void importEntityFile(File file) {
        try (FileReader fr = new FileReader(file)) {
            BufferedReader br = new BufferedReader(fr);
            String s;
            while ((s = br.readLine()) != null) {
                importEntity(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void importEntity(String row) {

    }

    private void importRelationshipFile(File file) {
        try (FileReader fr = new FileReader(file)) {
            BufferedReader br = new BufferedReader(fr);
            String s;
            while ((s = br.readLine()) != null) {
                importRelationship(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void importRelationship(String row) {
        String[] data = row.split("\t");
        String left = data[0];
        String rel = data[1];
        String right = data[2];

        if (left.startsWith("\"")) left = left.substring(1, left.length() - 2);
        if (right.startsWith("\"")) right = right.substring(1, right.length() - 2);

        //String[] sources = left.split()

    }

}
