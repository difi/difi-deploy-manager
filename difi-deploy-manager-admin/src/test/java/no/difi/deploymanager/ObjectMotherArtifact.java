package no.difi.deploymanager;

import no.difi.deploymanager.domain.Artifact;
import no.difi.deploymanager.domain.ArtifactType;

public class ObjectMotherArtifact {
    public static Artifact createArtifact() {
        Artifact artifact = new Artifact();
        artifact.setName("Artifact name");
        artifact.setGroupId("group-id");
        artifact.setArtifactId("artifact.id");
        artifact.setVersion("0.0.1");
        artifact.setApplicationType(ArtifactType.JAR);
        artifact.setStartParameters("-DsuperParam=runforyourlife");

        return artifact;
    }
}
