package no.difi.deploymanager.download.dto;

import no.difi.deploymanager.application.Application;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.domain.MonitoringApplications;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import no.difi.deploymanager.util.Common;
import no.difi.deploymanager.util.IOUtil;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;

import java.io.File;

import static org.junit.Assert.assertTrue;
import static no.difi.deploymanager.testutils.CustomAssert.assertApplicationList;
import static no.difi.deploymanager.testutils.ObjectMotherApplicationList.createApplicationListWithData;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class DownloadDtoIntegrationTest {
    private DownloadDto downloadDto;

    @Autowired Environment environment;
    @Autowired IOUtil ioUtil;

    private static final String BASE_DIR = System.getProperty("user.dir");
    private static String downloadPath;
    private static String failVersionUrl;
    private static String successVersionUrl;

    String cleanFilename;

    @Before
    public void setUp() {
        downloadPath = BASE_DIR + environment.getProperty("download.base.path");
        failVersionUrl = environment.getProperty("location.download");
        successVersionUrl = Common.replacePropertyParams(
                environment.getProperty("location.download"),
                MonitoringApplications.SPRINGFRAMEWORK_JDBC.getGroupId(),
                MonitoringApplications.SPRINGFRAMEWORK_JDBC.getArtifactId()
        );

        downloadDto = new DownloadDto(environment, ioUtil);
    }

    @Test
    public void should_save_and_retrieve_application_list_that_is_already_downloaded() throws Exception {
        ApplicationList expected = createApplicationListWithData();

        downloadDto.saveDownloadList(expected);
        ApplicationList actual = downloadDto.retrieveDownloadList();

        assertApplicationList(expected, actual);
    }

    @Test(expected = ConnectionFailedException.class)
    public void should_get_connection_exception_when_connection_fails() throws Exception {
        downloadDto.downloadApplication(failVersionUrl);
    }

    @Test
    public void should_retrieve_requested_artifact_and_put_it_in_correct_folder() throws Exception {
        downloadDto.downloadApplication(successVersionUrl);
        boolean folder = new File(downloadPath).exists();
        File[] files = new File(downloadPath).listFiles();

        assert files != null;
        cleanFilename = files[0].getName();

        assertTrue(folder);
        assertTrue(cleanFilename.length() > 0);
    }

    @AfterClass
    public static void tearDownAfterRun() {
        String forDownloadFile = BASE_DIR + "/data/download.difi";
        File binFolder = new File(downloadPath);

        for(File file : binFolder.listFiles()) {
            file.delete();
        }

        new File(forDownloadFile).delete();
    }
}