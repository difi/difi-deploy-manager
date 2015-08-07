package no.difi.deploymanager.artifact;

import no.difi.deploymanager.domain.*;
import no.difi.deploymanager.domain.Artifact;
import no.difi.deploymanager.service.ArtifactService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.eclipse.jetty.util.StringUtil.isBlank;

@RestController
@RequestMapping("/api")
public class ApiController {
    private static final Logger logger = LogManager.getLogger(ApiController.class);

    ArtifactService applicationService;

    @Autowired
    public ApiController(ArtifactService applicationService) {
        this.applicationService = applicationService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/app")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    ArtifactList getArtifactList() {
        return applicationService.fetchLatestArtifactList();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/app")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody boolean updateArtifact(@RequestParam String name,
                          @RequestParam String version,
                          @RequestParam ArtifactType type,
                          @RequestParam(required = false) String groupId,
                          @RequestParam(required = false) String artifactId,
                          @RequestParam(required = false) String startParams) {
        try {
            validateUpdateInput(name, groupId, artifactId, version, type);
        }
        catch (IllegalArgumentException e) {
            logger.warn("Validation failed.", e.getCause());

            return false;
        }

        return applicationService.updateArtifact(mapApplicationFromInput(name, groupId, artifactId, version, type, startParams));
    }

    private void validateUpdateInput(String name, String groupId, String artifactId, String version, ArtifactType type) {
        if (isBlank(name) || isBlank(version)) {
            throw new IllegalArgumentException("Required parameter version is missing.");
        }

        if (type == null) {
            throw new IllegalArgumentException("Artifact type is missing, application will have no idea of what to do with it.");
        }

        if (type == ArtifactType.JAR && (isBlank(groupId) || isBlank(artifactId))) {
            throw new IllegalArgumentException("Application type is JAR, version, groupId and artifactId must be provided.");
        }
        else if (type == ArtifactType.DOCKER && isBlank(version)) {
            throw new IllegalArgumentException("Application type is DOCKER, version must be provided.");
        }
    }

    private Artifact mapApplicationFromInput(String name, String groupId, String artifactId, String version,
                                             ArtifactType type, String startParams) {
        Artifact application = new Artifact();
        application.setName(name);
        application.setGroupId(groupId);
        application.setArtifactId(artifactId);
        application.setVersion(version);
        application.setApplicationType(type);
        application.setStartParameters(startParams);
        return application;
    }
}
