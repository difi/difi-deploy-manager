package versioncheck.testUtils;

import domain.ApplicationList;

import static versioncheck.testUtils.ObjectMotherApplicationData.createApplicationDataList;

public class ObjectMotherApplicationList {
    public static ApplicationList createApplicationListWithData() {
        ApplicationList list = new ApplicationList();
        list.setApplications(createApplicationDataList());

        return list;
    }

    public static ApplicationList createApplicationListEmpty() {
        return new ApplicationList();
    }
}
