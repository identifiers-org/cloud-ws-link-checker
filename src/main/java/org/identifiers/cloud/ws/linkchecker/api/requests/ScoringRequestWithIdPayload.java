package org.identifiers.cloud.ws.linkchecker.api.requests;

import java.io.Serializable;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.api.requests
 * Timestamp: 2018-05-26 10:33
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
public class ScoringRequestWithIdPayload implements Serializable {
    public String id;
    public String url;
}
