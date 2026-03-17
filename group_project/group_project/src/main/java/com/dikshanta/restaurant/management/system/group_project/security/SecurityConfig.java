package com.dikshanta.restaurant.management.system.group_project.security;


import com.dikshanta.restaurant.management.system.group_project.handlers.CustomAccessDeniedHandler;
import com.dikshanta.restaurant.management.system.group_project.handlers.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.dikshanta.restaurant.management.system.group_project.enums.Permission.*;
import static com.dikshanta.restaurant.management.system.group_project.enums.Role.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String ADMIN_ROUTE = "/api/v1/admin/**";
    private static final String RESTAURANT_ROUTE = "/api/v1/restaurant/**";
    private static final String PUBLIC_ROUTE = "/api/v1/health/**";
    private static final String AUTH_ROUTE = "/api/v1/auth/**";
    private static final String CUSTOMER_ROUTE = "/api/v1/customer/**";
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtFilter jwtFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(req -> req

                        // Auth routes
                        .requestMatchers(AUTH_ROUTE).permitAll()

                        // Swagger & public routes
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                PUBLIC_ROUTE,
                                AUTH_ROUTE
                        ).permitAll()

                        // ADMIN ROLE ACCESS
                        .requestMatchers(ADMIN_ROUTE).hasRole(ADMIN.name())
                        .requestMatchers(HttpMethod.GET, ADMIN_ROUTE).hasAuthority(ADMIN_READ.getPermissionName())
                        .requestMatchers(HttpMethod.POST, ADMIN_ROUTE).hasAuthority(ADMIN_CREATE.getPermissionName())
                        .requestMatchers(HttpMethod.PUT, ADMIN_ROUTE).hasAuthority(ADMIN_UPDATE.getPermissionName())
                        .requestMatchers(HttpMethod.DELETE, ADMIN_ROUTE).hasAuthority(ADMIN_DELETE.getPermissionName())

                        // CUSTOMER ACCESS
                        .requestMatchers(CUSTOMER_ROUTE)
                        .hasAnyRole(ADMIN.name(), RESTAURANT.name(), CUSTOMER.name())
                        .requestMatchers(HttpMethod.GET, CUSTOMER_ROUTE)
                        .hasAnyAuthority(
                                ADMIN_READ.getPermissionName(),
                                RESTAURANT_READ.getPermissionName(),
                                CUSTOMER_READ.getPermissionName()
                        )
                        .requestMatchers(HttpMethod.POST, CUSTOMER_ROUTE)
                        .hasAnyAuthority(
                                ADMIN_CREATE.getPermissionName(),
                                RESTAURANT_CREATE.getPermissionName(),
                                CUSTOMER_CREATE.getPermissionName()
                        )
                        .requestMatchers(HttpMethod.PUT, CUSTOMER_ROUTE)
                        .hasAnyAuthority(
                                ADMIN_UPDATE.getPermissionName(),
                                RESTAURANT_UPDATE.getPermissionName(),
                                CUSTOMER_UPDATE.getPermissionName()
                        )
                        .requestMatchers(HttpMethod.DELETE, CUSTOMER_ROUTE)
                        .hasAnyAuthority(
                                ADMIN_DELETE.getPermissionName(),
                                RESTAURANT_DELETE.getPermissionName(),
                                CUSTOMER_DELETE.getPermissionName()
                        )

                        // RESTAURANT MANAGEMENT
                        .requestMatchers(RESTAURANT_ROUTE)
                        .hasAnyRole(ADMIN.name(), RESTAURANT.name(), CUSTOMER.name())
                        .requestMatchers(HttpMethod.GET, RESTAURANT_ROUTE)
                        .hasAnyAuthority(
                                ADMIN_READ.getPermissionName(),
                                RESTAURANT_READ.getPermissionName()
                        )
                        .requestMatchers(HttpMethod.POST, RESTAURANT_ROUTE)
                        .hasAnyAuthority(
                                ADMIN_CREATE.getPermissionName(),
                                RESTAURANT_CREATE.getPermissionName()
                        )
                        .requestMatchers(HttpMethod.PUT, RESTAURANT_ROUTE)
                        .hasAnyAuthority(
                                ADMIN_UPDATE.getPermissionName(),
                                RESTAURANT_UPDATE.getPermissionName()
                        )
                        .requestMatchers(HttpMethod.DELETE, RESTAURANT_ROUTE)
                        .hasAnyAuthority(
                                ADMIN_DELETE.getPermissionName(),
                                RESTAURANT_DELETE.getPermissionName()
                        )

                        .anyRequest().authenticated()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

}

