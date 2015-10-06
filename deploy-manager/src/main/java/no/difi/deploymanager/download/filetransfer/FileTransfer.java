package no.difi.deploymanager.download.filetransfer;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

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
@Repository
public class FileTransfer {
    private static final int BUFFER_SIZE = 4096;

    private final Environment environment;

    @Autowired
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

        System.out.println("**Downloading update**");
        System.out.println("**URL: " + source);

        String filePath = System.getProperty("user.dir") + environment.getRequiredProperty("download.base.path");

        File destinationPath = new File(filePath);
        if (!destinationPath.exists()) {
            destinationPath.mkdir();
        }

        HttpURLConnection connection = (HttpURLConnection) source.openConnection();
        HttpURLConnection.setFollowRedirects(true);
        connection.setInstanceFollowRedirects(true);

        String fileName = "";
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            String disposition = connection.getHeaderField("Content-Disposition");

            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            }

            InputStream sourceInput = connection.getInputStream();
            String saveFilePath = destinationPath + File.separator + fileName;

            FileOutputStream fileOutput = new FileOutputStream(saveFilePath);

            int bytesRead;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = sourceInput.read(buffer)) != -1) {
                fileOutput.write(buffer, 0, bytesRead);
            }

            fileOutput.close();
            sourceInput.close();
        } else {
            throw new ConnectionFailedException("Could not download file " + fileName);
        }
        connection.disconnect();

        return fileName;
    }

    /***
     * Put together download url based on information in application data.
     *
     * @param data Contains information about the application to download.
     * @return The full filename that has been downloaded.
     * @throws MalformedURLException
     */
    public URL makeUrlForDownload(ApplicationData data) throws MalformedURLException {
        if (environment.getProperty("download.source").equals("production")) {
            System.out.println("***Getting download link from production");
            return new URL(
                    replacePropertyParams(environment.getRequiredProperty("location.download"),
                            data.getGroupId(), data.getArtifactId())
            );
        }
        else {
            System.out.println("***Getting download link from test");
            return new URL(
                    replacePropertyParams(environment.getRequiredProperty("location.test.download"),
                            data.getGroupId(), data.getArtifactId())
            );
        }
    }
}
