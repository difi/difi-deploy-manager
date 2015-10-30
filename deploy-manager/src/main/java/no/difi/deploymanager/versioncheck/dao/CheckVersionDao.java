package no.difi.deploymanager.versioncheck.dao;

import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.util.Common;
import no.difi.deploymanager.util.IOUtil;
import no.difi.deploymanager.util.JsonUtil;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
import org.json.JSONObject;
import org.springframework.core.env.Environment;

import java.io.IOException;

/***
 * CheckVersionDao will retrieve and update the list over applications that is monitored by Deploy manager.
 * The download list Restart in scheduler.
 */
public class CheckVersionDao {
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
    public JSONObject retrieveExternalArtifactStatus(String groupId, String artifactId) throws IOException, ConnectionFailedException {
        String location;
        if (environment.getProperty("application.runtime.environment").equals("production")) {
            location = environment.getRequiredProperty("location.version");
        }
        else {
            location = environment.getRequiredProperty("location.test.version");
        }

        JSONObject json = jsonUtil.retrieveJsonObject(
                Common.replacePropertyParams(location, groupId, artifactId)
        );

        return (JSONObject) json.get("data");
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
