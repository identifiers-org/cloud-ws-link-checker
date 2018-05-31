package org.identifiers.cloud.ws.linkchecker.test;

import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.test
 * Timestamp: 2018-05-31 10:23
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 *
 * This is a mediator for LearningTest unit tests
 */
@Component
@Scope("prototype")
@Profile("test")
public class LearningTestMediator {
    // TODO

    @PostConstruct
    public void postConstruct() {
        // TODO
    }
}
