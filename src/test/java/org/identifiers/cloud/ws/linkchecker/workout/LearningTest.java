package org.identifiers.cloud.ws.linkchecker.workout;

import org.identifiers.cloud.ws.linkchecker.test.LearningTestMediator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.workout
 * Timestamp: 2018-05-30 12:04
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 * <p>
 * This unit test is for working out how things work with Spring Data Redis and backend operations.
 */
@RunWith(SpringRunner.class)
//@ContextConfiguration(classes = ApplicationConfig.class)
@ActiveProfiles("test")
@SpringBootTest
public class LearningTest {
    // Autowire the test mediator
    @Autowired
    private LearningTestMediator mediator;

    @Test
    public void testLinkCheckQueuing() {
        mediator.testLinkCheckQueuing();
    }
}

