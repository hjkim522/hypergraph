package hypergraph.mss;

import org.neo4j.graphdb.Node;

import java.util.Set;

/**
 * Created by Hyunjun on 2015-05-12.
 */
public interface MinimalSourceSetFinder {
    MinimalSourceSet find(Node target);
}
