package org.identifiers.cloud.ws.linkchecker.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.identifiers.cloud.ws.linkchecker.api.requests.ScoringRequestWithIdPayload;
import org.identifiers.cloud.ws.linkchecker.data.models.LinkCheckRequest;
import org.identifiers.cloud.ws.linkchecker.data.models.LinkCheckResult;
import org.identifiers.cloud.ws.linkchecker.data.models.TrackedProvider;
import org.identifiers.cloud.ws.linkchecker.data.models.TrackedResource;
import org.identifiers.cloud.ws.linkchecker.data.repositories.LinkCheckResultRepository;
import org.identifiers.cloud.ws.linkchecker.data.repositories.TrackedProviderRepository;
import org.identifiers.cloud.ws.linkchecker.data.repositories.TrackedResourceRepository;
import org.identifiers.cloud.ws.linkchecker.models.HistoryTracker;
import org.identifiers.cloud.ws.linkchecker.models.ProviderTracker;
import org.identifiers.cloud.ws.linkchecker.models.ResourceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

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
    Cache<String, ProviderTracker> providers;
    Cache<String, ResourceTracker> resources;
    // Provider trackers by Provider ID
    private ConcurrentMap<String, ProviderTracker> providerTrackers = new ConcurrentHashMap<>();

    @Value("${org.identifiers.cloud.ws.linkchecker.backend.data.cache.expiry.seconds}")
    private long cacheExpirySeconds;

    @Value("${org.identifiers.cloud.ws.linkchecker.backend.data.cache.size}")
    private long cacheSize;

    // Repositories
    @Autowired
    private TrackedProviderRepository trackedProviderRepository;
    @Autowired
    private TrackedResourceRepository trackedResourceRepository;
    @Autowired
    private LinkCheckResultRepository linkCheckResultRepository;

    public SimpleHistoryTrackingService() {
        providers = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterWrite(cacheExpirySeconds, TimeUnit.SECONDS)
                .removalListener(new RemovalListener<String, ProviderTracker>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, ProviderTracker> removalNotification) {
                        processProviderEviction(removalNotification);
                    }
                })
                .build();
    }

    // Cache Eviction Listener Actions
    private void processProviderEviction(RemovalNotification<String, ProviderTracker> removalNotification) {
        logger.info("Evicting tracking cache for provider ID '{}', URL '{}', EVICTION CAUSE '{}'",
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
        if (!trackedProvider.isPresent()) {
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
        if (!trackedResource.isPresent()) {
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
        // TODO
    }

    @Override
    public ProviderTracker getTrackerForProvider(ScoringRequestWithIdPayload scoringRequestWithIdPayload) throws
            HistoryTrackingServiceException {
        try {
            return providers.get(scoringRequestWithIdPayload.getId(), new Callable<ProviderTracker>() {
                @Override
                public ProviderTracker call() throws Exception {
                    ProviderTracker providerTracker = loadCreateTrackedProvider(scoringRequestWithIdPayload);
                    // Initialize the stats for the given provider
                    List<LinkCheckResult> linkCheckResults = linkCheckResultRepository.findByProviderId
                            (scoringRequestWithIdPayload.getId());
                    if (linkCheckResults != null) {
                        providerTracker.initHistoryStats(linkCheckResults);
                    }
                    return providerTracker;
                }
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
        // TODO
        return null;
    }

    @Override
    public HistoryTracker updateTrackerWith(LinkCheckResult linkCheckResult) throws HistoryTrackingServiceException {
        ProviderTracker providerTracker = null;
        if (linkCheckResult.getProviderId() != null) {
            // It is a provider link check result
            return updateProviderTrackerWith(linkCheckResult);
        }
        if (linkCheckResult.getResourceId() != null) {
            // TODO - It is a resource link check result
            return null;
        }
        // TODO - It is a plain URL check result
        return null;
    }
}
