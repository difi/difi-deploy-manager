package no.difi.deploymanager.versioncheck.dao;

import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.util.Common;
import no.difi.deploymanager.util.IOUtil;
import no.difi.deploymanager.util.JsonUtil;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.io.IOException;

/***
 * CheckVersionDao will retrieve and update the list over applications that is monitored by Deploy manager.
 * The download list Restart in scheduler.
 */
public class CheckVersionDao {
    private static final Logger logger = LoggerFactory.getLogger(CheckVersionDao.class);


    private final Environment environment;
    private final IOUtil ioUtil;
    private final JsonUtil jsonUtil;

    public CheckVersionDao(Environment environment, IOUtil ioUtil, JsonUtil jsonUtil) {
        this.environment = environment;
        this.ioUtil = ioUtil;
        this.jsonUtil = jsonUtil;
    }

    /***
     * Retrieving external artifact that Deploy Manager is set to monitor.
     *
     * @param mavenArtifact identifying the maven artifact to verify latest version
     * @return Inner JSON object with only essential data to continue processing.
     * @throws IOException
     * @throws ConnectionFailedException
     */
    public JSONObject retrieveExternalArtifactStatus(MavenArtifact mavenArtifact) throws IOException, ConnectionFailedException {
        String url = getArtifactUrl(mavenArtifact);
        logger.info("Retrieving artifact from {}", url);
        JSONObject json = jsonUtil.retrieveJsonObject(url);

        return (JSONObject) json.get("data");
    }

    private String getArtifactUrl(MavenArtifact mavenArtifact) {
        String baseUrl = environment.getRequiredProperty("location.version");

        return Common.replacePropertyParams(baseUrl, mavenArtifact.getGroupId(), mavenArtifact.getArtifactId(), mavenArtifact.getVersion());
    }


    public JSONArray retrieveIntegrasjonspunktThroughLuceneSearch() throws IOException, ConnectionFailedException {
        String url;
        url = getUrlForArtifactSearch();

        logger.info("Searching for newest artifact from {}", url);
        JSONObject json = jsonUtil.retrieveJsonObject(url);
        return (JSONArray) json.get("data");
    }

    private String getUrlForArtifactSearch() {
        return environment.getProperty("location.search");
    }

    public void saveRunningAppsList(ApplicationList applicationList) throws IOException {
        ioUtil.saveApplicationList(
                applicationList,
                environment.getRequiredProperty("monitoring.base.path"),
                environment.getRequiredProperty("monitoring.running.file")
        );
    }

    public ApplicationList retrieveRunningAppsList() throws IOException {
        return ioUtil.retrieveApplicationList(
                environment.getRequiredProperty("monitoring.base.path"),
                environment.getRequiredProperty("monitoring.running.file")
        );
    }
}
