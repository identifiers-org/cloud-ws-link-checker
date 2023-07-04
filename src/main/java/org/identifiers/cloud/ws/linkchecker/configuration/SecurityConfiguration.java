package org.identifiers.cloud.ws.linkchecker.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@Profile("authenabled")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Value("${org.identifiers.cloud.ws.linkchecker.requiredrole}")
    String requiredRole; // Assumed that user gets role directly

    @Override
    protected void configure(HttpSecurity http) throws Exception {
       String aExp = String.format(
                "isAuthenticated() and principal.claims['realm_access']['roles'].contains('%s')",
                requiredRole
        );

        http.authorizeRequests()
                .antMatchers("/actuator").access(aExp)
                .antMatchers("/actuator/loggers/**").access(aExp)
                .antMatchers("/management/flushLinkCheckingHistory").access(aExp)
                .antMatchers(HttpMethod.GET, "/actuator/health/**").permitAll()
                .antMatchers(HttpMethod.POST,"/getScoreFor*").permitAll()
            .and()
                .csrf().disable()
                .logout().disable()
                .formLogin().disable()
                .oauth2ResourceServer().jwt();
    }
}
