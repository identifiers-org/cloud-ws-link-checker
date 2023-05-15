package org.identifiers.cloud.ws.linkchecker.data.models;

import java.io.Serializable;
import java.util.Date;
import java.sql.Timestamp;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.data.models
 * Timestamp: 2018-05-25 4:36
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 *
 * This model represents a request for checking a link
 */
public class LinkCheckRequest implements Serializable, Comparable<LinkCheckRequest> {
    // URL that has been checked
    private String url;
    // When it has been checked (UTC)
    private Timestamp timestamp = new Timestamp(new Date().getTime());
    // Link check request type / reference
    private String providerId;
    private String resourceId;
    private boolean accept401or403 = false;

    public boolean shouldAccept401or403() {
        return accept401or403;
    }

    public LinkCheckRequest setAccept401or403(boolean accept401or403) {
        this.accept401or403 = accept401or403;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public LinkCheckRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public LinkCheckRequest setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getProviderId() {
        return providerId;
    }

    public LinkCheckRequest setProviderId(String providerId) {
        this.providerId = providerId;
        return this;
    }

    public String getResourceId() {
        return resourceId;
    }

    public LinkCheckRequest setResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    @Override
    public int compareTo(LinkCheckRequest o) {
        return this.timestamp.compareTo(o.getTimestamp());
    }

}
