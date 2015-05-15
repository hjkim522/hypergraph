package hypergraph.discovery;

import hypergraph.Application;
import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.*;

/**
 * Created by Hyunjun on 2015-05-06.
 */

//XXX: not implemented yet
public class NaiveBackwardDiscovery implements BackwardDiscovery {
    private GraphDatabaseService graphDb;
    private Set<Long> visited;

    public NaiveBackwardDiscovery() {
        graphDb = HypergraphDatabase.getGraphDatabase();
        visited = new HashSet<>();
    }

    @Override
    public Set<Long> find(Node t) {
        Set<Node> targets = new HashSet<>();
        targets.add(t);
        Set<Node> sourceSet = findWithTraversal(targets);
        Set<Long> result = new HashSet<Long>();
        for (Node s : sourceSet) {
            result.add(s.getId());
        }
        return result;
    }

    // can find a minimal set
    private Set<Node> findWithTraversal(Set<Node> targets) {
        Queue<Node> queue = new LinkedList<Node>();
        Stack<Node> stack = new Stack<>();
        Map<Long, Integer> choice = new HashMap<>();

        for (Node t : targets) {
            setVisited(t);
            queue.add(t);
        }

        while (!queue.isEmpty()) {
            Node v = queue.poll();
            int inDegree = v.getDegree(Const.REL_TO_TARGET, Direction.INCOMING);

            if (inDegree == 1) {

            }


//            // get backward star
//            Iterable<Relationship> rels = v.getRelationships(Direction.INCOMING, Const.REL_TO_TARGET);
//            for (Relationship rel : rels) {
//                Node h = rel.getStartNode();
//
//                // get sources
//                Iterable<Relationship> sourceRels = h.getRelationships(Direction.INCOMING, Const.REL_FROM_SOURCE);
//                for (Relationship sourceRel : sourceRels) {
//                    Node s = sourceRel.getStartNode();
//                    if (isVisited(s))
//                        continue;;
//                    setVisited(s);
//
//                    if (s.hasLabel(Const.LABEL_STARTABLE)) {
//                        result.add(s);
//                    }
//                    else {
//                        queue.add(s);
//                    }
//                }
//
//                // choose one
//                break;
//            }
        }

        return null;//result;
    }

    private void setVisited(Node node) {
        visited.add(node.getId());
    }

    private boolean isVisited(Node node) {
        return visited.contains(node.getId());
    }
}
