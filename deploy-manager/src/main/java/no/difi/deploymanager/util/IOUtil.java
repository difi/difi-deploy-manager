package no.difi.deploymanager.util;

import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.domain.Self;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/***
 * General class for object serialization of application list.
 *
 * @see no.difi.deploymanager.domain.ApplicationList
 */
public class IOUtil {
    public ApplicationList retrieveApplicationList(String folder, String filename) throws IOException {
        return (ApplicationList) retrieve(folder, filename);
    }

    public void saveApplicationList(ApplicationList applicationList, String folder, String filename) throws IOException {
        save(applicationList, folder, filename);
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void save(Object objectToSave, String folder, String filename) throws IOException {
        String path = System.getProperty("user.dir");

        //Make sure that folder exists before creating file.
        final String destinationPath = path + folder;
        File pathStructure = IOUtil.createDestinationFolder(destinationPath);
        File file = new File(pathStructure, filename);

        ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(file, false)
        );

        oos.writeObject(objectToSave);

        oos.close();
    }

    public static File createDestinationFolder(String filePath) {
        File destinationPath = new File(filePath);
        if (!destinationPath.exists()) {
            final boolean directoryCreated = destinationPath.mkdir();
            if(!directoryCreated) {
                throw new RuntimeException("Failed to create destination folder: " + destinationPath);
            }
        }
        return destinationPath;
    }

    public synchronized Self getVersion() {
        Self.Builder self = new Self.Builder();

        Package pack = getClass().getPackage();
        if (pack != null) {
            if (pack.getName() == null) {
                if (pack.getImplementationTitle() == null) {
                    self.name(pack.getSpecificationTitle());
                }
                else {
                    self.name(pack.getImplementationTitle());
                }
            }
            else {
                self.name(pack.getName());
            }

            if (pack.getImplementationVersion() == null) {
                self.version(pack.getSpecificationVersion());
            }
            else {
                self.version(pack.getImplementationVersion());
            }
        }

        return self.build();
    }
}
