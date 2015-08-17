package no.difi.deploymanager.versioncheck.service;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.domain.Status;
import no.difi.deploymanager.domain.StatusCode;
import no.difi.deploymanager.download.dto.DownloadDto;
import no.difi.deploymanager.remotelist.exception.RemoteApplicationListException;
import no.difi.deploymanager.remotelist.service.RemoteListService;
import no.difi.deploymanager.testutils.ObjectMotherApplicationList;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
import no.difi.deploymanager.versioncheck.repository.CheckVersionRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CheckVersionServiceTest {
    public static final String TEST_URL = "http://testurl/?r=central-proxy&g=$GROUP_ID&a=$ARTIFACT_ID&v=RELEASE";
    private CheckVersionService service;

    @Mock Environment environmentMock;
    @Mock RemoteListService remoteListServiceMock;
    @Mock
    CheckVersionRepository checkVersionDtoMock;
    @Mock DownloadDto downloadDtoMock;

    @Before
    public void setUp() throws RemoteApplicationListException {
        initMocks(this);

        when(environmentMock.getRequiredProperty("location.version")).thenReturn(TEST_URL);
        when(remoteListServiceMock.execute()).thenReturn(ObjectMotherApplicationList.createApplicationListWithData());

        service = new CheckVersionService(environmentMock, remoteListServiceMock, checkVersionDtoMock, downloadDtoMock);
    }

    @Test
    public void should_get_critical_fail_when_property_for_location_not_found() {
        when(environmentMock.getRequiredProperty(anyString())).thenThrow(new IllegalStateException());

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.CRITICAL, actual.getStatusCode());
    }

    @Test
    public void should_get_error_when_url_is_malformed() throws Exception {
        when(checkVersionDtoMock.retrieveExternalArtifactStatus(anyString())).thenThrow(new MalformedURLException());

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.ERROR, actual.getStatusCode());
    }

    @Test
    public void should_get_error_when_socket_timeout_occur() throws Exception {
        when(checkVersionDtoMock.retrieveExternalArtifactStatus(anyString())).thenThrow(new SocketTimeoutException());

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.ERROR, actual.getStatusCode());
    }

    @Test
    public void should_get_error_when_IOException_occur() throws Exception {
        when(checkVersionDtoMock.retrieveExternalArtifactStatus(anyString())).thenThrow(new IOException());

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.ERROR, actual.getStatusCode());
    }

    @Test
    public void should_get_error_when_connection_failed_exception_occur() throws Exception {
        when(checkVersionDtoMock.retrieveExternalArtifactStatus(anyString())).thenThrow(new ConnectionFailedException("Message"));

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.ERROR, actual.getStatusCode());
    }

    @Test
    public void should_get_critical_error_when_JSON_exception_occur() throws Exception {
        when(checkVersionDtoMock.retrieveExternalArtifactStatus(anyString())).thenThrow(new JSONException("Message"));

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.CRITICAL, actual.getStatusCode());
    }

    @Test
    public void should_get_critical_error_when_returned_object_is_not_valid_JSON() throws Exception{
        when(checkVersionDtoMock.retrieveExternalArtifactStatus(anyString())).thenReturn(new JSONObject());

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.CRITICAL, actual.getStatusCode());
    }

    @Test
    public void should_get_success_when_operation_completed_without_error_and_no_download_needed() throws Exception {
        ApplicationList sameList = ObjectMotherApplicationList.createApplicationListWithData();
        ApplicationData sameData = sameList.getApplications().get(0);
        JSONObject jsonObject = createJsonObject(
                sameData.getActiveVersion(),
                sameData.getGroupId(),
                sameData.getArtifactId()
        );

        when(checkVersionDtoMock.retrieveExternalArtifactStatus(anyString())).thenReturn(jsonObject);
        when(downloadDtoMock.retrieveDownloadList()).thenReturn(sameList);

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.SUCCESS, actual.getStatusCode());
    }

    @Test
    public void should_get_success_when_operation_completed_without_error_and_download_needed() throws Exception {
        ApplicationList otherVersionList = ObjectMotherApplicationList.createApplicationListWithData();
        ApplicationData otherVersionData = otherVersionList.getApplications().get(0);
        JSONObject jsonObject = createJsonObject(
                "someVersion",
                otherVersionData.getGroupId(),
                otherVersionData.getArtifactId()
        );

        when(checkVersionDtoMock.retrieveExternalArtifactStatus(anyString())).thenReturn(jsonObject);
        when(downloadDtoMock.retrieveDownloadList()).thenReturn(otherVersionList);

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.SUCCESS, actual.getStatusCode());
    }

    private JSONObject createJsonObject(String version, String artifactId, String groupId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("groupId", groupId);
        jsonObject.put("artifactId", artifactId);
        jsonObject.put("version", version);
        jsonObject.put("snapshot", false);
        return jsonObject;
    }
}