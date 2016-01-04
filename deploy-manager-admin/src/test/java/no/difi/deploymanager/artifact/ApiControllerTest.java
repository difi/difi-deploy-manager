package no.difi.deploymanager.artifact;

import no.difi.deploymanager.domain.ArtifactList;
import no.difi.deploymanager.domain.ArtifactType;
import no.difi.deploymanager.service.ArtifactService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ApiControllerTest {
    private static final String TEST_GROUP_ID = "groupId";
    private static final String TEST_ARTIFACT_ID = "artifactId";
    private static final String TEST_VERSION = "version";
    private static final String TEST_PARAMS = "startParams";
    private static final String TEST_NAME = "name";
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
        assertFalse(apiController.updateArtifact(null, TEST_VERSION, ArtifactType.JAR, TEST_GROUP_ID, TEST_ARTIFACT_ID, TEST_PARAMS));
    }

    @Test
    public void should_fail_update_when_group_id_is_missing_and_artifactType_is_JAR() {
        assertFalse(apiController.updateArtifact(TEST_NAME, TEST_VERSION, ArtifactType.JAR, "  ", TEST_ARTIFACT_ID, TEST_PARAMS));
    }

    @Test
    public void should_fail_update_when_artifact_id_is_missing_and_artifactType_is_JAR() {
        assertFalse(apiController.updateArtifact(TEST_NAME, TEST_VERSION, ArtifactType.JAR, TEST_GROUP_ID, "", TEST_PARAMS));
    }

    @Test
    public void should_fail_update_when_artifactType_is_missing() {
        assertFalse(apiController.updateArtifact(TEST_NAME, TEST_VERSION, null, TEST_GROUP_ID, TEST_ARTIFACT_ID, TEST_PARAMS));
    }

    @Test
    public void should_fail_update_when_version_is_null_and_artifact_type_is_DOCKER() {
        assertFalse(apiController.updateArtifact(TEST_NAME, null, ArtifactType.DOCKER, TEST_GROUP_ID, TEST_ARTIFACT_ID, TEST_PARAMS));
    }

    @Test
    public void should_fail_update_when_version_is_null_and_artifact_type_is_JAR() {
        assertFalse(apiController.updateArtifact(TEST_NAME, null, ArtifactType.JAR, TEST_GROUP_ID, TEST_ARTIFACT_ID, TEST_PARAMS));
    }

    @Test
    public void should_run_fine_when_artifactType_is_JAR_and_param_is_blank() {
        assertFalse(apiController.updateArtifact(TEST_NAME, TEST_VERSION, ArtifactType.JAR, TEST_GROUP_ID, TEST_ARTIFACT_ID, TEST_PARAMS));
    }

    @Test
    public void should_run_fine_when_artifactType_is_DOCKER_and_param_is_blank() {
        assertFalse(apiController.updateArtifact(TEST_NAME, TEST_VERSION, ArtifactType.DOCKER, TEST_GROUP_ID, TEST_ARTIFACT_ID, TEST_PARAMS));
    }

    @Test
    public void should_run_fine_when_artifactType_is_DOCKER_and_groupId_and_artifactId_is_missing() {
        assertFalse(apiController.updateArtifact(TEST_NAME, TEST_VERSION, ArtifactType.DOCKER, null, null, TEST_PARAMS));
    }
}