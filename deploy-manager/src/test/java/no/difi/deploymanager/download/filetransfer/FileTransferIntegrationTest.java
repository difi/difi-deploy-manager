package no.difi.deploymanager.download.filetransfer;

import no.difi.deploymanager.artifact.Application;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

import static no.difi.deploymanager.util.Common.replacePropertyParams;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class FileTransferIntegrationTest {
    private FileTransfer fileTransfer;

    @Autowired
    private Environment environment;

    private static final String BASE_DIR = System.getProperty("user.dir");

    private static String failVersionUrl;
    private static String successVersionUrl;
    private static String downloadPath;

    @Before
    public void setUp() {
        failVersionUrl = environment.getProperty("location.download");
        successVersionUrl = replacePropertyParams(environment.getProperty("location.download"), "org.springframework", "spring-jdbc");
        downloadPath = BASE_DIR + environment.getProperty("download.base.path");

        fileTransfer = new FileTransfer(environment);
    }

    @Test(expected = ConnectionFailedException.class)
    public void should_get_connection_exception_when_connection_fails() throws Exception {
        fileTransfer.downloadApplication(failVersionUrl);
    }

    @Test
    public void should_retrieve_requested_artifact_and_put_it_in_correct_folder() throws Exception {
        fileTransfer.downloadApplication(successVersionUrl);
        boolean folder = new File(downloadPath).exists();
        File[] files = new File(downloadPath).listFiles();

        assert files != null;
        String cleanFilename = files[0].getName();

        assertTrue(folder);
        assertTrue(cleanFilename.length() > 0);
    }

    @AfterClass
    public static void tearDownAfterRun() {
        File binFolder = new File(downloadPath);

        for(File file : binFolder.listFiles()) {
            file.delete();
        }
    }
}