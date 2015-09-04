package no.difi.deploymanager.testutils;

import org.json.JSONObject;

public class ObjectMotherJSONObject {
    public static JSONObject createJsonObject(String version, String artifactId, String groupId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("groupId", groupId);
        jsonObject.put("artifactId", artifactId);
        jsonObject.put("version", version);
        jsonObject.put("snapshot", false);
        return jsonObject;
    }
}
