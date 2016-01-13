package no.difi.deploymanager.artifact;

import no.difi.deploymanager.download.service.DownloadService;
import no.difi.deploymanager.restart.service.RestartService;
import no.difi.deploymanager.versioncheck.service.CheckVersionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class StartupTest {
    private Startup startup;

    @Mock private CheckVersionService checkVersionServiceMock;
    @Mock private DownloadService downloadServiceMock;
    @Mock private RestartService restartServiceMock;

    @Before
    public void setUp() {
        initMocks(this);

        startup = new Startup(checkVersionServiceMock, downloadServiceMock, restartServiceMock);
    }

    @Test
    public void should_execute_full_restart_when_starting() {
        startup.runOnStartup();

        verify(checkVersionServiceMock, times(1)).execute();
        verify(downloadServiceMock, times(1)).execute();
        verify(restartServiceMock, times(1)).execute();
    }

    @Test
    public void should_run_stop_command_when_deploy_manager_is_stopped() {
        startup.forceStop();

        verify(restartServiceMock, times(1)).stopRunningApplications();
    }
}