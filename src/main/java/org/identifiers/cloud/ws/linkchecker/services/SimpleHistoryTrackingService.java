package org.identifiers.cloud.ws.linkchecker.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import lombok.RequiredArgsConstructor;
import org.identifiers.cloud.ws.linkchecker.api.requests.ScoringRequestWithIdPayload;
import org.identifiers.cloud.ws.linkchecker.channels.PublisherException;
import org.identifiers.cloud.ws.linkchecker.channels.management.flushhistorytrackingdata.FlushHistoryTrackingDataPublisher;
import org.identifiers.cloud.ws.linkchecker.data.models.*;
import org.identifiers.cloud.ws.linkchecker.data.repositories.TrackedProviderRepository;
import org.identifiers.cloud.ws.linkchecker.data.repositories.TrackedResourceRepository;
import org.identifiers.cloud.ws.linkchecker.data.services.LinkCheckResultsService;
import org.identifiers.cloud.ws.linkchecker.models.HistoryTracker;
import org.identifiers.cloud.ws.linkchecker.models.ProviderTracker;
import org.identifiers.cloud.ws.linkchecker.models.ResourceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.services
 * Timestamp: 2018-05-25 11:44
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@Component
@RequiredArgsConstructor
public class SimpleHistoryTrackingService implements HistoryTrackingService {
    private static final Logger logger = LoggerFactory.getLogger(SimpleHistoryTrackingService.class);
    // Cached stats
    private Cache<String, ProviderTracker> providers;
    private Cache<String, ResourceTracker> resources;

    @Value("${org.identifiers.cloud.ws.linkchecker.backend.data.cache.expiry}")
    Duration cacheExpiryDuration;
    @Value("${org.identifiers.cloud.ws.linkchecker.backend.data.cache.size}")
    long cacheSize;

    // Repositories
    private final TrackedProviderRepository trackedProviderRepository;
    private final TrackedResourceRepository trackedResourceRepository;
    // Persistence Services
    private final LinkCheckResultsService linkCheckResultsService;
    // Link check requests queue
    private final Deque<LinkCheckRequest> linkCheckRequestQueue;
    // Channels
    // Flush Link Checking historic data
    private final FlushHistoryTrackingDataPublisher flushHistoryTrackingDataPublisher;

    @PostConstruct
    public void initCache() {
        logger.info("Simple History Tracking Service - Cache SIZE = {}", cacheSize);
        providers = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterWrite(cacheExpiryDuration)
                .removalListener(this::processProviderEviction)
                .build();
        resources = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterWrite(cacheExpiryDuration)
                .removalListener(this::processResourceEviction)
                .build();
    }

    // Cache Eviction Listener Actions
    private void processEviction(String type, String id, String url, String cause) {
        logger.info("Evicting tracking cache for {} ID '{}', URL '{}', EVICTION CAUSE '{}'",
                type,
                id,
                url,
                cause);
    }
    private void processProviderEviction(RemovalNotification<String, ProviderTracker> removalNotification) {
        processEviction("Provider Tracker",
                removalNotification.getKey(),
                removalNotification.getValue().getUrl(),
                removalNotification.getCause().toString());
    }

    private void processResourceEviction(RemovalNotification<String, ResourceTracker> removalNotification) {
        processEviction("Resource Tracker",
                removalNotification.getKey(),
                removalNotification.getValue().getUrl(),
                removalNotification.getCause().toString());
    }

    private ProviderTracker loadCreateTrackedProvider(ScoringRequestWithIdPayload scoringRequestWithIdPayload) {
        return loadCreateTrackedProvider(scoringRequestWithIdPayload.getId(), scoringRequestWithIdPayload.getUrl());
    }

    private ProviderTracker loadCreateTrackedProvider(String id, String url) {
        ProviderTracker providerTracker = new ProviderTracker();
        providerTracker.setId(id)
                .setUrl(url);
        Optional<TrackedProvider> trackedProvider =
                trackedProviderRepository.findById(id);
        trackedProvider.ifPresent(entry -> providerTracker.setCreated(entry.getCreated()));
        if (trackedProvider.isEmpty()) {
            TrackedProvider newTrackedProvider = new TrackedProvider()
                    .setCreated(providerTracker.getCreated())
                    .setId(providerTracker.getId())
                    .setUrl(providerTracker.getUrl());
            trackedProviderRepository.save(newTrackedProvider);
        }
        return providerTracker;
    }


