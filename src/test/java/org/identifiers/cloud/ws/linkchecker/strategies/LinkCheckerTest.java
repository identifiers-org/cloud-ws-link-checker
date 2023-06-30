package org.identifiers.cloud.ws.linkchecker.strategies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.strategies
 * Timestamp: 2018-05-28 9:45
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class LinkCheckerTest {
    private static final Logger logger = LoggerFactory.getLogger(LinkCheckerTest.class);

    @Autowired
    private LinkChecker linkChecker;

    @Test
    public void reportOverviewTest() throws MalformedURLException {
        LinkCheckerReport report = linkChecker.check(new URL("https://www.ebi.ac.uk/chebi/"), false);
        // Just checking how the report looks like
        ObjectMapper mapper = new ObjectMapper();
        try {
            logger.info("Link Checker REPORT '{}'", mapper.writer().writeValueAsString(report));
        } catch (JsonProcessingException e) {
            logger.error("WTF! Error serializing a simple POJO!");
        }
    }
}
