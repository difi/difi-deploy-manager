package no.difi.deploymanager.util;

public class Common {
    public static String replacePropertyParams(String property, String groupId, String artifactId) {
        return property.replace("$GROUP_ID", groupId).replace("$ARTIFACT_ID", artifactId);
    }
}
