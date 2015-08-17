package no.difi.deploymanager.versioncheck.repository;

import no.difi.deploymanager.util.IOUtil;
import no.difi.deploymanager.util.JsonUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class CheckVersionRepositoryTest {
    public static final String GROUPID = "groupid";
    public static final String ARTIFACTID = "artifactid";
    private CheckVersionRepository repository;

    @Mock private Environment environmentMock;
    @Mock private IOUtil ioUtilMock;
    @Mock private JsonUtil jsonUtilMock;

    @Before
    public void setUp() {
        initMocks(this);

        repository = new CheckVersionRepository(environmentMock, ioUtilMock, jsonUtilMock);
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_illegalstateexception_when_location_version_not_found() throws Exception {
        when(environmentMock.getRequiredProperty("location.version")).thenThrow(new IllegalStateException());

        repository.retrieveExternalArtifactStatus(GROUPID, ARTIFACTID);
    }

    @Test(expected = JSONException.class)
    public void should_throw_json_exception_when_result_is_not_a_json_with_object_data() throws Exception {
        when(environmentMock.getRequiredProperty(anyString())).thenReturn("url");
        when(jsonUtilMock.retrieveJsonObject(anyString())).thenReturn(new JSONObject("{}"));

        repository.retrieveExternalArtifactStatus(GROUPID, ARTIFACTID);
    }

    @Test
    public void should_return_json_object_when_data_is_available_in_result() throws Exception {
        when(environmentMock.getRequiredProperty(anyString())).thenReturn("url");
        when(jsonUtilMock.retrieveJsonObject(anyString())).thenReturn(new JSONObject("{data: {something: \"1234\"}}"));

        JSONObject result = repository.retrieveExternalArtifactStatus(GROUPID, ARTIFACTID);

        assertTrue(result.getString("something").equals("1234"));
    }
}