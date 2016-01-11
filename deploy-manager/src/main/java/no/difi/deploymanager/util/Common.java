package no.difi.deploymanager.util;

public class Common {
    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");

    public static String replacePropertyParams(String property, String groupId, String artifactId) {
        return property.replace("$GROUP_ID", groupId).replace("$ARTIFACT_ID", artifactId);
    }

    public static String replacePropertyParams(String property, String groupId, String artifactId, String version) {
        return property.replace("$GROUP_ID", groupId).replace("$ARTIFACT_ID", artifactId).replace("&v=LATEST", "&v=" + version);
    }
}
