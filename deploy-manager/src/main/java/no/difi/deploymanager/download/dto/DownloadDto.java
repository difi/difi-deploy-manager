package no.difi.deploymanager.download.dto;

import no.difi.deploymanager.domain.ApplicationList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;
import no.difi.deploymanager.util.IOUtil;
import no.difi.deploymanager.versioncheck.exception.ConnectionFailedException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Repository
public class DownloadDto {
    private static final int BUFFER_SIZE = 4096;

    private final Environment environment;
    private final IOUtil ioUtil;

    @Autowired
    public DownloadDto(Environment environment, IOUtil ioUtil) {
        this.environment = environment;
        this.ioUtil = ioUtil;
    }

    public void saveDownloadList(ApplicationList forDownload) throws IOException {
        ioUtil.saveApplicationList(
                forDownload,
                environment.getRequiredProperty("monitoring.base.path"),
                environment.getRequiredProperty("monitoring.fordownload.file")
        );
    }

    public ApplicationList retrieveDownloadList() throws IOException {
        return ioUtil.retrieveApplicationList(
                environment.getRequiredProperty("monitoring.base.path"),
                environment.getRequiredProperty("monitoring.fordownload.file")
        );
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
