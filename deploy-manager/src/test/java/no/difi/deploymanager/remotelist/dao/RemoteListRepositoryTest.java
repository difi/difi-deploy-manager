package no.difi.deploymanager.remotelist.dao;

import no.difi.deploymanager.domain.ApplicationData;
import no.difi.deploymanager.domain.ApplicationList;
import no.difi.deploymanager.util.JsonUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class RemoteListRepositoryTest {
    private RemoteListRepository remoteListRepository;

    @Mock JsonUtil jsonUtilMock;

    @Before
    public void setUp() {
        initMocks(this);

        remoteListRepository = new RemoteListRepository(jsonUtilMock);
    }

    @Test
    public void should_have_parsed_elements_from_list_when_local_list_is_retrieved() {
        ApplicationList localList = remoteListRepository.getLocalList();

        assertTrue(localList.getApplications().size() > 0);
    }

    @Test
    public void should_have_elements_in_list_when_local_list_is_retrieved() {
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