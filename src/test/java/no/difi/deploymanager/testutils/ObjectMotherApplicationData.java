package no.difi.deploymanager.testutils;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.MonitoringApplications;

import java.util.List;

import static java.util.Arrays.asList;

public class ObjectMotherApplicationData {
    public static ApplicationData createApplicationData() {
        return createApplicationData(MonitoringApplications.SPRINGFRAMEWORK_JDBC, "1.2.3");
    }

    public static ApplicationData createApplicationData(MonitoringApplications application, String version) {
        ApplicationData data = new ApplicationData();
        data.setName(application);
        data.setActiveVersion(version);

        return data;
    }

    public static List<ApplicationData> createApplicationDataList() {
        return asList(createApplicationData());
    }
}
