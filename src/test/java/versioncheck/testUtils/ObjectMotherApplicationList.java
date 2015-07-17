package versioncheck.testUtils;

import domain.ApplicationData;
import domain.ApplicationList;
import domain.MonitoringApplications;

import static java.util.Arrays.asList;

public class ObjectMotherApplicationList {
    public static ApplicationList createApplicationListWithData() {
        ApplicationData data = new ApplicationData();
        data.setName(MonitoringApplications.SPRINGFRAMEWORK_JDBC);
        data.setActiveVersion("1.2.3");

        ApplicationList list = new ApplicationList();
        list.setApplications(asList(data));

        return list;
    }
}
