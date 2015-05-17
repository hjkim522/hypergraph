package hypergraph.discovery;

import hypergraph.common.Const;
import hypergraph.traversal.HypergraphTraversal;
import org.neo4j.cypher.internal.compiler.v1_9.parser.ParserPattern;
import org.neo4j.graphdb.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hyunjun on 2015-05-14.
 */
public class ForwardDiscovery {

    public Set<Node> find(Set<Node> source, Node t) {
        Set<Node> target = new HashSet<>();
        target.add(t);
        return find(source, target);
    }

    /**
     * Find reachable nodes among target
     * Following hypergraph traversal rule
     *
     * @param source source set
     * @param target target set
     * @return reachable nodes
     */
    public Set<Node> find(Set<Node> source, Set<Node> target) {
        Set<Node> result = new HashSet<>();
        HypergraphTraversal traversal = new HypergraphTraversal(node -> {
            if (target.contains(node)) {
                result.add(node);
            }
        });
        traversal.traverse(source);
        return result;
    }

    public boolean isReachable(Set<Node> source, Set<Node> target) {
        Set<Long> check = new HashSet<>();
        for (Node t : target)
            check.add(t.getId());

        HypergraphTraversal traversal = new HypergraphTraversal(node -> {
            if (check.contains(node.getId())) {
                check.remove(node.getId());
            }
        });
        traversal.traverse(source);

        return check.isEmpty();
    }
}
