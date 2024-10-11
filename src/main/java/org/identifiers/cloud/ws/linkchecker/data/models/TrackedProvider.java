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
 * Timestamp: 2018-05-26 9:49
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 * <p>
 * This models a provider being tracked by the service.
 */
@Getter @Setter @Accessors(chain = true)
@RedisHash("LinkCheckerTrackedProvider")
public class TrackedProvider implements Serializable {
    @Serial
    private static final long serialVersionUID = 8616696712787060386L;

    // Provider ID within the context of a namespace or prefix
    @Id
    private String id;
    // Home URL for this provider within the context of a namespace or prefix
    @Indexed
    private String url;
    // When the tracking was queued / added to the link checker (UTC)
    @Indexed
    private String created = (new Timestamp(new Date().getTime())).toString();

    public Timestamp getCreated() {
        return Timestamp.valueOf(created);
    }

    public TrackedProvider setCreated(Timestamp created) {
        this.created = created.toString();
        return this;
    }
}
