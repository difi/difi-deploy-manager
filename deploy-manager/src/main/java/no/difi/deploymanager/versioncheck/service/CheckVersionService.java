package no.difi.deploymanager.versioncheck.service;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.download.dao.DownloadDao;
import no.difi.deploymanager.remotelist.exception.RemoteApplicationListException;
import no.difi.deploymanager.remotelist.service.RemoteListService;
import no.difi.deploymanager.versioncheck.dao.CheckVersionDao;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import static java.lang.String.format;

/***
 * CheckVersionService is checking if any of the monitored application (including self) have a new version available in external repository.
 * If new version is available, it will update the list over applications/artifacts to download.
 */
public class CheckVersionService {
    private static final Logger logger = LoggerFactory.getLogger(CheckVersionService.class);

    private final RemoteListService remoteListService;
    private final CheckVersionDao checkVersionDao;
    private final DownloadDao downloadDao;


    public CheckVersionService(RemoteListService remoteListService, CheckVersionDao checkVersionDao, DownloadDao downloadDao) {
        this.remoteListService = remoteListService;
        this.checkVersionDao = checkVersionDao;
        this.downloadDao = downloadDao;
    }

    public void execute() {
        ApplicationList.Builder appList = new ApplicationList.Builder();

        try {
            for (ApplicationData remoteApp : remoteListService.execute().getApplications()) {
                verifyAndAddApplicationForDownloadList(appList, remoteApp);
            }
        } catch (RemoteApplicationListException | IOException e) {
            logger.error(format("Can not fetch remote application list with versions.%s", e.getCause()));
        }

        try {
            downloadDao.saveDownloadList(appList.build());
        } catch (IOException e) {
            logger.error(format("Failed to save download list. Reason: %s", e.getMessage()));
        }
    }

    public ApplicationList retrieveRunningAppsList() throws IOException {
        return checkVersionDao.retrieveRunningAppsList();
    }

    public void saveRunningAppsList(ApplicationList applicationList) throws IOException {
        checkVersionDao.saveRunningAppsList(applicationList);
    }

    private void verifyAndAddApplicationForDownloadList(ApplicationList.Builder appList, ApplicationData remoteApp) {
        try {
            JSONObject json = checkVersionDao.retrieveExternalArtifactStatus(
                    remoteApp.getGroupId(),
                    remoteApp.getArtifactId(),
                    getVersionFromJson()
            );
            ApplicationList downloadedApps = checkVersionDao.retrieveRunningAppsList();

            if (isInDownloadList(json, downloadDao.retrieveDownloadList())) {
                logger.info(format("%s already prepared for download.", remoteApp.getName()));
            }
            else if (isDownloaded(json, downloadedApps)) {
                logger.info(format("Latest version of %s is already downloaded.", remoteApp.getName()));
            }
            else if (!hasChangedParameters(remoteApp, json)) {
                logger.info("Parameters has not changed for " + remoteApp.getName());
            }
            else {
                addApplicationDataToDownloadList(appList, remoteApp, json);
            }
        }
        catch (MalformedURLException e) {
            logger.error(format("Failed to compose url to retrieve latest version for %s", remoteApp.getName()));
        }
        catch (SocketTimeoutException e) {
            logger.error(format("Socket timeout occurred. Cannot get latest version for %s", remoteApp.getName()));
        }
        catch (IOException | ConnectionFailedException e) {
            logger.error(format("Failed to retrieve latest version for %s", remoteApp.getName()));
        }
        catch (JSONException e) {
            logger.error(format("Result from external when retrieving latest version for %s is not JSON.", remoteApp.getName()));
        }
        catch (IllegalStateException e) {
            logger.error("Environment property 'location.version' not found.");
        }
    }

    private String getVersionFromJson() throws IOException, ConnectionFailedException {
        String tempString = checkVersionDao.retrieveIntegrasjonspunktThroughLuceneSearch().toString();
        int index = tempString.indexOf("\"latestSnapshot\"");
        int start = tempString.indexOf(":\"", index) + 2;
        int end = tempString.indexOf("\"", start);

        return tempString.substring(start, end);
    }

    private void addApplicationDataToDownloadList(ApplicationList.Builder appList, ApplicationData remoteApp, JSONObject json) {
        ApplicationData data = new ApplicationData.Builder()
                .name(remoteApp.getName())
                .groupId(remoteApp.getGroupId())
                .artifactId(remoteApp.getArtifactId())
                .activeVersion(json.getString("version"))
                .vmOptions(remoteApp.getVmOptions())
                .environmentVariables(remoteApp.getEnvironmentVariables())
                .mainClass(remoteApp.getMainClass())
                .build();

        appList.addApplicationData(data);

        logger.info(format("Application %s is prepared for download.", data.getName()));
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

    private boolean hasChangedParameters(ApplicationData remoteApp, JSONObject json) {
        return remoteApp.getVmOptions().equals(json.get("vmOptions"))
                && remoteApp.getEnvironmentVariables().equals(json.get("environmentVariables"))
                && remoteApp.getMainClass().equals(json.get("mainClass"));
    }
}