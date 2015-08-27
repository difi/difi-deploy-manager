package no.difi.deploymanager.download.service;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.domain.Status;
import no.difi.deploymanager.domain.StatusCode;
import no.difi.deploymanager.download.dao.DownloadDao;
import no.difi.deploymanager.download.filetransfer.FileTransfer;
import no.difi.deploymanager.restart.service.RestartService;
import no.difi.deploymanager.testutils.ObjectMotherApplicationList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.net.MalformedURLException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class DownloadServiceTest {
    private DownloadService service;

    @Mock private DownloadDao downloadDaoMock;
    @Mock private RestartService restartServiceMock;
    @Mock private FileTransfer fileTransferMock;

    @Before
    public void setUp() {
        initMocks(this);

        service = new DownloadService(downloadDaoMock, fileTransferMock, restartServiceMock);
    }

    @Test
    public void should_fail_with_error_when_dto_get_ioexception() throws Exception {
        when(downloadDaoMock.retrieveDownloadList()).thenThrow(new IOException());

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.ERROR, actual.getStatusCode());
    }

    @Test
    public void should_fail_with_error_when_dto_get_malformed_url_exception() throws Exception {
        when(fileTransferMock.downloadApplication(any(ApplicationData.class))).thenThrow(new MalformedURLException());
        when(downloadDaoMock.retrieveDownloadList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.ERROR, actual.getStatusCode());
    }

    @Test
    public void should_get_success_when_list_over_apps_to_download_is_empty() throws Exception {
        when(downloadDaoMock.retrieveDownloadList()).thenReturn(ObjectMotherApplicationList.createApplicationListEmpty());

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.SUCCESS, actual.getStatusCode());
    }

    @Test
    public void should_get_success_when_download_has_completed_without_error() throws Exception{
        when(downloadDaoMock.retrieveDownloadList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.SUCCESS, actual.getStatusCode());
    }

    @Test
    public void should_invoke_save_restart_list_when_successful_download() throws Exception {
        when(downloadDaoMock.retrieveDownloadList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());

        service.execute().get(0);

        verify(restartServiceMock, times(1)).saveRestartList(any(ApplicationList.class));
    }

    @Test
    public void should_not_invoke_save_restart_list_when_nothing_is_downloaded() throws Exception {
        when(downloadDaoMock.retrieveDownloadList()).thenReturn(ObjectMotherApplicationList.createApplicationListEmpty());

        service.execute().get(0);

        verify(restartServiceMock, never()).saveRestartList(any(ApplicationList.class));
    }

    @Test
    public void should_update_download_list_when_download_have_occured() throws Exception{
        when(downloadDaoMock.retrieveDownloadList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());

        Status actual = service.execute().get(0);

        verify(downloadDaoMock, times(1)).saveDownloadList(any(ApplicationList.class));
        assertEquals(StatusCode.SUCCESS, actual.getStatusCode());
    }
}