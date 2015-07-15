package versioncheck.checkversion;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import versioncheck.IOUtil;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CheckVersionServiceTest {
    private CheckVersionService service;

    @Mock Environment environmentMock;
    @Mock IOUtil ioUtilMock;

    @Before
    public void setUp() {
        initMocks(this);

        service = new CheckVersionService(environmentMock, ioUtilMock);
    }

    @Test
    public void should_fail_gracefully_when_location_version_is_not_present_in_environment_properties() {
        when(environmentMock.getRequiredProperty(anyString())).thenThrow(new IllegalStateException());

        service.execute();
    }
}