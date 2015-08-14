package no.difi.deploymanager.remotelist.dao;

import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.remotelist.exception.RemoteApplicationListException;
import no.difi.deploymanager.util.JsonUtil;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
import no.difi.deploymanager.domain.ApplicationData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RemoteListDto {
    private final JsonUtil jsonUtil;

    public RemoteListDto(JsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;
    }

    public ApplicationList getRemoteList() throws RemoteApplicationListException {
        JSONObject json;
        try {
            json = jsonUtil.retrieveJsonObject("http://localhost:9980/api/app");
        } catch (IOException | ConnectionFailedException e) {
            throw new RemoteApplicationListException();
        }

        List<ApplicationData> applications = new ArrayList<>();
        if (json != null) {
            JSONArray dataArray =  (JSONArray) json.get("artifacts");

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject dataObject = dataArray.getJSONObject(i);

                ApplicationData app = new ApplicationData();
                app.setName(convert(dataObject, "name"));
                app.setGroupId(convert(dataObject, "groupId"));
                app.setArtifactId(convert(dataObject, "artifactId"));
                app.setActiveVersion(convert(dataObject, "version"));
                app.artifactType(convert(dataObject, "applicationType"));
                app.setFilename(convert(dataObject, "filename"));
                app.setStartParameters(convert(dataObject, "startParameters"));

                applications.add(app);
            }
        }

        ApplicationList applicationList = new ApplicationList();
        applicationList.setApplications(applications);

        return applicationList;
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
