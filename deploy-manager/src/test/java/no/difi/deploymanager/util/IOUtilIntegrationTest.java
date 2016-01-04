package no.difi.deploymanager.util;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.testutils.ObjectMotherApplicationList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

//Requirements for this test class is access to write on disk. Doing a real test for file IO operations.
public class IOUtilIntegrationTest {
    private static final String TESTFOLDER = "/testfolder";
    private static final String TESTFILENAME = "/testfilename.file";
    private static final String SELF_PATH = System.getProperty("user.dir");

    private IOUtil ioUtil;

    @Before
    public void setUp() {
        ioUtil = new IOUtil();
    }

    @Test
    public void should_test_save_and_retrieve_when_called() throws Exception {
        //Using this to secure order tests are being run in.
        should_not_have_path_when_first_run();
        should_create_path_and_file_when_not_existing();
        should_retrieve_object_with_same_data_that_was_saved();
    }

    private void should_not_have_path_when_first_run() {
        assertFalse(new File(SELF_PATH + TESTFOLDER).exists());
    }

    private void should_create_path_and_file_when_not_existing() throws Exception {
        ioUtil.saveApplicationList(ObjectMotherApplicationList.createApplicationListWithData(), TESTFOLDER, TESTFILENAME);

        assertTrue(new File(SELF_PATH + TESTFOLDER).exists());
        assertTrue(new File(SELF_PATH + TESTFOLDER).isDirectory());
        assertTrue(new File(SELF_PATH + TESTFOLDER + TESTFILENAME).isFile());
    }

    private void should_retrieve_object_with_same_data_that_was_saved() throws Exception {
        ApplicationData expected = ObjectMotherApplicationList.createApplicationListWithData().getApplications().get(0);

        ApplicationData actual = ioUtil.retrieveApplicationList(TESTFOLDER, TESTFILENAME).getApplications().get(0);

        assertEquals(expected.getName(), actual.getName());
        assertTrue(expected.getActiveVersion().equals(actual.getActiveVersion()));
    }

    @After
    public void tearDown() {
        if (!new File(SELF_PATH + TESTFOLDER + TESTFILENAME).delete()) {
            fail(String.format("Cleanup of file %s failed. Manually cleanup necessary!", TESTFILENAME));
        }
        if (!new File(SELF_PATH + TESTFOLDER).delete()) {
            fail(String.format("Cleanup of folder %s failed. Manually cleanup necessary!", TESTFOLDER));
        }
    }
}