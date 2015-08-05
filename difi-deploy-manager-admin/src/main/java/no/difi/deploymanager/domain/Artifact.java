package no.difi.deploymanager.domain;

public class Artifact {
    private String name;
    private String groupId;
    private String artifactId;
    private String version;
    private ArtifactType applicationType;
    private String startParams;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ArtifactType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ArtifactType applicationType) {
        this.applicationType = applicationType;
    }

    public String getStartParameters() {
        return startParams;
    }

    public void setStartParameters(String startParams) {
        this.startParams = startParams;
    }
}
