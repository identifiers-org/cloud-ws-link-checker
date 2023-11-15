package org.identifiers.cloud.ws.linkchecker.listeners;

import org.identifiers.cloud.ws.linkchecker.channels.Listener;
import org.identifiers.cloud.ws.linkchecker.channels.linkcheckresults.LinkCheckResultsSubscriber;
import org.identifiers.cloud.ws.linkchecker.data.models.LinkCheckResult;
import org.identifiers.cloud.ws.linkchecker.services.HistoryTrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.listeners
 * Timestamp: 2018-05-29 16:04
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 *
 * This component listens to the link check results channel for announcements.
 */
@Component
public class LinkCheckResultListener implements Listener<LinkCheckResult> {
    private static final Logger logger = LoggerFactory.getLogger(LinkCheckResultListener.class);

    @Autowired
    private LinkCheckResultsSubscriber subscriber;

    @Autowired
    private HistoryTrackingService historyTrackingService;

    @PostConstruct
    private void init() {
        logger.info("Adding link check results listener to the channel subscriber");
        subscriber.addListener(this);
    }

    @Override
    public void process(LinkCheckResult value) {
        logger.info("Processing link check result announcement for URL '{}', provider ID '{}', resource ID '{}'",
                value.getUrl(),
                value.getProviderId(),
                value.getResourceId());
        historyTrackingService.updateTrackerWith(value);
    }
}
