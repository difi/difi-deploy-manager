package no.difi.deploymanager.testutils;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;

import static no.difi.deploymanager.testutils.ObjectMotherApplicationData.createApplicationData;

public class ObjectMotherApplicationList {
    public static ApplicationList createApplicationListWithData() {
        return new ApplicationList.Builder().addApplicationData(createApplicationData()).build();
    }

    public static ApplicationList createApplicationListEmpty() {
        return new ApplicationList.Builder().build();
    }

    public static ApplicationList createApplicationList(ApplicationData... applications) {
        ApplicationList.Builder listBuilder = new ApplicationList.Builder();

        for (ApplicationData data : applications) {
            listBuilder.addApplicationData(data);
        }

        return listBuilder.build();
    }
}
