package org.identifiers.cloud.ws.linkchecker.api.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.api.requests
 * Timestamp: 2018-05-26 10:31
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@Getter @Setter @Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceRequest<T extends Serializable> implements Serializable {
    private String apiVersion;
    private T payload;
}
