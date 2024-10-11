package org.identifiers.cloud.ws.linkchecker.models;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.identifiers.cloud.ws.linkchecker.data.models.LinkCheckResult;

import java.io.Serial;
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
@Getter @Setter @Accessors(chain = true)
public abstract class HistoryTracker implements Serializable {
    @Serial
    private static final long serialVersionUID = 1946649288954507733L;


    // Home URL for this provider within the context of a namespace or prefix
    private String url;
    // When the tracking was queued / added to the link checker (UTC)
    private Timestamp created = new Timestamp(new Date().getTime());
    // History stats for this tracker instance
    private final Map<String, CheckedUrlHistoryStats> historyStatsMap =
            Arrays.stream(HistoryStatsType.values())
                    .collect(Collectors.toMap(HistoryStatsType::getKey,
                            statsType -> statsType.getFactoryMethod().get()));

    public CheckedUrlHistoryStats getHistoryStats(HistoryStatsType historyStatsType) {
        return historyStatsMap.get(historyStatsType.getKey());
    }

    public void addLinkCheckResult(LinkCheckResult linkCheckResult) {
        // Update the history stats
        historyStatsMap.values().parallelStream().forEach(checkedUrlHistoryStat -> checkedUrlHistoryStat.update(linkCheckResult));
    }

    public void initHistoryStats(List<LinkCheckResult> linkCheckResults) {
        historyStatsMap.values().parallelStream().forEach(checkedUrlHistoryStat -> checkedUrlHistoryStat.init(linkCheckResults));
    }

    @Getter
    public enum HistoryStatsType implements Serializable {
        SIMPLE(CheckedUrlHistoryStatsSimple::new, "simple", "Simple UP/DOWN history tracking");

        private final Supplier<CheckedUrlHistoryStats> factoryMethod;
        private final String key;
        private final String description;

        HistoryStatsType(Supplier<CheckedUrlHistoryStats> factoryMethod, String key, String description) {
            this.factoryMethod = factoryMethod;
            this.key = key;
            this.description = description;
        }
    }
}
