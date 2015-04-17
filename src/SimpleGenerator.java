import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by Hyunun on 2015-04-17.
 */
public class SimpleGenerator {
    private static final int NUM_NODE = 100;
    private static final int NUM_STARTABLE = 10;
    private static final int NUM_HYPEREDGES = 100;
    private static final int MAX_SOURCE_SIZE = 5;

    private Random random;
    private Set<Integer> startable;

    public SimpleGenerator() {
        random = new Random();
        startable = new HashSet<>();
    }

    public void run() {
        try (FileWriter fw = new FileWriter("output.txt")) {
            BufferedWriter out = new BufferedWriter(fw);
            out.write(String.valueOf(NUM_NODE));
            out.newLine();

            // generate startable
            generateStartable(out);

            // generate and write hyperedges
            for (int i = 0; i < NUM_HYPEREDGES; i++) {
                generateHyperedge(out);
            }

            out.close();
            fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateStartable(BufferedWriter out) throws Exception {
        // randomly generate
        while (startable.size() < NUM_STARTABLE) {
            startable.add(random.nextInt(NUM_NODE));
        }

        // write startable
        for (Integer s : startable) {
            out.write(s + ",");
        }
        out.newLine();
    }

    private void generateHyperedge(BufferedWriter out) throws Exception {
        // randomly select source
        Set<Integer> sourceSet = new HashSet<>();
        int target = random.nextInt(NUM_NODE);
        int sourceSize = random.nextInt(MAX_SOURCE_SIZE) + 1;

        for (int i = 0; i < sourceSize; i++) {
            sourceSet.add(random.nextInt(NUM_NODE));
        }

        // write hyperedge
        for (Integer s : sourceSet) {
            out.write(s + ",");
        }
        out.write(" -> ");
        out.write(String.valueOf(target));
        out.newLine();
    }
}
