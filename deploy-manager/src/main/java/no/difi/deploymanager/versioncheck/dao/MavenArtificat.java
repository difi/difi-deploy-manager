package no.difi.deploymanager.versioncheck.dao;

/**
 * Contains properties to identify a Maven artifact
 */
public class MavenArtificat {
    private final String groupId;
    private final String artifactId;
    private final String version;

    public MavenArtificat(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }
}
