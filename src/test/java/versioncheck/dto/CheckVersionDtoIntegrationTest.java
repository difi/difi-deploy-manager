package versioncheck.dto;

import application.Application;
import domain.ApplicationList;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import util.Common;
import util.IOUtil;
import versioncheck.exception.ConnectionFailedException;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static versioncheck.testUtils.CustomAssert.assertApplicationList;
import static versioncheck.testUtils.ObjectMotherApplicationList.createApplicationListWithData;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class CheckVersionDtoIntegrationTest {
    private CheckVersionDto checkVersionDto;

    @Autowired Environment environment;
    @Autowired IOUtil ioUtil;

    private static final String BASE_DIR = System.getProperty("user.dir");
    private static final String TEST_GROUP_ID = "org.springframework";
    private static final String TEST_ARTIFACT_ID = "spring-jdbc";
    private static String failVersionUrl;
    private static String successVersionUrl;

    @Before
    public void setUp() {
        failVersionUrl = environment.getProperty("location.version");
        successVersionUrl = Common.replacePropertyParams(
                environment.getProperty("location.version"),
                TEST_GROUP_ID,
                TEST_ARTIFACT_ID
        );

        checkVersionDto = new CheckVersionDto(environment, ioUtil);
    }

    @Test
    public void should_save_and_retrieve_monitoring_application_list() throws Exception {
        ApplicationList expected = createApplicationListWithData();

        checkVersionDto.saveMonitoringList(expected);
        ApplicationList actual = checkVersionDto.retrieveMonitoringList();

        assertApplicationList(expected, actual);
    }

    @Test(expected = ConnectionFailedException.class)
    public void should_get_connection_exception_when_connection_fails() throws Exception {
        checkVersionDto.retrieveExternalArtifactStatus(failVersionUrl);
    }

    @Test
    public void should_retrieve_external_version_when_new_list_is_available() throws Exception {
        JSONObject json = checkVersionDto.retrieveExternalArtifactStatus(successVersionUrl);

        assertTrue(json.keySet().contains("version"));
        assertEquals(json.get("groupId"), TEST_GROUP_ID);
    }

    @After
    public void tearDown() {
        checkVersionDto.closeConnection();
    }

    @AfterClass
    public static void tearDownAfterRun() {
        String monitoring = BASE_DIR + "/data/monitoring.difi";

        if (!new File(monitoring).delete()) {
            fail(String.format("Cleanup of file %s failed. Manually cleanup necessary!", monitoring));
        }
    }
}