import hypergraph.mss.MinimalSourceSet;
import hypergraph.util.Log;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by Hyunun on 2015-04-17.
 */
public class MinimalSourceSetTest {

    @Test
    public void testAddSourceSet() throws Exception {
        MinimalSourceSet mss = new MinimalSourceSet();
        mss.addSourceSet(constructSourceSet(0, 1, 2));
        assertTrue(mss.contains(constructSourceSet(0, 1, 2)));
    }

    @Test
    public void testUnion() throws Exception {
        MinimalSourceSet mss1 = new MinimalSourceSet();
        mss1.addSourceSet(constructSourceSet(0, 1, 2));
        mss1.addSourceSet(constructSourceSet(3, 4));

        MinimalSourceSet mss2 = new MinimalSourceSet();
        mss2.addSourceSet(constructSourceSet(3, 4, 5));

        MinimalSourceSet union = mss1.union(mss2);
        assertTrue(union.contains(constructSourceSet(3, 4)));
        assertFalse(union.contains(constructSourceSet(3, 4, 5)));
    }

    @Test
    public void testCartesian() throws Exception {
        MinimalSourceSet mss1 = new MinimalSourceSet();
        mss1.addSourceSet(constructSourceSet(0, 1, 2));
        mss1.addSourceSet(constructSourceSet(3, 4));
        Log.debug(mss1.toString());

        MinimalSourceSet mss2 = new MinimalSourceSet();
        mss2.addSourceSet(constructSourceSet(3, 4, 5));
        Log.debug(mss2.toString());

        MinimalSourceSet cartesian = mss1.cartesian(mss2);
        Log.debug(cartesian.toString());
        assertTrue(cartesian.contains(constructSourceSet(3, 4, 5)));
        assertFalse(cartesian.contains(constructSourceSet(0, 1, 2, 3, 4, 5)));

        mss1 = new MinimalSourceSet();
        mss1.addSourceSet(constructSourceSet(0, 1, 2));
        mss2 = new MinimalSourceSet();
        mss2.addSourceSet(constructSourceSet(3, 4, 5));
        cartesian = mss1.cartesian(mss2);
        assertTrue(cartesian.contains(constructSourceSet(0, 1, 2, 3, 4, 5)));

        // test empty case
        mss1 = new MinimalSourceSet();
        cartesian = mss2.cartesian(mss1);
        assertTrue(cartesian.cardinality() == 0);
    }

    private Set<Long> constructSourceSet(Number... ids) {
        Set<Long> sourceSet = new HashSet<>();
        for (Number id : ids) {
            sourceSet.add(id.longValue());
        }
        return sourceSet;
    }
}