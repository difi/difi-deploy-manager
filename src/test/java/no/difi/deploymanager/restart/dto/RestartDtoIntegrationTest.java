package no.difi.deploymanager.restart.dto;

import no.difi.deploymanager.application.Application;
import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import no.difi.deploymanager.util.IOUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static no.difi.deploymanager.testutils.CustomAssert.assertApplicationList;
import static no.difi.deploymanager.testutils.ObjectMotherApplicationList.createApplicationListWithData;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class RestartDtoIntegrationTest {
    private RestartDto restartDto;

    @Autowired Environment environment;
    @Autowired IOUtil ioUtil;

    private static final int MILLIS_WAITING_FOR_OTHER_PROCESSES_TO_BE_DONE = 5000;
    private static final String TEST_APPLICATION_FILENAME = "deploy-manager-health-check-0.9.0.jar";
    private static final String TEMP_TEST_JAR_FILE = "./bin/" + TEST_APPLICATION_FILENAME;
    private static final String PERM_TEST_JAR_FILE = "./bin-test/" + TEST_APPLICATION_FILENAME;
    private static String basePath;
    private static String forRestartPathAndFile;
    private static String runningPathAndFile;

    @Before
    public void setUp() {
        basePath = System.getProperty("user.dir") + environment.getRequiredProperty("monitoring.base.path");
        forRestartPathAndFile = basePath + environment.getRequiredProperty("monitoring.forrestart.file");
        runningPathAndFile = basePath + environment.getRequiredProperty("monitoring.running.file");

        restartDto = new RestartDto(environment, ioUtil);
    }

    @Test
    public void should_save_and_retrieve_list_over_applications_to_restart() throws Exception {
        ApplicationList expected = createApplicationListWithData();

        restartDto.saveRestartList(expected);
        ApplicationList actual = restartDto.retrieveRestartList();

        assertApplicationList(expected, actual);
    }

    @Ignore("Long-running test. Un-ignore to test full running cycle.")
    @Test
    public void should_start_and_restart_and_stop_running_process_on_current_OS() throws Exception {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            Runtime.getRuntime().exec(new String[]{"cp", PERM_TEST_JAR_FILE, TEMP_TEST_JAR_FILE});
        }

        ApplicationData application = new ApplicationData();
        application.setName("no.difi.deploymanager");
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
        //Let other processes like startup/stop finish before checking.
        Thread.sleep(MILLIS_WAITING_FOR_OTHER_PROCESSES_TO_BE_DONE);

        URL request = new URL("http://localhost:9999/health");
        String result;

        try {
            HttpURLConnection connection;
            connection = (HttpURLConnection) request.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
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
        File forRestartFile = new File(forRestartPathAndFile);
        File runningFile = new File(runningPathAndFile);

        forRestartFile.delete();
        runningFile.delete();

        if (forRestartFile.exists() || runningFile.exists()) {
            fail(String.format("Cleanup of files in path %s failed. Manually cleanup necessary!", basePath));
        }

        File testJarFile = new File(TEMP_TEST_JAR_FILE);
        testJarFile.delete();
        if (testJarFile.exists()) {
            fail(String.format("Cleanup of test jar %s in bin folder failed. Manually cleanup necessary!", TEMP_TEST_JAR_FILE));
        }
    }
}