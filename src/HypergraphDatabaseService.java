import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.graphdb.traversal.BidirectionalTraversalDescription;
import org.neo4j.graphdb.traversal.TraversalDescription;

import java.util.Map;

/**
 * Created by Hyunjun on 2015-04-15.
 */
public class HypergraphDatabaseService implements GraphDatabaseService {
    private GraphDatabaseService graphDb;

    public HypergraphDatabaseService(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    public Hyperedge createHyperedge(Node target, Node... source) {
        Node hypernode = graphDb.createNode(DynamicLabel.label("Hypernode"));
        for (Node s : source) {
            s.createRelationshipTo(hypernode, DynamicRelationshipType.withName("fromSource"));
        }
        hypernode.createRelationshipTo(target, DynamicRelationshipType.withName("toTarget"));
        return null;
    }

    @Override
    public Node createNode() {
        return graphDb.createNode();
    }

    @Override
    public Node createNode(Label... labels) {
        return graphDb.createNode(labels);
    }

    @Override
    public Node getNodeById(long l) {
        return null;
    }

    @Override
    public Relationship getRelationshipById(long l) {
        return null;
    }

    @Override
    public Iterable<Node> getAllNodes() {
        return null;
    }

    @Override
    public ResourceIterator<Node> findNodes(Label label, String s, Object o) {
        return null;
    }

    @Override
    public Node findNode(Label label, String s, Object o) {
        return null;
    }

    @Override
    public ResourceIterator<Node> findNodes(Label label) {
        return null;
    }

    @Override
    public ResourceIterable<Node> findNodesByLabelAndProperty(Label label, String s, Object o) {
        return null;
    }

    @Override
    public Iterable<RelationshipType> getRelationshipTypes() {
        return null;
    }

    @Override
    public boolean isAvailable(long l) {
        return false;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public Transaction beginTx() {
        return null;
    }

    @Override
    public Result execute(String s) throws QueryExecutionException {
        return null;
    }

    @Override
    public Result execute(String s, Map<String, Object> map) throws QueryExecutionException {
        return null;
    }

    @Override
    public <T> TransactionEventHandler<T> registerTransactionEventHandler(TransactionEventHandler<T> transactionEventHandler) {
        return null;
    }

    @Override
    public <T> TransactionEventHandler<T> unregisterTransactionEventHandler(TransactionEventHandler<T> transactionEventHandler) {
        return null;
    }

    @Override
    public KernelEventHandler registerKernelEventHandler(KernelEventHandler kernelEventHandler) {
        return null;
    }

    @Override
    public KernelEventHandler unregisterKernelEventHandler(KernelEventHandler kernelEventHandler) {
        return null;
    }

    @Override
    public Schema schema() {
        return null;
    }

    @Override
    public IndexManager index() {
        return null;
    }

    @Override
    public TraversalDescription traversalDescription() {
        return null;
    }

    @Override
    public BidirectionalTraversalDescription bidirectionalTraversalDescription() {
        return null;
    }
}
