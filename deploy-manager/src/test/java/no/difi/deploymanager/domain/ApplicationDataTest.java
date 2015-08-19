package no.difi.deploymanager.domain;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ApplicationDataTest {

    @Test
    public void should_get_empty_download_list_when_not_initialized() {
        ApplicationData data = new ApplicationData.Builder().build();

        assertNotNull(data.getDownloadedVersions());
    }
}