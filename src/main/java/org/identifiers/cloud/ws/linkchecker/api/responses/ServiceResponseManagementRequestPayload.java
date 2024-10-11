package org.identifiers.cloud.ws.linkchecker.api.responses;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.api.responses
 * Timestamp: 2018-07-31 11:35
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 *
 * This is a generic payload for management requests.
 */
@Getter @Setter @Accessors(chain = true)
public class ServiceResponseManagementRequestPayload implements Serializable {
    private String message = "";
}
