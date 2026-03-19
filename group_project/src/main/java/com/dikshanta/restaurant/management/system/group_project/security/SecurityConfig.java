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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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

import static com.dikshanta.restaurant.management.system.group_project.enums.Role.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String AUTH_ROUTE = "/api/v1/auth/**";
    private static final String PUBLIC_ROUTE = "/api/v1/health/**";
    private static final String ADMIN_ROUTE = "/api/v1/admin/**";
    private static final String RESTAURANT_ROUTE = "/api/v1/restaurant/**";
    private static final String CUSTOMER_ROUTE = "/api/v1/customer/**";
    private static final String REVIEW_ROUTE = "/api/reviews/**";

    private static final String FOOD_ITEMS_LIST = "/api/v1/restaurant/food-items";
    private static final String FOOD_ITEMS_BY_REST = "/api/v1/restaurant/restaurants/*/food-items";

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
                        .requestMatchers(AUTH_ROUTE).permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                PUBLIC_ROUTE
                        ).permitAll()

                        .requestMatchers(ADMIN_ROUTE).hasRole(ADMIN.name())
                        .requestMatchers(HttpMethod.GET, FOOD_ITEMS_LIST).authenticated()
                        .requestMatchers(HttpMethod.GET, FOOD_ITEMS_BY_REST).authenticated()
                        .requestMatchers(HttpMethod.GET, RESTAURANT_ROUTE)
                        .hasAnyRole(RESTAURANT.name(), ADMIN.name())
                        .requestMatchers(HttpMethod.POST, RESTAURANT_ROUTE)
                        .hasAnyRole(RESTAURANT.name(), ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, RESTAURANT_ROUTE)
                        .hasAnyRole(RESTAURANT.name(), ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, RESTAURANT_ROUTE)
                        .hasAnyRole(RESTAURANT.name(), ADMIN.name())

                        .requestMatchers(CUSTOMER_ROUTE)
                        .hasAnyRole(ADMIN.name(), RESTAURANT.name(), CUSTOMER.name())


                        .requestMatchers(HttpMethod.GET, REVIEW_ROUTE).authenticated()
                        .requestMatchers(HttpMethod.POST, REVIEW_ROUTE)
                        .hasAnyRole(CUSTOMER.name(), RESTAURANT.name(), ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, REVIEW_ROUTE)
                        .hasAnyRole(CUSTOMER.name(), RESTAURANT.name(), ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, REVIEW_ROUTE)
                        .hasAnyRole(CUSTOMER.name(), RESTAURANT.name(), ADMIN.name())

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
        configuration.setAllowedOriginPatterns(List.of("http://localhost:5173"));
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
