package org.identifiers.cloud.ws.linkchecker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@TestConfiguration
public class EmbeddedRedditTestConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddedRedditTestConfiguration.class);
    private static RedisServer redisServer;

    public EmbeddedRedditTestConfiguration(RedisProperties redisProperties) {
        redisServer = new RedisServer(redisProperties.getPort());
    }

    @PostConstruct
    public void postConstruct() {
        redisServer.start();
    }

    @PreDestroy
    public void preDestroy() {
        redisServer.stop();
    }
}
