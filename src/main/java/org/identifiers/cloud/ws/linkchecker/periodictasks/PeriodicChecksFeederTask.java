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
@ConditionalOnProperty(value = "org.identifiers.cloud.ws.linkchecker.daemon.periodicchecksfeedertask.enabled")
public class PeriodicChecksFeederTask implements Runnable{
    static final Logger logger = LoggerFactory.getLogger(PeriodicChecksFeederTask.class);
    static final Random random = new Random(System.currentTimeMillis());


    @Value("${org.identifiers.cloud.ws.linkchecker.daemon.periodicchecksfeedertask.waittime.max:24h}")
    Duration waitTimeMaxBeforeNextRequest;
    @Value("${org.identifiers.cloud.ws.linkchecker.daemon.periodicchecksfeedertask.waittime.min:12h}")
    Duration waitTimeMinBeforeNextRequest;
    @Value("${org.identifiers.cloud.ws.linkchecker.daemon.periodicchecksfeedertask.waittime.error:1h}")
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
        if (insightResponse.getHttpStatus().is2xxSuccessful()) {
            logger.info("Queuing link check requests for #{} entries from the Resolution insight API",
                    insightResponse.getPayload().getResolvedResources().size());
            insightResponse.getPayload().getResolvedResources()
                    .parallelStream().forEach(resolvedResource -> {

                linkCheckRequestQueue.add(new LinkCheckRequest()
                        .setUrl(resolvedResource.getCompactIdentifierResolvedUrl())
                        .setResourceId(Long.toString(resolvedResource.getId()))
                        .setAccept401or403(resolvedResource.isProtectedUrls()));

                linkCheckRequestQueue.add(new LinkCheckRequest()
                        .setUrl(resolvedResource.getResourceHomeUrl())
                        .setProviderId(Long.toString(resolvedResource.getId()))
                        .setAccept401or403(false));
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
