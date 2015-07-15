package versioncheck;

import domain.ApplicationList;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class IOUtil {
    public ApplicationList retrieve(String versionfile) throws IOException {
        ObjectInputStream inputStream = null;
        try {
            inputStream = new ObjectInputStream(new FileInputStream(versionfile));

            return (ApplicationList) inputStream.readObject();
        }
        catch (ClassNotFoundException e) {
            //TODO: Should be logged.
            return null;
        }
        finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public void save(ApplicationList applicationList, String filename) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filename));

        outputStream.writeObject(applicationList);
        outputStream.flush();
        outputStream.close();
    }
}
