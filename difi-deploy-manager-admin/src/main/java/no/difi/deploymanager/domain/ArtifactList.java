package no.difi.deploymanager.domain;

import java.util.ArrayList;
import java.util.List;

public class ArtifactList {
    private List<Artifact> artifacts;

    public List<Artifact> getArtifacts() {
        if (artifacts == null) {
            artifacts = new ArrayList<>();
        }
        return artifacts;
    }

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts = artifacts;
    }
}
