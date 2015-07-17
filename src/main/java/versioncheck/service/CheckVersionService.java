package versioncheck.service;

import domain.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import versioncheck.dto.CheckVersionDto;
import versioncheck.exception.ConnectionFailedException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CheckVersionService {
    private final Environment environment;
    private final CheckVersionDto checkVersionDto;

    @Autowired
    public CheckVersionService(Environment environment, CheckVersionDto checkVersionDto) {
        this.environment = environment;
        this.checkVersionDto = checkVersionDto;
    }

    public List<Status> execute() {
        String location;
        List<Status> statuses = new ArrayList<>();
        List<ApplicationData> applicationsToDownload = new ArrayList<>();

        try {
            location = environment.getRequiredProperty("location.version");
        } catch (IllegalStateException e) {
            statuses.add(new Status(StatusCode.CRITICAL, "Enviroment property 'location.version' not found."));
            return statuses;
        }

        for (MonitoringApplications app : MonitoringApplications.getApplications()) {
            String url = location.replace("$GROUP_ID", app.getGroupId()).replace("$ARTIFACT_ID", app.getArtifactId());

            try {
                JSONObject json = checkVersionDto.retrieveMonitoringAppLastVersion(url);
                ApplicationList downloadedApps = checkVersionDto.retrievePreviousDownloadedList();

                if (isDownloaded(json, downloadedApps, json.getString("version"))) {
                    statuses.add(new Status(StatusCode.SUCCESS,
                            String.format("Latest version of %s is already downloaded.", url)));
                } else {
                    ApplicationData data = new ApplicationData();
                    data.setName(MonitoringApplications.SPRINGFRAMEWORK_JDBC);
                    data.setActiveVersion(json.getString("version"));
                    applicationsToDownload.add(data);

                    statuses.add(new Status(StatusCode.SUCCESS,
                            String.format("Application %s is set for download.", data.getName())));
                }
            } catch (MalformedURLException e) {
                statuses.add(new Status(StatusCode.ERROR,
                        String.format("Failed to compose url: %s Reason: %s", url, e.getMessage())));
            } catch (SocketTimeoutException e) {
                statuses.add(new Status(StatusCode.ERROR,
                        String.format("Timeout occured. Took too long to read from %s Reason: %s", url, e.getMessage())));
            }
            catch (IOException | ConnectionFailedException e) {
                statuses.add(new Status(StatusCode.ERROR,
                        String.format("Failed to retrieve data from %s Reason: %s", url, e.getMessage())));
            } catch (JSONException e) {
                statuses.add(new Status(StatusCode.CRITICAL,
                        String.format("Result returned from %s is not JSON. Reason: %s", url, e.getMessage())));
            }
            finally {
                checkVersionDto.closeConnection();
            }
        }

        ApplicationList forDownload = new ApplicationList();
        forDownload.setApplications(applicationsToDownload);

        try {
            checkVersionDto.saveDownloadList(forDownload);
        } catch (IOException e) {
            statuses.add(new Status(StatusCode.CRITICAL, String.format("Failed to save download list. Reason: %s", e.getMessage())));
        }

        return statuses;
    }

    private boolean isDownloaded(JSONObject json, ApplicationList downloadedApps, String latestVersion) {
        if (downloadedApps != null) {
            for (ApplicationData downloaded : downloadedApps.getApplications()) {
                if (downloaded.getName().getGroupId().equals(json.getString("groupId"))
                        && downloaded.getName().getArtifactId().equals(json.getString("artifactId"))) {
                    if (downloaded.getActiveVersion() == null || downloaded.getActiveVersion().equals(latestVersion)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}