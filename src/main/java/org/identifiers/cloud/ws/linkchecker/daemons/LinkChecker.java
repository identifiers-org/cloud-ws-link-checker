package org.identifiers.cloud.ws.linkchecker.daemons;

import org.identifiers.cloud.ws.linkchecker.data.models.LinkCheckRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Deque;
import java.util.Random;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.daemons
 * Timestamp: 2018-05-29 10:35
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 *
 * This daemon pulls link checking requests from a queue, runs them through a link checker, and lodges in the results.
 */
@Component
public class LinkChecker extends Thread {
    private static final int WAIT_TIME_LIMIT_SECONDS = 1800;
    private static final Logger logger = LoggerFactory.getLogger(LinkChecker.class);

    private boolean shutdown = false;

    @Autowired
    private Deque<LinkCheckRequest> linkCheckRequestQueue;

    public synchronized boolean isShutdown() {
        return shutdown;
    }

    public synchronized void setShutdown() {
        this.shutdown = true;
    }

    @Override
    public void run() {
        logger.info("--- [START] Link Checker Daemon ---");
        Random random = new Random(System.currentTimeMillis());
        // TODO
        // TODO - Pop element, if any, from the link checking request queue
        LinkCheckRequest linkCheckRequest = linkCheckRequestQueue.pollFirst();
        if (linkCheckRequest == null) {
            logger.info("No URL check request found");
            try {
                long waitTimeSeconds = random.nextInt(WAIT_TIME_LIMIT_SECONDS);
                logger.info("Waiting {}s before we checking again for URLs", waitTimeSeconds);
                Thread.sleep(waitTimeSeconds * 1000);
            } catch (InterruptedException e) {
                logger.warn("The Link Checker Daemon has been interrupted while waiting for " +
                        "another iteration. Stopping the daemon, no more URL check requests will be processed");
                shutdown = true;
            }
        }
        // TODO - If no element is in there, wait a random amount of time before trying again
        // TODO - Check URL
        // TODO - Log the results
        // TODO - Announce the link checking results
    }

    @PostConstruct
    public void autoStartThread() {
        start();
    }

    @PreDestroy
    public void stopDaemon() {
        logger.info("--- [STOPPING] Link Checker Daemon ---");
        setShutdown();
    }
}
