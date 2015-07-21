package hypergraph.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by Hyunjun on 2015-07-21.
 */
public class ResultWriter {

    private String filename = null;
    private FileWriter fw = null;
    private BufferedWriter out = null;

    public ResultWriter(String filename) {
        this.filename = filename;
        try {
            new File("results").mkdir();
            fw = new FileWriter("results/" + filename + ".txt");
            out = new BufferedWriter(fw);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //println("Name,NumNodes,NumEdges,QuerySize,NaiveTime,MixedTime,IndexedTime");
    }

    public void close() {
        try {
            out.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        out = null;
        fw = null;
    }

    public void print(String str) {
        System.out.print(str);
        try {
            out.write(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void newLine() {
        System.out.println();
        try {
            out.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void println(String str) {
        print(str);
        newLine();
    }
}
