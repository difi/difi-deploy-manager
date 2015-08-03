package no.difi.deploymanager.download.service;

import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.domain.Status;
import no.difi.deploymanager.domain.StatusCode;
import no.difi.deploymanager.download.dto.DownloadDto;
import no.difi.deploymanager.restart.dto.RestartDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.MalformedURLException;

import static no.difi.deploymanager.testutils.ObjectMotherApplicationList.createApplicationListEmpty;
import static no.difi.deploymanager.testutils.ObjectMotherApplicationList.createApplicationListWithData;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class DownloadServiceTest {
    private DownloadService service;

    @Mock private Environment environmentMock;
    @Mock private DownloadDto downloadDtoMock;
    @Mock private RestartDto restartDtoMock;

    @Before
    public void setUp() {
        initMocks(this);

        service = new DownloadService(environmentMock, downloadDtoMock, restartDtoMock);
    }

    @Test
    public void should_get_critical_fail_when_download_location_not_set_in_property() {
        when(environmentMock.getRequiredProperty("location.download")).thenThrow(new IllegalStateException());

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.CRITICAL, actual.getStatusCode());
    }

    @Test
    public void should_fail_with_error_when_dto_get_ioexception() throws Exception {
        when(downloadDtoMock.retrieveDownloadList()).thenThrow(new IOException());

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.ERROR, actual.getStatusCode());
    }

    @Test
    public void should_fail_with_error_when_dto_get_malformed_url_exception() throws Exception {
        when(downloadDtoMock.downloadApplication(anyString())).thenThrow(new MalformedURLException());
        when(downloadDtoMock.retrieveDownloadList()).thenReturn(createApplicationListWithData());

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.ERROR, actual.getStatusCode());
    }

    @Test
    public void should_get_success_when_list_over_apps_to_download_is_empty() throws Exception {
        when(downloadDtoMock.retrieveDownloadList()).thenReturn(createApplicationListEmpty());

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.SUCCESS, actual.getStatusCode());
    }

    @Test
    public void should_get_success_when_download_has_completed_without_error() throws Exception{
        when(downloadDtoMock.retrieveDownloadList()).thenReturn(createApplicationListWithData());

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.SUCCESS, actual.getStatusCode());
    }

    @Test
    public void should_invoke_save_restart_list_when_successful_download() throws Exception {
        when(downloadDtoMock.retrieveDownloadList()).thenReturn(createApplicationListWithData());

        service.execute().get(0);

        verify(restartDtoMock, times(1)).saveRestartList(any(ApplicationList.class));
    }

    @Test
    public void should_not_invoke_save_restart_list_when_nothing_is_downloaded() throws Exception {
        when(downloadDtoMock.retrieveDownloadList()).thenReturn(createApplicationListEmpty());

        service.execute().get(0);

        verify(restartDtoMock, never()).saveRestartList(any(ApplicationList.class));
    }

    @Test
    public void should_update_download_list_when_download_have_occured() throws Exception{
        when(downloadDtoMock.retrieveDownloadList()).thenReturn(createApplicationListWithData());

        Status actual = service.execute().get(0);

        verify(downloadDtoMock, times(1)).saveDownloadList(any(ApplicationList.class));
        assertEquals(StatusCode.SUCCESS, actual.getStatusCode());
    }
}