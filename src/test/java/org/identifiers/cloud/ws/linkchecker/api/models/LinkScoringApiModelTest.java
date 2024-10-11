package org.identifiers.cloud.ws.linkchecker.api.models;

import org.identifiers.cloud.ws.linkchecker.api.requests.ScoringRequestWithIdPayload;
import org.identifiers.cloud.ws.linkchecker.api.requests.ServiceRequestScoreResource;
import org.identifiers.cloud.ws.linkchecker.data.models.LinkCheckResult;
import org.identifiers.cloud.ws.linkchecker.models.ResourceTracker;
import org.identifiers.cloud.ws.linkchecker.services.HistoryTrackingService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertEquals;



class LinkScoringApiModelTest {
    private static final String RESOURCE_ID = "totally numeric ID";
    private int expectedScore;

    LinkScoringApiModel model;



    void setupModelAndMocks(int numValidResults, int numInvalidResults) {
        expectedScore = Math.round(100.0f*numValidResults/(numValidResults+numInvalidResults));

        HistoryTrackingService historyTrackingService = mock();
        model = new LinkScoringApiModel(historyTrackingService);

        var invalidResults = IntStream.range(0, numInvalidResults)
                .mapToObj(i -> new LinkCheckResult().setUrlAssessmentOk(false));
        var validResults = IntStream.range(0, numValidResults)
                .mapToObj(i -> new LinkCheckResult().setUrlAssessmentOk(true));

        var resourceTracker = new ResourceTracker();
        resourceTracker.setId(RESOURCE_ID);
        resourceTracker.initHistoryStats(Stream.concat(invalidResults, validResults).toList());
        doReturn(resourceTracker)
                .when(historyTrackingService)
                .getTrackerForResource(any());
        doReturn(List.of(resourceTracker))
                .when(historyTrackingService)
                .getAllResourceTrackers();
    }



    @ParameterizedTest(name = "valid={0}, invalid={1}")
    @MethodSource("getRandomParameters")
    void getScoreForResolvedId(int numValidResults, int numInvalidResults) {
        setupModelAndMocks(numValidResults, numInvalidResults);

        var request = new ServiceRequestScoreResource();
        var payload = new ScoringRequestWithIdPayload().setId(RESOURCE_ID);
        request.setPayload(payload);

        var response = model.getScoreForResolvedId(request);
        assertEquals(expectedScore, response.getPayload().getScore());
    }



    @ParameterizedTest(name = "valid={0}, invalid={1}")
    @MethodSource("getRandomParameters")
    void getResourcesIdsWithAvailabilityLowerThan(int numValidResults, int numInvalidResults) {
        setupModelAndMocks(numValidResults, numInvalidResults);

        var response = model.getResourcesIdsWithAvailabilityLowerThan(expectedScore - 1);
        assertEquals(0, response.size());

        response = model.getResourcesIdsWithAvailabilityLowerThan(expectedScore + 1);
        assertEquals(1, response.size());

        var entry = response.entrySet().iterator().next();
        assertEquals(RESOURCE_ID, entry.getKey());
        assertTrue(entry.getValue() < expectedScore + 1);
    }



    public static Stream<Arguments> getRandomParameters() {
        var random = new Random();
        int numArgumentPairs = random.nextInt(10, 20);
        return IntStream.range(0, numArgumentPairs).mapToObj(i -> Arguments.of(
          random.nextInt(3, 100), random.nextInt(3, 100)
        ));
    }
}