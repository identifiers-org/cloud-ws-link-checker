package org.identifiers.cloud.ws.linkchecker.api.models;

import org.identifiers.cloud.ws.linkchecker.api.ApiCentral;
import org.identifiers.cloud.ws.linkchecker.api.requests.ServiceRequestScoreProvider;
import org.identifiers.cloud.ws.linkchecker.api.requests.ServiceRequestScoreResource;
import org.identifiers.cloud.ws.linkchecker.api.requests.ServiceRequestScoring;
import org.identifiers.cloud.ws.linkchecker.api.responses.ServiceResponseScoringRequest;
import org.identifiers.cloud.ws.linkchecker.api.responses.ServiceResponseScoringRequestPayload;
import org.identifiers.cloud.ws.linkchecker.models.HistoryTracker;
import org.identifiers.cloud.ws.linkchecker.services.HistoryTrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.models
 * Timestamp: 2018-05-21 10:40
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 *
 * Link Scoring API, implements a reliability score [0, 100] on a given provider, resource or plain URL. '0' means it's
 * never seen on-line, '100' means it's always seen on-line.
 *
 * We need to keep in mind that this scoring is based on the perception that the running copies of this service have
 * about a provider, resource or URL, from wherever they're running. Other copies of the service running on different
 * infrastructures will have a different perception of the provicers, resources or URLs checked.
 */
@Component
@Scope("prototype")
public class LinkScoringApiModel {
    private static final Logger logger = LoggerFactory.getLogger(LinkScoringApiModel.class);

    @Autowired
    private HistoryTrackingService historyTrackingService;

    private ServiceResponseScoringRequest getDefaultResponse() {
        ServiceResponseScoringRequest response = new ServiceResponseScoringRequest();
        response.setApiVersion(ApiCentral.apiVersion);
        response.setPayload(new ServiceResponseScoringRequestPayload());
        return response;
    }

    // TODO - The following method is a very simple way of scoring a provider but, in the future, more complex logic
    // TODO - could be set in place, that also weights in 'how good' that provider has been when it comes to providing
    // TODO - resources.
    /**
     * Getting a score for a provider (within the context of a particular namespace), is based on the 'uptime' history
     * of that provider, i.e. how many times the provider was up over the total number of times we have checked its
     * status.
     * @param request the request that contains the reference to the provider being scored
     * @return a Service Response ready for the controller to send back to the client
     */
    public ServiceResponseScoringRequest getScoreForProvider(ServiceRequestScoreProvider request) {
        logger.info("Provider scoring request for ID '{}', URL '{}', accept401or403? '{}'",
                request.getPayload().getId(),
                request.getPayload().getUrl(),
                request.getPayload().getAccept401or403() ? "Yes" : "No");
        ServiceResponseScoringRequest response = getDefaultResponse();
        try {
            response.getPayload()
                    .setScore((int) Math.round(historyTrackingService.getTrackerForProvider(request.getPayload())
                            .getHistoryStats(HistoryTracker.HistoryStats.SIMPLE).getUpPercentage()));
        } catch (Exception e) {
            response.setErrorMessage(String.format("Scoring could not be calculated due to '%s'", e.getMessage()));
            response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            logger.error("Provider Scoring request for ID'{}', URL '{}', HTTP Status '{}', ERROR -> '{}'",
                    request.getPayload().getId(),
                    request.getPayload().getUrl(),
                    response.getHttpStatus().value(),
                    response.getErrorMessage());
        }
        return response;
    }

    /**
     * Get the reliability score for a resource (within the context of a particular namespace), the calculation is
     * based on the seen 'uptime' history of that resource, i.e. how many times the resource was up over the total
     * number of times we have checked its status.
     * @param request the request that contains the reference to the resource being scored
     * @return a Service Response ready for the controller to send back to the client
     */
    public ServiceResponseScoringRequest getScoreForResolvedId(ServiceRequestScoreResource request) {
        logger.info("Resource scoring request for ID '{}', URL '{}', accept401or403? '{}'",
                request.getPayload().getId(),
                request.getPayload().getUrl(),
                request.getPayload().getAccept401or403() ? "Yes" : "No");
        ServiceResponseScoringRequest response = getDefaultResponse();
        try {
            response.getPayload().setScore((int) Math.round(historyTrackingService.getTrackerForResource(request
                    .getPayload()).getHistoryStats(HistoryTracker.HistoryStats.SIMPLE).getUpPercentage()));
        } catch (Exception e) {
            response.setErrorMessage(String.format("Scoring could not be calculated due to '%s'", e.getMessage()));
            response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            logger.error("Resource Scoring request for ID '{}', URL '{}', HTTP Status '{}', ERROR -> '{}'",
                    request.getPayload().getId(),
                    request.getPayload().getUrl(),
                    response.getHttpStatus().value(),
                    response.getErrorMessage());
        }
        return response;
    }

    /**
     * The same way we can keep historical data on providers and resources, we can do the same with plain URLs, but
     * right now, this functionality is not implemented, as it is not immediately needed.
     * @param request the request that contains the reference to the URL being scored
     * @return a Service Response reporting an HTTP BAD REQUEST reply to the client
     */
    public ServiceResponseScoringRequest getScoreForUrl(ServiceRequestScoring request) {
        // TODO - Maybe I'll remove this use case in the future
        logger.warn("URL scoring request for URL '{}' - NOT IMPLEMENTED", request.getPayload().getUrl());
        ServiceResponseScoringRequest response = getDefaultResponse();
        response.setHttpStatus(HttpStatus.BAD_REQUEST);
        response.setErrorMessage("This functionality if not available yet.");
        return response;
    }
}
