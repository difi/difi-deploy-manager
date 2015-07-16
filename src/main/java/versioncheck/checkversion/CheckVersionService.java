package versioncheck.checkversion;

import domain.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import versioncheck.IOUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
public class CheckVersionService {
    private static final int CONNECTION_TIMEOUT = 5000; // ms for connection to occur
    private static final int DATARETRIEVAL_TIMEOUT = 10000; // ms for next byte to be read

    private final Environment enviroment;
    private final IOUtil ioUtil;

    @Autowired
    public CheckVersionService(Environment environment, IOUtil ioUtil) {
        this.enviroment = environment;
        this.ioUtil = ioUtil;
    }

    public List<Status> execute() {
        String location;
        List<Status> statuses = new ArrayList<>();
        List<ApplicationData> applicationsToDownload = new ArrayList<>();
        try {
            location = enviroment.getRequiredProperty("location.version");
        }
        catch(IllegalStateException e) {
            statuses.add(new Status(StatusCode.CRITICAL, "Enviroment property 'location.version' not found."));
            return statuses;
        }

        for (MonitoringApplications app : MonitoringApplications.getApplications()) {
            String url = location
                    .replace("$GROUP_ID", app.getGroupId())
                    .replace("$ARTIFACT_ID", app.getArtifactId());

            HttpURLConnection connection = null;
            try {
                URL request = new URL(url);

                connection = (HttpURLConnection) request.openConnection();
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setReadTimeout(DATARETRIEVAL_TIMEOUT);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    statuses.add(new Status(StatusCode.CRITICAL, "Can not establish connection to chekc for updates."));
                }
                InputStream stream = new BufferedInputStream(connection.getInputStream());

                JSONObject json = (JSONObject) new JSONObject(new Scanner(stream).useDelimiter("\\A").next()).get("data");
                ApplicationList downloadedApps = ioUtil.retrieve(
                        enviroment.getRequiredProperty("monitoring.local.path"),
                        enviroment.getRequiredProperty("monitoring.fordownload.file")
                );

                boolean found = false;
                String latestVersion = json.getString("version");

                if (downloadedApps != null) {
                    for (ApplicationData downloaded : downloadedApps.getApplications()) {
                        if (downloaded.getName().getGroupId().equals(json.getString("groupId")) && downloaded.getName().getArtifactId().equals(json.getString("artifactId"))) {
                            if (downloaded.getActiveVersion() == null
                                    || downloaded.getActiveVersion().equals(latestVersion)) {
                                statuses.add(new Status(StatusCode.SUCCESS, String.format("Application %s already have latest version", app.getArtifactId())));
                                found = true;
                            }
                            break;
                        }
                    }
                }

                if (found) {
                    statuses.add(new Status(StatusCode.SUCCESS,
                            String.format("Latest version of %s is already downloaded.", url)));
                }
                else {
                    ApplicationData data = new ApplicationData();
                    data.setName(MonitoringApplications.SPRINGFRAMEWORK_JDBC);
                    data.setActiveVersion(latestVersion);
                    applicationsToDownload.add(data);

                    System.out.println(data);

                    statuses.add(new Status(StatusCode.SUCCESS,
                            String.format("Application %s is set for download.", app.getArtifactId())));
                }
            }
            catch (MalformedURLException e) {
                statuses.add(new Status(StatusCode.ERROR,
                        String.format("Failed to compose url: %s Reason: %s", url, e.getMessage())));
            }
            catch (SocketTimeoutException e) {
                statuses.add(new Status(StatusCode.ERROR,
                        String.format("Timeout occured. Took too long to read from %s Reason: %s", url, e.getMessage())));
            }
            catch (IOException e) {
                statuses.add(new Status(StatusCode.ERROR,
                        String.format("Failed to retrieve data from %s Reason: %s", url, e.getMessage())));
            }
            catch (JSONException e) {
                statuses.add(new Status(StatusCode.CRITICAL, String.format("Result returned from %s is not JSON. Reason: %s", url, e.getMessage())));
            }
            finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        ApplicationList forDownload = new ApplicationList();
        forDownload.setApplications(applicationsToDownload);

        try {
            ioUtil.save(
                    forDownload,
                    enviroment.getRequiredProperty("monitoring.local.path"),
                    enviroment.getRequiredProperty("monitoring.fordownload.file")
            );
        }
        catch (IOException e) {
            statuses.add(new Status(StatusCode.CRITICAL, String.format("Failed to save download list. Reason: %s", e.getMessage())));
        }

        return statuses;
    }
}