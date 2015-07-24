package restart.dto;

import application.Application;
import domain.ApplicationData;
import domain.ApplicationList;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import util.IOUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import static domain.MonitoringApplications.SPRINGFRAMEWORK_JDBC;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static versioncheck.testUtils.CustomAssert.assertApplicationList;
import static versioncheck.testUtils.ObjectMotherApplicationList.createApplicationListWithData;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class RestartDtoIntegrationTest {
    private static final String TEST_APPLICATION_FILENAME = "deploy-manager-health-check-0.9.0.jar";
    private static final int MILLIS_WAITING_FOR_OTHER_PROCESSES = 5000;
    public static final String TEMP_TEST_JAR_FILE = "./bin/deploy-manager-health-check-0.9.0.jar";
    public static final String PERM_TEST_JAR_FILE = "./bin-test/deploy-manager-health-check-0.9.0.jar";

    private RestartDto restartDto;

    @Autowired Environment environment;
    @Autowired IOUtil ioUtil;

    private static final String BASE_DIR = System.getProperty("user.dir");

    @Before
    public void setUp() {
        restartDto = new RestartDto(environment, ioUtil);
    }

    @Test
    public void should_save_and_retrieve_list_over_applications_to_restart() throws Exception {
        ApplicationList expected = createApplicationListWithData();

        restartDto.saveRestartList(expected);
        ApplicationList actual = restartDto.retrieveRestartList();

        assertApplicationList(expected, actual);
    }

    @Test
    public void should_start_and_restart_and_stop_running_process_on_current_OS() throws Exception {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            Runtime.getRuntime().exec(new String[]{"cp", PERM_TEST_JAR_FILE, TEMP_TEST_JAR_FILE});
        }

        ApplicationData application = new ApplicationData();
        application.setName(SPRINGFRAMEWORK_JDBC);
        application.setFilename(TEST_APPLICATION_FILENAME);
        application.setActiveVersion("0.9.0");

        assertTrue(restartDto.startProcess(application));
        assertTrue(applicationIsRunning());
        assertTrue(restartDto.executeRestart(application, application));
        assertTrue(applicationIsRunning());
        assertTrue(restartDto.stopProcess(application));
        assertFalse(applicationIsRunning());
    }

    private boolean applicationIsRunning() throws InterruptedException, MalformedURLException {
        Thread.sleep(MILLIS_WAITING_FOR_OTHER_PROCESSES); //Let other processes like startup/stop finish before checking.

        URL request = new URL("http://localhost:9999/health");
        String result;

        try {
            HttpURLConnection connection;
            connection = (HttpURLConnection) request.openConnection();
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return false;
            }
            InputStream stream = new BufferedInputStream(connection.getInputStream());
            result = new Scanner(stream).useDelimiter("\\A").next();

        } catch (IOException e) {
            return false;
        }

        return result.contains("status") && result.contains("UP");
    }

    @AfterClass
    public static void tearDownAfterRun() {
        String monitoring = BASE_DIR + "/data/updating.difi";

        new File(monitoring).delete();
        new File(TEMP_TEST_JAR_FILE).delete();
    }
}