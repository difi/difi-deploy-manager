package no.difi.deploymanager.download.service;

import no.difi.deploymanager.domain.*;
import no.difi.deploymanager.download.dao.DownloadDao;
import no.difi.deploymanager.download.filetransfer.FileTransfer;
import no.difi.deploymanager.restart.dto.RestartDto;
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
    private final RestartDto restartDto;

    private List<Status> statuses = new ArrayList<>();

    @Autowired
    public DownloadService(DownloadDao downloadDao, FileTransfer fileTransfer, RestartDto restartDto) {
        this.downloadDao = downloadDao;
        this.fileTransfer = fileTransfer;
        this.restartDto = restartDto;
    }

    public List<Status> execute() {
        List<ApplicationData> restartList;

        try {
            ApplicationList forDownload = downloadDao.retrieveDownloadList();

            if (forDownload != null && forDownload.getApplications() != null) {
                restartList = downloadApplications(forDownload);

                ApplicationList notDownloaded = new ApplicationList();
                notDownloaded.setApplications(updateNotDownloadedList(restartList, forDownload));
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

    private List<ApplicationData> updateNotDownloadedList(List<ApplicationData> restartList, ApplicationList forDownload) {
        List<ApplicationData> notDownloaded = new ArrayList<>();

        //Update list for applications that failed download.
        for (ApplicationData checklist : restartList) {
            boolean found = false;
            for (ApplicationData worklist : forDownload.getApplications()) {
                if (checklist.getName().equals(worklist.getName())) {
                    found = true;
                }
            }
            if (!found) {
                notDownloaded.add(checklist);
            }
        }
        return notDownloaded;
    }

    private void saveRestartList(List<ApplicationData> restartList) throws IOException {
        if (restartList != null && restartList.size() != 0) {
            ApplicationList applicationsForRestart = new ApplicationList();
            applicationsForRestart.setApplications(restartList);

            restartDto.saveRestartList(applicationsForRestart);

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
