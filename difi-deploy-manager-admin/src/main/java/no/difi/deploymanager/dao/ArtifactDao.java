package no.difi.deploymanager.dao;

import no.difi.deploymanager.domain.Artifact;
import no.difi.deploymanager.domain.ArtifactList;
import no.difi.deploymanager.domain.ArtifactType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ArtifactDao {
    public ArtifactList fetchArtifactList() {
        Artifact art1 = new Artifact();
        art1.setName("difi-deploy-manager");
        art1.setGroupId("difi-deploy-manager");
        art1.setArtifactId("no.difi.deploymanager");
        art1.setVersion("0.9.1-SNAPSHOT");
        art1.setApplicationType(ArtifactType.JAR);

        Artifact art2 = new Artifact();
        art2.setName("Deploy manager health check");
        art2.setGroupId("difi-deploy-manager-health-check");
        art2.setArtifactId("difi-deploy-manager-health-check");
        art2.setVersion("0.9.1-SNAPSHOT");
        art2.setApplicationType(ArtifactType.JAR);

        ArtifactList artifactList = new ArtifactList();

        List<Artifact> applications = artifactList.getArtifacts();
        applications.add(art1);
        applications.add(art2);

        artifactList.setArtifacts(applications);

        return artifactList;
    }

    public boolean updateArtifact(Artifact artifact) {
        return false;
    }
}
