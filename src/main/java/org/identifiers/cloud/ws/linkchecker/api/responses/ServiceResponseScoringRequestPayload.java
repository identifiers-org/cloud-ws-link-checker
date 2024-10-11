package org.identifiers.cloud.ws.linkchecker.api.responses;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.api.requests
 * Timestamp: 2018-05-26 10:23
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 *
 * Response to a Scoring request
 */
@Getter @Setter @Accessors(chain = true)
public class ServiceResponseScoringRequestPayload implements Serializable {
    // Default scoring
    private int score = 50;
}
