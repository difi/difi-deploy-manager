package no.difi.deploymanager.util;

import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.Scanner;

/***
 * General class for retrieving JSON object from a specified URL.
 */
@Component
public class JsonUtil {
    private static final int CONNECTION_TIMEOUT = 5000; // ms for connection to occur
    private static final int DATARETRIEVAL_TIMEOUT = 10000; // ms for next byte to be read

    private HttpURLConnection connection;

    public JSONObject retrieveJsonObject(String url) throws IOException, ConnectionFailedException {
        URL request = new URL(url);

        connection = createConnection(request);

        try {
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new ConnectionFailedException("Connection to repository failed.");
            }
        } catch (SocketException e) {
            throw new ConnectionFailedException("Connection to repository failed.");
        }

        InputStream stream = new BufferedInputStream(connection.getInputStream());

        String next = new Scanner(stream).useDelimiter("\\A").next();
        JSONObject json = new JSONObject(next);

        closeConnection();
        stream.close();

        return json;
    }

    private HttpURLConnection createConnection(URL request) throws IOException {
        connection = (HttpURLConnection) request.openConnection();
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(DATARETRIEVAL_TIMEOUT);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        return connection;
    }

    private void closeConnection() {
        if (connection != null) {
            connection.disconnect();
        }
    }
}