    private ResourceTracker loadCreateTrackedResource(String id, String url) {
        // NOTE - I know this method looks a lot like 'loadCreateTrackedProvider', at this iteration of the service,
        // both entities look pretty much the same, but not only they're expected to diverge, maybe, slightly in the
        // future, but it also makes sense to ease things with serialization/deserialization on the data backend, so,
        // by doing what it looks like duplicating code... it is not actually, as it is contributing to having a lot of
        // heavy lifting done for free.
        ResourceTracker resourceTracker = new ResourceTracker();
        resourceTracker.setId(id).setUrl(url);
        Optional<TrackedResource> trackedResource = trackedResourceRepository.findById(id);
        trackedResource.ifPresent(entry -> resourceTracker.setCreated(entry.getCreated()));
        if (trackedResource.isEmpty()) {
            TrackedResource newTrackedResource = new TrackedResource()
                    .setCreated(resourceTracker.getCreated())
                    .setId(resourceTracker.getId())
                    .setUrl(resourceTracker.getUrl());
            trackedResourceRepository.save(newTrackedResource);
        }
        return resourceTracker;
    }

    private ResourceTracker loadCreateTrackedResource(ScoringRequestWithIdPayload scoringRequestWithIdPayload) {
        return loadCreateTrackedResource(scoringRequestWithIdPayload.getId(), scoringRequestWithIdPayload.getUrl());
    }

    private ProviderTracker updateProviderTrackerWith(LinkCheckResult linkCheckResult) {
        try {
            ProviderTracker providerTracker = providers.get(linkCheckResult.getProviderId(), () -> {
                var tracker = loadCreateTrackedProvider(linkCheckResult.getProviderId(), linkCheckResult.getUrl());
                // Initialize stats for the given resource
                List<LinkCheckResult> linkCheckResults = linkCheckResultsService.findByProviderId(linkCheckResult.getProviderId());
                if (linkCheckResults != null) {
                    tracker.initHistoryStats(linkCheckResults);
                }
                return tracker;
            });
            logger.info("Updating history tracker for provider ID '{}' with link check result on URL '{}', " +
                            "request timestamp '{}', check timestamp '{}', elapsed '{}'",
                    linkCheckResult.getProviderId(),
                    linkCheckResult.getUrl(),
                    linkCheckResult.getRequestTimestamp(),
                    linkCheckResult.getTimestamp(),
                    (linkCheckResult.getTimestamp().getTime() - linkCheckResult.getRequestTimestamp().getTime()));
            providerTracker.addLinkCheckResult(linkCheckResult);
            return providerTracker;
        } catch (ExecutionException ex) {
            logger.info("SKIP NOT CACHED history tracker for provider ID '{}' with link check result on URL '{}', " +
                            "request timestamp '{}', check timestamp '{}', due to '{}'",
                    linkCheckResult.getProviderId(),
                    linkCheckResult.getUrl(),
                    linkCheckResult.getRequestTimestamp(),
                    linkCheckResult.getTimestamp(),
                    ex.getMessage());
            return null;
        }
    }

    private ResourceTracker updateResourceTrackerWith(LinkCheckResult linkCheckResult) {
        try {
            ResourceTracker resourceTracker = resources.get(linkCheckResult.getResourceId(), () -> {
                var tracker = loadCreateTrackedResource(linkCheckResult.getResourceId(), linkCheckResult.getUrl());
                // Initialize stats for the given resource
                List<LinkCheckResult> linkCheckResults = linkCheckResultsService.findByResourceId(linkCheckResult.getResourceId());
                if (linkCheckResults != null) {
                    tracker.initHistoryStats(linkCheckResults);
                }
                return tracker;
            });
            logger.info("Updating history tracker for resource ID '{}' with link check result on URL '{}', " +
                            "request timestamp '{}', check timestamp '{}', elapsed '{}'",
                    linkCheckResult.getResourceId(),
                    linkCheckResult.getUrl(),
                    linkCheckResult.getRequestTimestamp(),
                    linkCheckResult.getTimestamp(),
                    (linkCheckResult.getTimestamp().getTime() - linkCheckResult.getRequestTimestamp().getTime()));
            resourceTracker.addLinkCheckResult(linkCheckResult);
            return resourceTracker;
        } catch (ExecutionException e) {
            logger.info("SKIP NOT CACHED history tracker for resource ID '{}' with link check result on URL '{}', " +
                            "request timestamp '{}', check timestamp '{}', due to '{}'",
                    linkCheckResult.getResourceId(),
                    linkCheckResult.getUrl(),
                    linkCheckResult.getRequestTimestamp(),
                    linkCheckResult.getTimestamp(),
                    e.getMessage());
            return null;
        }
    }

