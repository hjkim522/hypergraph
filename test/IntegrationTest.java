import hypergraph.common.HypergraphDatabase;
import hypergraph.data.Importer;
import hypergraph.data.SimpleImporter;
import hypergraph.mss.MinimalSourceSetBuilder;
import hypergraph.mss.NaiveBuilder;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Created by Hyunjun on 2015-05-12.
 */
public class IntegrationTest {

    @Test
    public void testSimple() throws Exception {
        HypergraphDatabase.init("db/test");
        GraphDatabaseService graphDb = HypergraphDatabase.getGraphDatabase();

        Importer importer = new SimpleImporter("input/example-1.txt");
        MinimalSourceSetBuilder builder = new NaiveBuilder();

        importer.run();
        builder.run();

        HypergraphDatabase.close();
    }
//
//    private void testBuildThenFind(Importer importer, MinimalSourceSetBuilder builder, MinimalSourceSetFinder finder) throws Exception {
//        HypergraphDatabase.init("db/test");
//        GraphDatabaseService graphDb = HypergraphDatabase.getGraphDatabase();
//
//        importer.run();
//        builder.run();
//        //finder.find();
//
//        HypergraphDatabase.close();
//    }

//    private find(MinimalSourceSetFinder finder) {
//
//    }
}
