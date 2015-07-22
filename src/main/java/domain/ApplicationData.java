package domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApplicationData implements Serializable {
    MonitoringApplications name;
    String activeVersion;
    List<DownloadedVersion> downloadedVersions;
    private String filename;

    public MonitoringApplications getName() {
        return name;
    }

    public void setName(MonitoringApplications name) {
        this.name = name;
    }

    public String getActiveVersion() {
        return activeVersion;
    }

    public void setActiveVersion(String activeVersion) {
        this.activeVersion = activeVersion;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public List<DownloadedVersion> getDownloadedVersions() {
        if (downloadedVersions == null) {
            downloadedVersions = new ArrayList<>();
        }
        return downloadedVersions;
    }
    public void setDownloadedVersions(List<DownloadedVersion> downloadedVersions) {
        this.downloadedVersions = downloadedVersions;
    }
}
