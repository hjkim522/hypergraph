package hypergraph.data;

import hypergraph.Application;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Created by hyunjun on 2015. 5. 7..
 */
public class KeggImporter {
    private GraphDatabaseService graphDb;

    public KeggImporter() {
        graphDb = Application.getGraphDatabase();
    }

    
}
