package no.difi.deploymanager.testutils;

import no.difi.deploymanager.domain.ApplicationList;

import static no.difi.deploymanager.testutils.ObjectMotherApplicationData.createApplicationDataList;

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
