package no.difi.deploymanager.artifact;

import no.difi.deploymanager.domain.ArtifactList;
import no.difi.deploymanager.domain.ArtifactType;
import no.difi.deploymanager.service.ArtifactService;
import org.eclipse.jetty.server.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ApiControllerTest {
    public static final String TEST_GROUP_ID = "groupId";
    public static final String TEST_ARTIFACT_ID = "artifactId";
    public static final String TEST_VERSION = "version";
    public static final String TEST_PARAMS = "startParams";
    public static final String TEST_NAME = "name";
    private ApiController apiController;

    @Mock private ArtifactService artifactServiceMock;

    @Before
    public void setUp() {
        initMocks(this);

        apiController = new ApiController(artifactServiceMock);
    }

    @Test
    public void should_get_artifact_list_when_get_is_called() {
        when(artifactServiceMock.fetchLatestArtifactList()).thenReturn(new ArtifactList());

        ArtifactList result = apiController.getArtifactList();

        assertNotNull(result.getArtifacts());
    }

    @Test
    public void should_fail_update_when_name_is_missing() {
        int result = apiController.updateArtifact(null, TEST_GROUP_ID, TEST_ARTIFACT_ID, TEST_VERSION, ArtifactType.JAR, TEST_PARAMS);

        assertEquals(Response.SC_EXPECTATION_FAILED, result);
    }

    @Test
    public void should_fail_update_when_group_id_is_missing_and_artifactType_is_JAR() {
        int result = apiController.updateArtifact(TEST_NAME, "  ", TEST_ARTIFACT_ID, TEST_VERSION, ArtifactType.JAR, TEST_PARAMS);

        assertEquals(Response.SC_EXPECTATION_FAILED, result);
    }

    @Test
    public void should_fail_update_when_artifact_id_is_missing_and_artifactType_is_JAR() {
        int result = apiController.updateArtifact(TEST_NAME, TEST_GROUP_ID, "", TEST_VERSION, ArtifactType.JAR, TEST_PARAMS);

        assertEquals(Response.SC_EXPECTATION_FAILED, result);
    }

    @Test
    public void should_fail_update_when_artifactType_is_missing() {
        int result = apiController.updateArtifact(TEST_NAME, TEST_GROUP_ID, TEST_ARTIFACT_ID, TEST_VERSION, null, TEST_PARAMS);

        assertEquals(Response.SC_EXPECTATION_FAILED, result);
    }

    @Test
    public void should_fail_update_when_version_is_null_and_artifact_type_is_DOCKER() {
        int result = apiController.updateArtifact(TEST_NAME, TEST_GROUP_ID, TEST_ARTIFACT_ID, null, ArtifactType.DOCKER, TEST_PARAMS);

        assertEquals(Response.SC_EXPECTATION_FAILED, result);
    }

    @Test
    public void should_fail_update_when_version_is_null_and_artifact_type_is_JAR() {
        int result = apiController.updateArtifact(TEST_NAME, TEST_GROUP_ID, TEST_ARTIFACT_ID, null, ArtifactType.JAR, TEST_PARAMS);

        assertEquals(Response.SC_EXPECTATION_FAILED, result);
    }

    @Test
    public void should_run_fine_when_artifactType_is_JAR_and_param_is_blank() {
        int result = apiController.updateArtifact(TEST_NAME, TEST_GROUP_ID, TEST_ARTIFACT_ID, TEST_VERSION, ArtifactType.JAR, TEST_PARAMS);

        assertEquals(Response.SC_CREATED, result);
    }

    @Test
    public void should_run_fine_when_artifactType_is_DOCKER_and_param_is_blank() {
        int result = apiController.updateArtifact(TEST_NAME, TEST_GROUP_ID, TEST_ARTIFACT_ID, TEST_VERSION, ArtifactType.DOCKER, TEST_PARAMS);

        assertEquals(Response.SC_CREATED, result);
    }

    @Test
    public void should_run_fine_when_artifactType_is_DOCKER_and_groupId_and_artifactId_is_missing() {
        int result = apiController.updateArtifact(TEST_NAME, null, null, TEST_VERSION, ArtifactType.DOCKER, TEST_PARAMS);

        assertEquals(Response.SC_CREATED, result);
    }
}