package org.identifiers.cloud.ws.linkchecker.models;

import org.identifiers.cloud.ws.linkchecker.data.models.LinkCheckResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.data.models
 * Timestamp: 2018-05-22 14:47
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
public class CheckedUrlHistoryStatsSimple implements CheckedUrlHistoryStats, Serializable {
    private static final Logger logger = LoggerFactory.getLogger(CheckedUrlHistoryStatsSimple.class);
    // Number of events where the checked URL was considered to be up
    private int nUpEvents = 0;
    // Number of events where the checked URL was considered to be down
    private int nDownEvents = 0;

    public CheckedUrlHistoryStatsSimple() {
        logger.info("CheckedUrlHistoryStatsSimple instantiated");
    }

    @Override
    public synchronized void init(List<LinkCheckResult> linkCheckResults) throws CheckedUrlHistoryStatsException {
        if ((nUpEvents + nDownEvents) == 0) {
            nUpEvents += linkCheckResults.parallelStream().filter(LinkCheckResult::isUrlAssessmentOk).count();
            nDownEvents = linkCheckResults.size() - nUpEvents;
        } else {
            throw new CheckedUrlHistoryStatsException("CANNOT INITIALIZE stats for already initialized stats");
        }
    }

    @Override
    public synchronized void update(LinkCheckResult linkCheckResult) {
        if (linkCheckResult.isUrlAssessmentOk()) {
            nUpEvents++;
        } else {
            nDownEvents++;
        }
    }

    @Override
    public double getUpPercentage() {
        int nEvents = nDownEvents + nUpEvents;
        if (nEvents == 0) {
            return 50.0;
        }
        return(nUpEvents * 100.0) / nEvents;
    }
}
