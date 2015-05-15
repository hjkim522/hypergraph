package hypergraph.discovery;

import hypergraph.Application;
import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import hypergraph.util.Log;
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

    // to enumerate backward star
    Stack<Node> branchingNode = new Stack<>();
    Map<Long, Integer> nextEdge = new HashMap<>();

    public NaiveBackwardDiscovery() {
        graphDb = HypergraphDatabase.getGraphDatabase();
        visited = new HashSet<>();
    }

    @Override
    public Set<Long> find(Node t) {
        Set<Node> targets = new HashSet<>();
        targets.add(t);

        findOpt(targets);

//        Set<Node> sourceSet = findWithTraversal(targets);
//        printNodes(sourceSet);
//
//        printNodes(findWithTraversal(targets));
//
        Set<Long> result = new HashSet<Long>();
//        for (Node s : sourceSet) {
//            result.add(s.getId());
//        }

        return result;
    }

    public Set<Long> findOpt(Set<Node> targets) {

        do {
            visited = new HashSet<>();
            Set<Node> sources = findWithTraversal(targets);
            printNodes(sources);

            // manage branch
            while (!branchingNode.empty()) {
                Node b = branchingNode.peek();
                int inDegree = b.getDegree(Const.REL_TO_TARGET, Direction.INCOMING);
                int edgeIndex = nextEdge.getOrDefault(b.getId(), 0);

                int name = (int) b.getProperty(Const.PROP_UNIQUE);
                Log.debug("branching node " + name);

                if (!b.hasLabel(Const.LABEL_STARTABLE) && edgeIndex == 0)
                    edgeIndex = 1;

                if (inDegree == edgeIndex) {
                    branchingNode.pop();
                } else if (inDegree > edgeIndex) {
                    nextEdge.put(b.getId(), edgeIndex + 1);
                    break;
                }
            }
        } while (!branchingNode.empty());

        return null;
    }

    private void printNodes(Set<Node> nodes) {
        System.out.print("{");
        for (Node v : nodes) {
            int name = (int) v.getProperty(Const.PROP_UNIQUE);
            System.out.print(name + ", ");
        }
        System.out.println("}");
    }

    private Set<Node> findWithTraversal(Set<Node> targets) {
        Queue<Node> queue = new LinkedList<Node>();
        Set<Node> result = new HashSet<>();

        for (Node t : targets) {
            setVisited(t);
            queue.add(t);
            if (t.hasLabel(Const.LABEL_STARTABLE))
                result.add(t);
        }

        while (!queue.isEmpty()) {
            Node v = queue.poll();
            Node selectedHyperedge = null;

            int inDegree = v.getDegree(Const.REL_TO_TARGET, Direction.INCOMING);
            if (inDegree == 0)
                continue;

            int edgeIndex = nextEdge.getOrDefault(v.getId(), 0);

            // if startable, then can choose stop traversing!
            if (v.hasLabel(Const.LABEL_STARTABLE) && edgeIndex == 0) {
                if (inDegree > 0 && !branchingNode.contains(v)) {
                    branchingNode.push(v);
                }
                continue;
            }

            if (!v.hasLabel(Const.LABEL_STARTABLE) && edgeIndex == 0)
                edgeIndex = 1;

            // selected a hyperedge from backward star
            int edgeCount = 0;
            Iterable<Relationship> toTargets = v.getRelationships(Direction.INCOMING, Const.REL_TO_TARGET);
            for (Relationship toTarget : toTargets) {
                edgeCount++;
                if (edgeCount == edgeIndex) {
                    selectedHyperedge = toTarget.getStartNode();
                    break;
                }
            }

            // insert sources into queue
            Iterable<Relationship> fromSources = selectedHyperedge.getRelationships(Direction.INCOMING, Const.REL_FROM_SOURCE);
            for (Relationship fromSource : fromSources) {
                Node s = fromSource.getStartNode();
                if (!isVisited(s)) {
                    setVisited(s);
                    queue.add(s);
                    if (s.hasLabel(Const.LABEL_STARTABLE))
                        result.add(s);
                }
            }

            // edge remains
//            if (edgeCount < inDegree && !branchingNode.contains(v)) {
            if (inDegree > 1 && !branchingNode.contains(v)) {
                branchingNode.push(v);
            }
        }

        return result;
    }

    // can find any minimal set
    private Set<Node> findAny(Set<Node> targets) {
        Queue<Node> queue = new LinkedList<Node>();
        Set<Node> result = new HashSet<>();

        for (Node t : targets) {
            setVisited(t);
            queue.add(t);
            if (t.hasLabel(Const.LABEL_STARTABLE))
                result.add(t);
        }

        while (!queue.isEmpty()) {
            Node v = queue.poll();
            Node selectedHyperedge = null;
            int inDegree = v.getDegree(Const.REL_TO_TARGET, Direction.INCOMING);
            int edgeSelect = 1; //XXX: temp
            int edgeCount = 0;

            // selected a hyperedge from backward star
            Iterable<Relationship> toTargets = v.getRelationships(Direction.INCOMING, Const.REL_TO_TARGET);
            for (Relationship toTarget : toTargets) {
                edgeCount++;
                if (edgeCount == edgeSelect) {
                    selectedHyperedge = toTarget.getStartNode();
                    break;
                }
            }

            if (selectedHyperedge == null) // or visited
                continue;

            // insert sources into queue
            Iterable<Relationship> fromSources = selectedHyperedge.getRelationships(Direction.INCOMING, Const.REL_FROM_SOURCE);
            for (Relationship fromSource : fromSources) {
                Node s = fromSource.getStartNode();
                if (!isVisited(s)) {
                    setVisited(s);
                    queue.add(s);
                    if (s.hasLabel(Const.LABEL_STARTABLE))
                        result.add(s);
                }
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
