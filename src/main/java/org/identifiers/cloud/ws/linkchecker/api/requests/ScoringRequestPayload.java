package org.identifiers.cloud.ws.linkchecker.api.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.api.requests
 * Timestamp: 2018-05-26 10:34
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScoringRequestPayload implements Serializable {
    protected String url;
    protected boolean accept401or403;

    public boolean getAccept401or403() {
        return accept401or403;
    }
    public ScoringRequestPayload setAccept401or403(boolean accept401or403) {
        this.accept401or403 = accept401or403;
        return this;
    }

    public String getUrl() {
        return url;
    }
    public ScoringRequestPayload setUrl(String url) {
        this.url = url;
        return this;
    }
}
