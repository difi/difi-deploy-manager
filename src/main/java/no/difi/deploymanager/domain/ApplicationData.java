package no.difi.deploymanager.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApplicationData implements Serializable {
    private String name;
    private String groupId;
    private String artifactId;
    private String activeVersion;
    private List<DownloadedVersion> downloadedVersions;
    private String filename;
    private String startParameters;
    private String artifactType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
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

    public void setStartParameters(String startParameters) {
        this.startParameters = startParameters;
    }

    public String getStartParameters() {
        return startParameters;
    }

    public void artifactType(String artifactType) {
        this.artifactType = artifactType;
    }

    public String getArtifactType() {
        return this.artifactType;
    }
}
