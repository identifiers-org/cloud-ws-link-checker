package org.identifiers.cloud.ws.linkchecker.models;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.models
 * Timestamp: 2018-06-12 13:27
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@Getter @Setter @Accessors(chain = true)
public class ResourceTracker extends HistoryTracker {
    // Resource ID within the context of a namespace / prefix
    private String id;
}
