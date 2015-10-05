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

/***
 * Retrieve list over applications to monitor.
 *
 * Required parameters: name, groupId and artifactId.
 * Optional parameters: version and startParameters.
 */
@Repository
public class RemoteListRepository {
    private final JsonUtil jsonUtil;

    public RemoteListRepository(JsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;
    }

    /***
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

        appList.addApplicationData(
                new ApplicationData.Builder()
                .name("Difi Deploy Manager")
                .groupId("difi-deploy-manager")
                .artifactId("no.difi.deploymanager")
                .activeVersion("")
                .artifactType("JAR")
                .filename("no.difi.deploymanager-0.9.1-SNAPSHOT.jar")
                .startParameters("-Ddownload.source=test")
                .build()
        );

        appList.addApplicationData(
                new ApplicationData.Builder()
                .name("Difi Integrasjonspunkt")
                .groupId("no.difi.meldingsutveksling")
                .artifactId("integrasjonspunkt")
                .activeVersion("")
                .filename("integrasjonspunkt-1.4.jar")
                .startParameters("-Dprivatekeyalias=910094092 " +
                        "-Dprivatekeypassword=changeit " +
                        "-Dkeystorelocation=/home/miif/test-certificates.jks " +
                        "-Dserver.port=9092 " +
                        "-Dorgnummer=910094092 " +
                        "-Daltinn.username=2435 " +
                        "-Daltinn.password=ROBSTAD1" +
                        "-Dspring.profiles.active=dev "
                )
                .build()
        );

        return appList.build();
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
