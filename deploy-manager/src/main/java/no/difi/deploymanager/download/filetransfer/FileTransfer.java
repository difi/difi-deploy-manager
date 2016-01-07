package no.difi.deploymanager.download.filetransfer;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.util.IOUtil;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static no.difi.deploymanager.util.Common.replacePropertyParams;

/***
 * FileTransfer perform the actual download of new applications/processes to be started or updated by Deploy Manager.
 */
public class FileTransfer {
    private static final int BUFFER_SIZE = 4096;

    private final Environment environment;

    public FileTransfer(Environment environment) {
        this.environment = environment;
    }

    /***
     * Download application given in data.
     *
     * @param data Contains information about the application to download.
     * @return The full filename that has been downloaded.
     * @throws IOException
     * @throws ConnectionFailedException
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public String downloadApplication(ApplicationData data) throws IOException, ConnectionFailedException {
        URL source = makeUrlForDownload(data);

        String filePath = System.getProperty("user.dir") + environment.getRequiredProperty("download.base.path");

        File destinationPath = IOUtil.createDestinationFolder(filePath);

        HttpURLConnection connection = (HttpURLConnection) source.openConnection();
        HttpURLConnection.setFollowRedirects(true);
        connection.setInstanceFollowRedirects(true);

        String fileName = "";
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            fileName = fetchFilenameFromHeader(connection, fileName);
            saveFile(destinationPath, fileName, connection.getInputStream());
        } else {
            throw new ConnectionFailedException("Could not download file " + fileName);
        }

        connection.disconnect();

        return fileName;
    }

    private String fetchFilenameFromHeader(HttpURLConnection connection, String fileName) {
        String disposition = connection.getHeaderField("Content-Disposition");

        if (disposition != null) {
            // extracts file name from header field
            int index = disposition.indexOf("filename=");
            if (index > 0) {
                fileName = disposition.substring(index + 10,
                        disposition.length() - 1);
            }
        }
        return fileName;
    }

    private void saveFile(File destinationPath, String fileName, InputStream sourceInput) throws IOException {
        String fileToSave = destinationPath + File.separator + fileName;
        try(FileOutputStream fileOutput = new FileOutputStream(fileToSave)) {

            int bytesRead;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = sourceInput.read(buffer)) != -1) {
                fileOutput.write(buffer, 0, bytesRead);
            }
        } finally {
            sourceInput.close();
        }
    }

    /***
     * Put together download url based on information in application data.
     *
     * @param data Contains information about the application to download.
     * @return The full filename that has been downloaded.
     * @throws MalformedURLException
     */
    protected URL makeUrlForDownload(ApplicationData data) throws MalformedURLException {
        if (environment.getProperty("application.runtime.environment").equals("production")) {
            return new URL(
                    replacePropertyParams(environment.getRequiredProperty("location.download"),
                            data.getGroupId(), data.getArtifactId())
            );
        }
        else if (environment.getProperty("application.runtime.environment").equals("staging")) {
            return new URL(
                    replacePropertyParams(environment.getRequiredProperty("location.staging.download"),
                            data.getGroupId(), data.getArtifactId())
            );
        }
        else {
            return new URL(
                    replacePropertyParams(environment.getRequiredProperty("location.test.download"),
                            data.getGroupId(), data.getArtifactId())
            );
        }
    }
}
