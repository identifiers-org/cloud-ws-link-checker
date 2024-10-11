package org.identifiers.cloud.ws.linkchecker.api.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.api.requests
 * Timestamp: 2018-05-26 10:34
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@Getter @Setter @Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScoringRequestPayload implements Serializable {
    protected String url;
    protected boolean accept401or403;

    public boolean getAccept401or403() {
        return accept401or403;
    }
}
