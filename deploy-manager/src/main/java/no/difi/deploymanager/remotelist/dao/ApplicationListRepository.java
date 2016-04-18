package no.difi.deploymanager.remotelist.dao;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.remotelist.exception.RemoteApplicationListException;
import no.difi.deploymanager.util.Common;
import no.difi.deploymanager.util.JsonUtil;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Retrieve list over applications to monitor.
 * <p/>
 * Required parameters: name, groupId and artifactId.
 * Optional parameters: version and startParameters.
 */
public class ApplicationListRepository {

    private final JsonUtil jsonUtil;

    public ApplicationListRepository(JsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;
    }

    /**
     * Retrieve list of applications/artifacts to monitor from remote location.
     *
     * @return List of applications to monitor.
     * @throws RemoteApplicationListException when list cannot be retrieved.
     */
    public ApplicationList getRemoteList() throws RemoteApplicationListException {
        JSONObject json;
        try {
            json = jsonUtil.retrieveJsonObject("http://localhost:9980/api/app");
        } catch (IOException | ConnectionFailedException e) {
            throw new RemoteApplicationListException();
        }

        return parseJson(json);
    }

    private ApplicationList parseJson(JSONObject json) {
        ApplicationList.Builder applications = new ApplicationList.Builder();

        if (json != null) {
            JSONArray dataArray = (JSONArray) json.get("artifacts");

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject dataObject = dataArray.getJSONObject(i);

                ApplicationData app = new ApplicationData.Builder()
                        .name(fetchElement(dataObject, "name"))
                        .groupId(fetchElement(dataObject, "groupId"))
                        .artifactId(fetchElement(dataObject, "artifactId"))
                        .activeVersion(fetchElement(dataObject, "version"))
                        .artifactType(fetchElement(dataObject, "applicationType"))
                        .filename(fetchElement(dataObject, "filename"))
                        .vmOptions(fetchElement(dataObject, "vmOptions"))
                        .environmentVariables(fetchElement(dataObject, "environmentVariables"))
                        .mainClass(fetchElement(dataObject, "mainClass"))
                        .build();

                applications.addApplicationData(app);
            }
        }

        return applications.build();
    }

    public ApplicationList getLocalList() throws IOException {
        InputStreamReader reader;

        try {
            Path userDir = Paths.get(System.getProperty("user.dir"));

            Path path;
            if (userDir.endsWith("deploymanager") || userDir.endsWith("deploy-manager")) {
                path = userDir.resolve("data/monitorApps.json");
            } else {
                path = userDir.resolve("deploy-manager/data/monitorApps.json");
            }

            reader = new InputStreamReader(new FileInputStream(path.toFile()), StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            return new ApplicationList.Builder().build();
        }

        JSONObject json = new JSONObject(new JSONTokener(reader));

        reader.close();
        return parseJson(json);
    }

    private static String fetchElement(JSONObject obj, String fetch) {
        try {
            return obj.getString(fetch);
        } catch (JSONException e) {
            return "";
        }
    }
}
