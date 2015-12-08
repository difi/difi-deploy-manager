package no.difi.deploymanager.util;

import no.difi.deploymanager.domain.Status;
import no.difi.deploymanager.domain.StatusCode;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class Common {
    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");

    public static String replacePropertyParams(String property, String groupId, String artifactId) {
        return property.replace("$GROUP_ID", groupId).replace("$ARTIFACT_ID", artifactId);
    }

    public static String replacePropertyParams(String property, String groupId, String artifactId, String version) {
        return property.replace("$GROUP_ID", groupId).replace("$ARTIFACT_ID", artifactId).replace("&v=LATEST", "&v=" + version);
    }

    public static void logStatus(List<Status> result, Logger logManager) {
        Level logLevel;
        for (Status status : result) {
            if (status.getStatusCode() == StatusCode.SUCCESS) {
                logLevel = Level.INFO;
            } else if (status.getStatusCode() == StatusCode.ERROR) {
                logLevel = Level.ERROR;
            } else {
                logLevel = Level.FATAL;
            }
            logManager.log(logLevel, status.getDescription());
        }
    }
}
