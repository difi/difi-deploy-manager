package no.difi.deploymanager.versioncheck.service;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.download.dao.DownloadDao;
import no.difi.deploymanager.remotelist.exception.RemoteApplicationListException;
import no.difi.deploymanager.remotelist.service.ApplicationListService;
import no.difi.deploymanager.versioncheck.dao.CheckVersionDao;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

/***
 * CheckVersionService is checking if any of the monitored application (including self) have a new version available in external repository.
 * If new version is available, it will update the list over applications/artifacts to download.
 */
public class CheckVersionService {
    private static final Logger logger = LoggerFactory.getLogger(CheckVersionService.class);

    private final ApplicationListService applicationListService;
    private final CheckVersionDao checkVersionDao;
    private final DownloadDao downloadDao;


    public CheckVersionService(ApplicationListService applicationListService, CheckVersionDao checkVersionDao, DownloadDao downloadDao) {
        this.applicationListService = applicationListService;
        this.checkVersionDao = checkVersionDao;
        this.downloadDao = downloadDao;
    }

    public void execute() {
        ApplicationList.Builder appList = new ApplicationList.Builder();

        try {
            for (ApplicationData remoteApp : applicationListService.getApplications()) {
                verifyAndAddApplicationForDownloadList(appList, remoteApp);
            }
        } catch (RemoteApplicationListException | IOException e) {
            logger.error("Can not fetch remote application list with versions.", e);
        }

        try {
            downloadDao.saveDownloadList(appList.build());
        } catch (IOException e) {
            logger.error("Failed to save download list.", e);
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
                logger.info("{} already prepared for download.", remoteApp.getName());
            }
            else if (isDownloaded(json, downloadedApps)) {
                logger.info("Latest version of {} is already downloaded.", remoteApp.getName());
            }
            else if (!hasChangedParameters(remoteApp)) {
                logger.info("Parameters has not changed for {}", remoteApp.getName());
            }
            else {
                addApplicationDataToDownloadList(appList, remoteApp, json);
            }
        }
        catch (MalformedURLException e) {
            logger.error("Failed to compose url to retrieve latest version for {}", remoteApp.getName());
        }
        catch (SocketTimeoutException e) {
            logger.error("Socket timeout occurred. Cannot get latest version for {}", remoteApp.getName());
        }
        catch (IOException | ConnectionFailedException e) {
            logger.error("Failed to retrieve latest version for {}", remoteApp.getName());
        }
        catch (JSONException e) {
            logger.error("Result from external when retrieving latest version for {} is not JSON.", remoteApp.getName());
        }
        catch (IllegalStateException e) {
            logger.error("Environment property 'location.version' not found.");
        } catch (RemoteApplicationListException e) {
            logger.error("Cannot find list of applications to download (monitorApps.json)", e);
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

        logger.info("Application {} is prepared for download.", data.getName());
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

    private boolean hasChangedParameters(ApplicationData remoteApp) throws RemoteApplicationListException, IOException {
        final ApplicationList remoteList = applicationListService.getApplications();

        for (ApplicationData remote : remoteList.getApplications()) {
            if (isSameApplication(remoteApp, remote)) {
                return remoteApp.getVmOptions().equals(remote.getVmOptions())
                        && remoteApp.getEnvironmentVariables().equals(remote.getEnvironmentVariables())
                        && remoteApp.getMainClass().equals(remote.getMainClass());
            }
        }
        return true;
    }

    private boolean isSameApplication(ApplicationData local, ApplicationData remote) {
        return local.getArtifactId().equals(remote.getArtifactId())
                && local.getGroupId().equals(remote.getGroupId());
    }
}