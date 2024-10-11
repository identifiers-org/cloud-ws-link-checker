package org.identifiers.cloud.ws.linkchecker.models;

import org.identifiers.cloud.ws.linkchecker.data.models.LinkCheckResult;

import java.util.List;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.data.models
 * Timestamp: 2018-05-22 13:48
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
public interface CheckedUrlHistoryStats {
    /**
     * Initialize the history with a given list of checked URLs events
     * @param linkCheckResults list of checked URLs events
     */
    void init(List<LinkCheckResult> linkCheckResults) throws CheckedUrlHistoryStatsException;

    /**
     * Update the history with a given checked URL event
     * @param linkCheckResult checked URL event
     */
    void update(LinkCheckResult linkCheckResult);

    /**
     * Get the percentage of events where the checked URL was considered to be up and running
     * @return up and running percentage
     */
    float getUpPercentage();
}
