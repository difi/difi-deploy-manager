package no.difi.deploymanager.versioncheck.service;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.domain.Status;
import no.difi.deploymanager.domain.StatusCode;
import no.difi.deploymanager.download.dto.DownloadDto;
import no.difi.deploymanager.remotelist.exception.RemoteApplicationListException;
import no.difi.deploymanager.remotelist.service.RemoteListService;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
import no.difi.deploymanager.versioncheck.repository.CheckVersionRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

@Service
public class CheckVersionService {
    private final RemoteListService remoteListService;
    private final CheckVersionRepository checkVersionRepository;
    private final DownloadDto downloadDto;

    @Autowired
    public CheckVersionService(RemoteListService remoteListService, CheckVersionRepository checkVersionRepository, DownloadDto downloadDto) {
        this.remoteListService = remoteListService;
        this.checkVersionRepository = checkVersionRepository;
        this.downloadDto = downloadDto;
    }

    public List<Status> execute() {
        List<Status> statuses = new ArrayList<>();
        List<ApplicationData> applicationsToDownload = new ArrayList<>();

        try {
            for (ApplicationData remoteApp : remoteListService.execute().getApplications()) {
                try {
                    JSONObject json = checkVersionRepository.retrieveExternalArtifactStatus(remoteApp.getGroupId(), remoteApp.getArtifactId());
                    ApplicationList downloadedApps = checkVersionRepository.retrieveRunningAppsList();

                    if (isInDownloadList(json, downloadDto.retrieveDownloadList())) {
                        statuses.add(new Status(StatusCode.SUCCESS,
                                String.format("%s already in download list", remoteApp.getName())));
                    }
                    if (isDownloaded(json, downloadedApps)) {
                        statuses.add(new Status(StatusCode.SUCCESS,
                                format("Latest version of %s is already downloaded.", remoteApp.getName())));
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
                }
                catch (MalformedURLException e) {
                    statuses.add(new Status(StatusCode.ERROR,
                            String.format("Failed to compose url to retrieve latest version for %s", remoteApp.getName())));
                }
                catch (SocketTimeoutException e) {
                    statuses.add(new Status(StatusCode.ERROR,
                            String.format("Socket timeout occured. Cannot get latest version for %s", remoteApp.getName())));
                }
                catch (IOException | ConnectionFailedException e) {
                    statuses.add(new Status(StatusCode.ERROR,
                            String.format("Failed to retrieve latest version for %s", remoteApp.getName())));
                }
                catch (JSONException e) {
                    statuses.add(new Status(StatusCode.CRITICAL,
                            String.format("Result from external when retrieving latest version for %s is not JSON.", remoteApp.getName())));
                }
                catch (IllegalStateException e) {
                    statuses.add(new Status(StatusCode.CRITICAL, "Enviroment property 'location.version' not found."));
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