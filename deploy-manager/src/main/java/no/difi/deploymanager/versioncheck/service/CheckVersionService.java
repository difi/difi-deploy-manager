package no.difi.deploymanager.versioncheck.service;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.domain.Status;
import no.difi.deploymanager.domain.StatusCode;
import no.difi.deploymanager.download.dto.DownloadDto;
import no.difi.deploymanager.remotelist.exception.RemoteApplicationListException;
import no.difi.deploymanager.remotelist.service.RemoteListService;
import no.difi.deploymanager.util.Common;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
import no.difi.deploymanager.versioncheck.repository.CheckVersionRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

@Service
public class CheckVersionService {
    private final Environment environment;
    private final RemoteListService remoteListService;
    private final CheckVersionRepository checkVersionDto;
    private final DownloadDto downloadDto;

    @Autowired
    public CheckVersionService(Environment environment, RemoteListService remoteListService, CheckVersionRepository checkVersionDto, DownloadDto downloadDto) {
        this.environment = environment;
        this.remoteListService = remoteListService;
        this.checkVersionDto = checkVersionDto;
        this.downloadDto = downloadDto;
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

        try {
            for (ApplicationData remoteApp : remoteListService.execute().getApplications()) {
                String url = Common.replacePropertyParams(location, remoteApp.getGroupId(), remoteApp.getArtifactId());

                try {
                    JSONObject json = checkVersionDto.retrieveExternalArtifactStatus(url);
                    ApplicationList downloadedApps = checkVersionDto.retrieveRunningAppsList();

                    if (isInDownloadList(json, downloadDto.retrieveDownloadList())) {
                        statuses.add(new Status(StatusCode.SUCCESS, format("%s is already in download list", url)));
                    }
                    if (isDownloaded(json, downloadedApps)) {
                        statuses.add(new Status(StatusCode.SUCCESS,
                                format("Latest version of %s is already downloaded.", url)));
                    } else {
                        ApplicationData data = new ApplicationData();
                        data.setName(remoteApp.getName());
                        data.setGroupId(remoteApp.getGroupId());
                        data.setArtifactId(remoteApp.getArtifactId());
                        data.setActiveVersion(json.getString("version"));
                        applicationsToDownload.add(data);

                        statuses.add(new Status(StatusCode.SUCCESS,
                                format("Application %s is set for download.", data.getName())));
                    }
                } catch (MalformedURLException e) {
                    statuses.add(new Status(StatusCode.ERROR,
                            format("Failed to compose url: %s Reason: %s", url, e.getMessage())));
                } catch (SocketTimeoutException e) {
                    statuses.add(new Status(StatusCode.ERROR,
                            format("Timeout occured. Took too long to read from %s Reason: %s", url, e.getMessage())));
                }
                catch (IOException | ConnectionFailedException e) {
                    statuses.add(new Status(StatusCode.ERROR,
                            format("Failed to retrieve data from %s Reason: %s", url, e.getMessage())));
                } catch (JSONException e) {
                    statuses.add(new Status(StatusCode.CRITICAL,
                            format("Result returned from %s is not JSON. Reason: %s", url, e.getMessage())));
                }
            }
        } catch (RemoteApplicationListException e) {
            statuses.add(new Status(StatusCode.CRITICAL, format("Can not fetch remote application list with versions.%s", e.getCause())));
        }

        ApplicationList forDownload = new ApplicationList();
        forDownload.setApplications(applicationsToDownload);

        try {
            downloadDto.saveDownloadList(forDownload);
        } catch (IOException e) {
            statuses.add(new Status(StatusCode.CRITICAL, format("Failed to save download list. Reason: %s", e.getMessage())));
        }

        return statuses;
    }

    private boolean isInDownloadList(JSONObject json, ApplicationList applicationList) {
        if (applicationList != null && applicationList.getApplications() != null) {
            for (ApplicationData data : applicationList.getApplications()) {
                if (data.getArtifactId().equals(json.getString("groupId"))
                    && data.getGroupId().equals(json.getString("artifactId")))
                    if (data.getActiveVersion() != null && data.getActiveVersion().equals(json.getString("version"))) {
                        return true;
                }
            }
        }
        return false;
    }

    private boolean isDownloaded(JSONObject json, ApplicationList downloadedApps) {
        if (downloadedApps != null) {
            for (ApplicationData downloaded : downloadedApps.getApplications()) {
                if (downloaded.getGroupId().equals(json.getString("groupId"))
                        && downloaded.getArtifactId().equals(json.getString("artifactId"))) {
                    if (downloaded.getActiveVersion() == null
                            || downloaded.getActiveVersion().equals(json.getString("version"))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}