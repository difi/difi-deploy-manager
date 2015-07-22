package restart.dto;

import application.Application;
import domain.ApplicationData;
import domain.ApplicationList;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import util.IOUtil;
import versioncheck.testUtils.CustomAssert;
import versioncheck.testUtils.ObjectMotherApplicationList;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.fail;
import static versioncheck.testUtils.CustomAssert.assertApplicationList;
import static versioncheck.testUtils.ObjectMotherApplicationList.createApplicationListWithData;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class RestartDtoIntegrationTest {
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

    @Ignore("Not implemented yet")
    @Test
    public void testing() throws IOException {
        ApplicationData applicationData = new ApplicationData();
        restartDto.executeRestart(applicationData);
    }

    @AfterClass
    public static void tearDownAfterRun() {
        String monitoring = BASE_DIR + "/data/updating.difi";

        new File(monitoring).delete();
    }
}