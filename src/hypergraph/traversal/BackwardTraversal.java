package hypergraph.traversal;

import hypergraph.Application;
import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created by Hyunjun on 2015-05-06.
 */
public class BackwardTraversal {
    private GraphDatabaseService graphDb;
    private Set<Long> visited;

    public BackwardTraversal() {
        graphDb = HypergraphDatabase.getGraphDatabase();
        visited = new HashSet<>();
    }

    public Set<Node> traverse(Set<Node> targets) {
        Queue<Node> queue = new LinkedList<Node>();
        Set<Node> result = new HashSet<>();

        for (Node t : targets) {
            setVisited(t);
            queue.add(t);
        }

        while (!queue.isEmpty()) {
            Node v = queue.poll();

            // get backward star
            Iterable<Relationship> rels = v.getRelationships(Direction.INCOMING, Const.REL_TO_TARGET);
            for (Relationship rel : rels) {
                Node h = rel.getStartNode();

                // get sources
                Iterable<Relationship> sourceRels = h.getRelationships(Direction.INCOMING, Const.REL_FROM_SOURCE);
                for (Relationship sourceRel : sourceRels) {
                    Node s = sourceRel.getStartNode();
                    if (isVisited(s))
                        continue;;
                    setVisited(s);

                    if (s.hasLabel(Const.LABEL_STARTABLE)) {
                        result.add(s);
                    }
                    else {
                        queue.add(s);
                    }
                }

                // choose one
                break;
            }
        }

        return result;
    }

    private void setVisited(Node node) {
        visited.add(node.getId());
    }

    private boolean isVisited(Node node) {
        return visited.contains(node.getId());
    }
}
