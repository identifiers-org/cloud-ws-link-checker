package org.identifiers.cloud.ws.linkchecker.services;

import org.identifiers.cloud.ws.linkchecker.api.requests.ScoringRequestWithIdPayload;
import org.identifiers.cloud.ws.linkchecker.data.models.LinkCheckResult;
import org.identifiers.cloud.ws.linkchecker.models.HistoryTracker;
import org.identifiers.cloud.ws.linkchecker.models.ProviderTracker;
import org.identifiers.cloud.ws.linkchecker.models.ResourceTracker;

import java.util.Collection;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.services
 * Timestamp: 2018-05-25 11:40
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 *
 * A history tracking service maintains and provides the tracking information for the requested entity.
 */
public interface HistoryTrackingService {
    ProviderTracker getTrackerForProvider(ScoringRequestWithIdPayload scoringRequestWithIdPayload) throws HistoryTrackingServiceException ;
    ResourceTracker getTrackerForResource(ScoringRequestWithIdPayload scoringRequestWithIdPayload) throws HistoryTrackingServiceException ;
    // TODO - Add the methods for the rest of the entities
    HistoryTracker updateTrackerWith(LinkCheckResult linkCheckResult) throws HistoryTrackingServiceException;

    // --- Flushing link checking historical data ---
    // Multiple copies of this service may be running at the same time, so this request will be completed in two steps:
    //  - The service that receives the request will wipe out the link checking history and then send a message on a particular channel to notify the other services of what just happened
    //  - All copies of this service will attend the flush announcement by flushing their history tracking service
    // Attend flushing request directly
    void deleteHistoryTrackingData() throws HistoryTrackingServiceException;

    // Attend flushing request received by a sibling service
    void flushHistoryTrackers() throws HistoryTrackingServiceException;

    Collection<ResourceTracker> getAllResourceTrackers();
}
