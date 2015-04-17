import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

/**
 * Created by Hyunjun on 2015-04-17.
 */
public class Const {
    public static final String DB_PATH = "db/graphdb";

    public static final Label LABEL_NODE = DynamicLabel.label("Node");
    public static final Label LABEL_HYPERNODE = DynamicLabel.label("Hypernode");
    public static final Label LABEL_STARTABLE = DynamicLabel.label("Startable");

    public static final RelationshipType REL_FROM_SOURCE = DynamicRelationshipType.withName("fromSource");
    public static final RelationshipType REL_TO_TARGET = DynamicRelationshipType.withName("toTarget");

    public static final String PROP_UNIQUE = "name";
    public static final String PROP_MSS = "mss";
}