    @Override
    public ProviderTracker getTrackerForProvider(ScoringRequestWithIdPayload scoringRequestWithIdPayload) throws
            HistoryTrackingServiceException {
        linkCheckRequestQueue.add(new LinkCheckRequest()
                .setProviderId(scoringRequestWithIdPayload.getId())
                .setUrl(scoringRequestWithIdPayload.getUrl())
                .setAccept401or403(scoringRequestWithIdPayload.getAccept401or403()));
        try {
            return providers.get(scoringRequestWithIdPayload.getId(), () -> {
                ProviderTracker providerTracker = loadCreateTrackedProvider(scoringRequestWithIdPayload);
                // Initialize the stats for the given provider
                List<LinkCheckResult> linkCheckResults = linkCheckResultsService.findByProviderId
                        (scoringRequestWithIdPayload.getId());
                if (linkCheckResults != null) {
                    providerTracker.initHistoryStats(linkCheckResults);
                }
                return providerTracker;
            });
        } catch (ExecutionException e) {
            logger.error("Error while getting scoring stats for Provider ID '{}', URL '{}'",
                    scoringRequestWithIdPayload.getId(), scoringRequestWithIdPayload.getUrl(), e);
            var ex = new SimpleHistoryTrackingServiceException(String.format(
                    "Error while getting scoring stats for Provider ID '%s', URL '%s', because '%s'",
                            scoringRequestWithIdPayload.getId(), scoringRequestWithIdPayload.getUrl(),
                            e.getMessage()));
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    public ResourceTracker getTrackerForResource(ScoringRequestWithIdPayload scoringRequestWithIdPayload) throws HistoryTrackingServiceException {
        linkCheckRequestQueue.add(new LinkCheckRequest()
                .setResourceId(scoringRequestWithIdPayload.getId())
                .setUrl(scoringRequestWithIdPayload.getUrl())
                .setAccept401or403(scoringRequestWithIdPayload.getAccept401or403()));
        try {
            return resources.get(scoringRequestWithIdPayload.getId(), () -> {
                ResourceTracker resourceTracker = loadCreateTrackedResource(scoringRequestWithIdPayload);
                // Initialize stats for the given resource
                List<LinkCheckResult> linkCheckResults = linkCheckResultsService.findByResourceId(scoringRequestWithIdPayload.getId());
                if (linkCheckResults != null) {
                    resourceTracker.initHistoryStats(linkCheckResults);
                }
                return resourceTracker;
            });
        } catch (ExecutionException e) {
            throw new SimpleHistoryTrackingServiceException(String.format("Error while getting scoring stats " +
                            "for Resource ID '%s', URL '%s', accept401or403? '%s', because '%s'",
                    scoringRequestWithIdPayload.getId(),
                    scoringRequestWithIdPayload.getUrl(),
                    scoringRequestWithIdPayload.getAccept401or403() ? "Yes" : "No",
                    e.getMessage()));
        }
    }

    public Collection<ResourceTracker> getAllResourceTrackers() {
        return resources.asMap().values();
    }

    @Override
    public HistoryTracker updateTrackerWith(LinkCheckResult linkCheckResult) throws HistoryTrackingServiceException {
        if (linkCheckResult.getProviderId() != null) {
            return updateProviderTrackerWith(linkCheckResult);
        }
        if (linkCheckResult.getResourceId() != null) {
            return updateResourceTrackerWith(linkCheckResult);
        }
        throw new IllegalStateException("Result can't have both IDs null: " + linkCheckResult.getUrl());
    }

    @Override
    public void deleteHistoryTrackingData() throws HistoryTrackingServiceException {
        try {
            linkCheckResultsService.deleteAll();
            logger.warn("ALL LINK CHECKING HISTORICAL DATA HAS BEEN WIPED OUT as requested");
        } catch (RuntimeException e) {
            throw new HistoryTrackingServiceException(String.format(
                    "History tracker could not delete the historical data, due to '%s'",
                    e.getMessage()));
        }
        try {
            flushHistoryTrackingDataPublisher.publish(new FlushHistoryTrackingDataMessage());
        } catch (PublisherException e) {
            throw new HistoryTrackingServiceException(String.format(
                    "History tracker could not announce the request to delete the historical data, due to '%s'",
                    e.getMessage()));
        }
    }

    @Override
    public void flushHistoryTrackers() throws HistoryTrackingServiceException {
        try {
            providers.invalidateAll();
            logger.warn("ALL cached stats for providers have been WIPED OUT as requested");
            resources.invalidateAll();
            logger.warn("ALL cached stats for resources have been WIPED OUT as requested");
        } catch (RuntimeException e) {
            throw new HistoryTrackingServiceException(String.format(
                    "History tracker could not flush history trackers, due to '%s'",
                    e.getMessage()));
        }
    }
}
