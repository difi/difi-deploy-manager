package no.difi.deploymanager.versioncheck.dao;

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
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest(value = {"spring.profiles.active=production", "server.port=9002"})
@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class CheckVersionDaoIntegrationTest {
    private static final String TEST_VERSION = "Version";
    private CheckVersionDao checkVersionDao;

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

        checkVersionDao = new CheckVersionDao(environment, ioUtil, jsonUtil);
    }

    @Test
    public void should_save_and_retrieve_monitoring_application_list() throws Exception {
        ApplicationList expected = ObjectMotherApplicationList.createApplicationListWithData();

        checkVersionDao.saveRunningAppsList(expected);
        ApplicationList actual = checkVersionDao.retrieveRunningAppsList();

        CustomAssert.assertApplicationList(expected, actual);
    }

    @Test(expected = ConnectionFailedException.class)
    public void should_get_connection_exception_when_connection_fails() throws Exception {
        checkVersionDao.retrieveExternalArtifactStatus("", "", "");
    }

    @Test
    public void should_retrieve_external_version_when_new_list_is_available() throws Exception {
        JSONObject json = checkVersionDao.retrieveExternalArtifactStatus(TEST_GROUP_ID, TEST_ARTIFACT_ID, TEST_VERSION);

        assertTrue(json.keySet().contains("version"));
        assertEquals(json.get("groupId"), TEST_GROUP_ID);
    }

    @AfterClass
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void tearDownAfterRun() {
        File file = new File(TEST_PATH);
        file.delete();
        if (file.exists()) {
            fail(String.format("Cleanup of file %s failed. Manually cleanup necessary!", TEST_PATH));
        }
    }
}