import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

/**
 * Created by Hyunjun on 2015-04-17.
 */
public class Const {
    public static final String DatabasePath = "db/graphdb";

    public static final Label HypernodeLabel = DynamicLabel.label("Hypernode");
    public static final RelationshipType HypernodeFromSource = DynamicRelationshipType.withName("fromSource");
    public static final RelationshipType HypernodeToTarget = DynamicRelationshipType.withName("toTarget");

    public static final Label NodeLabel = DynamicLabel.label("Node");
    public static final String NodeUniqueAttr = "name";

    public static final Label StartableLabel = DynamicLabel.label("Startable");
}
