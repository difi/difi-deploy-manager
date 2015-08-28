package no.difi.deploymanager.restart.dao;

import no.difi.deploymanager.artifact.Application;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class RestartCommandLineIntegrationTest {
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
    private RestartCommandLine restartCommandLine;

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired Environment environment;

    private static final String TEST_APPLICATION_FILENAME = "deploy-manager-health-check-0.9.0.jar";
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