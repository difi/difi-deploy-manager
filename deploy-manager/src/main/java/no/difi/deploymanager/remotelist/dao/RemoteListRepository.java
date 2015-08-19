package no.difi.deploymanager.remotelist.dao;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.remotelist.exception.RemoteApplicationListException;
import no.difi.deploymanager.util.JsonUtil;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@Repository
public class RemoteListRepository {
    private final JsonUtil jsonUtil;

    public RemoteListRepository(JsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;
    }

    public ApplicationList getRemoteList() throws RemoteApplicationListException {
        JSONObject json;
        try {
            json = jsonUtil.retrieveJsonObject("http://localhost:9980/api/app");
        } catch (IOException | ConnectionFailedException e) {
            throw new RemoteApplicationListException();
        }

        ApplicationList.Builder applications = new ApplicationList.Builder();
        if (json != null) {
            JSONArray dataArray =  (JSONArray) json.get("artifacts");

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject dataObject = dataArray.getJSONObject(i);

                ApplicationData app = new ApplicationData.Builder()
                        .name(convert(dataObject, "name"))
                        .groupId(convert(dataObject, "groupId"))
                        .artifactId(convert(dataObject, "artifactId"))
                        .activeVersion(convert(dataObject, "version"))
                        .artifactType(convert(dataObject, "applicationType"))
                        .filename(convert(dataObject, "filename"))
                        .startParameters(convert(dataObject, "startParameters"))
                        .build();

                applications.addApplicationData(app);
            }
        }

        return applications.build();
    }

    public ApplicationList getHardcodedList() {
        ApplicationList.Builder appList = new ApplicationList.Builder();

        appList.addApplicationData(createApplicationDataObject("Difi Deploy Manager", "difi-deploy-manager", "no.difi.deploymanager", "", "JAR", "no.difi.deploymanager-0.9.1-SNAPSHOT.jar", ""));

        return appList.build();
    }

    private ApplicationData createApplicationDataObject(String name, String groupId, String artifactId, String version,
                                                        String applicationType, String filename, String startParameters) {
        return new ApplicationData.Builder()
                .name(name)
                .groupId(groupId)
                .artifactId(artifactId)
                .activeVersion(version)
                .artifactType(applicationType)
                .filename(filename)
                .startParameters(startParameters)
                .build();
    }

    private static String convert(JSONObject obj, String fetch) {
        try {
            return obj.getString(fetch);
        }
        catch (JSONException e) {
            return "";
        }
    }
}
