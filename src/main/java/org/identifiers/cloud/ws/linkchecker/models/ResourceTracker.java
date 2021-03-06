package org.identifiers.cloud.ws.linkchecker.models;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.models
 * Timestamp: 2018-06-12 13:27
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
public class ResourceTracker extends HistoryTracker {
    // Resource ID within the context of a namespace / prefix
    private String id;

    public String getId() {
        return id;
    }

    public ResourceTracker setId(String id) {
        this.id = id;
        return this;
    }
}
