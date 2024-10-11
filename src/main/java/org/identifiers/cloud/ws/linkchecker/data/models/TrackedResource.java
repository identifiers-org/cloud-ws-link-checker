package org.identifiers.cloud.ws.linkchecker.data.models;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.data.models
 * Timestamp: 2018-06-12 13:11
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 *
 * This models a resource being tracked by the service. A resource provides information on a given Compact ID, and the
 * URL is a resolved URL given that Compact ID.
 */
@Getter @Setter @Accessors(chain = true)
@RedisHash("LinkCheckerTrackedResource")
public class TrackedResource implements Serializable {
    @Serial
    private static final long serialVersionUID = -8415715191054737703L;

    // Resource ID within the context of a namespace / prefix
    @Id
    private String id;
    // Resolved URL
    @Indexed
    private String url;
    // When the tracking was queued / added to the link checker (UTC)
    @Indexed
    private String created = (new Timestamp(new Date().getTime())).toString();

    public Timestamp getCreated() {
        return Timestamp.valueOf(created);
    }

    public TrackedResource setCreated(Timestamp created) {
        this.created = created.toString();
        return this;
    }
}
