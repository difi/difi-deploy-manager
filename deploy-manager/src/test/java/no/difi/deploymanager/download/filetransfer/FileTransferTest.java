package no.difi.deploymanager.download.filetransfer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.core.env.Environment;

import static no.difi.deploymanager.testutils.ObjectMotherApplicationData.createApplicationData;
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
    public void should_retrieve_environment_variables_when_download() throws Exception {
        when(environmentMock.getProperty("spring.profiles.active")).thenReturn("production");

        fileTransfer.makeUrlForDownload(createApplicationData());

        verify(environmentMock, times(1)).getRequiredProperty("location.download");
    }
}