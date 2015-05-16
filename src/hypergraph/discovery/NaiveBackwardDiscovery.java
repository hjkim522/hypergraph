package hypergraph.discovery;

import hypergraph.Application;
import hypergraph.common.Const;
import hypergraph.common.HypergraphDatabase;
import hypergraph.mss.MinimalSourceSet;
import hypergraph.util.Log;
import org.neo4j.cypher.internal.compiler.v1_9.parser.ParserPattern;
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
    public MinimalSourceSet findMinimal(Set<Node> target) {
        MinimalSourceSet mss = new MinimalSourceSet();

        do {
            visited = new HashSet<>();
            Set<Node> sources = findWithTraversal(target);
//            printNodes(sources);

            if (!sources.isEmpty()) { // empty == unreachable
                Set<Long> result = new HashSet<Long>();
                for (Node s : sources) {
                    result.add(s.getId());
                }
                mss.addSourceSet(result);
            }

            // manage branch
            while (!branchingNode.empty()) {
                Node b = branchingNode.peek();
                int inDegree = b.getDegree(Const.REL_TO_TARGET, Direction.INCOMING);
                int edgeIndex = nextEdge.getOrDefault(b.getId(), 0);

//                int name = (int) b.getProperty(Const.PROP_UNIQUE);
//                Log.debug("branching node " + name);

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

        MinimalSourceSet result = new MinimalSourceSet();
        for (Set<Long> sourceSet : mss.getSourceSets()) {
            Set<Node> sources = toNodeSet(sourceSet);
            ForwardDiscovery discovery = new ForwardDiscovery();
            if (discovery.isReachable(sources, target)) {
                result.getSourceSets().add(sourceSet);
            }
        }

        return result;
    }

    private Set<Node> toNodeSet(Set<Long> sourceSet) {
        Set<Node> nodeSet = new HashSet<>();
        for (Long s : sourceSet) {
            Node v = graphDb.getNodeById(s);
            nodeSet.add(v);
        }
        return nodeSet;
    }

    private void printNodes(Set<Node> nodes) {
        System.out.print("{");
        for (Node v : nodes) {
            String name = (String) v.getProperty(Const.PROP_UNIQUE);
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
        }

        while (!queue.isEmpty()) {
            Node v = queue.poll();
            Node selectedHyperedge = null;

            int inDegree = v.getDegree(Const.REL_TO_TARGET, Direction.INCOMING);
            int edgeIndex = nextEdge.getOrDefault(v.getId(), 0);

            // if startable, then can choose stop traversing!
            if (v.hasLabel(Const.LABEL_STARTABLE) && edgeIndex == 0) {
                result.add(v);
                if (inDegree > 0 && !branchingNode.contains(v)) {
                    branchingNode.push(v);
                }
                continue;
            }

            if (inDegree == 0)
                continue;
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
    @Deprecated
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
