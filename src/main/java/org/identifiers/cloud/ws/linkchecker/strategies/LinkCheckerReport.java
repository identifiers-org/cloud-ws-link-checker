package org.identifiers.cloud.ws.linkchecker.strategies;

import java.sql.Timestamp;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.strategies
 * Timestamp: 2018-05-28 8:29
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 *
 * Report built by a link checking strategy.
 */
public class LinkCheckerReport {
    // Checked URL
    private String url;
    // UTC time stamp when the URL was checked
    private Timestamp timestamp;
    // Returned HTTP Status
    private int httpStatus;

    public String getUrl() {
        return url;
    }

    public LinkCheckerReport setUrl(String url) {
        this.url = url;
        return this;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public LinkCheckerReport setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public LinkCheckerReport setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }
}