package no.difi.deploymanager.testutils;

import no.difi.deploymanager.domain.ApplicationData;

import java.util.List;

import static java.util.Arrays.asList;

public class ObjectMotherApplicationData {
    public static ApplicationData createApplicationData() {
        return createApplicationData("org.springframework", "org.springframework", "spring-jdbc", "1.2.3");
    }

    public static ApplicationData createApplicationData(String name, String groupId, String artifactId, String version) {
        ApplicationData data = new ApplicationData();
        data.setName(name);
        data.setGroupId(groupId);
        data.setArtifactId(artifactId);
        data.setActiveVersion(version);

        return data;
    }

    public static List<ApplicationData> createApplicationDataList() {
        return asList(createApplicationData());
    }
}
