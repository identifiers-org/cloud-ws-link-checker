package org.identifiers.cloud.ws.linkchecker;

import org.identifiers.cloud.ws.linkchecker.strategies.LinkChecker;
import org.identifiers.cloud.ws.linkchecker.strategies.LinkCheckerReport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EmbeddedRedditTestConfiguration.class})
@ActiveProfiles("authdisabled")
public class LinkCheckerTest {

    @Autowired
    private LinkChecker checker;

    boolean checkForStatusCode(int code, boolean accept401or403) throws IOException {
        HttpURLConnection mockedConnection = mock(HttpURLConnection.class);

        URLStreamHandler stubUrlHandler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                return mockedConnection;
            }
        };
        URL mockedURL = new URL("proto", "host", 69, "/file", stubUrlHandler); //Nice
        when(mockedConnection.getResponseCode()).thenReturn(code);

        LinkCheckerReport report = checker.check(mockedURL, accept401or403);
        return report.isUrlAssessmentOk();
    }

    @Test
    public void check200isOk() throws IOException {
        assertTrue(checkForStatusCode(200, false));
        assertTrue(checkForStatusCode(200, true));
    }

    @Test
    public void checkAccept4xxFlag() throws IOException {
        assertTrue(checkForStatusCode(401, true));
        assertFalse(checkForStatusCode(401, false));

        assertTrue(checkForStatusCode(403, true));
        assertFalse(checkForStatusCode(403, false));
    }
}
