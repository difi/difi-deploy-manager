package no.difi.deploymanager.versioncheck.service;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.domain.Status;
import no.difi.deploymanager.domain.StatusCode;
import no.difi.deploymanager.download.dao.DownloadDao;
import no.difi.deploymanager.remotelist.exception.RemoteApplicationListException;
import no.difi.deploymanager.remotelist.service.RemoteListService;
import no.difi.deploymanager.versioncheck.dao.CheckVersionDao;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import static no.difi.deploymanager.testutils.ObjectMotherApplicationList.createApplicationListWithData;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CheckVersionServiceTest {
    private CheckVersionService service;

    @Mock RemoteListService remoteListServiceMock;
    @Mock
    CheckVersionDao checkVersionDaoMock;
    @Mock
    DownloadDao downloadDaoMock;

    @Before
    public void setUp() throws RemoteApplicationListException {
        initMocks(this);

        when(remoteListServiceMock.execute()).thenReturn(createApplicationListWithData());

        service = new CheckVersionService(remoteListServiceMock, checkVersionDaoMock, downloadDaoMock);
    }

    @Test
    public void should_get_error_when_url_is_malformed() throws Exception {
        when(checkVersionDaoMock.retrieveExternalArtifactStatus(anyString(), anyString())).thenThrow(new MalformedURLException());

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.ERROR, actual.getStatusCode());
    }

    @Test
    public void should_get_error_when_socket_timeout_occur() throws Exception {
        when(checkVersionDaoMock.retrieveExternalArtifactStatus(anyString(), anyString())).thenThrow(new SocketTimeoutException());

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.ERROR, actual.getStatusCode());
    }

    @Test
    public void should_get_error_when_IOException_occur() throws Exception {
        when(checkVersionDaoMock.retrieveExternalArtifactStatus(anyString(), anyString())).thenThrow(new IOException());

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.ERROR, actual.getStatusCode());
    }

    @Test
    public void should_get_error_when_connection_failed_exception_occur() throws Exception {
        when(checkVersionDaoMock.retrieveExternalArtifactStatus(anyString(), anyString())).thenThrow(new ConnectionFailedException("Message"));

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.ERROR, actual.getStatusCode());
    }

    @Test
    public void should_get_critical_error_when_JSON_exception_occur() throws Exception {
        when(checkVersionDaoMock.retrieveExternalArtifactStatus(anyString(), anyString())).thenThrow(new JSONException("Message"));

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.CRITICAL, actual.getStatusCode());
    }

    @Test
    public void should_get_critical_error_when_returned_object_is_not_valid_JSON() throws Exception{
        when(checkVersionDaoMock.retrieveExternalArtifactStatus(anyString(), anyString())).thenReturn(new JSONObject());

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.CRITICAL, actual.getStatusCode());
    }

    @Test
    public void should_get_success_when_operation_completed_without_error_and_no_download_needed() throws Exception {
        ApplicationList sameList = createApplicationListWithData();
        ApplicationData sameData = sameList.getApplications().get(0);
        JSONObject jsonObject = createJsonObject(
                sameData.getActiveVersion(),
                sameData.getGroupId(),
                sameData.getArtifactId()
        );

        when(checkVersionDaoMock.retrieveExternalArtifactStatus(anyString(), anyString())).thenReturn(jsonObject);
        when(downloadDaoMock.retrieveDownloadList()).thenReturn(sameList);

        Status actual = service.execute().get(0);

        assertEquals(StatusCode.SUCCESS, actual.getStatusCode());
    }

    @Test
    public void should_get_success_when_operation_completed_without_error_and_download_needed() throws Exception {
        ApplicationList otherVersionList = createApplicationListWithData();
        ApplicationData otherVersionData = otherVersionList.getApplications().get(0);
        JSONObject jsonObject = createJsonObject(
                "someVersion",
                otherVersionData.getGroupId(),
                otherVersionData.getArtifactId()
        );

        when(checkVersionDaoMock.retrieveExternalArtifactStatus(anyString(), anyString())).thenReturn(jsonObject);
        when(downloadDaoMock.retrieveDownloadList()).thenReturn(otherVersionList);

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