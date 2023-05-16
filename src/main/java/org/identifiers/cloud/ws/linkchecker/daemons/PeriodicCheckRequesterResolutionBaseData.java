package org.identifiers.cloud.ws.linkchecker.daemons;

import org.identifiers.cloud.libapi.models.resolver.ServiceResponseResolve;
import org.identifiers.cloud.libapi.services.ApiServicesFactory;
import org.identifiers.cloud.ws.linkchecker.data.models.LinkCheckRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
public class PeriodicCheckRequesterResolutionBaseData extends Thread {
    @Value("${org.identifiers.cloud.ws.linkchecker.daemon.periodiclinkcheckrequester.waittime.max}")
    private int waitTimeMaxBeforeNextRequestSeconds;

    @Value("${org.identifiers.cloud.ws.linkchecker.daemon.periodiclinkcheckrequester.waittime.min}")
    private int waitTimeMinBeforeNextRequestSeconds;

    @Value("${org.identifiers.cloud.ws.linkchecker.daemon.periodiclinkcheckrequester.waittime.error}")
    private int waitTimeErrorBeforeNextRequestSeconds;

    private static final Logger logger = LoggerFactory.getLogger(PeriodicCheckRequesterResolutionBaseData.class);

    private boolean shutdown = false;

    @Value("${org.identifiers.cloud.ws.linkchecker.daemon.periodiclinkcheckrequester.enabled}")
    private boolean enabled;

    @Value("${org.identifiers.cloud.ws.linkchecker.backend.service.resolver.host}")
    private String wsResolverHost;

    @Value("${org.identifiers.cloud.ws.linkchecker.backend.service.resolver.port}")
    private String wsResolverPort;

    @Autowired
    private BlockingDeque<LinkCheckRequest> linkCheckRequestQueue;

    public synchronized boolean isShutdown() {
        return shutdown;
    }

    public synchronized void setShutdown() {
        this.shutdown = true;
    }

    @PostConstruct
    public void autoStartThread() {
        start();
    }

    @PreDestroy
    public void stopDaemon() {
        logger.info("--- [STOPPING] Periodic Link Check Requester on Resolution Base Data ---");
        setShutdown();
    }

    @Override
    public void run() {
        if (!enabled) {
            logger.warn("--- [DISABLED] Periodic Link Check Requester on Resolution Base Data ---");
        } else {
            logger.info("--- [START] Periodic Link Check Requester on Resolution Base Data ---");
        }
        // TODO - Refactor this code to make it cleaner
        Random random = new Random(System.currentTimeMillis());
        while (!isShutdown() && enabled) {
            // Next random number of seconds to wait before the next iteration
            logger.debug("Taking wait time from {} to {}", waitTimeMinBeforeNextRequestSeconds, waitTimeMaxBeforeNextRequestSeconds);
            int waitTimeSeconds = waitTimeMinBeforeNextRequestSeconds +
                    random.nextInt(waitTimeMaxBeforeNextRequestSeconds
                            - waitTimeMinBeforeNextRequestSeconds);
            // Get Resolution client and insight data on resolution samples, as they also contain the provider home URL,
            // we'll only need one request.
            ServiceResponseResolve insightResponse = ApiServicesFactory
                    .getResolverService(wsResolverHost, wsResolverPort)
                    .getAllSampleIdsResolved();
            if (insightResponse.getHttpStatus() == HttpStatus.OK) {
                logger.info("Queuing link check requests for #{} entries from the Resolution insight API",
                        insightResponse.getPayload().getResolvedResources().size());
                insightResponse.getPayload().getResolvedResources()
                        .parallelStream().forEach(resolvedResource -> {
                    // Create link checking requests for resolution samples
                    // TODO - Have a look at the data types for refactoring
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
                waitTimeSeconds = random.nextInt(waitTimeErrorBeforeNextRequestSeconds);
            }
            // Wait before the next wave of link check requests
            try {
                logger.info("Waiting {}s before we check again for resolution insight data", waitTimeSeconds);
                Thread.sleep(waitTimeSeconds * 1000);
            } catch (InterruptedException e) {
                logger.warn("The Periodic Link Check Requester on Resolution insight data has been interrupted while " +
                        "waiting for another iteration. Stopping the service, no more link checking requests will be " +
                        "submitted");
                setShutdown();
            }
        }
    }
}
