import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created by Hyunjun on 2015-04-17.
 */
public class HypergraphTraversal {
    private GraphDatabaseService graphDb;

    public HypergraphTraversal() {
        graphDb = Application.getGraphDatabase();
    }

    public void traverse(Set<Node> start) {
        Queue<Node> queue = new LinkedList<Node>();
        Set<Long> visited = new HashSet<Long>(); // visited nodes

        for (Node s : start) {
            System.out.println(s.getId());
            visited.add(s.getId());
            queue.add(s);
        }

        while (!queue.isEmpty()) {
            Node v = queue.poll(); // a normal node
            //v.getHyperedge?
            //v.getRelationships()
        }
    }
}
