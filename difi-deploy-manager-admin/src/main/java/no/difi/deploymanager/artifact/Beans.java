package no.difi.deploymanager.artifact;

import no.difi.deploymanager.dao.ArtifactDao;
import no.difi.deploymanager.service.ArtifactService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class Beans {
    @Bean(name = "artifactService")
    public ArtifactService artifactServiceBean() {
        return new ArtifactService(artifactDaoBean());
    }

    @Bean(name = "artifactDao")
    public ArtifactDao artifactDaoBean() {
        return new ArtifactDao();
    }
}
