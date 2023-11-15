package org.identifiers.cloud.ws.linkchecker.api.models;

import org.identifiers.cloud.ws.linkchecker.api.controllers.HealthApiController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.api.models
 * Timestamp: 2018-06-14 11:57
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 * Health Check API Controller model.
 */
@Component
@Deprecated
@ConditionalOnBean(HealthApiController.class)
public class HealthApiModel {
    private static final String runningSessionId = UUID.randomUUID().toString();

    public String livenessCheck() {
        return runningSessionId;
    }

    public String readinessCheck() {
        return runningSessionId;
    }
}
