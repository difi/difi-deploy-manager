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

/**
 * Retrieve list over applications to monitor.
 * <p/>
 * Required parameters: name, groupId and artifactId.
 * Optional parameters: version and startParameters.
 */
public class RemoteListRepository {

    private final JsonUtil jsonUtil;

    public RemoteListRepository(JsonUtil jsonUtil) {
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
            String userDir = System.getProperty("user.dir");
            String probePath = "/deploy-manager";
            String probePath2 = "/deploymanager";
            String path;

            if (Common.IS_WINDOWS) {
                probePath = probePath.replace("/", "\\");
                probePath2 = probePath2.replace("/", "\\");
            }

            if (userDir.contains(probePath) || userDir.contains(probePath2)) {
                path = "/data/monitorApps.json";
            } else {
                path = "/deploy-manager/data/monitorApps.json";
            }

            if (Common.IS_WINDOWS) {
                path = path.replace("/", "\\");
            }

            reader = new InputStreamReader(new FileInputStream((userDir + path)), StandardCharsets.UTF_8);
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
