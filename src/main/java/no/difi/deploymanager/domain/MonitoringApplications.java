package no.difi.deploymanager.domain;

import java.util.Arrays;
import java.util.List;

public enum MonitoringApplications {
    SPRINGFRAMEWORK_JDBC("org.springframework", "spring-jdbc");

    private final String groupId;
    private final String artifactId;

    MonitoringApplications(String groupId, String artifactId) {
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public static List<MonitoringApplications> getApplications() {
        return Arrays.asList(MonitoringApplications.values());
    }
}
