package no.difi.deploymanager.service;

import no.difi.deploymanager.dao.ArtifactDao;
import no.difi.deploymanager.domain.Artifact;
import no.difi.deploymanager.domain.ArtifactList;
import org.springframework.stereotype.Service;

@Service
public class ArtifactService {
    private final ArtifactDao artifactDao;

    public ArtifactService(ArtifactDao artifactDao) {
        this.artifactDao = artifactDao;
    }

    public ArtifactList fetchLatestArtifactList() {
        return artifactDao.fetchArtifactList();
    }

    public boolean updateArtifact(Artifact artifact) {
        return artifactDao.updateArtifact(artifact);
    }
}
