package com.taskify.task.config;

import com.taskify.task.client.ProjectServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security setup: stateless, no sessions or login forms, standard security headers, and the user
 * identity gatekeeper for {@code /api/**}. Access decisions are made by that filter, so the chain
 * itself permits requests through.
 */
@Configuration
public class SecurityConfig {

    /**
     * Builds the security filter chain.
     *
     * @param http           the HTTP security builder
     * @param projectService client used by the identity filter
     * @param problems       problem writer for filter responses
     * @return the filter chain
     * @throws Exception if the chain cannot be built
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, ProjectServiceClient projectService, ProblemWriter problems)
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .headers(h -> h.contentTypeOptions(Customizer.withDefaults())
                        .frameOptions(f -> f.deny())
                        .cacheControl(Customizer.withDefaults()))
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .addFilterBefore(new UserIdentityFilter(projectService, problems),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
