package hypergraph.data;

import hypergraph.common.HypergraphDatabase;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Created by Hyunjun on 2015-06-04.
 */
class SyntheticImporter implements Importer {
    private String filename;
    private GraphDatabaseService graphDb;

    public SyntheticImporter(String filename) {
        this.filename = filename;
        this.graphDb = HypergraphDatabase.getGraphDatabase();
    }

    @Override
    public void run() {
        
    }
}
