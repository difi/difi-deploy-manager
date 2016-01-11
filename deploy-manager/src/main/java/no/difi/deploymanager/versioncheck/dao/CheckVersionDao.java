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
     * @param groupId group id of the application to verify latest version
     * @param artifactId artifact id of the application to verify latest version
     * @return Inner JSON object with only essential data to continue processing.
     * @throws IOException
     * @throws ConnectionFailedException
     */
    public JSONObject retrieveExternalArtifactStatus(String groupId, String artifactId, String version) throws IOException, ConnectionFailedException {
        String url = getArtifactUrl(groupId, artifactId, version);
        logger.info(String.format("Retrieving artifact from %s", url));
        JSONObject json = jsonUtil.retrieveJsonObject(url);

        return (JSONObject) json.get("data");
    }

    private String getArtifactUrl(String groupId, String artifactId, String version) {
        String baseUrl;
        if (isProduction()) {
            baseUrl = environment.getRequiredProperty("location.version");
        }
        else if (isStaging()) {
            baseUrl = environment.getRequiredProperty("location.staging.version");
        }
        else {
            baseUrl = environment.getRequiredProperty("location.test.version");
        }
        return Common.replacePropertyParams(baseUrl, groupId, artifactId, version);
    }


    public JSONArray retrieveIntegrasjonspunktThroughLuceneSearch() throws IOException, ConnectionFailedException {
        String url;
        url = getUrlForArtifactSearch();

        logger.info(String.format("Searching for newest artifact from %s", url));
        JSONObject json = jsonUtil.retrieveJsonObject(url);
        return (JSONArray) json.get("data");
    }

    private String getUrlForArtifactSearch() {
        String location;
        if (isProduction()) {
            location = environment.getProperty("location.production.search");
        }
        else if (isStaging()) {
            location = environment.getProperty("location.staging.search");
        }
        else {
            location = environment.getProperty("location.test.search");
        }
        return location;
    }

    private boolean isProduction() {
        return environment.getProperty("application.runtime.environment").equals("production");
    }

    private boolean isStaging() {
        return environment.getProperty("application.runtime.environment").equals("staging");
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
