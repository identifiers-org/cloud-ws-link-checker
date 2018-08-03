package org.identifiers.cloud.ws.linkchecker.channels.management.flushhistorytrackingdata;

import org.identifiers.cloud.ws.linkchecker.channels.Publisher;
import org.identifiers.cloud.ws.linkchecker.data.models.FlushHistoryTrackingDataMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.channels.management.flushhistorytrackingdata
 * Timestamp: 2018-08-03 11:18
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@Component
public class FlushHistoryTrackingDataPublisher extends Publisher<String, FlushHistoryTrackingDataMessage> {
    @Autowired
    private RedisTemplate<String, FlushHistoryTrackingDataMessage> flushHistoryTrackingDataMessageRedisTemplate;

    @Autowired
    private ChannelTopic channelTopicFlushHistoryTrackingData;

    @Override
    protected ChannelTopic getChannelTopic() {
        return channelTopicFlushHistoryTrackingData;
    }

    @Override
    protected RedisTemplate<String, FlushHistoryTrackingDataMessage> getRedisTemplate() {
        return flushHistoryTrackingDataMessageRedisTemplate;
    }
}
