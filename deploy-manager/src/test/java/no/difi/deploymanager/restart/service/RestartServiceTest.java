package no.difi.deploymanager.restart.service;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.Status;
import no.difi.deploymanager.domain.StatusCode;
import no.difi.deploymanager.restart.dto.RestartDto;
import no.difi.deploymanager.testutils.ObjectMotherApplicationList;
import no.difi.deploymanager.versioncheck.repository.CheckVersionRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class RestartServiceTest {
    private RestartService service;

    @Mock RestartDto restartDtoMock;
    @Mock CheckVersionRepository checkVersionRepository;

    @Before
    public void setUp() {
        initMocks(this);

        service = new RestartService(restartDtoMock, checkVersionRepository);
    }

    @Test
    public void should_retrieve_restart_list_when_run() throws Exception {
        service.execute();

        verify(restartDtoMock, times(1)).retrieveRestartList();
    }

    @Test
    public void should_get_status_error_when_restart_service_list_gets_io_exception() throws Exception {
        when(restartDtoMock.retrieveRestartList()).thenThrow(new IOException());

        Status result = service.execute().get(0);

        assertEquals(StatusCode.ERROR, result.getStatusCode());
    }

    @Test
    public void should_only_call_start_application_when_no_old_version_is_found() throws IOException {
        when(restartDtoMock.retrieveRestartList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());

        service.execute();

        verify(restartDtoMock, times(1)).startProcess(any(ApplicationData.class));
    }

    @Test
    public void should_call_restart_when_application_is_set_for_restarting() throws Exception {
        when(restartDtoMock.retrieveRestartList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());
        when(checkVersionRepository.retrieveRunningAppsList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());

        service.execute();

        verify(restartDtoMock, times(1)).executeRestart(any(ApplicationData.class), any(ApplicationData.class));
    }

    @Test
    public void should_not_call_restart_when_nothing_to_do() throws Exception {
        when(restartDtoMock.retrieveRestartList()).thenReturn(ObjectMotherApplicationList.createApplicationListEmpty());
        when(checkVersionRepository.retrieveRunningAppsList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());

        service.execute();

        verify(restartDtoMock, never()).executeRestart(any(ApplicationData.class), any(ApplicationData.class));
    }

    @Test
    public void should_get_status_success_when_restart_of_application_has_occured() throws Exception {
        when(restartDtoMock.retrieveRestartList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());
        when(checkVersionRepository.retrieveRunningAppsList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());
        when(restartDtoMock.executeRestart(any(ApplicationData.class), any(ApplicationData.class))).thenReturn(true);

        Status result = service.execute().get(0);

        assertEquals(StatusCode.SUCCESS, result.getStatusCode());
    }
}