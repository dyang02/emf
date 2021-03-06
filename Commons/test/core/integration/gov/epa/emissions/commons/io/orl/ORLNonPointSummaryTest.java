package gov.epa.emissions.commons.io.orl;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableReader;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.importer.SummaryTable;

import java.io.File;
import java.util.Random;

public class ORLNonPointSummaryTest extends PersistenceTestCase {

    private SqlDataTypes sqlDataTypes;

    private Dataset dataset;

    private Datasource emissionDatasource;

    private Datasource referenceDatasource;

    private DbServer dbServer;

    protected void setUp() throws Exception {
        super.setUp();
        dbServer = dbSetup.getDbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();
        emissionDatasource = dbServer.getEmissionsDatasource();
        referenceDatasource = dbServer.getReferenceDatasource();

        dataset = new SimpleDataset();
        dataset.setName("test");
        dataset.setId(Math.abs(new Random().nextInt()));

    }

    protected void doTearDown() throws Exception {
        DbUpdate dbUpdate = dbSetup.dbUpdate(emissionDatasource);
        dbUpdate.dropTable(emissionDatasource.getName(), dataset.getInternalSources()[0].getTable());
        dbUpdate.dropTable(emissionDatasource.getName(), "test_summary");
    }

    public void testShouldImportASmallAndSimpleNonPointFilesAndCreateSummary() throws Exception {
        try {
            File folder = new File("test/data/orl/nc");
            Importer importer = new ORLNonPointImporter(folder, new String[] { "small-nonpoint-comma.txt" }, dataset,
                    dbServer, sqlDataTypes);
            importer.run();
            SummaryTable summary = new ORLNonPointSummary(emissionDatasource, referenceDatasource, dataset);
            summary.createSummary();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertEquals(6, countRecords(dataset.getInternalSources()[0].getTable()));
        assertEquals(1, countRecords("test_summary"));
    }

    private int countRecords(String tableName) {
        TableReader tableReader = tableReader(emissionDatasource);
        return tableReader.count(emissionDatasource.getName(), tableName);
    }

}
