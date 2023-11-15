package org.identifiers.cloud.ws.linkchecker.channels.linkcheckresults;

import org.identifiers.cloud.ws.linkchecker.channels.Subscriber;
import org.identifiers.cloud.ws.linkchecker.data.models.LinkCheckResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.channels.linkcheckresults
 * Timestamp: 2018-08-02 14:10
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@Component
public class LinkCheckResultsSubscriber extends Subscriber<String, LinkCheckResult> {
    private final RedisMessageListenerContainer redisContainer;
    private final ChannelTopic channelTopicLinkCheckResults;
    private final RedisTemplate<String, LinkCheckResult> linkCheckResultRedisTemplate;

    public LinkCheckResultsSubscriber(
            @Autowired RedisMessageListenerContainer redisContainer,
            @Autowired ChannelTopic channelTopicLinkCheckResults,
            @Autowired RedisTemplate<String, LinkCheckResult> linkCheckResultRedisTemplate) {
        this.redisContainer = redisContainer;
        this.channelTopicLinkCheckResults = channelTopicLinkCheckResults;
        this.linkCheckResultRedisTemplate = linkCheckResultRedisTemplate;
    }

    @PostConstruct
    public void registerSubscriber() {
        doRegisterSubscriber();
    }

    @Override
    protected RedisMessageListenerContainer getRedisContainer() {
        return redisContainer;
    }

    @Override
    protected ChannelTopic getChannelTopic() {
        return channelTopicLinkCheckResults;
    }

    @Override
    protected RedisTemplate<String, LinkCheckResult> getRedisTemplate() {
        return linkCheckResultRedisTemplate;
    }
}
