package no.difi.deploymanager.restart.dao;

import no.difi.deploymanager.artifact.Application;
import no.difi.deploymanager.domain.ApplicationList;
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

import static no.difi.deploymanager.testutils.CustomAssert.assertApplicationList;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@WebIntegrationTest(value = {"application.runtime.status=production", "spring.boot.admin.url=localhost:8090"})
@SpringApplicationConfiguration(classes = Application.class)
@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class RestartDaoIntegrationTest {
    private RestartDao restartDao;

    @Autowired private Environment environment;
    @Autowired private IOUtil ioUtil;

    private static String basePath;
    private static String forRestartPathAndFile;

    @Before
    public void setUp() {
        basePath = System.getProperty("user.dir") + environment.getRequiredProperty("monitoring.base.path");
        forRestartPathAndFile = basePath + environment.getRequiredProperty("monitoring.forrestart.file");

        restartDao = new RestartDao(environment, ioUtil);
    }

    @Test
    public void should_save_and_retrieve_list_over_applications_to_restart() throws Exception {
        ApplicationList expected = ObjectMotherApplicationList.createApplicationListWithData();

        restartDao.saveRestartList(expected);
        ApplicationList actual = restartDao.retrieveRestartList();

        assertApplicationList(expected, actual);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @AfterClass
    public static void tearDownAfterRun() {
        File forRestartFile = new File(forRestartPathAndFile);

        forRestartFile.delete();

        if (forRestartFile.exists()) {
            fail(String.format("Cleanup of files in path %s failed. Manually cleanup necessary!", basePath));
        }
    }
}