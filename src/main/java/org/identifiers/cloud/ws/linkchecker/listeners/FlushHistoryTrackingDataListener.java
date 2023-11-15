package org.identifiers.cloud.ws.linkchecker.listeners;

import org.identifiers.cloud.ws.linkchecker.channels.Listener;
import org.identifiers.cloud.ws.linkchecker.channels.management.flushhistorytrackingdata
        .FlushHistoryTrackingDataSubscriber;
import org.identifiers.cloud.ws.linkchecker.data.models.FlushHistoryTrackingDataMessage;
import org.identifiers.cloud.ws.linkchecker.services.HistoryTrackingService;
import org.identifiers.cloud.ws.linkchecker.services.HistoryTrackingServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.listeners
 * Timestamp: 2018-08-03 11:40
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@Component
public class FlushHistoryTrackingDataListener implements Listener<FlushHistoryTrackingDataMessage> {
    private static final Logger logger = LoggerFactory.getLogger(FlushHistoryTrackingDataListener.class);

    @Autowired
    private FlushHistoryTrackingDataSubscriber subscriber;

    @Autowired
    private HistoryTrackingService historyTrackingService;

    @PostConstruct
    private void init() {
        logger.info("Adding listener for requests on flushing history tracking data");
        subscriber.addListener(this);
    }

    @Override
    public void process(FlushHistoryTrackingDataMessage value) {
        logger.info("Attending announcement of request to flush history tracking data");
        try {
            historyTrackingService.flushHistoryTrackers();
            logger.warn("ALL history trackers have been flushed");
        } catch (HistoryTrackingServiceException e) {
            logger.error("Could not FLUSH history trackers due to the following error '{}'", e.getMessage());
        }
    }
}
