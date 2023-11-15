package org.identifiers.cloud.ws.linkchecker.periodictasks;

import org.identifiers.cloud.libapi.models.resolver.ServiceResponseResolve;
import org.identifiers.cloud.libapi.services.ResolverService;
import org.identifiers.cloud.ws.linkchecker.data.models.LinkCheckRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.BlockingDeque;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.daemons
 * Timestamp: 2018-05-31 15:43
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 * <p>
 * This is a check requester daemon that will use resolution insight data for periodically request link checking of
 * resources and providers.
 */
@Component
@ConditionalOnProperty(value = "org.identifiers.cloud.ws.linkchecker.daemon.periodiclinkcheckrequester.enabled")
public class PeriodicChecksFeederTask implements Runnable{
    static final Logger logger = LoggerFactory.getLogger(PeriodicChecksFeederTask.class);
    static final Random random = new Random(System.currentTimeMillis());


    @Value("${org.identifiers.cloud.ws.linkchecker.daemon.periodiclinkcheckrequester.waittime.max:24h}")
    Duration waitTimeMaxBeforeNextRequest;
    @Value("${org.identifiers.cloud.ws.linkchecker.daemon.periodiclinkcheckrequester.waittime.min:12h}")
    Duration waitTimeMinBeforeNextRequest;
    @Value("${org.identifiers.cloud.ws.linkchecker.daemon.periodiclinkcheckrequester.waittime.error:1h}")
    Duration waitTimeErrorBeforeNextRequest;


    final BlockingDeque<LinkCheckRequest> linkCheckRequestQueue;
    final ResolverService resolverService;

    public PeriodicChecksFeederTask(
            @Autowired BlockingDeque<LinkCheckRequest> linkCheckRequestQueue,
            @Autowired ResolverService resolverService) {
        this.linkCheckRequestQueue = linkCheckRequestQueue;
        this.resolverService = resolverService;
    }

    long waitTimeSeconds;
    public long getNextWaitTimeSeconds() {
        return waitTimeSeconds;
    }

    @Override
    public void run() {
        logger.info("--- [START] Periodic Link Check Requester on Resolution Base Data ---");

        waitTimeSeconds = getRandomWaitTimeSeconds();

        // Get Resolution client and insight data on resolution samples, as they also contain the provider home URL,
        // we'll only need one request.
        ServiceResponseResolve insightResponse = resolverService.getAllSampleIdsResolved();
        if (insightResponse.getHttpStatus() == HttpStatus.OK) {
            logger.info("Queuing link check requests for #{} entries from the Resolution insight API",
                    insightResponse.getPayload().getResolvedResources().size());
            insightResponse.getPayload().getResolvedResources()
                    .parallelStream().forEach(resolvedResource -> {

                linkCheckRequestQueue.add(new LinkCheckRequest()
                        .setUrl(resolvedResource.getCompactIdentifierResolvedUrl())
                        .setResourceId(Long.toString(resolvedResource.getId()))
                        .setAccept401or403(resolvedResource.isProtectedUrls()));

                // Create link checking requests for home URLs (a.k.a. providers)
                // NOTE - This implementation assumes that every provider in the resolution dataset has a different
                // ID depending on the namespace context where it's providing an access URL, the provider home URL
                // may be the same for different namespaces where this provider is a resource, but the provider ID
                // will be different, and stats are collected by provider ID, not by the URL of the provider. This
                // is done this way to scope the statistical information about a provider within a particular
                // namespace. This way, more complex scoring can be calculated by combining metrics related to the
                // same provider / resource ID, i.e. scoped by the namespace where the provider is a resource.
                linkCheckRequestQueue.add(new LinkCheckRequest()
                        .setUrl(resolvedResource.getResourceHomeUrl())
                        .setProviderId(Long.toString(resolvedResource.getId())));
            });
        } else {
            logger.error("Got HTTP Status '{}' from Resolution Service Insight API, reason '{}', " +
                            "SKIPPING this link checking request iteration",
                    insightResponse.getHttpStatus().value(),
                    insightResponse.getErrorMessage());
            // Adjust the time to wait before checking the insight api again
            waitTimeSeconds = getErrorWaitTimeSeconds();
        }
        logger.info("Waiting {}s before we check again for resolution insight data", waitTimeSeconds);
    }

    long getErrorWaitTimeSeconds() {
        return random.nextLong(waitTimeErrorBeforeNextRequest.getSeconds());
    }

    long getRandomWaitTimeSeconds() {
        return random.longs(waitTimeMinBeforeNextRequest.getSeconds(),
                            waitTimeMinBeforeNextRequest.getSeconds())
                .findFirst().getAsLong();
    }
}
