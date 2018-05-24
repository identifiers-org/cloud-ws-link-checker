package org.identifiers.cloud.ws.linkchecker.data.models;

import javafx.collections.transformation.SortedList;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.data.models
 * Timestamp: 2018-05-22 15:48
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 *
 * Base class for the different trackers this service uses
 */
public abstract class HistoryTracker implements Serializable {
    // Home URL for this provider within the context of a namespace or prefix
    protected String url;
    // Historical information
    protected SortedList<LinkCheckResult> history;
    // When the tracking was queued / added to the link checker (UTC)
    protected Timestamp created;

    public String getUrl() {
        return url;
    }

    public HistoryTracker setUrl(String url) {
        this.url = url;
        return this;
    }

    public SortedList<LinkCheckResult> getHistory() {
        return history;
    }

    public HistoryTracker setHistory(SortedList<LinkCheckResult> history) throws HistoryTrackerException {
        if (!history.isEmpty()) {
            throw new HistoryTrackerException("CANNOT SET HISTORY for a NON-empty pre-existing history");
        }
        this.history = history;
        return this;
    }

    public Timestamp getCreated() {
        return created;
    }

    public HistoryTracker setCreated(Timestamp created) {
        this.created = created;
        return this;
    }

    public List<CheckedUrlHistoryStats> getHistoryStats() {
        return Arrays.stream(HistoryStats.values()).map(HistoryStats::getHistoryStats).collect(Collectors.toList());
    }

    public void addCheckedUrlEvent(LinkCheckResult linkCheckResult) {
        // Keep it for the records
        history.add(linkCheckResult);
        // Update the history stats
        Arrays.stream(HistoryStats.values())
                .forEach(historyStats -> historyStats.getHistoryStats().update(linkCheckResult));
    }

    public enum HistoryStats implements Serializable {
        SIMPLE(new CheckedUrlHistoryStatsSimple(), "Simple UP/DOWN history tracking");

        private CheckedUrlHistoryStats historyStats;
        private String description;

        HistoryStats(CheckedUrlHistoryStats historyStats, String description) {
            this.historyStats = historyStats;
            this.description = description;
        }

        public CheckedUrlHistoryStats getHistoryStats() {
            return historyStats;
        }

        public HistoryStats setHistoryStats(CheckedUrlHistoryStats historyStats) {
            this.historyStats = historyStats;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public HistoryStats setDescription(String description) {
            this.description = description;
            return this;
        }
    }
}
