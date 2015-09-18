package no.difi.deploymanager.versioncheck.service;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.domain.Status;
import no.difi.deploymanager.download.dao.DownloadDao;
import no.difi.deploymanager.remotelist.exception.RemoteApplicationListException;
import no.difi.deploymanager.remotelist.service.RemoteListService;
import no.difi.deploymanager.versioncheck.dao.CheckVersionDao;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
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
import static no.difi.deploymanager.util.StatusFactory.*;

/***
 * CheckVersionService is checking if any of the monitored appication (including self) have a new version available in external repository.
 * If new version is available, it will update the list over applications/artifacts to download.
 */
@Service
public class CheckVersionService {
    private final RemoteListService remoteListService;
    private final CheckVersionDao checkVersionDao;
    private final DownloadDao downloadDao;

    @Autowired
    public CheckVersionService(RemoteListService remoteListService, CheckVersionDao checkVersionDao, DownloadDao downloadDao) {
        this.remoteListService = remoteListService;
        this.checkVersionDao = checkVersionDao;
        this.downloadDao = downloadDao;
    }

    public List<Status> execute() {
        List<Status> statuses = new ArrayList<>();
        ApplicationList.Builder appList = new ApplicationList.Builder();

        try {
            for (ApplicationData remoteApp : remoteListService.execute().getApplications()) {
                verifyAndAddApplicationForDownloadList(statuses, appList, remoteApp);
            }
        } catch (RemoteApplicationListException e) {
            statuses.add(statusCritical(format("Can not fetch remote application list with versions.%s", e.getCause())));
        }

        try {
            downloadDao.saveDownloadList(appList.build());
        } catch (IOException e) {
            statuses.add(statusCritical(format("Failed to save download list. Reason: %s", e.getMessage())));
        }

        return statuses;
    }

    public ApplicationList retrieveRunningAppsList() throws IOException {
        return checkVersionDao.retrieveRunningAppsList();
    }

    private void verifyAndAddApplicationForDownloadList(List<Status> statuses, ApplicationList.Builder appList, ApplicationData remoteApp) {
        try {
            JSONObject json = checkVersionDao.retrieveExternalArtifactStatus(remoteApp.getGroupId(), remoteApp.getArtifactId());
            ApplicationList downloadedApps = checkVersionDao.retrieveRunningAppsList();

            if (isInDownloadList(json, downloadDao.retrieveDownloadList())) {
                statuses.add(statusSuccess(String.format("%s already prepared for download.", remoteApp.getName())));
            }
            else if (isDownloaded(json, downloadedApps)) {
                statuses.add(statusSuccess(format("Latest version of %s is already downloaded.", remoteApp.getName())));
            }
            else {
                statuses.add(addApplicationDataToDownloadList(appList, remoteApp, json));
            }
        }
        catch (MalformedURLException e) {
            statuses.add(statusError(String.format("Failed to compose url to retrieve latest version for %s", remoteApp.getName())));
        }
        catch (SocketTimeoutException e) {
            statuses.add(statusError(format("Socket timeout occured. Cannot get latest version for %s", remoteApp.getName())));
        }
        catch (IOException | ConnectionFailedException e) {
            statuses.add(statusError(format("Failed to retrieve latest version for %s", remoteApp.getName())));
        }
        catch (JSONException e) {
            statuses.add(statusCritical(format("Result from external when retrieving latest version for %s is not JSON.", remoteApp.getName())));
        }
        catch (IllegalStateException e) {
            statuses.add(statusCritical("Environment property 'location.version' not found."));
        }
    }

    private Status addApplicationDataToDownloadList(ApplicationList.Builder appList, ApplicationData remoteApp, JSONObject json) {
        ApplicationData data = new ApplicationData.Builder()
                .name(remoteApp.getName())
                .groupId(remoteApp.getGroupId())
                .artifactId(remoteApp.getArtifactId())
                .activeVersion(json.getString("version"))
                .startParameters(remoteApp.getStartParameters())
                .build();

        appList.addApplicationData(data);

        return statusSuccess(format("Application %s is prepared for download.", data.getName()));
    }

    private boolean isInDownloadList(JSONObject json, ApplicationList applicationList) {
        if (applicationList != null && applicationList.getApplications() != null) {
            for (ApplicationData data : applicationList.getApplications()) {
                if (data.getGroupId().equals(json.getString("groupId"))
                    && data.getArtifactId().equals(json.getString("artifactId")))
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