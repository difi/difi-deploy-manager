package no.difi.deploymanager.versioncheck.dao;

import no.difi.deploymanager.util.IOUtil;
import no.difi.deploymanager.util.JsonUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.env.Environment;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
@WebIntegrationTest(value = {"spring.profiles.active=production"})
public class CheckVersionDaoTest {
    private static final JSONObject JSON_OBJECT = new JSONObject("{data: {something: \"1234\"}}");
    private CheckVersionDao repository;

    private static final String GROUPID = "groupid";
    private static final String ARTIFACTID = "artifactid";
    private static final String VERSION = "version";
    private final MavenArtificat mavenArtificat = new MavenArtificat(GROUPID, ARTIFACTID, VERSION);
    private static final String TEST_URL = "http://testurl/?r=central-proxy&g=$GROUP_ID&a=$ARTIFACT_ID&v=RELEASE";

    @Mock private Environment environmentMock;
    @Mock private IOUtil ioUtilMock;
    @Mock private JsonUtil jsonUtilMock;

    @Before
    public void setUp() {
        initMocks(this);

        repository = new CheckVersionDao(environmentMock, ioUtilMock, jsonUtilMock);
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_illegalstateexception_when_location_version_not_found() throws Exception {
        when(environmentMock.getProperty("spring.profiles.active")).thenReturn("production");
        when(environmentMock.getRequiredProperty("location.version")).thenThrow(new IllegalStateException());

        repository.retrieveExternalArtifactStatus(mavenArtificat);
    }

    @Test(expected = JSONException.class)
    public void should_throw_json_exception_when_result_is_not_a_json_with_object_data() throws Exception {
        when(environmentMock.getProperty("spring.profiles.active")).thenReturn("production");
        when(environmentMock.getRequiredProperty(anyString())).thenReturn(TEST_URL);
        when(jsonUtilMock.retrieveJsonObject(anyString())).thenReturn(new JSONObject("{}"));

        repository.retrieveExternalArtifactStatus(mavenArtificat);
    }

    @Test
    public void should_return_json_object_when_data_is_available_in_result() throws Exception {
        when(environmentMock.getProperty("spring.profiles.active")).thenReturn("production");
        when(environmentMock.getRequiredProperty(anyString())).thenReturn("url");
        when(jsonUtilMock.retrieveJsonObject(anyString())).thenReturn(JSON_OBJECT);

        JSONObject result = repository.retrieveExternalArtifactStatus(mavenArtificat);

        assertTrue(result.getString("something").equals("1234"));
    }

    @Test
    public void should_use_production_location_for_download_when_production_is_set_in_property_for_download_source() throws Exception {
        when(environmentMock.getProperty("spring.profiles.active")).thenReturn("production");
        when(environmentMock.getRequiredProperty("location.version")).thenReturn(TEST_URL);
        when(jsonUtilMock.retrieveJsonObject(anyString())).thenReturn(JSON_OBJECT);

        repository.retrieveExternalArtifactStatus(mavenArtificat);

        verify(environmentMock, times(1)).getRequiredProperty("location.version");
    }
}