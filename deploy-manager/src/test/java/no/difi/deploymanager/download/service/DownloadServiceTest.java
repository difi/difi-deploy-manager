package no.difi.deploymanager.download.service;

import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.download.dao.DownloadDao;
import no.difi.deploymanager.download.filetransfer.FileTransfer;
import no.difi.deploymanager.restart.service.RestartService;
import no.difi.deploymanager.testutils.ObjectMotherApplicationList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    public void should_invoke_save_restart_list_when_successful_download() throws Exception {
        when(downloadDaoMock.retrieveDownloadList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());

        service.execute();

        verify(restartServiceMock, times(1)).performSaveOfRestartList(any(ApplicationList.class));
    }

    @Test
    public void should_not_invoke_save_restart_list_when_nothing_is_downloaded() throws Exception {
        when(downloadDaoMock.retrieveDownloadList()).thenReturn(ObjectMotherApplicationList.createApplicationListEmpty());

        service.execute();

        verify(restartServiceMock, never()).performSaveOfRestartList(any(ApplicationList.class));
    }

    @Test
    public void should_update_download_list_when_download_have_occurred() throws Exception{
        when(downloadDaoMock.retrieveDownloadList()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());

        service.execute();

        verify(downloadDaoMock, times(1)).saveDownloadList(any(ApplicationList.class));
    }
}