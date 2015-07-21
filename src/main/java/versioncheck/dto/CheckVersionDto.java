package versioncheck.dto;

import domain.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;
import util.IOUtil;
import versioncheck.exception.ConnectionFailedException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@Repository
public class CheckVersionDto {
    private static final int CONNECTION_TIMEOUT = 5000; // ms for connection to occur
    private static final int DATARETRIEVAL_TIMEOUT = 10000; // ms for next byte to be read

    private final Environment environment;
    private final IOUtil ioUtil;

    private HttpURLConnection connection;

    @Autowired
    public CheckVersionDto(Environment environment, IOUtil ioUtil) {
        this.environment = environment;
        this.ioUtil = ioUtil;
    }

    public JSONObject retrieveExternalArtifactStatus(String url) throws IOException, ConnectionFailedException {
        URL request = new URL(url);

        connection = createConnection(request);

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new ConnectionFailedException("Connection to repository failed.");
        }
        InputStream stream = new BufferedInputStream(connection.getInputStream());

        return (JSONObject) new JSONObject(new Scanner(stream).useDelimiter("\\A").next()).get("data");
    }

    public void saveMonitoringList(ApplicationList applicationList) throws IOException {
        ioUtil.save(
                applicationList,
                environment.getRequiredProperty("monitoring.base.path"),
                environment.getRequiredProperty("monitoring.downloaded.file")
        );
    }

    public ApplicationList retrieveMonitoringList() throws IOException {
        return ioUtil.retrieve(
                environment.getRequiredProperty("monitoring.base.path"),
                environment.getRequiredProperty("monitoring.downloaded.file")
        );
    }

    public void closeConnection() {
        if (connection != null) {
            connection.disconnect();
        }
    }

    private HttpURLConnection createConnection(URL request) throws IOException {
        connection = (HttpURLConnection) request.openConnection();
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(DATARETRIEVAL_TIMEOUT);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        return connection;
    }
}
