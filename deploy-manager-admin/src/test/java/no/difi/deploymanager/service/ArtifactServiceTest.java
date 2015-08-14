package no.difi.deploymanager.service;

import no.difi.deploymanager.ObjectMotherArtifact;
import no.difi.deploymanager.dao.ArtifactDao;
import no.difi.deploymanager.domain.Artifact;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class ArtifactServiceTest {
    private ArtifactService artifactService;

    @Mock ArtifactDao artifactDaoMock;

    @Before
    public void setUp() {
        initMocks(this);

        artifactService = new ArtifactService(artifactDaoMock);
    }

    @Test
    public void should_pass_get_call_to_dao_when_requesting_to_get_artifact_list() {
        artifactService.fetchLatestArtifactList();

        verify(artifactDaoMock, times(1)).fetchArtifactList();
    }

    @Test
    public void should_pass_update_of_artifact_to_dao_when_update_is_called() {
        Artifact artifact = ObjectMotherArtifact.createArtifact();

        artifactService.updateArtifact(artifact);

        verify(artifactDaoMock, times(1)).updateArtifact(artifact);
    }
}