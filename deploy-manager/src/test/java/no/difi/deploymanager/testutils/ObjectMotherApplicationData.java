package no.difi.deploymanager.testutils;

import no.difi.deploymanager.domain.ApplicationData;

import java.util.List;

import static java.util.Arrays.asList;

public class ObjectMotherApplicationData {
    public static ApplicationData createApplicationData() {
        return createApplicationData("org.springframework", "org.springframework", "spring-jdbc", "1.2.3");
    }

    public static ApplicationData createApplicationData(String name, String groupId, String artifactId, String version) {
        return new ApplicationData.Builder()
                .name(name)
                .groupId(groupId)
                .artifactId(artifactId)
                .activeVersion(version)
                .build();
    }

    public static ApplicationData createApplicationData(String groupId, String artifactId) {
        return createApplicationData("test-app", groupId, artifactId, "");
    }

    public static List<ApplicationData> createApplicationDataList() {
        return asList(createApplicationData());
    }
}
