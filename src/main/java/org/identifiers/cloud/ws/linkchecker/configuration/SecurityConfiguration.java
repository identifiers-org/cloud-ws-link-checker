package org.identifiers.cloud.ws.linkchecker.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfiguration {
    @Value("${org.identifiers.cloud.ws.linkchecker.requiredrole:chad}")
    String requiredRole; // Assumed that user gets role directly

    @Bean
    @Profile("authenabled")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("THIS RUN ***************************************************");
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/actuator").hasAnyAuthority(requiredRole)
                .requestMatchers("/actuator/loggers/**").hasAnyAuthority(requiredRole)
                .requestMatchers("/management/flushLinkCheckingHistory").hasAnyAuthority(requiredRole)
                .requestMatchers(HttpMethod.GET, "/actuator/health/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/getScoreFor*").permitAll())
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)
            .oauth2ResourceServer(customizer -> customizer
                    .jwt(withDefaults()));
        return http.build();
    }

    @Bean
    @Profile("!authenabled")
    public SecurityFilterChain filterChainDev(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
