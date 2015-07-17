package versioncheck.testUtils;

import domain.ApplicationData;
import domain.ApplicationList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CustomAssert {
    public static void assertApplicationList(ApplicationList expected, ApplicationList actual) {
        assertTrue(expected.getApplications().size() == actual.getApplications().size());

        for (int i = 0; i < expected.getApplications().size(); i++) {
            ApplicationData expectedData = expected.getApplications().get(i);
            ApplicationData actualData = actual.getApplications().get(i);

            assertTrue(expectedData.getActiveVersion().equals(actualData.getActiveVersion()));
            assertEquals(expectedData.getName(), actualData.getName());
        }
    }
}
