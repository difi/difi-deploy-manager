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
import org.springframework.stereotype.Repository;

import java.io.*;

/**
 * Retrieve list over applications to monitor.
 * <p/>
 * Required parameters: name, groupId and artifactId.
 * Optional parameters: version and startParameters.
 */
@Repository
public class RemoteListRepository {
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");

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

                ApplicationData app = new ApplicationData.Builder().name(fetchElement(dataObject, "name")).groupId(fetchElement(dataObject, "groupId")).artifactId(fetchElement(dataObject, "artifactId")).activeVersion(fetchElement(dataObject, "version")).artifactType(fetchElement(dataObject, "applicationType")).filename(fetchElement(dataObject, "filename")).vmOptions(fetchElement(dataObject, "vmOptions")).environmentVariables(fetchElement(dataObject, "environmentVariables")).mainClass(fetchElement(dataObject, "mainClass")).build();

                applications.addApplicationData(app);
            }
        }

        return applications.build();
    }

    public ApplicationList getLocalList() throws IOException {
        validateAndSetDefaultForDownloadIfNoDownloadExists();

        FileReader reader;

        try {
            String userDir = System.getProperty("user.dir");
            String probePath = "/deploy-manager";
            String path = "";

            if (Common.IS_WINDOWS) {
                probePath = probePath.replace("/", "\\");
            }

            if (userDir.contains(probePath)) {
                path = "/data/monitorApps.json";
            } else {
                path = "/deploy-manager/data/monitorApps.json";
            }

            if (Common.IS_WINDOWS) {
                path = path.replace("/", "\\");
            }

            reader = new FileReader(userDir + path);
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


    private static void validateAndSetDefaultForDownloadIfNoDownloadExists() {
        String userDir = System.getProperty("user.dir");
        String pathWithFile = "";
        try {
            pathWithFile = "/data/monitorApps.json";
            if (!IS_WINDOWS && (!userDir.contains("/deploy-manager") && !userDir.contains("/deploymanager"))) {
                pathWithFile = "/deploy-manager" + pathWithFile;
            }
            else if (IS_WINDOWS && (!userDir.contains("\\deploy-manager") && !userDir.contains("\\deploymanager"))) {
                pathWithFile = "\\deploy-manager" + pathWithFile.replace("/", "\\");
            }
            new FileReader(userDir + pathWithFile);
        } catch (FileNotFoundException e) {
            try {
                System.out.println("*** monitorApps.json does not exist ***");
                System.out.println("Trying to create default monitorApps.json in folder "
                        + System.getProperty("user.dir") + pathWithFile);
                File file = new File(System.getProperty("user.dir") + pathWithFile);
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write("{\"artifacts\": [\n" +
                        "    {\n" +
                        "        \"name\": \"Difi Deploy Manager\",\n" +
                        "        \"groupId\": \"no.difi.deploymanager\",\n" +
                        "        \"artifactId\": \"deploy-manager\",\n" +
                        "        \"activeVersion\": \"\",\n" +
                        "        \"artifactType\": \"JAR\",\n" +
                        "        \"filename\": \"\",\n" +
                        "        \"vmOptions\": \"\",\n" +
                        "        \"environmentVariables\": \"-Dapplication.runtime.status=test -Dspring.application.name=\\\"Deploy Manager Testserver\\\" -Dspring.boot.admin.url=http://10.243.200.51:9000 \",\n" +
                        "        \"mainClass\": \"\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "        \"name\": \"Difi Integrasjonspunkt\",\n" +
                        "        \"groupId\": \"no.difi.meldingsutveksling\",\n" +
                        "        \"artifactId\": \"integrasjonspunkt\",\n" +
                        "        \"activeVersion\": \"\",\n" +
                        "        \"filename\": \"\",\n" +
                        "        \"vmOptions\": \"\",\n" +
                        "        \"environmentVariables\": \"-Dprivatekeyalias=910094092 -Dprivatekeypassword=changeit -Dkeystorelocation=/home/miif/test-certificates.jks -Dserver.port=9092 -Dorgnumber=910094092 -Daltinn.username=2435 -Daltinn.password=ROBSTAD1 -Dspring.profiles.active=dev\",\n" +
                        "        \"mainClass\": \"no.difi.meldingsutveksling.IntegrasjonspunktApplication\"\n" +
                        "    }]\n" +
                        "}");
                writer.flush();
                writer.close();
            } catch (IOException e1) {
                System.out.println("Failed creating json that is necessary for the test");
            }
        }
    }
}
