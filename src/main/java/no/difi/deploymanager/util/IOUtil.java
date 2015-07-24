package no.difi.deploymanager.util;

import no.difi.deploymanager.domain.ApplicationList;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;

@Component
public class IOUtil {
    public ApplicationList retrieveApplicationList(String folder, String filename) throws IOException {
        return (ApplicationList) retrieve(folder, filename);
    }

    public void saveApplicationList(ApplicationList applicationList, String folder, String filename) throws IOException {
        save(applicationList, folder, filename);
    }

    public List<Process> retrieveRunningProcesses() {
        return null;
    }

    private Object retrieve(String folder, String filename) throws IOException {
        ObjectInputStream ois = null;
        try {
            String path = System.getProperty("user.dir");
            File file = new File(path + folder + filename);

            if (!file.exists() || !file.isFile()) {
                return null;
            }
            ois = new ObjectInputStream(new FileInputStream(file));

            return ois.readObject();
        }
        catch (ClassNotFoundException e) {
            return null;
        }
        finally {
            if (ois != null) {
                ois.close();
            }
        }
    }

    private void save(Object objectToSave, String folder, String filename) throws IOException {
        String path = System.getProperty("user.dir");

        //Make sure that folder exists before creating file.
        File pathStructure = new File(path + folder);
        if (!pathStructure.exists()) {
            pathStructure.mkdir();
        }
        File file = new File(path + folder + filename);

        ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(file, false)
        );

        oos.writeObject(objectToSave);

        oos.close();
    }
}
