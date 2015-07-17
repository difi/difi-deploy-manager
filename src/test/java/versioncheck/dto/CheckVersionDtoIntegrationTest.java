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
import versioncheck.IOUtil;
import versioncheck.exception.ConnectionFailedException;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static versioncheck.testUtils.CustomAssert.assertApplicationList;
import static versioncheck.testUtils.ObjectMotherApplicationList.createApplicationListWithData;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class CheckVersionDtoIntegrationTest {
    public static final String BASE_DIR = System.getProperty("user.dir");
    private CheckVersionDto checkVersionDto;

    @Autowired Environment environment;
    @Autowired IOUtil ioUtil;

    public static final String TEST_GROUP_ID = "org.springframework";
    public static final String TEST_ARTIFACT_ID = "spring-jdbc";

    public static String failVersionUrl;
    public static String successVersionUrl;

    @Before
    public void setUp() {
        failVersionUrl = environment.getProperty("location.version");
        successVersionUrl = environment.getProperty("location.version")
                .replace("$GROUP_ID", TEST_GROUP_ID)
                .replace("$ARTIFACT_ID", TEST_ARTIFACT_ID);

        checkVersionDto = new CheckVersionDto(environment, ioUtil);
    }

    @Test
    public void should_save_and_retrieve_application_list_that_is_already_downloaded() throws Exception {
        ApplicationList expected = createApplicationListWithData();

        checkVersionDto.savePreviousDownloadedList(expected);
        ApplicationList actual = checkVersionDto.retrievePreviousDownloadedList();

        assertApplicationList(expected, actual);
    }

    @Test
    public void should_save_and_retrieve_application_list_set_for_download() throws Exception {
        ApplicationList expected = createApplicationListWithData();

        checkVersionDto.saveDownloadList(expected);
        ApplicationList actual = checkVersionDto.retrieveDownloadList();

        assertApplicationList(expected, actual);
    }

    @Test(expected = ConnectionFailedException.class)
    public void should_get_connection_exception_when_connection_fails() throws Exception {
        checkVersionDto.retrieveMonitoringAppLastVersion(failVersionUrl);
    }

    @Test
    public void should_retrieve_external_version_when_new_list_is_available() throws Exception {
        JSONObject json = checkVersionDto.retrieveMonitoringAppLastVersion(successVersionUrl);

        assertTrue(json.keySet().contains("version"));
        assertEquals(json.get("groupId"), TEST_GROUP_ID);
    }

    @After
    public void tearDown() {
        checkVersionDto.closeConnection();
    }

    @AfterClass
    public static void tearDownAfterRun() {
        String downloadedFile = BASE_DIR + "/data/monitoring.difi";
        String forDownloadFile = BASE_DIR + "/data/download.difi";

        if (!new File(downloadedFile).delete()) {
            fail(String.format("Cleanup of file %s failed. Manually cleanup necessary!", downloadedFile));
        }
        if (!new File(forDownloadFile).delete()) {
            fail(String.format("Cleanup of folder %s failed. Maually cleanup necessary!", forDownloadFile));
        }
    }
}