package org.identifiers.cloud.ws.linkchecker.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.identifiers.cloud.ws.linkchecker.TestRedisServer;
import org.identifiers.cloud.ws.linkchecker.api.models.LinkScoringApiModel;
import org.identifiers.cloud.ws.linkchecker.api.models.ManagementApiModel;
import org.identifiers.cloud.ws.linkchecker.api.requests.*;
import org.identifiers.cloud.ws.linkchecker.api.responses.ServiceResponseScoringRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {TestRedisServer.class})
@ActiveProfiles("authenabled")
class SecurityConfigurationTest {
    @MockBean
    LinkScoringApiModel linkScoringApiModel;

    @Spy
    ManagementApiModel managementApiModel;

    @MockBean
    JwtDecoder jwtDecoder;

    MockMvc mvc;
    static ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setupMock(@Autowired WebApplicationContext context) {
        var response = new ServiceResponseScoringRequest();
        doReturn(response).when(linkScoringApiModel).getScoreForResolvedId(any());
        doReturn(response).when(linkScoringApiModel).getScoreForProvider(any());
        doCallRealMethod().when(linkScoringApiModel).getScoreForUrl(any());

        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testGetScoreForUrl() throws Exception {
        ScoringRequestPayload payload = new ScoringRequestPayload();
        payload.setUrl("google.com").setAccept401or403(false);
        ServiceRequestScoring request = new ServiceRequestScoring();
        request.setApiVersion("1.0").setPayload(payload);

        mvc.perform(post("/getScoreForUrl")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Not implemented
    }

    @Test
    void testGetScoreForResolvedId() throws Exception {
        ScoringRequestWithIdPayload payload = new ScoringRequestWithIdPayload().setId("1");
        payload.setUrl("google.com").setAccept401or403(false);
        ServiceRequestScoreResource request = new ServiceRequestScoreResource();
        request.setApiVersion("1.0").setPayload(payload);

        mvc.perform(post("/getScoreForResolvedId")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetScoreForProvider() throws Exception {
        ScoringRequestWithIdPayload payload = new ScoringRequestWithIdPayload().setId("1");
        payload.setUrl("google.com").setAccept401or403(false);
        ServiceRequestScoreProvider request = new ServiceRequestScoreProvider();
        request.setApiVersion("1.0").setPayload(payload);

        mvc.perform(post("/getScoreForProvider")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testFlushLinkCheckingHistory() throws Exception {
        mvc.perform(get("/management/flushLinkCheckingHistory"))
                .andExpect(status().isUnauthorized());
        verify(managementApiModel, never()).flushLinkCheckingHistory();
    }

    @Test
    void testActuators() throws Exception {
        mvc.perform(get("/actuator"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testActuatorsLoggers() throws Exception {
        mvc.perform(get("/actuator/loggers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testActuatorsHealth() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}