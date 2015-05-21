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
    public interface Rule {
        boolean isTarget(Node node);
    }

    public Set<Node> find(Set<Node> source, Rule rule) {
        Set<Node> result = new HashSet<>();
        HypergraphTraversal traversal = new HypergraphTraversal(node -> {
            if (rule.isTarget(node)) {
                result.add(node);
            }
        });
        traversal.traverse(source);
        return result;
    }

    public Set<Node> find(Node s, Rule rule) {
        Set<Node> source = new HashSet<>();
        source.add(s);
        return find(source, rule);
    }

    public Set<Node> find(Set<Node> source, Set<Node> target) {
        return find(source, (node) -> { return target.contains(node); });
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
