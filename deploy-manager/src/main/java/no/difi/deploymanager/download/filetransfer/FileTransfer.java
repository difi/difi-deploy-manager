package no.difi.deploymanager.download.filetransfer;

import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Repository
public class FileTransfer {
    private static final int BUFFER_SIZE = 4096;
    private final Environment environment;

    @Autowired
    public FileTransfer(Environment environment) {
        this.environment = environment;
    }

    public String downloadApplication(String url) throws IOException, ConnectionFailedException {
        URL source = new URL(url);

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
}
