package versioncheck;

import domain.ApplicationData;
import domain.ApplicationList;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class IOUtil {
    public ApplicationList retrieve(String filename) throws IOException {
        ObjectInputStream ois = null;
        try {
            String path = System.getProperty("user.dir");
            File file = new File(path + filename);

            if (!file.exists() || !file.isFile()) {
                return null;
            }
            ois = new ObjectInputStream(new FileInputStream(file));

            return (ApplicationList) ois.readObject();
        }
        catch (ClassNotFoundException e) {
            //TODO: Should be logged.
            return null;
        }
        finally {
            if (ois != null) {
                ois.close();
            }
        }
    }

    public void save(ApplicationList applicationList, String filename) throws IOException {
        String path = System.getProperty("user.dir");

        //Make sure that folder exists before creating file.
        File pathStructure = new File(path + "/data");
        if (!pathStructure.exists()) {
            pathStructure.mkdir();
        }
        File file = new File(path + filename);

        ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(file, false)
        );

        oos.writeObject(applicationList);

        oos.close();
    }
}
