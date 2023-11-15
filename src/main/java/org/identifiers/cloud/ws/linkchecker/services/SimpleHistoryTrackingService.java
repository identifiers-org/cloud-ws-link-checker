package org.identifiers.cloud.ws.linkchecker.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.services
 * Timestamp: 2018-05-25 11:44
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@Component
public class SimpleHistoryTrackingService implements HistoryTrackingService {
    private final static Logger logger = LoggerFactory.getLogger(SimpleHistoryTrackingService.class);
    // Cached stats
    private Cache<String, ProviderTracker> providers;
    private Cache<String, ResourceTracker> resources;

    @Value("${org.identifiers.cloud.ws.linkchecker.backend.data.cache.expiry}")
    Duration cacheExpiry;

    @Value("${org.identifiers.cloud.ws.linkchecker.backend.data.cache.size}")
    long cacheSize;

    // Repositories
    // TODO - Refactor out these repositories to services in the future (when possible), in the meantime, I will use
    // TODO - link check results repository as an example of whether that makes sense in such a small component like
    // TODO - this microservice, maybe in some cases we can break rules
    @Autowired
    private TrackedProviderRepository trackedProviderRepository;
    @Autowired
    private TrackedResourceRepository trackedResourceRepository;
    // Persistence Services
    @Autowired
    private LinkCheckResultsService linkCheckResultsService;

    // Link check requests queue
    @Autowired
    private Deque<LinkCheckRequest> linkCheckRequestQueue;

    // Channels
    // Flush Link Checking historic data
    @Autowired
    private FlushHistoryTrackingDataPublisher flushHistoryTrackingDataPublisher;

    @PostConstruct
    public void initCache() {
        logger.info("Simple History Tracking Service - Cache SIZE = {}", cacheSize);
        providers = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterWrite(cacheExpiry.getSeconds(), TimeUnit.SECONDS)
                .removalListener(this::processProviderEviction)
                .build();
        resources = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterWrite(cacheExpiry.getSeconds(), TimeUnit.SECONDS)
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

    // TODO - I may need to make this method synchronized because of the non-atomic gap between checking if a provider
    // TODO - already has a tracking entry and creating one for it. It really depends on whether the Cache locks write
    // TODO - operation by key or not.
    // Tracking entry Loaders
    private ProviderTracker loadCreateTrackedProvider(ScoringRequestWithIdPayload scoringRequestWithIdPayload) {
        ProviderTracker providerTracker = new ProviderTracker();
        providerTracker.setId(scoringRequestWithIdPayload.getId())
                .setUrl(scoringRequestWithIdPayload.getUrl());
        Optional<TrackedProvider> trackedProvider =
                trackedProviderRepository.findById(scoringRequestWithIdPayload.getId());
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

    private ResourceTracker loadCreateTrackedResource(ScoringRequestWithIdPayload scoringRequestWithIdPayload) {
        // NOTE - I know this method looks a lot like 'loadCreateTrackedProvider', at this iteration of the service,
        // both entities look pretty much the same, but not only they're expected to diverge, maybe, slightly in the
        // future, but it also makes sense to ease things with serialization/deserialization on the data backend, so,
        // by doing what it looks like duplicating code... it is not actually, as it is contributing to having a lot of
        // heavy lifting done for free.
        ResourceTracker resourceTracker = new ResourceTracker();
        resourceTracker.setId(scoringRequestWithIdPayload.getId())
                .setUrl(scoringRequestWithIdPayload.getUrl());
        Optional<TrackedResource> trackedResource =
                trackedResourceRepository.findById(scoringRequestWithIdPayload.getId());
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

    private ProviderTracker updateProviderTrackerWith(LinkCheckResult linkCheckResult) {
        ProviderTracker providerTracker = providers.getIfPresent(linkCheckResult.getProviderId());
        if (providerTracker != null) {
            logger.info("Updating history tracker for provider ID '{}' with link check result on URL '{}', " +
                            "request timestamp '{}', check timestamp '{}', elapsed '{}'",
                    linkCheckResult.getProviderId(),
                    linkCheckResult.getUrl(),
                    linkCheckResult.getRequestTimestamp(),
                    linkCheckResult.getTimestamp(),
                    (linkCheckResult.getTimestamp().getTime() - linkCheckResult.getRequestTimestamp().getTime()));
            providerTracker.addLinkCheckResult(linkCheckResult);
        } else {
            logger.info("SKIP NOT CACHED history tracker for provider ID '{}' with link check result on URL '{}', " +
                            "request timestamp '{}', check timestamp '{}', elapsed '{}'",
                    linkCheckResult.getProviderId(),
                    linkCheckResult.getUrl(),
                    linkCheckResult.getRequestTimestamp(),
                    linkCheckResult.getTimestamp(),
                    (linkCheckResult.getTimestamp().getTime() - linkCheckResult.getRequestTimestamp().getTime()));
        }
        return providerTracker;
    }

    private ResourceTracker updateResourceTrackerWith(LinkCheckResult linkCheckResult) {
        ResourceTracker resourceTracker = resources.getIfPresent(linkCheckResult.getResourceId());
        if (resourceTracker != null) {
            logger.info("Updating history tracker for resource ID '{}' with link check result on URL '{}', " +
                            "request timestamp '{}', check timestamp '{}', elapsed '{}'",
                    linkCheckResult.getResourceId(),
                    linkCheckResult.getUrl(),
                    linkCheckResult.getRequestTimestamp(),
                    linkCheckResult.getTimestamp(),
                    (linkCheckResult.getTimestamp().getTime() - linkCheckResult.getRequestTimestamp().getTime()));
            resourceTracker.addLinkCheckResult(linkCheckResult);
        } else {
            logger.info("SKIP NOT CACHED history tracker for resource ID '{}' with link check result on URL '{}', " +
                            "request timestamp '{}', check timestamp '{}', elapsed '{}'",
                    linkCheckResult.getResourceId(),
                    linkCheckResult.getUrl(),
                    linkCheckResult.getRequestTimestamp(),
                    linkCheckResult.getTimestamp(),
                    (linkCheckResult.getTimestamp().getTime() - linkCheckResult.getRequestTimestamp().getTime()));
        }
        return resourceTracker;
    }

    @Override
    public ProviderTracker getTrackerForProvider(ScoringRequestWithIdPayload scoringRequestWithIdPayload) throws
            HistoryTrackingServiceException {
        // TODO - Queue the provider scoring requests
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
            throw new SimpleHistoryTrackingServiceException(String.format("Error while getting scoring stats " +
                            "for Provider ID '%s', URL '%s', because '%s'",
                    scoringRequestWithIdPayload.getId(),
                    scoringRequestWithIdPayload.getUrl(),
                    e.getMessage()));
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

    @Override
    public HistoryTracker updateTrackerWith(LinkCheckResult linkCheckResult) throws HistoryTrackingServiceException {
        ProviderTracker providerTracker = null;
        if (linkCheckResult.getProviderId() != null) {
            // It is a provider link check result
            return updateProviderTrackerWith(linkCheckResult);
        }
        if (linkCheckResult.getResourceId() != null) {
            // It is a resource link check result
            return updateResourceTrackerWith(linkCheckResult);
        }
        // TODO - It is a plain URL check result
        return null;
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
