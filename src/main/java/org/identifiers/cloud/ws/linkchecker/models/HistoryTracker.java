package org.identifiers.cloud.ws.linkchecker.models;

import org.identifiers.cloud.ws.linkchecker.data.models.LinkCheckResult;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.data.models
 * Timestamp: 2018-05-22 15:48
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 *
 * This is a local cache for the history tracking information on a link. Base class for the different tracked entities.
 */
public abstract class HistoryTracker implements Serializable {
    // Home URL for this provider within the context of a namespace or prefix
    private String url;
    // When the tracking was queued / added to the link checker (UTC)
    private Timestamp created = new Timestamp(new Date().getTime());
    // History stats for this tracker instance
    private final Map<String, CheckedUrlHistoryStats> historyStatsMap =
            Arrays.stream(HistoryStats.values()).collect(Collectors.toMap(HistoryStats::getKey, historyStats -> historyStats.getFactoryMethod().get()));

    public String getUrl() {
        return url;
    }

    public HistoryTracker setUrl(String url) {
        this.url = url;
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
        return new ArrayList<>(historyStatsMap.values());
    }

    public CheckedUrlHistoryStats getHistoryStats(HistoryStats historyStatsType) {
        return historyStatsMap.get(historyStatsType.getKey());
    }

    public void addLinkCheckResult(LinkCheckResult linkCheckResult) {
        // Update the history stats
        historyStatsMap.values().parallelStream().forEach(checkedUrlHistoryStat -> {checkedUrlHistoryStat.update(linkCheckResult);});
    }

    public void initHistoryStats(List<LinkCheckResult> linkCheckResults) {
        historyStatsMap.values().parallelStream().forEach(checkedUrlHistoryStat -> {checkedUrlHistoryStat.init(linkCheckResults);});
    }

    public enum HistoryStats implements Serializable {
        SIMPLE(CheckedUrlHistoryStatsSimple::new, "simple", "Simple UP/DOWN history tracking");

        private final Supplier<CheckedUrlHistoryStats> factoryMethod;
        private final String key;
        private final String description;

        HistoryStats(Supplier<CheckedUrlHistoryStats> factoryMethod, String key, String description) {
            this.factoryMethod = factoryMethod;
            this.key = key;
            this.description = description;
        }

        public Supplier<CheckedUrlHistoryStats> getFactoryMethod() {
            return factoryMethod;
        }

        public String getKey() {
            return key;
        }

        public String getDescription() {
            return description;
        }
    }
}
