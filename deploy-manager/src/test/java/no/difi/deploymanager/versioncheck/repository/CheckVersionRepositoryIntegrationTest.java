package no.difi.deploymanager.versioncheck.repository;

import no.difi.deploymanager.artifact.Application;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.testutils.CustomAssert;
import no.difi.deploymanager.testutils.ObjectMotherApplicationList;
import no.difi.deploymanager.util.IOUtil;
import no.difi.deploymanager.util.JsonUtil;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class CheckVersionRepositoryIntegrationTest {
    private CheckVersionRepository checkVersionRepository;

    @Autowired Environment environment;
    @Autowired IOUtil ioUtil;
    @Autowired JsonUtil jsonUtil;

    private static String TEST_PATH;
    private static final String TEST_GROUP_ID = "org.springframework";
    private static final String TEST_ARTIFACT_ID = "spring-jdbc";

    @Before
    public void setUp() {
        TEST_PATH = System.getProperty("user.dir")
                + environment.getRequiredProperty("monitoring.base.path")
                + environment.getRequiredProperty("monitoring.running.file");

        checkVersionRepository = new CheckVersionRepository(environment, ioUtil, jsonUtil);
    }

    @Test
    public void should_save_and_retrieve_monitoring_application_list() throws Exception {
        ApplicationList expected = ObjectMotherApplicationList.createApplicationListWithData();

        checkVersionRepository.saveRunningAppsList(expected);
        ApplicationList actual = checkVersionRepository.retrieveRunningAppsList();

        CustomAssert.assertApplicationList(expected, actual);
    }

    @Test(expected = ConnectionFailedException.class)
    public void should_get_connection_exception_when_connection_fails() throws Exception {
        checkVersionRepository.retrieveExternalArtifactStatus("", "");
    }

    @Test
    public void should_retrieve_external_version_when_new_list_is_available() throws Exception {
        JSONObject json = checkVersionRepository.retrieveExternalArtifactStatus(TEST_GROUP_ID, TEST_ARTIFACT_ID);

        assertTrue(json.keySet().contains("version"));
        assertEquals(json.get("groupId"), TEST_GROUP_ID);
    }

    @AfterClass
    public static void tearDownAfterRun() {
        File file = new File(TEST_PATH);
        file.delete();
        if (file.exists()) {
            fail(String.format("Cleanup of file %s failed. Manually cleanup necessary!", TEST_PATH));
        }
    }
}