package no.difi.deploymanager.dao;

import no.difi.deploymanager.domain.Artifact;
import no.difi.deploymanager.domain.ArtifactList;
import no.difi.deploymanager.domain.ArtifactType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ArtifactDao {
    public ArtifactList fetchArtifactList() {
        Artifact app1 = new Artifact();
        app1.setName("difi-deploy-manager");
        app1.setGroupId("difi-deploy-manager");
        app1.setArtifactId("no.difi.deploymanager");
        app1.setVersion("0.9.1-SNAPSHOT");
        app1.setApplicationType(ArtifactType.JAR);

        ArtifactList artifactList = new ArtifactList();

        List<Artifact> applications = artifactList.getArtifacts();
        applications.add(app1);

        artifactList.setArtifacts(applications);

        return artifactList;
    }

    public boolean updateArtifact(Artifact artifact) {
        return false;
    }
}
