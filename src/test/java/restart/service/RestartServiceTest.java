package restart.service;

import domain.ApplicationData;
import domain.Status;
import domain.StatusCode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import restart.dto.RestartDto;
import versioncheck.testUtils.ObjectMotherApplicationList;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static versioncheck.testUtils.ObjectMotherApplicationList.*;

public class RestartServiceTest {
    private RestartService service;

    @Mock
    RestartDto restartDtoMock;

    @Before
    public void setUp() {
        initMocks(this);

        service = new RestartService(restartDtoMock);
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
    public void should_call_restart_when_application_is_set_for_restarting() throws Exception {
        when(restartDtoMock.retrieveRestartList()).thenReturn(createApplicationListWithData());

        service.execute();

        verify(restartDtoMock, times(1)).executeRestart(any(ApplicationData.class), any(ApplicationData.class));
    }

    @Test
    public void should_not_call_restart_when_nothing_to_do() throws Exception {
        when(restartDtoMock.retrieveRestartList()).thenReturn(createApplicationListEmpty());

        service.execute();

        verify(restartDtoMock, never()).executeRestart(any(ApplicationData.class), any(ApplicationData.class));
    }

    @Test
    public void should_get_status_success_when_restart_of_application_has_occured() throws Exception {
        when(restartDtoMock.retrieveRestartList()).thenReturn(createApplicationListWithData());
        when(restartDtoMock.executeRestart(any(ApplicationData.class), any(ApplicationData.class))).thenReturn(true);

        Status result = service.execute().get(0);

        assertEquals(StatusCode.SUCCESS, result.getStatusCode());
    }
}