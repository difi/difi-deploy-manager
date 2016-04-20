package no.difi.deploymanager.versioncheck.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class MavenArtifactTest {
    final String groupId = "org.apache.commons";
    final String artifactId = "commons";

    @Test
    public void mavenArtifactEqual() {

        MavenArtifact a1 = new MavenArtifact(groupId, artifactId, "1.0");
        MavenArtifact a2 = new MavenArtifact(groupId, artifactId, "1.0");

        assertEquals(a1, a2);
    }

    @Test
    public void mavenArtifactsNotEqual() {
        MavenArtifact a1 = new MavenArtifact(groupId, artifactId, "1.0");
        MavenArtifact a2 = new MavenArtifact(groupId, artifactId, "2.0");

        assertFalse(a1.equals(a2));
    }

    @Test
    public void mavenArtifactEqualIgnoreVersion() {
        MavenArtifact a1 = new MavenArtifact(groupId, artifactId, "1.0");
        MavenArtifact a2 = new MavenArtifact(groupId, artifactId, "2.0");

        assertTrue(a1.equalsIgnoreVersion(a2));
    }

    @Test
    public void mavenArtifactNotEqualIgnoreVersion() {
        MavenArtifact a1 = new MavenArtifact(groupId, artifactId, "1.0");
        MavenArtifact a2 = new MavenArtifact(groupId, artifactId, "2.0");

        assertTrue(a1.equalsIgnoreVersion(a2));
    }

}