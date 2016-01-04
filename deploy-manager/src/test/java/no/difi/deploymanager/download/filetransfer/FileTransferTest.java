package no.difi.deploymanager.download.filetransfer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.core.env.Environment;

import static no.difi.deploymanager.testutils.ObjectMotherApplicationData.createApplicationData;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class FileTransferTest {
    private static final String HTTP_DUMMY_VALUE = "http://not.relevant/for/test";
    private FileTransfer fileTransfer;

    @Mock
    Environment environmentMock;

    @Before
    public void setUp() {
        initMocks(this);

        when(environmentMock.getRequiredProperty("location.test.download")).thenReturn(HTTP_DUMMY_VALUE);
        when(environmentMock.getRequiredProperty("location.staging.download")).thenReturn(HTTP_DUMMY_VALUE);
        when(environmentMock.getRequiredProperty("location.download")).thenReturn(HTTP_DUMMY_VALUE);

        fileTransfer = new FileTransfer(environmentMock);
    }

    @Test
    public void should_use_connection_url_from_test_download_when_download_source_is_test() throws Exception {
        when(environmentMock.getProperty("application.runtime.environment")).thenReturn("test");

        fileTransfer.makeUrlForDownload(createApplicationData());

        verify(environmentMock, never()).getRequiredProperty("location.download");
        verify(environmentMock, never()).getRequiredProperty("location.staging.download");
        verify(environmentMock, times(1)).getRequiredProperty("location.test.download");
    }

    @Test
    public void should_use_connection_url_from_staging_download_when_download_source_is_staging() throws Exception {
        when(environmentMock.getProperty("application.runtime.environment")).thenReturn("staging");

        fileTransfer.makeUrlForDownload(createApplicationData());

        verify(environmentMock, never()).getRequiredProperty("location.download");
        verify(environmentMock, times(1)).getRequiredProperty("location.staging.download");
        verify(environmentMock, never()).getRequiredProperty("location.test.download");
    }

    @Test
    public void should_use_production_download_link_when_download_source_is_production() throws Exception {
        when(environmentMock.getProperty("application.runtime.environment")).thenReturn("production");

        fileTransfer.makeUrlForDownload(createApplicationData());

        verify(environmentMock, times(1)).getRequiredProperty("location.download");
        verify(environmentMock, never()).getRequiredProperty("location.staging.download");
        verify(environmentMock, never()).getRequiredProperty("location.test.download");
    }

    @Test
    public void should_use_production_download_link_when_download_source_is_not_given() throws Exception {
        when(environmentMock.getProperty("application.runtime.environment")).thenReturn("");

        fileTransfer.makeUrlForDownload(createApplicationData());

        verify(environmentMock, never()).getRequiredProperty("location.download");
        verify(environmentMock, times(1)).getRequiredProperty("location.test.download");
    }
}