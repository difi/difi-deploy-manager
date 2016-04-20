package no.difi.deploymanager.versioncheck.dao;

import java.io.Serializable;

/**
 * Contains properties to identify a Maven artifact
 */
public class MavenArtifact implements Serializable {
    private final String groupId;
    private final String artifactId;
    private final String version;

    public MavenArtifact(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public MavenArtifact(MavenArtifact mavenArtifact) {
        this(mavenArtifact.getGroupId(), mavenArtifact.getArtifactId(), mavenArtifact.getVersion());
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

    public boolean equalsIgnoreVersion(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MavenArtifact that = (MavenArtifact) o;

        return groupId.equals(that.groupId) && artifactId.equals(that.artifactId);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MavenArtifact that = (MavenArtifact) o;

        return groupId.equals(that.groupId) && artifactId.equals(that.artifactId) && version.equals(that.version);

    }

    @Override
    public int hashCode() {
        int result = groupId.hashCode();
        result = 31 * result + artifactId.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }
}
