package no.difi.deploymanager.restart.dto;

import no.difi.deploymanager.artifact.Application;
import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.testutils.ObjectMotherApplicationList;
import no.difi.deploymanager.util.IOUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

import static no.difi.deploymanager.testutils.CustomAssert.assertApplicationList;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class RestartDtoIntegrationTest {
    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
    private RestartDto restartDto;

    @Autowired Environment environment;
    @Autowired IOUtil ioUtil;

    private static final int MILLIS_WAITING_FOR_OTHER_PROCESSES_TO_BE_DONE = 5000;
    private static final String TEST_APPLICATION_FILENAME = "deploy-manager-health-check-0.9.0.jar";
    private static final String TEMP_TEST_JAR_FILE = "./bin/" + TEST_APPLICATION_FILENAME;
    private static final String PERM_TEST_JAR_FILE = "./bin-test/" + TEST_APPLICATION_FILENAME;
    private static final String TEMP_TEST_JAR_FILE_WIN = System.getProperty("user.dir") + "\\bin\\" + TEST_APPLICATION_FILENAME;
    private static final String PERM_TEST_JAR_FILE_WIN = System.getProperty("user.dir") + "\\bin-test\\" + TEST_APPLICATION_FILENAME;
    private static String basePath;
    private static String forRestartPathAndFile;
    private static String runningPathAndFile;

    @Before
    public void setUp() {
        basePath = System.getProperty("user.dir") + environment.getRequiredProperty("monitoring.base.path");
        forRestartPathAndFile = basePath + environment.getRequiredProperty("monitoring.forrestart.file");
        runningPathAndFile = basePath + environment.getRequiredProperty("monitoring.running.file");

        if (IS_WINDOWS) {
            basePath = basePath.replace("/", "\\");
            forRestartPathAndFile = forRestartPathAndFile.replace("/", "\\");
            runningPathAndFile = runningPathAndFile.replace("/", "\\");
        }

        restartDto = new RestartDto(environment, ioUtil);
    }

    @Test
    public void should_save_and_retrieve_list_over_applications_to_restart() throws Exception {
        ApplicationList expected = ObjectMotherApplicationList.createApplicationListWithData();

        restartDto.saveRestartList(expected);
        ApplicationList actual = restartDto.retrieveRestartList();

        assertApplicationList(expected, actual);
    }

    @Test
    public void should_start_and_restart_and_stop_running_process_on_current_OS() throws Exception {
        if (!IS_WINDOWS) {
            Runtime.getRuntime().exec(new String[]{"cp", PERM_TEST_JAR_FILE, TEMP_TEST_JAR_FILE});
        }
        else {
            Runtime.getRuntime().exec("cmd /c copy " + PERM_TEST_JAR_FILE_WIN + " "  + TEMP_TEST_JAR_FILE_WIN);
        }

        ApplicationData application = new ApplicationData();
        application.setName("no.difi.deploymanager");
        application.setFilename(TEST_APPLICATION_FILENAME);
        application.setActiveVersion("0.9.1");

        assertTrue(restartDto.startProcess(application));
        assertTrue(restartDto.executeRestart(application, application));
        assertTrue(restartDto.stopProcess(application));
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