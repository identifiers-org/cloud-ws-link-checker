package org.identifiers.cloud.ws.linkchecker.channels.management.flushhistorytrackingdata;

import org.identifiers.cloud.ws.linkchecker.channels.Subscriber;
import org.identifiers.cloud.ws.linkchecker.data.models.FlushHistoryTrackingDataMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.channels.management.flushhistorytrackingdata
 * Timestamp: 2018-08-03 11:19
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@Component
public class FlushHistoryTrackingDataSubscriber extends Subscriber<String, FlushHistoryTrackingDataMessage> {
    private final RedisMessageListenerContainer redisContainer;
    private final ChannelTopic channelTopicFlushHistoryTrackingData;
    private final RedisTemplate<String, FlushHistoryTrackingDataMessage> flushHistoryTrackingDataMessageRedisTemplate;

    public FlushHistoryTrackingDataSubscriber(
            @Autowired RedisMessageListenerContainer redisContainer,
            @Autowired ChannelTopic channelTopicFlushHistoryTrackingData,
            @Autowired RedisTemplate<String, FlushHistoryTrackingDataMessage> flushHistoryTrackingDataMessageRedisTemplate) {
        this.redisContainer = redisContainer;
        this.channelTopicFlushHistoryTrackingData = channelTopicFlushHistoryTrackingData;
        this.flushHistoryTrackingDataMessageRedisTemplate = flushHistoryTrackingDataMessageRedisTemplate;
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
        return channelTopicFlushHistoryTrackingData;
    }

    @Override
    protected RedisTemplate<String, FlushHistoryTrackingDataMessage> getRedisTemplate() {
        return flushHistoryTrackingDataMessageRedisTemplate;
    }
}
