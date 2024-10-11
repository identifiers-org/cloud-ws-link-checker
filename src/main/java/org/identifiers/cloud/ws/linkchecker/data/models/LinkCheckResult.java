package org.identifiers.cloud.ws.linkchecker.data.models;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.identifiers.cloud.ws.linkchecker.configuration.LinkCheckResultConfig;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.data.models
 * Timestamp: 2018-05-22 10:48
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 *
 * This entity models a checked URL, for either a provider, a resource or a plain URL.
 */
@Getter @Setter @Accessors(chain = true)
@RedisHash(value = "LinkCheckerLinkCheckResult")
public class LinkCheckResult implements Serializable, Comparable<LinkCheckResult> {
    @Serial
    private static final long serialVersionUID = -7180056948087986389L;

    // TTL as property
    @TimeToLive private Long timeToLive = LinkCheckResultConfig.timeToLive;
    // Result ID, hopefully manufactured by Redis
    @Id
    private String id;
    // URL that has been checked
    @Indexed
    private String url;
    // When it has been checked (UTC), indexed to easily find those entities to remove
    @Indexed
    private String timestamp = (new Timestamp(new Date().getTime())).toString();
    // When this check was requested (UTC)
    private String requestTimestamp;
    // Link check request type / reference
    @Indexed
    private String providerId;
    @Indexed
    private String resourceId;
    // Returning HTTP Status code
    private int httpStatus;
    // Checking strategy URL status evaluation. This is an assessment of the checked URL, on whether it is considered to
    // lead to a non-error resource, and it is calculated by the link checking strategy used.
    private boolean urlAssessmentOk = false;

    public LinkCheckResult setRequestTimestamp(Timestamp requestTimestamp) {
        this.requestTimestamp = requestTimestamp.toString();
        return this;
    }
    public Timestamp getRequestTimestamp() {
        return Timestamp.valueOf(requestTimestamp);
    }
    public Timestamp getTimestamp() {
        return Timestamp.valueOf(requestTimestamp);
    }

    @Override
    public int compareTo(LinkCheckResult o) {
        return getTimestamp().compareTo(o.getTimestamp());
    }
}
