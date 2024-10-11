package org.identifiers.cloud.ws.linkchecker.periodictasks;

import org.identifiers.cloud.ws.linkchecker.channels.PublisherException;
import org.identifiers.cloud.ws.linkchecker.channels.linkcheckresults.LinkCheckResultsPublisher;
import org.identifiers.cloud.ws.linkchecker.data.LinkCheckModelsHelper;
import org.identifiers.cloud.ws.linkchecker.data.models.LinkCheckRequest;
import org.identifiers.cloud.ws.linkchecker.data.models.LinkCheckResult;
import org.identifiers.cloud.ws.linkchecker.data.services.LinkCheckResultServiceException;
import org.identifiers.cloud.ws.linkchecker.data.services.LinkCheckResultsService;
import org.identifiers.cloud.ws.linkchecker.strategies.LinkCheckerException;
import org.identifiers.cloud.ws.linkchecker.strategies.LinkCheckerReport;
import org.identifiers.cloud.ws.linkchecker.strategies.LinkCheckerStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.daemons
 * Timestamp: 2018-05-29 10:35
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 * <p>
 * This daemon pulls link checking requests from a queue, runs them through a link checker, and lodges in the results.
 */
@Component
public class LinkCheckingTask implements Runnable{
    static final Logger logger = LoggerFactory.getLogger(LinkCheckingTask.class);
    static final Random random = new Random(System.currentTimeMillis());


    @Value("${org.identifiers.cloud.ws.linkchecker.daemon.linkchecker.waittime.max}")
    Duration waitTimeLimit;
    @Value("${org.identifiers.cloud.ws.linkchecker.daemon.linkchecker.waittime.polltimeout}")
    Duration waitTimePoolLinkCheckRequestQueue;


    final LinkCheckerStrategy linkCheckingStrategy;
    final BlockingDeque<LinkCheckRequest> linkCheckRequestQueue;
    final LinkCheckResultsService linkCheckResultsService;
    final LinkCheckResultsPublisher linkCheckResultsPublisher;
    public LinkCheckingTask(@Autowired BlockingDeque<LinkCheckRequest> linkCheckRequestQueue,
                            @Autowired LinkCheckerStrategy linkCheckingStrategy,
                            @Autowired LinkCheckResultsService linkCheckResultsService,
                            @Autowired LinkCheckResultsPublisher linkCheckResultsPublisher) {
        this.linkCheckRequestQueue = linkCheckRequestQueue;
        this.linkCheckingStrategy = linkCheckingStrategy;
        this.linkCheckResultsService = linkCheckResultsService;
        this.linkCheckResultsPublisher = linkCheckResultsPublisher;
    }


    public long getNextRandomWait() {
        long waitTimeSeconds = random.nextLong(waitTimeLimit.getSeconds());
        logger.info("Random wait {}s", waitTimeSeconds);
        return waitTimeSeconds;
    }

    @Override
    public void run() {
        logger.debug("--- [START] Link Checker Task ---");
        try {
            // Pop element, if any, from the link checking request queue
            logger.info("Polling link check request queue");
            LinkCheckRequest linkCheckRequest = nextLinkCheckRequest();
            if (linkCheckRequest == null) {
                logger.info("No URL check request found");
            }
            while (linkCheckRequest != null) {
                LinkCheckResult linkCheckResult = attendLinkCheckRequest(linkCheckRequest);
                if (linkCheckResult != null) { // FIXME: Sometimes a null result here is a result of a check IO error, possibly an offline resource
                    persist(linkCheckResult);
                    announce(linkCheckResult);
                } else {
                    logger.info("Failed to attend link check");
                }
                linkCheckRequest = nextLinkCheckRequest();
            }
        } catch (RuntimeException e) {
            logger.error("An error has been stopped for preventing the task from crashing", e);
        }
        logger.debug("--- [END] Link Checker Task ---");
    }


    private LinkCheckRequest nextLinkCheckRequest() {
        try {
            return linkCheckRequestQueue.pollFirst(waitTimePoolLinkCheckRequestQueue.getSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("The Link Check Request Queue is unresponsive, operation timed out, {}", e.getMessage());
        }
        return null;
    }

    private LinkCheckResult attendLinkCheckRequest(LinkCheckRequest linkCheckRequest) {
        // Check URL
        LinkCheckerReport linkCheckerReport;
        try {
            logger.info("Attending link check request for URL '{}'", linkCheckRequest.getUrl());
            URL linkCheckRequestUrl = new URL(linkCheckRequest.getUrl());
            linkCheckerReport = linkCheckingStrategy.check(linkCheckRequestUrl, linkCheckRequest.shouldAccept401or403());
        } catch (LinkCheckerException | MalformedURLException e) {
            logger.warn("Could not attend link checking request for URL '{}'", linkCheckRequest.getUrl());
            logger.debug("Exception thrown", e);
            linkCheckerReport = new LinkCheckerReport();
            linkCheckerReport.setUrl(linkCheckRequest.getUrl());
            linkCheckerReport.setUrlAssessmentOk(false);
        }
        // Log the results
        logger.info("Link Check result for URL '{}', HTTP Status '{}', assessment '{}'",
                linkCheckerReport.getUrl(),
                linkCheckerReport.getHttpStatus(),
                linkCheckerReport.isUrlAssessmentOk() ? "OK" : "NOT OK");
        return LinkCheckModelsHelper.getResultFromReport(linkCheckerReport, linkCheckRequest);
    }

    void persist(LinkCheckResult linkCheckResult) {
        try {
            linkCheckResultsService.save(linkCheckResult);
        } catch (LinkCheckResultServiceException e) {
            logger.error("COULD not save link check result for URL '{}', reason '{}'", linkCheckResult.getUrl(), e.getMessage());
        }
    }

    void announce(LinkCheckResult linkCheckResult) {
        try {
            linkCheckResultsPublisher.publish(linkCheckResult);
        } catch (PublisherException e) {
            logger.error("COULD not announce link check result for URL '{}', reason '{}'", linkCheckResult.getUrl(), e.getMessage());
        }
    }
}
