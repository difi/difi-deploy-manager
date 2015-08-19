package no.difi.deploymanager.restart.service;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.Self;
import no.difi.deploymanager.domain.Status;
import no.difi.deploymanager.domain.StatusCode;
import no.difi.deploymanager.restart.dao.RestartCommandLine;
import no.difi.deploymanager.restart.dao.RestartDao;
import no.difi.deploymanager.testutils.ObjectMotherApplicationList;
import no.difi.deploymanager.versioncheck.dao.CheckVersionDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class RestartServiceTest {
    private RestartService service;

    @Mock RestartDao restartDao;
    @Mock CheckVersionDao checkVersionDao;
    @Mock private RestartCommandLine restartCommandLineMock;

    @Before
    public void setUp() {
        initMocks(this);

        service = new RestartService(restartDao, restartCommandLineMock, checkVersionDao);
    }

    @Test
    public void should_retrieve_restart_list_when_run() throws Exception {
        service.execute();

        verify(restartDao, times(1)).retrieveRestartList();
    }

    @Test
    public void should_get_status_error_when_restart_service_list_gets_io_exception() throws Exception {
        when(restartDao.retrieveRestartList()).thenThrow(new IOException());

        Status result = service.execute().get(0);

        assertEquals(StatusCode.ERROR, result.getStatusCode());
    }

    @Test
    public void should_only_call_start_application_when_no_old_version_is_found() throws IOException {
        when(restartDao.retrieveRestartList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());

        service.execute();

        verify(restartCommandLineMock, times(1)).startProcess(any(ApplicationData.class));
    }

    @Test
    public void should_call_restart_when_application_is_set_for_restarting() throws Exception {
        when(restartDao.retrieveRestartList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());
        when(checkVersionDao.retrieveRunningAppsList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());

        service.execute();

        verify(restartCommandLineMock, times(1)).executeRestart(any(ApplicationData.class), any(ApplicationData.class), any(Self.class));
    }

    @Test
    public void should_not_call_restart_when_nothing_to_do() throws Exception {
        when(restartDao.retrieveRestartList()).thenReturn(ObjectMotherApplicationList.createApplicationListEmpty());
        when(checkVersionDao.retrieveRunningAppsList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());

        service.execute();

        verify(restartCommandLineMock, never()).executeRestart(any(ApplicationData.class), any(ApplicationData.class), any(Self.class));
    }

    @Test
    public void should_get_status_success_when_restart_of_application_has_occured() throws Exception {
        when(restartDao.retrieveRestartList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());
        when(checkVersionDao.retrieveRunningAppsList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());
        when(restartCommandLineMock.executeRestart(any(ApplicationData.class), any(ApplicationData.class), any(Self.class))).thenReturn(true);

        Status result = service.execute().get(0);

        assertEquals(StatusCode.SUCCESS, result.getStatusCode());
    }
}