package org.identifiers.cloud.ws.linkchecker.configuration;

import lombok.extern.slf4j.Slf4j;
import org.identifiers.cloud.libapi.services.ApiServicesFactory;
import org.identifiers.cloud.libapi.services.ResolverService;
import org.identifiers.cloud.ws.linkchecker.data.models.FlushHistoryTrackingDataMessage;
import org.identifiers.cloud.ws.linkchecker.data.models.LinkCheckRequest;
import org.identifiers.cloud.ws.linkchecker.data.models.LinkCheckResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.support.collections.DefaultRedisList;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Executors;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Project: link-checker
 * Package: org.identifiers.cloud.ws.linkchecker.configuration
 * Timestamp: 2018-05-22 13:10
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 *
 * Default Application configuration
 */
@Configuration
@Slf4j
@EnableRedisRepositories(enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
public class ApplicationConfig {
    @Value("${org.identifiers.cloud.ws.linkchecker.backend.data.queue.key.linkcheckrequests}")
    private String queueKeyLinkCheckRequests;

    @Value("${org.identifiers.cloud.ws.linkchecker.backend.data.channel.key.linkcheckresults}")
    private String channelKeyLinkCheckResults;

    @Value("${org.identifiers.cloud.ws.linkchecker.backend.data.channel.key.flushhistorytrackingdata}")
    private String channelKeyFlushHistoryTrackingData;

    @Value("${org.identifiers.cloud.ws.linkchecker.backend.service.resolver.host}")
    String wsResolverHost;

    @Value("${org.identifiers.cloud.ws.linkchecker.backend.service.resolver.port}")
    String wsResolverPort;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(
            @Value("${spring.redis.host}") String redisHost,
            @Value("${spring.redis.port}") int redisPort
    ) {
        RedisStandaloneConfiguration config
                = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate(@Autowired LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<byte[], byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    public RedisTemplate<String, LinkCheckRequest>
    linkCheckRequestRedisTemplate(@Autowired LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, LinkCheckRequest> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    public BlockingDeque<LinkCheckRequest>
    linkCheckRequestQueue(@Autowired RedisTemplate<String, LinkCheckRequest> linkCheckRequestRedisTemplate) {
        return new DefaultRedisList<>(queueKeyLinkCheckRequests, linkCheckRequestRedisTemplate);
    }

    // Publisher Subscriber
    // Link Check Results
    @Bean
    public RedisTemplate<String, LinkCheckResult>
    linkCheckResultRedisTemplate(@Autowired LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, LinkCheckResult> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    // Flush History Tracking Data Message
    @Bean
    public RedisTemplate<String, FlushHistoryTrackingDataMessage>
    flushHistoryTrackingDataMessageRedisTemplate(@Autowired LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, FlushHistoryTrackingDataMessage> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    // Redis Container
    @Bean
    public RedisMessageListenerContainer
    redisContainer(@Autowired LettuceConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.start();
        return container;
    }

    // Channels
    // Link Check Results Channel
    @Bean
    public ChannelTopic channelTopicLinkCheckResults() {
        return new ChannelTopic(channelKeyLinkCheckResults);
    }

    @Bean
    public ChannelTopic channelTopicFlushHistoryTrackingData() {
        return new ChannelTopic(channelKeyFlushHistoryTrackingData);
    }

    @Bean
    public HttpClient linkCheckerHttpClient() {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.of(12, SECONDS))
                .executor(Executors.newCachedThreadPool())
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    @Bean
    public ResolverService serviceResponseResolve() {
        return ApiServicesFactory.getResolverService(wsResolverHost, wsResolverPort);
    }
}
