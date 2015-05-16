package hypergraph.discovery;

import hypergraph.common.Const;
import hypergraph.traversal.HypergraphTraversal;
import org.neo4j.cypher.internal.compiler.v1_9.parser.ParserPattern;
import org.neo4j.graphdb.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hyunun on 2015-05-14.
 */
public class ForwardDiscovery {

    public Set<Node> find(Node s) {
        Set<Node> source = new HashSet<>();
        source.add(s);
        return find(source);
    }

    public Set<Node> find(Set<Node> source) {
        Set<Node> result = new HashSet<>();
//        HypergraphTraversal traversal = new HypergraphTraversal(node -> {
//            if (node.hasLabel(Const.LABEL_STARTABLE)) {
//                result.add(node);
//            }
//        });
//        traversal.traverse(source);
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
