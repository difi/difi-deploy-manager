package no.difi.deploymanager.restart.service;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.Self;
import no.difi.deploymanager.restart.dao.RestartCommandLine;
import no.difi.deploymanager.restart.dao.RestartDao;
import no.difi.deploymanager.testutils.ObjectMotherApplicationList;
import no.difi.deploymanager.versioncheck.service.CheckVersionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class RestartServiceTest {
    private static final String TEST_PROCESS_ID = "123";
    private RestartService service;

    @Mock private RestartDao restartDaoMock;
    @Mock private RestartCommandLine restartCommandLineMock;
    @Mock private CheckVersionService checkVersionServiceMock;

    @Before
    public void setUp() {
        initMocks(this);

        service = new RestartService(restartDaoMock, restartCommandLineMock, checkVersionServiceMock);
    }

    @Test
    public void should_retrieve_restart_list_when_run() throws Exception {
        service.execute();

        verify(restartDaoMock, times(1)).retrieveRestartList();
    }

    @Test
    public void should_only_call_start_application_when_no_old_version_is_found() throws Exception {
        when(restartDaoMock.retrieveRestartList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());
        when(restartCommandLineMock.findProcessId(any(ApplicationData.class))).thenReturn(TEST_PROCESS_ID);

        service.execute();

        verify(restartCommandLineMock, times(1)).startProcess(any(ApplicationData.class));
    }

    @Test
    public void should_call_restart_when_application_is_set_for_restarting() throws Exception {
        when(restartDaoMock.retrieveRestartList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());
        when(checkVersionServiceMock.retrieveRunningAppsList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());
        when(restartCommandLineMock.findProcessId(any(ApplicationData.class))).thenReturn(TEST_PROCESS_ID);

        service.execute();

        verify(restartCommandLineMock, times(1)).executeRestart(any(ApplicationData.class), any(ApplicationData.class), any(Self.class));
    }

    @Test
    public void should_not_call_restart_when_nothing_to_do() throws Exception {
        when(restartDaoMock.retrieveRestartList()).thenReturn(ObjectMotherApplicationList.createApplicationListEmpty());
        when(checkVersionServiceMock.retrieveRunningAppsList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());
        when(restartCommandLineMock.findProcessId(any(ApplicationData.class))).thenReturn(TEST_PROCESS_ID);

        service.execute();

        verify(restartCommandLineMock, never()).executeRestart(any(ApplicationData.class), any(ApplicationData.class), any(Self.class));
    }

    @Test
    public void should_try_restart_when_process_id_not_found_indicating_that_application_is_not_running() throws Exception {
        when(restartDaoMock.retrieveRestartList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());
        when(checkVersionServiceMock.retrieveRunningAppsList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());
        when(restartCommandLineMock.findProcessId(any(ApplicationData.class))).thenReturn("");

        service.execute();

        verify(restartCommandLineMock, times(1)).startProcess(any(ApplicationData.class));
    }
}