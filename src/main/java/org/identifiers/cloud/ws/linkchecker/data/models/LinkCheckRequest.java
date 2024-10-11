package org.identifiers.cloud.ws.linkchecker.data.models;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
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
@Getter @Setter @EqualsAndHashCode @Accessors(chain = true)
public class LinkCheckRequest implements Serializable, Comparable<LinkCheckRequest> {
    @Serial
    private static final long serialVersionUID = 8460578676300241510L;

    // URL that has been checked
    private String url;
    // When it has been checked (UTC)
    private Timestamp timestamp = new Timestamp(new Date().getTime());
    // Link check request type / reference
    private String providerId;
    private String resourceId;

    @Getter(AccessLevel.NONE)
    private boolean accept401or403 = false;

    public boolean shouldAccept401or403() {
        return accept401or403;
    }

    @Override
    public int compareTo(LinkCheckRequest o) {
        return this.timestamp.compareTo(o.getTimestamp());
    }
}
