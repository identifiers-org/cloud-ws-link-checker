package org.identifiers.cloud.ws.linkchecker;

import org.identifiers.cloud.ws.linkchecker.strategies.LinkCheckerStrategy;
import org.identifiers.cloud.ws.linkchecker.strategies.LinkCheckerReport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestRedisServer.class })
public class LinkCheckerTests {

    @Autowired
    private LinkCheckerStrategy checker;

    @Autowired
    @Qualifier("linkCheckerRestTemplate")
    private RestTemplate restTemplate;

    boolean checkForStatusCode(int code, boolean accept401or403) throws MalformedURLException {
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

        URI mockedURI = URI.create("https://host:69/file");
        mockServer.expect(ExpectedCount.once(), requestTo(mockedURI))
                .andExpect(method(HttpMethod.HEAD))
                .andRespond(withStatus(HttpStatus.valueOf(code)));

        LinkCheckerReport report = checker.check(mockedURI.toURL(), accept401or403);
        try {
            mockServer.verify();
            return report.isUrlAssessmentOk();
        } catch (AssertionError err) {
            return false;
        }
    }

    @Test
    public void check200isOk() throws MalformedURLException {
        assertTrue(checkForStatusCode(200, false));
        assertTrue(checkForStatusCode(200, true));
    }

    @Test
    public void checkAccept4xxFlag() throws MalformedURLException {
        assertTrue(checkForStatusCode(401, true));
        assertFalse(checkForStatusCode(401, false));

        assertTrue(checkForStatusCode(403, true));
        assertFalse(checkForStatusCode(403, false));
    }

    @Test
    public void codesShouldAlwaysFail() throws MalformedURLException {
        int[] codesThatShouldAlwaysFail = {400, 404, 500};
        for (int code : codesThatShouldAlwaysFail) {
            assertFalse(checkForStatusCode(code, true));
            assertFalse(checkForStatusCode(code, false));
        }
    }
}
