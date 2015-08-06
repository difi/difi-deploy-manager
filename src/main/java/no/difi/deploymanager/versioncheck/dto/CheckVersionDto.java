package no.difi.deploymanager.versioncheck.dto;

import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.util.IOUtil;
import no.difi.deploymanager.util.JsonUtil;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@Repository
public class CheckVersionDto {
    private final Environment environment;
    private final IOUtil ioUtil;
    private final JsonUtil jsonUtil;

    @Autowired
    public CheckVersionDto(Environment environment, IOUtil ioUtil, JsonUtil jsonUtil) {
        this.environment = environment;
        this.ioUtil = ioUtil;
        this.jsonUtil = jsonUtil;
    }

    public JSONObject retrieveExternalArtifactStatus(String url) throws IOException, ConnectionFailedException {
        JSONObject json = jsonUtil.retrieveJsonObject(url);

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
