package no.difi.deploymanager.restart.dao;

import no.difi.deploymanager.artifact.Application;
import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.Self;
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@WebIntegrationTest(value = {"application.runtime.status=production", "spring.boot.admin.url=localhost:8090"})
@SpringApplicationConfiguration(classes = Application.class)
public class RestartCommandLineIntegrationTest {
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
    private RestartCommandLine restartCommandLine;

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired Environment environment;

    private static final String TEST_APPLICATION_FILENAME = "deploy-manager-health-check-0.9.25.jar";
    private static final String TEMP_TEST_JAR_FILE = "./bin/" + TEST_APPLICATION_FILENAME;
    private static final String PERM_TEST_JAR_FILE = "./bin-test/" + TEST_APPLICATION_FILENAME;
    private static final String TEMP_TEST_JAR_FILE_WIN = System.getProperty("user.dir") + "\\bin\\" + TEST_APPLICATION_FILENAME;
    private static final String PERM_TEST_JAR_FILE_WIN = System.getProperty("user.dir") + "\\bin-test\\" + TEST_APPLICATION_FILENAME;
    private static String basePath;
    private static String runningPathAndFile;

    @Before
    public void setUp() {
        basePath = System.getProperty("user.dir") + environment.getRequiredProperty("monitoring.base.path");
        runningPathAndFile = basePath + environment.getRequiredProperty("monitoring.running.file");

        if (IS_WINDOWS) {
            basePath = basePath.replace("/", "\\");
            runningPathAndFile = runningPathAndFile.replace("/", "\\");
        }

        restartCommandLine = new RestartCommandLine(environment);
    }

    @Test
    public void should_start_and_restart_and_stop_running_process_on_current_OS() throws Exception {
        if (!IS_WINDOWS) {
            Runtime.getRuntime().exec(new String[]{"cp", PERM_TEST_JAR_FILE, TEMP_TEST_JAR_FILE});
        }
        else {
            Runtime.getRuntime().exec("cmd /c copy " + PERM_TEST_JAR_FILE_WIN + " "  + TEMP_TEST_JAR_FILE_WIN);
        }

        ApplicationData oldApp = new ApplicationData.Builder()
                .name("no.difi.deploymanager")
                .filename(TEST_APPLICATION_FILENAME)
                .activeVersion("0.9.1")
                .build();

        ApplicationData newApp = new ApplicationData.Builder()
                .name("no.difi.deploymanager")
                .filename(TEST_APPLICATION_FILENAME)
                .activeVersion("0.9.2")
                .build();

        Self self = new Self.Builder()
                .name("no.no.no")
                .version("0.9.3")
                .build();

        assertTrue(restartCommandLine.startProcess(newApp));
        assertTrue(restartCommandLine.executeRestart(oldApp, newApp, self));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @AfterClass
    public static void tearDownAfterRun() {
        File runningFile = new File(runningPathAndFile);

        runningFile.delete();

        if (runningFile.exists()) {
            fail(String.format("Cleanup of files in path %s failed. Manually cleanup necessary!", basePath));
        }

        File testJarFile = new File(TEMP_TEST_JAR_FILE);
        testJarFile.delete();
        if (testJarFile.exists()) {
            fail(String.format("Cleanup of test jar %s in bin folder failed. Manually cleanup necessary!", TEMP_TEST_JAR_FILE));
        }
    }
}