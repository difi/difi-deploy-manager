package no.difi.deploymanager.download.service;

import no.difi.deploymanager.domain.*;
import no.difi.deploymanager.download.dao.DownloadDao;
import no.difi.deploymanager.download.filetransfer.FileTransfer;
import no.difi.deploymanager.restart.service.RestartService;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

@Service
public class DownloadService {
    private final DownloadDao downloadDao;
    private final FileTransfer fileTransfer;
    private final RestartService restartService;

    private List<Status> statuses = new ArrayList<>();

    @Autowired
    public DownloadService(DownloadDao downloadDao, FileTransfer fileTransfer, RestartService restartService) {
        this.downloadDao = downloadDao;
        this.fileTransfer = fileTransfer;
        this.restartService = restartService;
    }

    public List<Status> execute() {
        try {
            ApplicationList forDownload = downloadDao.retrieveDownloadList();

            if (forDownload != null && forDownload.getApplications() != null) {
                ApplicationList restartList = new ApplicationList();
                restartList.setApplications(downloadApplications(forDownload));

                ApplicationList notDownloaded = updateNotDownloadedList(restartList, forDownload);
                downloadDao.saveDownloadList(notDownloaded);

                saveRestartList(restartList);
            } else {
                statuses.add(new Status(StatusCode.SUCCESS, "Nothing to download."));
            }
        }
        catch (IOException e) {
            statuses.add(new Status(StatusCode.ERROR, format("Failed to retrieve download list. Reason: %s", e.getMessage())));
        }

        return statuses;
    }

    private ApplicationList updateNotDownloadedList(ApplicationList restartList, ApplicationList forDownload) {
        List<ApplicationData> notDownloaded = new ArrayList<>();

        //Update list for applications that failed download.
        for (ApplicationData checklist : restartList.getApplications()) {
            if (!forDownload.hasApplicationData(checklist)) {
                notDownloaded.add(checklist);
            }
        }

        ApplicationList applicationList = new ApplicationList();
        applicationList.setApplications(notDownloaded);

        return applicationList;
    }

    private void saveRestartList(ApplicationList restartList) throws IOException {
        if (restartList != null && restartList.getApplications().size() != 0) {
            restartService.saveRestartList(restartList);

            statuses.add(new Status(StatusCode.SUCCESS, format("Downloaded apps, prepared for restart.")));
        }
        else {
            statuses.add(new Status(StatusCode.SUCCESS, format("No applications set for download.")));
        }
    }

    private List<ApplicationData> downloadApplications(ApplicationList forDownload) throws IOException {
        List<ApplicationData> restartList = new ArrayList<>();

        for (ApplicationData data : forDownload.getApplications()) {
            try {
                String versionDownloaded = fileTransfer.downloadApplication(data);

                DownloadedVersion downloadedVersion = new DownloadedVersion();
                downloadedVersion.setVersion(versionDownloaded);

                List<DownloadedVersion> allVersions = data.getDownloadedVersions();
                allVersions.add(downloadedVersion);
                data.setDownloadedVersions(allVersions);

                restartList.add(data);
            }
            catch (MalformedURLException e) {
                statuses.add(new Status(StatusCode.ERROR, format("Failed to compose URL for %s.", data.getName())));
            }
            catch (SocketTimeoutException e) {
                statuses.add(new Status(StatusCode.ERROR,
                        format("Timeout occured. It too too long to download %s %s", data.getName(), data.getFilename())));
            }
            catch (ConnectionFailedException e) {
                statuses.add(new Status(StatusCode.ERROR, "Connection for downloading updates failed."));
            }
        }

        return restartList;
    }
}
