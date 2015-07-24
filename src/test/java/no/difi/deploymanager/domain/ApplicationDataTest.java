package no.difi.deploymanager.domain;

import no.difi.deploymanager.domain.ApplicationData;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ApplicationDataTest {
    private ApplicationData data;

    @Test
    public void should_get_empty_download_list_when_not_initialized() {
        data = new ApplicationData();

        assertNotNull(data.getDownloadedVersions());
    }
}