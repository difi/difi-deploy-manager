package no.difi.deploymanager.download.dao;

import no.difi.deploymanager.artifact.Application;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.testutils.CustomAssert;
import no.difi.deploymanager.testutils.ObjectMotherApplicationList;
import no.difi.deploymanager.util.IOUtil;
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

import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest(value = {"download.source=production", "spring.boot.admin.url=localhost:8090"})
@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class DownloadDaoIntegrationTest {
    private DownloadDao downloadDao;

    @Autowired Environment environment;
    @Autowired IOUtil ioUtil;

    private static String testDataPath;

    @Before
    public void setUp() {
        testDataPath = System.getProperty("user.dir")
                + environment.getRequiredProperty("monitoring.base.path")
                + environment.getRequiredProperty("monitoring.fordownload.file");

        downloadDao = new DownloadDao(environment, ioUtil);
    }

    @Test
    public void should_save_and_retrieve_application_list_that_is_already_downloaded() throws Exception {
        ApplicationList expected = ObjectMotherApplicationList.createApplicationListWithData();

        downloadDao.saveDownloadList(expected);
        ApplicationList actual = downloadDao.retrieveDownloadList();

        CustomAssert.assertApplicationList(expected, actual);
    }

    @AfterClass
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void tearDownAfterRun() {
        String forDownloadFile = testDataPath;

        File downloadFile = new File(forDownloadFile);
        downloadFile.delete();
        if (downloadFile.exists()) {
            fail(String.format("Cleanup of file %s failed. Manually cleanup necessary!", testDataPath));
        }
    }
}