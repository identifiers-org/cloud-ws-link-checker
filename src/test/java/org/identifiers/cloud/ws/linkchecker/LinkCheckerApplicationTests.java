package org.identifiers.cloud.ws.linkchecker;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestRedisServer.class })
public class LinkCheckerApplicationTests {

    @Test
    public void contextLoads() {
    }

}
