package com.umbrella.recipes.config;

import com.umbrella.recipes.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableWebSecurity
public class SecurityConfig implements UserDetailsService {

    private final UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Searching for user with username: {}", username);
        return repository
                .findAppUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }



    @Bean
    public SecurityFilterChain securityChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder mvcB = new MvcRequestMatcher.Builder(introspector);
        http.httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable).headers(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                        .requestMatchers(mvcB.pattern(HttpMethod.POST, "/api/recipe/new")).authenticated()
                        .requestMatchers(mvcB.pattern(HttpMethod.PUT, "/api/recipe/{id}")).authenticated()
                        .requestMatchers(mvcB.pattern(HttpMethod.DELETE, "/api/recipe/{id}")).authenticated()
                        .requestMatchers(mvcB.pattern(HttpMethod.GET, "/api/recipe/{id}")).authenticated()
                        .requestMatchers(mvcB.pattern(HttpMethod.GET, "/api/recipe/search")).authenticated()
                        .requestMatchers(mvcB.pattern(HttpMethod.POST, "/api/register")).permitAll()
                        .anyRequest().denyAll()
                )
                //.formLogin(Customizer.withDefaults())
                .userDetailsService(this);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
