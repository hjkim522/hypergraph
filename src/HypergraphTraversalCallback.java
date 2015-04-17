import org.neo4j.graphdb.Node;

/**
 * Created by Hyunjun on 2015-04-17.
 */
public interface HypergraphTraversalCallback {
    void onVisit(Node node);
}
