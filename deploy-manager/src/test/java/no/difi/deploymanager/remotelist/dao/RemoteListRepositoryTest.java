package no.difi.deploymanager.remotelist.dao;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.util.JsonUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

import java.io.*;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class RemoteListRepositoryTest {
    private RemoteListRepository remoteListRepository;

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");

    @Mock JsonUtil jsonUtilMock;

    @BeforeClass
    public static void validateAndSetup() {
        String userDir = System.getProperty("user.dir");
        String pathWithFile = "";
        try {
            pathWithFile = "/data/monitorApps.json";
            if (!IS_WINDOWS && !userDir.contains("/deploy-manager")) {
                pathWithFile = "/deploy-manager" + pathWithFile;
            }
            else if (IS_WINDOWS && !userDir.contains("\\deploy-manager")) {
                pathWithFile = "\\deploy-manager" + pathWithFile.replace("/", "\\");
            }
            new FileReader(userDir + pathWithFile);
        } catch (FileNotFoundException e) {
            try {
                File file = new File(System.getProperty("user.dir") + pathWithFile);
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write("{\"artifacts\": [\n" +
                        "    {\n" +
                        "        \"name\": \"Difi Deploy Manager\",\n" +
                        "        \"groupId\": \"no.difi.deploymanager\",\n" +
                        "        \"artifactId\": \"deploy-manager\",\n" +
                        "        \"activeVersion\": \"\",\n" +
                        "        \"artifactType\": \"JAR\",\n" +
                        "        \"filename\": \"\",\n" +
                        "        \"vmOptions\": \"\",\n" +
                        "        \"environmentVariables\": \"-Dapplication.runtime.status=test\",\n" +
                        "        \"mainClass\": \"\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "        \"name\": \"Difi Integrasjonspunkt\",\n" +
                        "        \"groupId\": \"no.difi.meldingsutveksling\",\n" +
                        "        \"artifactId\": \"integrasjonspunkt\",\n" +
                        "        \"activeVersion\": \"\",\n" +
                        "        \"filename\": \"\",\n" +
                        "        \"vmOptions\": \"\",\n" +
                        "        \"environmentVariables\": \"-Dprivatekeyalias=910094092 -Dprivatekeypassword=changeit -Dkeystorelocation=/home/miif/test-certificates.jks -Dserver.port=9092 -Dorgnumber=910094092 -Daltinn.username=2435 -Daltinn.password=ROBSTAD1 -Dspring.profiles.active=dev\",\n" +
                        "        \"mainClass\": \"no.difi.meldingsutveksling.IntegrasjonspunktApplication\"\n" +
                        "    }]\n" +
                        "}");
                writer.flush();
                writer.close();
            } catch (IOException e1) {
                System.out.println("Failed creating json that is necessary for the test");
            }
        }
    }

    @Before
    public void setUp() {
        initMocks(this);

        remoteListRepository = new RemoteListRepository(jsonUtilMock);
    }

    @Test
    public void should_have_parsed_elements_from_list_when_local_list_is_retrieved() throws Exception {
        ApplicationList localList = remoteListRepository.getLocalList();

        assertTrue(localList.getApplications().size() > 0);
    }

    @Test
    public void should_have_elements_in_list_when_local_list_is_retrieved() throws Exception {
        ApplicationData data = remoteListRepository.getLocalList().getApplications().get(0);

        assertTrue(data.getName().length() > 0);
        assertTrue(data.getGroupId().length() > 0);
    }

    @Test
    public void should_call_retrieve_on_json_when_fetching_remote_list() throws Exception {
        remoteListRepository.getRemoteList();

        verify(jsonUtilMock, times(1)).retrieveJsonObject(anyString());
    }
}